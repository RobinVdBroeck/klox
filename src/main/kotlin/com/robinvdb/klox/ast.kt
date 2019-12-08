package com.robinvdb.klox

import com.robinvdb.klox.errors.RuntimeError


abstract class Visitable {
    fun <T> accept(visitor: Visitor<T>): T {
        // name of the visit function
        val thisClass = this.javaClass
        val name = thisClass.simpleName
        val functionName = "visit$name"

        // find the function on the visitor
        val vClass = visitor.javaClass
        val functions = vClass.methods
        val function = functions.find { it.name == functionName }
            ?: throw RuntimeError("Could not find visit function called '$functionName' on visitor", null)
        return function.invoke(visitor, this) as T
    }
}

abstract class Expression : Visitable()
abstract class Statement : Visitable()

/**
 * Expressions
 */

data class AssignExpression(
    val name: Token,
    val value: Expression
) : Expression()

data class BinaryExpression(
    val left: Expression,
    val operator: Token,
    val right: Expression
) : Expression()

data class CallExpression(val callee: Expression, val paren: Token, val arguments: List<Expression>) : Expression()

data class GroupingExpression(val expression: Expression) : Expression()

data class LiteralExpression(val value: Any?) : Expression()

data class UnaryExpression(
    val operator: Token,
    val right: Expression
) : Expression()

data class VariableExpression(val name: Token) : Expression()

data class LogicalExpression(
    val left: Expression,
    val operator: Token,
    val right: Expression
) : Expression()

/**
 * Statements
 */

data class ExpressionStatement(val expression: Expression) : Statement()

data class PrintStatement(val expression: Expression) : Statement()

data class VariableStatement(
    val name: Token,
    val initializer: Expression?
) : Statement()

data class BlockStatement(val statements: Iterable<Statement>) : Statement()

data class IfStatement(
    val condition: Expression,
    val thenBranch: Statement,
    val elseBranch: Statement?
) : Statement()

data class WhileStatement(val condition: Expression, val body: Statement) : Statement()

