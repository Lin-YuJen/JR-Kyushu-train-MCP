package com.enix.server.log

import com.enix.common.Logger
import io.modelcontextprotocol.kotlin.sdk.LoggingLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MessageLoggerAdaptor(
    private val logger: MessageLogger,
    private val scope: CoroutineScope = CoroutineScope(
        Dispatchers.IO + SupervisorJob()
    )
) : Logger {

    override fun trace(messageSupplier: () -> String) {
        throw UnsupportedOperationException("Logging level TRACE is not supported.")
    }

    override fun debug(messageSupplier: () -> String) {
        scope.launch {
            logger.log(LoggingLevel.debug, messageSupplier)
        }
    }

    override fun info(messageSupplier: () -> String) {
        scope.launch {
            logger.log(LoggingLevel.info, messageSupplier)
        }
    }

    override fun warn(messageSupplier: () -> String) {
        scope.launch {
            logger.log(LoggingLevel.warning, messageSupplier)
        }
    }

    override fun error(
        exception: Exception?,
        messageSupplier: () -> String
    ) {
        scope.launch {
            logger.error(exception, messageSupplier.invoke())
        }
    }

}