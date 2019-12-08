package com.robinvdb.klox

import com.robinvdb.klox.errors.RuntimeError

class Context {
    var stage: String = "initial";
    val errors = mutableListOf<String>()
    val runtimeErrors = mutableListOf<String>()

    val hadError: Boolean
        get() = this.errors.isNotEmpty()

    val hadRuntimeError: Boolean
        get() = this.runtimeErrors.isNotEmpty()

    fun error(line: Int, message: String) {
        val errorString = "[line $line] Error: $message (stage $stage)"
        this.errors.add(errorString)
    }

    fun runtimeError(error: RuntimeError) {
        val errorString = "[line ${error.token?.line}] Error: ${error.message} (stage $stage)"
        runtimeErrors.add(errorString)
    }

    fun clear() {
        this.errors.clear()
        this.runtimeErrors.clear()
    }
}