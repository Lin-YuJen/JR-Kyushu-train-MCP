package com.enix.server.resource

import com.enix.app.KyushuJRApp
import com.enix.server.log.MessageLogger
import com.enix.server.log.MessageLoggerAdaptor
import com.enix.server.log.getLogger
import io.modelcontextprotocol.kotlin.sdk.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.server.Server

/**
 * # How do you use the resources in Claude Desktop?
 *
 * You can add the resources manually in Claude Desktop by clicking the `+` button in the chat windows.
 *
 * Notice that it is different from the MCP tools button.
 *
 * ```txt
 *                    ✱ Evening, Enix
 * ┌──────────────────────────────────────────────────────┐
 * │   How can I help you today?                          │
 * │   [+]  [=]  [0]                      Haiku 4.5  [↑]  │
 * └──────────────────────────────────────────────────────┘
 * ```
 * You can see there are three buttons in the left bottom corner:
 * * `[+]`: **Add a resource**
 * * `[=]`: Search and tools. You can see all the MCP tools here.
 * * `[0]`: Extended thinking
 */
fun Server.configurateResources() {

    val logger: MessageLogger = this.getLogger(this::class.java)
    val kyushuJRApp = KyushuJRApp(MessageLoggerAdaptor(logger))

    this.addResource(
        uri = "system://japan-holidays-list",
        name = "Japan Holidays List",
        description = """
            Get the holidays list of Japan. Duration.
            Duration is from 5 years ago to half a year later from today.
        """.trimMargin(),
        mimeType = "text/plain",
    ) { request ->
        try {
            logger.logReadResourceRequest(request)
            val holidayList = kyushuJRApp.getJapanHolidayList()
            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        text = holidayList.joinToString("\n") { "${it.holidayName} - ${it.date}" },
                        uri = request.uri,
                        mimeType = "text/plain"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to get the holiday list." }
            ReadResourceResult(
                listOf(
                    TextResourceContents(
                        text = "",
                        uri = request.uri,
                        mimeType = "text/plain"
                    )
                )
            )
        }
    }
}