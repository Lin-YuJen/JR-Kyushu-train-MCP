package com.enix.server.tool

import com.enix.app.KyushuJRApp
import com.enix.server.log.MessageLogger
import com.enix.server.log.MessageLoggerAdaptor
import com.enix.server.log.getLogger
import io.modelcontextprotocol.kotlin.sdk.server.Server

fun Server.configurateTool() {

    val logger: MessageLogger = this.getLogger(this::class)
    val kyushuJRApp = KyushuJRApp(MessageLoggerAdaptor(logger))

    this.setSearchKyushuJRStationsTool(
        logger = logger,
        kyushuJRApp = kyushuJRApp
    )

    this.setSearchKyushuJRTrainTool(
        logger = logger,
        kyushuJRApp = kyushuJRApp
    )
}