package com.enix.app

import com.enix.common.Logger
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

class KyushuJRHttpClient(
    private val logger: Logger
) {

    companion object {
        private val httpClient = OkHttpClient.Builder().apply {
            // Force HTTP/1.1 to avoid HTTP/2 errors
            protocols(listOf(Protocol.HTTP_1_1))
            followRedirects(true)
            followSslRedirects(true)
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
        }.build()
    }

    private val header = Headers.Builder().apply {
        mapOf(
            "Accept" to "*/*",
            "Accept-Language" to "ja,en-US;q=0.9,en;q=0.8",
            "Origin" to "https://train.yoyaku.jrkyushu.co.jp",
            "Referer" to "https://train.yoyaku.jrkyushu.co.jp/jr/pc/route/Top/",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "sec-ch-ua" to "\"Microsoft Edge\";v=\"141\", \"Not?A_Brand\";v=\"8\"",
            "sec-ch-ua-mobile" to "?0",
            "sec-ch-ua-platform" to "\"Windows\"",
            "sec-fetch-dest" to "empty",
            "sec-fetch-mode" to "cors",
            "sec-fetch-site" to "same-origin"
        ).forEach(this::add)
    }.build()

    fun createPostRequest(
        api: KyushuJRAPI,
        requestBody: RequestBody
    ): Request = Request.Builder().apply {
        if (api.method != KyushuJRAPI.Method.POST) throw IllegalArgumentException("API must be POST.")
        headers(header)
        url(api.getHttpUrl())
        post(requestBody)
    }.build()

    fun createPostRequest(
        api: KyushuJRAPI,
        requestBody: () -> RequestBody
    ): Request = createPostRequest(api, requestBody())

    fun createGetRequest(
        api: KyushuJRAPI,
        queryParams: Map<String, String> = emptyMap()
    ): Request = Request.Builder().apply {
        if (api.method != KyushuJRAPI.Method.GET) throw IllegalArgumentException("API must be GET.")
        headers(header)
        url(api.getHttpUrl(queryParams))
        get()
    }.build()

    private fun <T> sendRequest(
        request: Request,
        responseStringProcessor: (String) -> T
    ): T {
        logger.debug("Sending request to ${request.url}")
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception(
                """
                    Request failed with code: ${response.code}. 
                    Response Body: ${response.body.string()}
                """.trimIndent()
            )
            val responseBody = response.body.string()
            logger.debug("Response is $responseBody")
            return responseStringProcessor(responseBody)
        }
    }

    fun sendAndGetAsDocument(
        request: Request
    ): Document = sendRequest(request) { responseBody ->
        Ksoup.parse(responseBody)
    }

    fun sendAndGetAsJson(
        request: Request
    ): JsonElement = sendRequest(request) { responseBody ->
        Json.parseToJsonElement(responseBody)
    }
}