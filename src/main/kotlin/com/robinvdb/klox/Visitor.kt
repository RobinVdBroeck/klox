package com.robinvdb.klox

/**
 * TODO: use annotation processing for this
 **/
interface Visitor<E> {
    fun visitAssignExpression(assignExpression: AssignExpression): E
    fun visitBinaryExpression(binaryExpression: BinaryExpression): E
    fun visitCallExpression(callExpression: CallExpression): E
    fun visitGroupingExpression(groupingExpression: GroupingExpression): E
    fun visitLiteralExpression(literalExpression: LiteralExpression): E
    fun visitUnaryExpression(unaryExpression: UnaryExpression): E
    fun visitVariableExpression(variableExpression: VariableExpression): E
    fun visitLogicalExpression(logicalExpression: LogicalExpression): E
    fun visitExpressionStatement(expressionStatement: ExpressionStatement)
    fun visitPrintStatement(printStatement: PrintStatement)
    fun visitVariableStatement(variableStatement: VariableStatement)
    fun visitBlockStatement(blockStatement: BlockStatement)
    fun visitIfStatement(ifStatement: IfStatement)
    fun visitWhileStatement(whileStatement: WhileStatement)
}