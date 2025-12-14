package com.enix.server

import com.enix.server.log.configureLogging
import com.enix.server.prompt.configuratePrompts
import com.enix.server.resource.configurateResources
import com.enix.server.tool.configurateTool
import io.ktor.utils.io.streams.asInput
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun runMCPServer() {
    // Create the MCP Server instance with a basic implementation
    val server = Server(
        Implementation(
            name = "Kyushu-train", // Tool name
            version = "0.1.0"
        ),
        ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
                resources = ServerCapabilities.Resources(
                    subscribe = true,
                    listChanged = true
                ),
                prompts = ServerCapabilities.Prompts(listChanged = true),
//                logging = null // Set null for inspector
                logging = buildJsonObject {
                    put("level", "info")
                }
            )
        )
    )

    // Create a transport using standard IO for server communication
    val transport = StdioServerTransport(
        System.`in`.asInput(),
        System.out.asSink().buffered()
    )

    server.apply {
        configureLogging()
        configurateTool()
        configurateResources()
        configuratePrompts()
    }

    runBlocking {
        server.connect(transport)
        val done = Job()
        server.onClose {
            done.complete()
        }
        done.join()
    }
}