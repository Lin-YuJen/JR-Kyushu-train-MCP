package com.enix.common

interface Logger {

    fun trace(message: String) = trace { message }

    fun trace(messageSupplier: () -> String)

    fun debug(message: String) = debug { message }

    fun debug(messageSupplier: () -> String)

    fun info(message: String) = info { message }

    fun info(messageSupplier: () -> String)

    fun warn(message: String) = warn { message }

    fun warn(messageSupplier: () -> String)

    fun error(
        exception: Exception? = null,
        message: String
    ) = error(exception) { message }

    fun error(
        exception: Exception? = null,
        messageSupplier: () -> String
    )
}
