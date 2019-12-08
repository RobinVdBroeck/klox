package com.robinvdb.klox

import com.robinvdb.klox.errors.ParseError

class Parser(private val ctx: Context, private val tokens: List<Token>) {
    private var current = 0

    private val isAtEnd: Boolean
        get() = peek().type == TokenType.EOF

    fun parse(): List<Statement> {
        return program()
    }

    /**
     * Check if the current matches of the given types.
     * If it does, return the current one and advance. If it doesn't, return null
     */
    private fun eat(vararg types: TokenType): Token? {
        if (match(*types)) {
            return advance()
        }

        return null
    }

    /**
     * Check if the current matches of the given types.
     */
    private fun match(vararg types: TokenType): Boolean {
        return types.any { check(it) }
    }

    private fun advance(): Token {
        val currentToken = peek()
        current += 1
        return currentToken
    }

    /**
     * Check if the current type match the given type
     */
    private fun check(type: TokenType): Boolean {
        return peek().type == type
    }

    /**
     * Get the current token
     */
    private fun peek(offset: Int = 0): Token {
        return tokens[current + offset]
    }

    /**
     * Check if the current token is of the given expectedType. If it's not, throw an error
     */
    private fun consume(expectedType: TokenType, errorMessage: String): Token {
        val current = peek()
        if (current.type != expectedType) {
            throw error(current, errorMessage)
        }
        return advance()
    }

    private fun error(token: Token, errorMessage: String): ParseError {
        ctx.error(token.line, errorMessage)
        return ParseError(errorMessage)
    }

    /**
     * Go back to a valid state
     */
    private fun synchronize() {
        val endTypes = setOf(
            TokenType.CLASS,
            TokenType.FUN,
            TokenType.VAR,
            TokenType.FOR,
            TokenType.IF,
            TokenType.WHILE,
            TokenType.PRINT,
            TokenType.RETURN,
            TokenType.EOF
        )

        while (!(isAtEnd || endTypes.contains(peek().type))) {
            advance()
        }
    }

    private fun program(): List<Statement> {
        val statements = mutableListOf<Statement>()

        while (!isAtEnd) {
            val decl = declaration()
            if (decl != null) {
                statements.add(decl)
            }
        }

        consume(TokenType.EOF, "Expect program to end with EOF.")

        return statements
    }

    private fun declaration(): Statement? {
        return try {
            if (eat(TokenType.VAR) != null) {
                varDeclaration()
            } else {
                statement()
            }
        } catch (err: ParseError) {
            synchronize()
            null
        }
    }

    private fun varDeclaration(): VariableStatement {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")

        var initializer: Expression? = null
        if (eat(TokenType.EQUAL) != null) {
            initializer = expression()
        }

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration")

        return VariableStatement(name, initializer)
    }


    private fun statement(): Statement {
        if (eat(TokenType.FOR) != null) {
            return forStatement()
        }
        if (eat(TokenType.IF) != null) {
            return ifStatement()
        }
        if (eat(TokenType.PRINT) != null) {
            return printStatement()
        }
        if (eat(TokenType.WHILE) != null) {
            return whileStatement()
        }
        if (eat(TokenType.LEFT_BRACE) != null) {
            return blockStatement()
        }
        return expressionStatement()

    }

    private fun forStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after for.")

        // Initializer (ex. var i = 0)
        val initializer: Statement? = when {
            eat(TokenType.SEMICOLON) != null -> null
            eat(TokenType.VAR) != null -> varDeclaration()
            else -> expressionStatement()
        }

