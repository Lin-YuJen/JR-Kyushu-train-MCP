package com.enix.server.tool

import com.enix.app.KyushuJRApp
import com.enix.common.SystemOutLogger
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Ignore
import kotlin.test.Test

class SearchKyushuJRTrainTest {

    private val tool = SearchKyushuJRTrain(
        logger = SystemOutLogger(),
        kyushuJRApp = KyushuJRApp(SystemOutLogger())
    )

    @Test
    @Ignore
    fun testTrainSearchingFunction() = runTest {
        val deferred = async {
            assertDoesNotThrow {
                tool.action(
                    CallToolRequest(
                        name = "SearchKyushuJRTrain",
                        arguments = mapOf(
                            "departureStationName" to "博多",
                            "departureStationCode" to "00F00B261279",
                            "arrivalStationName" to "熊本",
                            "arrivalStationCode" to "00F00B261299",
                            "arrivalStationName" to "由布院",
                            "arrivalStationCode" to "00F00B361329",
                            "timeCondition" to 0,
                            "searchDatetime" to "2025/12/06 23:45"
                        )
                            .mapValues { JsonPrimitive(it.value.toString()) }
                            .let { buildJsonObject { it.forEach(this::put) } }
                    )
                )
            }
        }
        val result = deferred.await()
        result.content.forEach { println(it) }
    }
}