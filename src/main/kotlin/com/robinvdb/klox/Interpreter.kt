package com.robinvdb.klox

import com.robinvdb.klox.errors.RuntimeError
import kotlin.contracts.contract


fun checkNumberOperand(operatorToken: Token, operand: Any?) {
    contract {
        returns() implies (operand is Double)
    }

    if (operand !is Double) {
        throw RuntimeError("Operand must be a number", operatorToken)
    }
}

class Interpreter(private val ctx: Context) : Visitor<Any?> {
    private var env = Environment()

    fun interpret(statements: Iterable<Statement>) {
        for (statement in statements) {
            try {
                execute(statement)
            } catch (err: RuntimeError) {
                ctx.runtimeError(err)
            }
        }
    }

    private fun execute(statement: Statement) {
        statement.accept(this)
    }

    private fun evaluate(expression: Expression): Any? {
        return expression.accept(this)
    }

    private fun executeBlock(blockStatement: BlockStatement, environment: Environment) {
        val previousEnv = env
        try {
            env = environment
            for (statement in blockStatement.statements) {
                execute(statement)
            }
        } finally {
            env = previousEnv
        }
    }

    override fun visitAssignExpression(assignExpression: AssignExpression): Any? {
        val name = assignExpression.name
        val value = evaluate(assignExpression.value)
        env.assign(name, value)
        return value
    }

    override fun visitBinaryExpression(binaryExpression: BinaryExpression): Any? {
        val left = evaluate(binaryExpression.left)
        val right = evaluate(binaryExpression.right)
        val operator = binaryExpression.operator

        if (operator.type === TokenType.PLUS) {
            return when {
                left is Double && right is Double -> left + right
                left is String && right is String -> left + right
                else -> throw RuntimeError("Operandus must be either float or string", operator)
            }
        }
        checkNumberOperand(operator, left)
        checkNumberOperand(operator, right)
        return when (operator.type) {
            TokenType.MINUS -> left - right
            TokenType.SLASH -> {
                if (right == 0.0) {
                    TODO("Division by zero not implemented")
                }
                return left / right
            }
            TokenType.STAR -> left * right
            TokenType.GREATER -> left > right
            TokenType.GREATER_EQUAL -> left >= right
            TokenType.LESS -> left < right
            TokenType.LESS_EQUAL -> left < right
            TokenType.EQUAL_EQUAL -> left == right
            TokenType.BANG_EQUAL -> left != right
            else -> throw IllegalStateException()
        }
    }

    override fun visitCallExpression(callExpression: CallExpression): Any? {
        val callee = evaluate(callExpression.callee)

        if (callee !is LoxCallable) {
            throw RuntimeError(
                "Can only call functions and classes.",
                callExpression.paren
            )
        }


        val arguments = callExpression.arguments.map { evaluate(it) }

        if(callee.arity != arguments.size) {
            throw RuntimeError("Expected ${callee.arity} arguments but got ${arguments.size}.", callExpression.paren)
        }

        return callee.call(this, arguments)
    }

    override fun visitGroupingExpression(groupingExpression: GroupingExpression): Any? {
        return evaluate(groupingExpression.expression)
    }

    override fun visitLiteralExpression(literalExpression: LiteralExpression): Any? {
        return literalExpression.value
    }

    override fun visitUnaryExpression(unaryExpression: UnaryExpression): Any? {
        val operator = unaryExpression.operator
        val right = unaryExpression.right
        checkNumberOperand(operator, right)

        return when (operator.type) {
            TokenType.MINUS -> right * -1f
            TokenType.BANG -> !isTruthy(right)
            else -> throw IllegalStateException()
        }
    }

    override fun visitVariableExpression(variableExpression: VariableExpression): Any? {
        return env[variableExpression.name]
    }

    override fun visitLogicalExpression(logicalExpression: LogicalExpression): Any? {
        val left = evaluate(logicalExpression.left)
        val leftTruthy = isTruthy(left)

        val operator = logicalExpression.operator
        when {
            operator.type == TokenType.OR -> if (leftTruthy) return left
            operator.type == TokenType.AND -> if (!leftTruthy) return left
            else -> throw IllegalStateException()
        }

        return evaluate(logicalExpression.right)
    }

    override fun visitExpressionStatement(expressionStatement: ExpressionStatement) {
        evaluate(expressionStatement.expression)
    }

    override fun visitPrintStatement(printStatement: PrintStatement) {
        val value = evaluate(printStatement.expression)

        if (value == null) {
            println("nill")
        } else {
            println(value)
        }
    }

    override fun visitVariableStatement(variableStatement: VariableStatement) {
        val identifierToken = variableStatement.name
        assert(identifierToken.type == TokenType.IDENTIFIER)
        val name = identifierToken.lexeme

        var value: Any? = null
        if (variableStatement.initializer !== null) {
            value = evaluate(variableStatement.initializer)
        }
        env.define(name, value)
    }

    override fun visitBlockStatement(blockStatement: BlockStatement) {
        val newEnvironment = env.enclose()
        executeBlock(blockStatement, newEnvironment)
    }

    override fun visitIfStatement(ifStatement: IfStatement) {
        val condition = evaluate(ifStatement.condition)
        when {
            isTruthy(condition) -> execute(ifStatement.thenBranch)
            ifStatement.elseBranch != null -> execute(ifStatement.elseBranch)
        }
    }

    override fun visitWhileStatement(whileStatement: WhileStatement) {
        val condition = whileStatement.condition
        val body = whileStatement.body

        while (isTruthy(evaluate(condition))) {
            execute(body)
        }
    }

    private fun isTruthy(value: Any?) = when (value) {
        null -> false
        is Double -> value != 0f
        is Boolean -> value
        else -> true
    }
}

interface LoxCallable {
    /**
     * Amount of arguments the callable takes
     */
    val arity: Int
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}