        // condition (ex. i <= 5)
        val condition = when {
            check(TokenType.SEMICOLON) -> LiteralExpression(true)
            else -> expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition")

        // incrementer (ex. i = i + 1)
        var incrementer: Statement? = null
        if (!check(TokenType.RIGHT_PAREN)) {
            incrementer = ExpressionStatement(expression())
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses")

        // Build the statement object
        var body = statement()
        if (incrementer != null) {
            body = BlockStatement(listOf(body, incrementer))
        }
        body = WhileStatement(condition, body)
        if (initializer != null) {
            body = BlockStatement(listOf(initializer, body))
        }

        return body
    }

    private fun ifStatement(): IfStatement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after 'if'")

        val thenBranch = statement()

        var elseBranch: Statement? = null
        if (eat(TokenType.ELSE) != null) {
            elseBranch = statement()
        }
        return IfStatement(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Statement {
        val expression = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return PrintStatement(expression)
    }

    private fun whileStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after while.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return WhileStatement(condition, body)
    }

    private fun blockStatement(): BlockStatement {
        val statements = mutableListOf<Statement>()

        while (!(isAtEnd || check(TokenType.RIGHT_BRACE))) {
            val decl = declaration()
            if (decl != null) {
                statements.add(decl)
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block")

        return BlockStatement(statements)
    }


    private fun expressionStatement(): ExpressionStatement {
        val expression = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value")
        return ExpressionStatement(expression)
    }


    private fun expression(): Expression {
        return assignment()
    }

    private fun assignment(): Expression {
        val expression = logicalOr()

        if (match(TokenType.EQUAL)) {
            val equals = advance()
            val value = assignment()

            if (expression is VariableExpression) {
                val name = expression.name
                return AssignExpression(name, value)
            } else {
                error(equals, "Invalid assignment target")
            }
        }

        return expression
    }

    private fun logicalOr(): Expression {
        var expression = logicalAnd()

        while (match(TokenType.OR)) {
            val operator = advance()
            val right = logicalAnd()
            expression = LogicalExpression(expression, operator, right)
        }

        return expression
    }

    private fun logicalAnd(): Expression {
        var expression = equality()

        while (match(TokenType.AND)) {
            val operator = advance()
            val right = equality()
            expression = LogicalExpression(expression, operator, right)
        }

        return expression
    }

    private fun equality(): Expression {
        var expression = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = advance()
            val right = comparison()
            expression = BinaryExpression(expression, operator, right)
        }

        return expression
    }

    private fun comparison(): Expression {
        var expr = addition()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = advance()
            val right = addition()
            expr = BinaryExpression(expr, operator, right)
        }

        return expr
    }

    private fun addition(): Expression {
        var expr = multiplication()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = advance()
            val right = multiplication()
            expr = BinaryExpression(expr, operator, right)
        }

        return expr
    }

    private fun multiplication(): Expression {
        var expr = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = advance()
            val right = unary()
            expr = BinaryExpression(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expression {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = advance()
            val right = unary()
            return UnaryExpression(operator, right)
        }

        return call()
    }

    private fun call(): Expression {
        var expression =  primary()

        while(true) {
            if(match(TokenType.LEFT_PAREN)) {
                expression = finishCall(expression)
            } else {
                break
            }
        }

        return expression
    }

    /**
     * +- arguments rule, but also handles zero arguments
     */
    private fun finishCall(callee: Expression): Expression {
        val arguments = mutableListOf<Expression>()
        if(!check(TokenType.RIGHT_PAREN)) {
            do {
                if(arguments.size >= 255) {
                    error(peek(), "Cannot have more than 255 arguments")
                }
                arguments.add(expression())
            } while(match(TokenType.COMMA))
        }
        val paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments")
        return CallExpression(callee, paren, arguments)
    }

    private fun primary(): Expression {
        return when {
            eat(TokenType.FALSE) != null -> LiteralExpression(false)
            eat(TokenType.TRUE) != null -> LiteralExpression(true)
            eat(TokenType.NIL) != null -> LiteralExpression(null)
            eat(TokenType.LEFT_PAREN) != null -> {
                val expression = expression()
                consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
                GroupingExpression(expression)
            }
            match(TokenType.NUMBER, TokenType.STRING) -> {
                val value = advance().literal
                LiteralExpression(value)
            }
            match(TokenType.IDENTIFIER) -> {
                val token = advance()
                VariableExpression(token)
            }
            else -> throw error(peek(), "Expect expression")
        }
    }
}
