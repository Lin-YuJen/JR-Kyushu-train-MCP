package com.enix.server.tool

import com.enix.app.KyushuJRApp
import com.enix.server.log.MessageLogger
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

fun Server.setSearchKyushuJRStationsTool(
    logger: MessageLogger,
    kyushuJRApp: KyushuJRApp
) {

    this.addTool(
        name = "search-kyushu-JR-stations",
        title = "Search Kyushu JR stations",
        description = """
            Search Kyushu JR station names and codes by the keyword.
            This tool can help you find the exact station you want.
            The keyword DOES only support japanese words.
            DO NOT use English/Roman characters as keywords.
            The max size of the result is fixed to 10.
        """.trimIndent(),
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("keyword") {
                    put("type", "string")
                    put("description", "The station name keyword in japanese word.")
                }
            },
            required = listOf("keyword")
        ),
        outputSchema = Tool.Output(
            properties = buildJsonObject {
                put("format", "StationName (StationCode)")
            }
        )
    ) { request ->
        try {
            SearchKyushuJRStations(logger, kyushuJRApp).action(request)
        } catch (exception: Exception) {
            logger.error(exception) { "Failed to search Kyushu JR stations." }
            CallToolResult(
                _meta = buildJsonObject {
                    put("error", exception.message ?: "")
                },
                content = emptyList(),
                isError = true,
            )
        }
    }
}

class SearchKyushuJRStations(
    val logger: MessageLogger,
    val kyushuJRApp: KyushuJRApp,
) {
    suspend fun action(
        request: CallToolRequest
    ): CallToolResult {
        logger.logCallToolRequest(request)
        val keyword = request.arguments["keyword"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Keyword is required.")
        val stationList = kyushuJRApp.findStationName(keyword)

        return CallToolResult(
            _meta = buildJsonObject { put("totalCount", stationList.size) },
            content = stationList.map {
                TextContent("${it.stationName} (${it.stationCode})")
            }
        )
    }
}