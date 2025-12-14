package com.enix.server.log

import io.modelcontextprotocol.kotlin.sdk.EmptyRequestResult
import io.modelcontextprotocol.kotlin.sdk.LoggingLevel
import io.modelcontextprotocol.kotlin.sdk.LoggingMessageNotification
import io.modelcontextprotocol.kotlin.sdk.Method
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.shared.RequestHandlerExtra
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.reflect.KClass

/**
 * # Interaction Workflow
 * 1. Configure Logging:
 *     * The client initiates logging by sending a `logging/setLevel` request
 *       specifying the desired log level (e.g., `info`).
 *     * The server responds with an empty result, confirming the logging level
 *       has been set.
 * 2. Server Activity
 *     * After logging is configured, the server begins sending structured
 *       log notifications (`notifications/message`) back to the client.
 *     * Notifications include messages at or above the specified verbosity level,
 *       such as `info`, `warning`, and `error`.
 * 3. Dynamic Log Level Change
 *     * Clients can adjust verbosity dynamically by issuing another logging/setLevel
 *       request (e.g., changing from `info` to `error`).
 *     * The server acknowledges the change with an empty response.
 *     * Subsequent logs are filtered based on this new level, with the server now only
 *       sending logs at the new level (`error`) or higher, excluding less severe messages.
 *
 * ## Reference
 * 1. [MCP Logging Tutorial](https://www.mcpevals.io/blog/mcp-logging-tutorial)
 * 2. [Github - logging/setLevel request not handled #293](https://github.com/modelcontextprotocol/kotlin-sdk/issues/293)
 */
fun Server.configureLogging() {
    setRequestHandler<LoggingMessageNotification.SetLevelRequest>(
        method = Method.Defined.LoggingSetLevel
    ) { setLevelRequest, _log ->
        // SetLevelRequest => A request from the client to the server to enable or adjust logging.
        // RequestHandlerExtra => Extra data given to request handlers.
        setLevelRequest.level.also { newLevel ->
            GlobalLoggingLevel.level = newLevel
        }

        EmptyRequestResult(
            buildJsonObject { put("level", GlobalLoggingLevel.level.name) }
        )
    }
}

object GlobalLoggingLevel {
    // Default logging level is `info`.
    var level: LoggingLevel = LoggingLevel.info
}

private val messageLoggerMap = mutableMapOf<String, MessageLogger>()

fun Server.getLogger(name: String): MessageLogger = messageLoggerMap[name] ?: let {
    MessageLogger(this, name).apply { messageLoggerMap[name] = this }
}

fun Server.getLogger(clazz: Class<*>): MessageLogger = getLogger(clazz.name)

fun Server.getLogger(clazz: KClass<*>): MessageLogger = getLogger(clazz.java)