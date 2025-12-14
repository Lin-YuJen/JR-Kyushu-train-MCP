package com.enix.app

import com.enix.app.parser.JourneyInfoParser
import com.enix.app.parser.SimpleJourneyInfoParser
import com.enix.common.Logger
import com.enix.kyushu.JRServer
import com.enix.kyushu.JapanHoliday
import com.enix.kyushu.JourneyInfo
import com.enix.kyushu.SearchQuery
import com.enix.kyushu.Station
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MultipartBody
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class KyushuJRApp(
    private val logger: Logger,
    private val journeyInfoParser: JourneyInfoParser = SimpleJourneyInfoParser(logger)
) : JRServer {

    private val httpClient = KyushuJRHttpClient(logger)
    private lateinit var token: String

    private fun getToken(): String {
        val request = httpClient.createGetRequest(KyushuJRAPI.SearchingHomepage)
        val document = httpClient.sendAndGetAsDocument(request)
        return document.getElementsByTag("input")
            .find { it.attr("name") == "_TRANSACTION_TOKEN" }
            ?.value() ?: throw IllegalStateException("Cannot get token.")
    }

    override fun isMaintenance(): Boolean {
        val request = httpClient.createGetRequest(KyushuJRAPI.SearchingHomepage)
        val document = httpClient.sendAndGetAsDocument(request)

        // Check title tag for maintenance indicator
        val title = document.getElementsByTag("title").firstOrNull()?.text()
        if (title?.contains("メンテナンス") == true) {
            return true
        }

        // Check for the maintenance message with specific id
        val errorTitleElements = document.getElementsByAttributeValue("id", "errorTitle")
        return errorTitleElements.any {
            it.text().contains("只今メンテナンスを行っております")
        }.apply {
            if (!this) token = getToken()
        }
    }

    override fun findStationName(
        stationNameKeyword: String
    ): List<Station> {
        verifyStationNameKeyword(stationNameKeyword)
        val request = httpClient.createGetRequest(
            api = KyushuJRAPI.FindSuggestStation,
            queryParams = mapOf(
                "q" to stationNameKeyword,
                "limit" to "10",
                "qt" to "1"
            )
        )
        val jsonResponse = httpClient.sendAndGetAsJson(request) as JsonArray
        return jsonResponse.map { stationJson ->
            Station(
                stationJson.jsonObject["name"]?.jsonPrimitive?.content!!,
                stationJson.jsonObject["code"]?.jsonPrimitive?.content!!
            )
        }
    }

    private val yearMonthDatetimeFormatter = DateTimeFormatter.ofPattern("yyyyMM")
    private fun LocalDateTime.toYearMonthString(): String = this.format(yearMonthDatetimeFormatter)
    private val daysDatetimeFormatter = DateTimeFormatter.ofPattern("d") // 不補 0
    private fun LocalDateTime.toDayString(): String = this.format(daysDatetimeFormatter)
    private val hourOfDayDatetimeFormatter = DateTimeFormatter.ofPattern("H") // 不補 0
    private fun LocalDateTime.toHourOfDayString(): String = this.format(hourOfDayDatetimeFormatter)
    private val minutesDatetimeFormat = DateTimeFormatter.ofPattern("m") // 不補 0
    private fun LocalDateTime.toMinutesString(): String = this.format(minutesDatetimeFormat)

    fun searchStation(
        query: SearchQuery,
        exceptionHandler: (Exception) -> Unit
    ): List<JourneyInfo> {
        try {
            return searchStation(query)
        } catch (e: Exception) {
            exceptionHandler(e)
            return emptyList()
        }
    }

    override fun searchStation(
        query: SearchQuery
    ): List<JourneyInfo> {

        val request = httpClient.createPostRequest(KyushuJRAPI.SearchStation) {
            MultipartBody.Builder().apply {
                setType(MultipartBody.FORM)
                mapOf(
                    // required parameters
                    "btSearch" to "0",
                    "dsName" to query.departureStation.stationName,
                    "dsCode" to query.departureStation.stationCode,
                    "asName" to query.arrivalStation.stationName,
                    "asCode" to query.arrivalStation.stationCode,
                    "trYearMonth" to query.searchDatetime.toYearMonthString(),
                    "trDay" to query.searchDatetime.toDayString(),
                    "trHour" to query.searchDatetime.toHourOfDayString(),
                    "trMinute" to query.searchDatetime.toMinutesString(),
                    "daType" to query.timeCondition.ordinal.toString(),
                    "adult" to query.adultCount.toString(),
                    "child" to query.childCount.toString(),
                    "_TRANSACTION_TOKEN" to if (::token.isInitialized) token else getToken(),
                ).forEach(this::addFormDataPart)

                // Optional checkbox parameters
                query.takeIf { it.includeExpress }?.let {
                    addFormDataPart("useExpress", "on")
                }
                query.takeIf { !it.allowTransfer }?.let {
                    addFormDataPart("nonChange", "on")
                }
                query.takeIf { it.includeShinkansen }?.let {
                    addFormDataPart("useShinkansen", "on")
                }
                query.takeIf { it.includeOuterKyushu }?.let {
                    addFormDataPart("serviceKind", "on")
                }
            }.build()
        }
        val document = httpClient.sendAndGetAsDocument(request)
        try {
            return journeyInfoParser.parse(document)
        } catch (parsingFailureException: Exception) {
            val errorMessage = document.getElementById("errorTitle")?.text()
                ?: "Failed to parse the response."
            throw IllegalStateException(errorMessage, parsingFailureException)
        }
    }

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d")

    /**
     * Get the list of Japan holidays.
     * Duration is from 5 years ago to half a year later from today.
     */
    override fun getJapanHolidayList(): List<JapanHoliday> {
        val request = httpClient.createGetRequest(KyushuJRAPI.FindJpHoliday)
        val jsonResponse = httpClient.sendAndGetAsJson(request) as JsonArray
        val holidayList = jsonResponse
            .map { jsonElement ->
                jsonElement.jsonObject["holidayDate"]?.jsonPrimitive?.content to jsonElement.jsonObject["holidayName"]?.jsonPrimitive?.content
            }
            .filter { !it.first.isNullOrBlank() }
            .filter { !it.second.isNullOrBlank() }
            .map { pair ->
                JapanHoliday(
                    dateTimeFormatter.parse(pair.first!!, LocalDate::from),
                    pair.second!!
                )
            }
        return holidayList
    }
}