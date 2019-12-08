package com.robinvdb.klox

val keywords = mapOf(
    "and" to TokenType.AND,
    "class" to TokenType.CLASS,
    "else" to TokenType.ELSE,
    "false" to TokenType.FALSE,
    "for" to TokenType.FOR,
    "fun" to TokenType.FUN,
    "if" to TokenType.IF,
    "nil" to TokenType.NIL,
    "or" to TokenType.OR,
    "print" to TokenType.PRINT,
    "return" to TokenType.RETURN,
    "super" to TokenType.SUPER,
    "this" to TokenType.THIS,
    "true" to TokenType.TRUE,
    "var" to TokenType.VAR,
    "while" to TokenType.WHILE
)

class Scanner(private val ctx: Context, private val sourceCode: String) {
    private val tokens = mutableListOf<Token>()
    private var current = 0
    private var start = 0
    private var line = 1

    private val isAtEnd: Boolean
        get() {
            return sourceCode.length <= current
        }

    private val currentSubstring: String
        get() {
            return sourceCode.substring(start, current)
        }


    fun scanTokens(): List<Token> {
        while (!isAtEnd) {
            start = current
            scanToken()
        }
        val token = Token(TokenType.EOF, "", null, line)
        tokens.add(token)
        return tokens
    }

    /**
     * Looks for the next token, and if one exists, adds them.
     */
    private fun scanToken() {
        val c = advance()
        if (c == '(') {
            addToken(TokenType.LEFT_PAREN)
        } else if (c == ')') {
            addToken(TokenType.RIGHT_PAREN)
        } else if (c == '{') {
            addToken(TokenType.LEFT_BRACE)
        } else if (c == '}') {
            addToken(TokenType.RIGHT_BRACE)
        } else if (c == ',') {
            addToken(TokenType.COMMA)
        } else if (c == '.') {
            addToken(TokenType.DOT)
        } else if (c == '-') {
            addToken(TokenType.MINUS)
        } else if (c == '+') {
            addToken(TokenType.PLUS)
        } else if (c == ';') {
            addToken(TokenType.SEMICOLON)
        } else if (c == '*') {
            addToken(TokenType.STAR)
        } else if (c == '!') {
            val token: TokenType = if (match('=')) {
                TokenType.BANG_EQUAL
            } else {
                TokenType.BANG
            }
            addToken(token)
        } else if (c == '=') {
            val token = if (match('=')) {
                TokenType.EQUAL_EQUAL
            } else {
                TokenType.EQUAL
            }
            addToken(token)
        } else if (c == '<') {
            val token = if (match('=')) {
                TokenType.LESS_EQUAL
            } else {
                TokenType.LESS
            }
            addToken(token)
        } else if (c == '>') {
            val token = if (match('=')) {
                TokenType.GREATER_EQUAL
            } else {
                TokenType.GREATER
            }
            addToken(token)
        } else if (c == '/') {
            if(!match('/')) {
                addToken(TokenType.SLASH)
                return
            }
            while(this.peek() != '\n' && !this.isAtEnd) this.advance() // skip the comment
        } else if (c == ' ' || c == '\r' || c == '\t') {
            return
        } else if (c == '\n') {
            line += 1
        } else if (c == '"') {
            string()
        } else if (c.isDigit()) {
            number()
        } else if (c.isLetter()) {
            identifier()
        } else {
            ctx.error(line, "Unexpected character '$c'")
        }
    }


    private fun advance(): Char {
        current += 1
        return sourceCode[current - 1]
    }


    private fun addToken(tokenType: TokenType, literal: Any? = null) {
        val lexeme = currentSubstring
        val token = Token(tokenType, lexeme, literal, line)
        tokens.add(token)
    }


    /**
     * Check if the current char matches the expected one.
     * If it does, also advances the current
     */
    private fun match(expected: Char): Boolean {
        if (this.isAtEnd || sourceCode[current] != expected) {
            return false
        }
        current += 1
        return true
    }


    private fun string() {
        while (peek() != '"' && !isAtEnd) {
            if (peek() == '\n') {
                line += 1
            }
            advance()
        }

        // String is not closed
        if (isAtEnd) {
            ctx.error(line, "Unterminated string")
            return
        }

        // closes the string
        advance()

        val value = sourceCode.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun number() {
        while (peek().isDigit()) {
            advance()
        }

        if (peek() == '.' && peek(1).isDigit()) {
            advance()
            while (peek().isDigit()) {
                advance()
            }
        }

        val number = currentSubstring.toDouble()
        addToken(TokenType.NUMBER, number)
    }

    private fun identifier() {
        while (peek().isLetterOrDigit()) {
            advance()
        }

        val text = currentSubstring
        if (text in keywords.keys) {
            val type = keywords.getValue(text)
            addToken(type)
        } else {
            addToken(TokenType.IDENTIFIER)
        }
    }

    /**
     * Return the current char. If none is found, return a null char
     */
    private fun peek(offset: Int = 0): Char {
        if (sourceCode.length <= current) {
            return '\u0000'
        }

        return sourceCode[current + offset]
    }
}
