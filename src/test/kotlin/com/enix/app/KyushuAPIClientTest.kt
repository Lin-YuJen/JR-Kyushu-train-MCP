package com.enix.app

import com.enix.common.SystemOutLogger
import kotlinx.serialization.json.JsonArray
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test

class KyushuAPIClientTest {

    val client = KyushuJRHttpClient(SystemOutLogger())

    @Test
    fun testGettingStationNameAndCode() {
        val request = client.createGetRequest(
            KyushuJRAPI.FindSuggestStation,
            mapOf(
                "q" to "福",
                "limit" to "10",
                "qt" to "1"
            )
        )
        val jsonResponse = assertDoesNotThrow("Successfully get the response.") {
            client.sendAndGetAsJson(request)
        }
        assert(jsonResponse is JsonArray)
    }

    @Test
    fun testGettingHolidayList() {
        val request = client.createGetRequest(
            KyushuJRAPI.FindJpHoliday
        )
        val jsonResponse = assertDoesNotThrow("Successfully get the response.") {
            client.sendAndGetAsJson(request)
        }
        assert(jsonResponse is JsonArray)
    }

    @Test
    fun testGettingHomepageHtml() {
        val request = client.createGetRequest(
            KyushuJRAPI.SearchingHomepage
        )
        val document = assertDoesNotThrow("Successfully get the response.") {
            client.sendAndGetAsDocument(request)
        }
        assert(document.title().isNotEmpty())
        assert("ネット予約" == document.title())
        val token = document.getElementsByTag("input")
            .find { it.attr("name") == "_TRANSACTION_TOKEN" }
            ?.value()
        assert(token?.isNotEmpty() == true)
    }

}