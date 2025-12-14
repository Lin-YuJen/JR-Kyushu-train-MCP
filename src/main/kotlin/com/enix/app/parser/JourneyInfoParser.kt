package com.enix.app.parser

import com.enix.kyushu.JourneyInfo
import com.fleeksoft.ksoup.nodes.Document

interface JourneyInfoParser {

    fun parse(
        document: Document
    ): List<JourneyInfo>

}