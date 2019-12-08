package com.robinvdb.klox

import com.robinvdb.klox.errors.RuntimeError

class Environment constructor(private val enclosing: Environment? = null) {
    private val values: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Creates a new environment with the current as encloosing
     */
    fun enclose(): Environment {
        return Environment(this)
    }

    /**
     * Returns the enclosing environment
     */
    fun pop(): Environment? {
        return enclosing
    }

    fun isDeclared(name: String): Boolean {
        return values.containsKey(name)
    }

    operator fun get(nameToken: Token): Any? {
        assert(nameToken.type == TokenType.IDENTIFIER)

        val name = nameToken.lexeme

        return when {
            this.isDeclared(name) -> values[name]
            enclosing != null -> enclosing.get(nameToken)
            else -> throw RuntimeError("Undefined variable $name", nameToken)
        }
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun assign(nameToken: Token, value: Any?) {
        assert(nameToken.type == TokenType.IDENTIFIER)
        val name = nameToken.lexeme

        when {
            isDeclared(name) -> values[name] = value
            enclosing != null -> enclosing.assign(nameToken, value)
            else -> throw RuntimeError("Undefined variable '$name'", nameToken)
        }
    }
}