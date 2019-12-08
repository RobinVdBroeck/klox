package com.robinvdb.klox

import java.io.File
import java.nio.charset.Charset


fun main(args: Array<String>) {
    when {
        args.isEmpty() -> runRepl()
        args.size == 1 -> runFile(args[0])
        else -> System.err.println("usage: klox <script>")
    }
}

fun runRepl() {
    val exitConstants = setOf("quit", "exit")
    val prompt = "lox> "

    println("Enter quit or exit to exit the repl")

    var line: String?
    do {
        print(prompt)
        line = readLine()
        if (line != null && !exitConstants.contains(line)) {
            run(line)
        }
    } while (!exitConstants.contains(line))
}

fun runFile(path: String) {
    val file = File(path)
    val contents = file.inputStream().readBytes().toString(Charset.defaultCharset())
    run(contents)
}

fun run(sourceCode: String, debugMode: Boolean = true) {
    // setup context
    val ctx = Context()

    // scanning
    ctx.stage = "Scanning"
    val scanner = Scanner(ctx, sourceCode)
    val tokens = scanner.scanTokens()
    if (debugMode) {
        println("---Tokens---")
        for (token in tokens) {
            println(token.toString())
        }
    }
    if (ctx.hadError) {
        for (error in ctx.errors) {
            System.err.println(error)
        }
        return
    }

    // parsing
    ctx.stage = "Parsing"
    val parser = Parser(ctx, tokens)
    val statements = parser.parse()
    if (debugMode) {
        println("---Statements---")
        for (statement in statements) {
            println(statement.toString())
        }
    }
    if (ctx.hadError) {
        for (error in ctx.errors) {
            System.err.println(error)
        }
        return
    }

    // executing
    ctx.stage = "Executing"
    if (debugMode) {
        println("---Interpreter---")
    }
    val interpreter = Interpreter(ctx)
    interpreter.interpret(statements)
    if (ctx.hadRuntimeError) {
        for (error in ctx.runtimeErrors) {
            System.err.println(error)
        }
    }
}