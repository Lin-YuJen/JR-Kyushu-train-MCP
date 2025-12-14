package com.enix.common

class SLF4JLogger(
    private val logger: org.slf4j.Logger
) : Logger {
    override fun trace(messageSupplier: () -> String) {
        logger.trace(messageSupplier.invoke())
    }

    override fun debug(messageSupplier: () -> String) {
        logger.debug(messageSupplier.invoke())
    }

    override fun info(messageSupplier: () -> String) {
        logger.info(messageSupplier.invoke())
    }

    override fun warn(messageSupplier: () -> String) {
        logger.warn(messageSupplier.invoke())
    }

    override fun error(
        exception: Exception?,
        messageSupplier: () -> String
    ) {
        logger.error(messageSupplier.invoke(), exception)
    }
}