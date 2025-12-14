package com.enix.server.log

import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.LoggingLevel
import io.modelcontextprotocol.kotlin.sdk.LoggingMessageNotification
import io.modelcontextprotocol.kotlin.sdk.ReadResourceRequest
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val customJson = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
    isLenient = true
}

open class MessageLogger(
    private val server: Server,
    private val name: String
) {

    /**
     * Logs a JSON message at a specified logging level.
     * Messages with a logging level lower than the global logging level will not be sent.
     *
     * @param level The severity level of the log message, represented as a [LoggingLevel].
     * @param jsonMessage The JSON object containing the log message data to be sent.
     */
    suspend fun log(
        level: LoggingLevel,
        jsonMessage: JsonObject
    ) {
        if (level < GlobalLoggingLevel.level) return
        server.sendLoggingMessage(
            LoggingMessageNotification(
                LoggingMessageNotification.Params(
                    level = level,
                    logger = name,
                    data = jsonMessage
                )
            )
        )
    }

    /**
     * Logs a message at the specified logging level, dynamically generating the content
     * using the provided content supplier.
     * If the logging level is lower than the global logging level, the message will not be sent.
     *
     * @param level The severity level of the log message, represented as a [LoggingLevel].
     * @param contentSupplier A function that supplies the log message content as a string.
     */
    suspend fun log(
        level: LoggingLevel,
        contentSupplier: () -> String
    ) {
        if (level < GlobalLoggingLevel.level) return
        server.sendLoggingMessage(
            LoggingMessageNotification(
                LoggingMessageNotification.Params(
                    level = level,
                    logger = name,
                    data = buildJsonObject {
                        put("message", contentSupplier.invoke())
                    }
                )
            )
        )
    }

    suspend fun debug(
        messageSupplier: () -> String
    ) {
        log(LoggingLevel.debug, messageSupplier)
    }

    suspend fun info(
        messageSupplier: () -> String
    ) {
        log(LoggingLevel.info, messageSupplier)
    }

    suspend fun info(
        message: String
    ) {
        val messageSupplier: () -> String = { message }
        info(messageSupplier)
    }

    suspend fun error(
        exception: Exception? = null,
        message: String,
    ) {
        exception?.let {
            log(LoggingLevel.error, buildJsonObject {
                put("message", message)
                put("exceptionType", it::class.qualifiedName ?: it::class.simpleName.toString())
                put("exceptionMessage", it.message ?: "")
                put("filteredStackTrace", buildJsonArray {
                    exception.filteredStacktrace().take(10).forEach { stackTraceElement ->
                        add(stackTraceElement.toFormattedString())
                    }
                })
                put("stackTrace", buildJsonArray {
                    exception.stackTrace?.toList()?.subList(0, 9)?.forEach { stackTraceElement ->
                        add(JsonPrimitive("${stackTraceElement.className}.${stackTraceElement.methodName}::${stackTraceElement.lineNumber}"))
                    }
                })
                it.cause?.let { cause ->
                    put("causedBy", "${cause::class.simpleName}: ${cause.message}")
                }
            })
        } ?: log(LoggingLevel.error) { message }
    }

    suspend fun error(
        exception: Exception? = null,
        message: () -> String,
    ) {
        error(exception, message.invoke())
    }

    suspend fun logCallToolRequest(
        request: CallToolRequest
    ) {
        if (LoggingLevel.info < GlobalLoggingLevel.level) return
        log(LoggingLevel.info, buildJsonObject {
            put("message", "Received request.")
            val requestJsonString = customJson.encodeToString(request)
            put("CallToolRequest", Json.parseToJsonElement(requestJsonString))
        })
    }

    suspend fun logReadResourceRequest(
        request: ReadResourceRequest
    ) {
        if (LoggingLevel.info < GlobalLoggingLevel.level) return
        log(LoggingLevel.info, buildJsonObject {
            put("message", "Received request.")
            val requestJsonString = customJson.encodeToString(request)
            put("ReadResourceRequest", Json.parseToJsonElement(requestJsonString))
        })
    }

    private fun Exception.filteredStacktrace() = this.stackTrace.asSequence()
        .filterNot { frame ->
            // Remove Kotlin/coroutine internals
            frame.className.contains($$"$Continuation") ||
                    frame.className.contains("$$") ||
                    frame.methodName == "invokeSuspend" ||
                    frame.className.startsWith("kotlin.coroutines") ||
                    frame.className.startsWith("kotlinx.coroutines")
        }

    private fun StackTraceElement.toFormattedString() =
        JsonPrimitive(buildString {
            append(className.substringAfterLast('.'))
            append('.')
            append(methodName)
            append('(')
            append(fileName ?: "Unknown")
            append(':')
            append(lineNumber)
            append(')')
        })

}