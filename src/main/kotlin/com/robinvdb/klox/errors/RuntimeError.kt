package com.robinvdb.klox.errors

import com.robinvdb.klox.Token

class RuntimeError(message: String, val token: Token?): Throwable(message)