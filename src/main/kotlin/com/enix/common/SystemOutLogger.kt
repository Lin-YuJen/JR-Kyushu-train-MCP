package com.enix.common

/**
 * DO NOT use this class in stdout mode.
 */
class SystemOutLogger: Logger {
    override fun trace(messageSupplier: () -> String) {
        printlnWithLevel("TRACE", messageSupplier)
    }

    override fun debug(messageSupplier: () -> String) {
        printlnWithLevel("DEBUG", messageSupplier)
    }

    override fun info(messageSupplier: () -> String) {
        printlnWithLevel("INFO ", messageSupplier)
    }

    override fun warn(messageSupplier: () -> String) {
        printlnWithLevel("WARN ", messageSupplier)
    }

    override fun error(
        exception: Exception?,
        messageSupplier: () -> String
    ) {
        printlnWithLevel("ERROR", messageSupplier)
        exception?.printStackTrace()
    }

    private fun printlnWithLevel(
        level: String,
        messageSupplier: () -> String
    ) {
        println("[$level] ${messageSupplier.invoke()}")
    }
}