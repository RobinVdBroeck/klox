package com.robinvdb.klox

import com.robinvdb.klox.errors.RuntimeError
import kotlin.test.*


class EnvironmentTests {
    private val name = "__name__"
    private val nameToken = Token(TokenType.IDENTIFIER, name, null, -1)
    private val value = "__value"

    @Test
    fun `test simple environment`() {
        val env = Environment()


        assertFalse(env.isDeclared(name))

        assertFailsWith<RuntimeError> { env[nameToken] }

        val value = "__value__"

        env.define(name, value)

        assertTrue(env.isDeclared(name))
        assertEquals(env[nameToken], value)
    }

    @Test
    fun `test double environment`() {
        val top = Environment()
        val bot = top.enclose()

        top.define(name, value)

        assertTrue(top.isDeclared(name))
        assertFalse(bot.isDeclared(name))

        assertEquals(top[nameToken], value)
        assertEquals(bot[nameToken], value)

        val nextValue = value.reversed()
        bot.assign(nameToken, nextValue)

        assertEquals(top[nameToken], nextValue)
        assertEquals(bot[nameToken], nextValue)
    }
}