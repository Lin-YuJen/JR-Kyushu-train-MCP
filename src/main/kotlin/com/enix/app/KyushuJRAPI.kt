package com.enix.app

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * This object contains all the APIs for Kyushu JR.
 */
sealed class KyushuJRAPI(
    val path: String,
    val method: Method,
    val queryStringSet: Set<String> = setOf()
) {
    private val baseUrlString = "https://train.yoyaku.jrkyushu.co.jp"

    open fun getHttpUrl(
        queryParams: Map<String, String> = emptyMap()
    ): HttpUrl = "${baseUrlString}${path}".toHttpUrl().newBuilder().apply {
        queryParams.filter { it.key in queryStringSet }
            .forEach(this::addQueryParameter)
    }.build()

    open fun getRequestBody(
        conditionsMap: Map<String, String> = emptyMap()
    ): RequestBody {
        throw UnsupportedOperationException("This API does not support RequestBody.")
    }

    /**
     * Get the station information include the station code and station name.
     * The response is a JSON array.
     *
     * There are 3 parameters:
     * - `q`: The station name to search.
     * - `limit`: The number of results to return. Should always be `10`.
     * - `qt`: Unknown property. Should always be `1`
     *
     * Example:
     * ```json
     * [
     * 	 {
     * 	    "code": "00F00A520A42",
     * 	    "name": "福井"
     * 	 },
     * 	 {
     * 	    "code": "00F00A7409B1",
     * 	    "name": "福江"
     * 	 }
     * ]
     * ```
     */
    object FindSuggestStation : KyushuJRAPI(
        "/jr/sugg/FindSuggestStationJson",
        Method.GET,
        setOf("q", "limit", "qt")
    )

    /**
     * Get the holiday information in Japan.
     * The date range is from 5 years ago to half a year later from today.
     * The response is a JSON array.
     *
     * Example:
     * ```json
     * [
     *   {
     *     "holidayDate": "2026/2/23",
     *     "holidayName": "天皇誕生日"
     *   },
     *   {
     *     "holidayDate": "2026/3/20",
     *     "holidayName": "春分の日"
     *   }
     * ]
     * ```
     */
    object FindJpHoliday : KyushuJRAPI(
        "/jr/cal/FindJpHolidayJson",
        Method.GET
    )

    /**
     * Get the homepage of Kyushu JR.
     */
    object SearchingHomepage : KyushuJRAPI(
        "/jr/pc/Top",
        Method.GET
    )

    /**
     * Get the HTML page of the station search result.
     */
    object SearchStation : KyushuJRAPI(
        "/jr/pc/route/Top/searchStation",
        Method.POST
    ) {
        // All parameter names need in this API
        val formParameterNames = setOf(
            "btSearch",
            "dsName", "dsCode", "asName", "asCode",
            "trYearMonth", "trDay", "trHourHid", "trMinuteHid",
            "trHour", "trMinute",
            "daType",
            "adult", "child",
            "useExpress", "useShinkansen", "serviceKind", "nonChange",
            "_TRANSACTION_TOKEN"
        )

        override fun getRequestBody(
            conditionsMap: Map<String, String>
        ) = MultipartBody.Builder().apply {
            setType(MultipartBody.Companion.FORM)
            conditionsMap
                .filter { it.key in formParameterNames }
                .filter { it.value.isNotEmpty() }
                .forEach(this::addFormDataPart)
        }.build()
    }

    enum class Method {
        GET,
        POST
    }
}