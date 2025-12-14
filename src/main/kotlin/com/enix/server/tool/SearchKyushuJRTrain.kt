package com.enix.server.tool

import com.enix.app.KyushuJRApp
import com.enix.common.Logger
import com.enix.kyushu.FareInfo
import com.enix.kyushu.SearchQuery
import com.enix.kyushu.Station
import com.enix.kyushu.TimeCondition
import com.enix.server.log.MessageLogger
import com.enix.server.log.MessageLoggerAdaptor
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Server.setSearchKyushuJRTrainTool(
    logger: MessageLogger,
    kyushuJRApp: KyushuJRApp
) {
    this.addTool(
        name = "search-kyushu-JR-train",
        title = "Search for Kyushu JR trains",
        description = """
            Search for Kyushu JR trains.
            Get the list of trains from the station you specify.
            The limit of the number of the routes is 3.
            You should use the `search-kyushu-JR-stations` tool to find out the station codes.
        """.trimIndent(),
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("departureStationName") {
                    put("type", "string")
                    put("description", "The departure station name")
                }
                putJsonObject("departureStationCode") {
                    put("type", "string")
                    put(
                        "description", """
                        The departure station code.
                        You can get the code through the `search-kyushu-JR-stations` tool.
                    """.trimIndent()
                    )
                }
                putJsonObject("arrivalStationName") {
                    put("type", "string")
                    put("description", "The arrival station name")
                }
                putJsonObject("arrivalStationCode") {
                    put("type", "string")
                    put(
                        "description", """
                        The arrival station code.
                        You can get the code through the `search-kyushu-JR-stations` tool.
                    """.trimIndent()
                    )
                }
                putJsonObject("timeCondition") {
                    put("type", "integer")
                    put(
                        "description", """
                        The time condition of the search. There are 4 options:
                        0: DepartureTime (default)
                        1: ArrivalTime
                        2: First class departure time (time of the first train in the route)
                        3: Last class departure time (time of the last train in the route)
                    """.trimIndent()
                    )
                }
                putJsonObject("searchDatetime") {
                    put("type", "string")
                    put(
                        "description", """
                        The format of the search datetime is `yyyy/MM/dd HH:mm`.
                        Notice that if `timeCondition` is set to 2 or 3, 
                        hours and minutes will be ignored but you still need to fill them.
                    """.trimIndent()
                    )
                }
                putJsonObject("adult") {
                    put("type", "integer")
                    put("description", "How many adults ticket are needed. (default: 1)")
                }
                putJsonObject("child") {
                    put("type", "integer")
                    put("description", "How many children ticket are needed. (default: 0)")
                }
                putJsonObject("allowTransfer") {
                    put("type", "boolean")
                    put("description", "Whether to allow transfer (乗換え). (default: true)")
                }
                putJsonObject("includeExpress") {
                    put("type", "boolean")
                    put("description", "Whether to include express trains (特急・急行). (default: true)")
                }
                putJsonObject("includeShinkansen") {
                    put("type", "boolean")
                    put("description", "新幹線を使う. (default: true)")
                }
                putJsonObject("includeOuterKyushu") {
                    put("type", "boolean")
                    put("description", "小倉⇔博多間の新幹線や、九州外の発着を含める. (default: true)")
                }
            },
            required = listOf(
                "departureStationName", "departureStationCode", "arrivalStationName", "arrivalStationCode",
                "searchDatetime"
            )
        ),
        outputSchema = Tool.Output(
            properties = buildJsonObject {
                put("Format of 運賃情報", """
                    Each line represents the availability, seat type, ticket type and price of a fare.
                    Format: `Availability [Seat type] Ticket type: Price`
                        - Availability: there are three marks
                            * × means the seat is full or ただいまの時間帯は、空席状況のご案内はできません
                            * △ means the seat is available but there aren't many remaining spots left.
                            * ○ means the seat is available and there are many remaining spots left.
                        - Price: in JPY
                    Example: × [指定席] 通常のきっぷ: ¥11,190
                """.trimIndent())
            }
        )
    ) { request ->
        try {
            logger.info("SearchKyushuJRTrain called. ${request.arguments}")
            SearchKyushuJRTrain(
                MessageLoggerAdaptor(logger),
                kyushuJRApp
            ).action(request)
        } catch (exception: Exception) {
            logger.error(exception) { "Failed to search Kyushu JR Trains." }
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

/**
 * # Sample output
 *
 * ```txt
 * 乗車日: 2025/12/02    おとな1人    こども0人
 * 博多 ➜ 由布院
 *
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚄 Route 3: 11:15 ▸ 14:12
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 📅 事業日: 2025/12/02
 * ⏱️ 所要時間: 177分
 * 📏 距離: 240.9Km
 *
 * 🚉 列車詳細:
 * 1. 新幹線のぞみ２４号
 * 博多 11:15 → 小倉 11:30
 *
 * 2. 特急ソニック１７号
 * 小倉 11:42 → 大分 13:01
 *
 * 3. 特急ゆふ４号
 * 大分 13:25 → 由布院 14:12
 *
 * 💰 運賃情報:
 *   × [指定席] 通常のきっぷ: ¥11,190
 *   × [指定席] e きっぷ: ¥10,300
 *   × [自由席] 通常のきっぷ: ¥8,710
 *   × [自由席] e きっぷ: ¥8,710
 *
 * 🔄 乗換: 2回
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * ```
 */
class SearchKyushuJRTrain(
    val logger: Logger,
    val kyushuJRApp: KyushuJRApp,
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun action(
        request: CallToolRequest
    ): CallToolResult {
        val query = SearchQuery(
            departureStation = Station(
                stationName = request.argument("departureStationName"),
                stationCode = request.argument("departureStationCode")
            ),
            arrivalStation = Station(
                stationName = request.argument("arrivalStationName"),
                stationCode = request.argument("arrivalStationCode")
            ),
            timeCondition = TimeCondition.of(
                request.argument("timeCondition") {
                    logger.info("Use the default value `DepartureTime` as the `timeCondition` argument.`")
                    "0"
                }.toInt()
            ),
            searchDatetime = request.argument("searchDatetime").let {
                LocalDateTime.parse(it, dateTimeFormatter)
            },
            adultCount = request.arguments["adult"]?.jsonPrimitive?.intOrNull ?: let {
                logger.info("Use the default value 1 as the `adult` argument.")
                1
            },
            childCount = request.arguments["child"]?.jsonPrimitive?.intOrNull ?: let {
                logger.info("Use the default value 0 as the `child` argument.")
                0
            },
            includeExpress = true,
            allowTransfer = true,
            includeShinkansen = true,
            includeOuterKyushu = true
        )

        if (kyushuJRApp.isMaintenance()) {
            logger.info("Kyushu JR is currently under maintenance.")
            return CallToolResult(
                content = listOf(
                    TextContent("Kyushu JR is currently under maintenance. Please try again later.")
                )
            )
        }

        var errorMessage = ""
        val journeyList = kyushuJRApp.searchStation(query) { exception ->
            logger.info { "Failed to search Kyushu JR trains. ${exception.message}" }
            errorMessage = exception.message ?: ""
        }
        if (errorMessage.isNotEmpty()) return CallToolResult(content = listOf(TextContent(errorMessage)))

        return CallToolResult(
            _meta = buildJsonObject {
                put("totalCount", journeyList.size)
            },
            content = journeyList.mapIndexed { index, journey ->
                formatJourneyContent(index + 1, journey, query)
            }.map { TextContent(it) }
        )
    }

    private fun formatJourneyContent(
        routeNumber: Int,
        journey: com.enix.kyushu.JourneyInfo,
        query: SearchQuery
    ): String {
        val date = journey.departureTime.format(dateFormatter)
        val departureTime = journey.departureTime.format(timeFormatter)
        val arrivalTime = journey.arrivalTime.format(timeFormatter)

        val transferDetails = formatTransferDetails(journey.transferDetailList)
        val fareInfo = formatFareInfo(journey.fareInfoList)
        val transferIcon = if (journey.isTransfer) "🔄" else "➡️"

        return """
            乗車日: $date    おとな${query.adultCount}人    こども${query.childCount}人
            ${query.departureStation.stationName} ➜ ${query.arrivalStation.stationName}
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            🚄 Route $routeNumber: $departureTime ▸ $arrivalTime
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            📅 事業日: $date
            ⏱️ 所要時間: ${journey.duration.toMinutes()}分
            📏 距離: ${journey.distance}Km
            
            🚉 列車詳細:
            $transferDetails
            
            💰 運賃情報:
            $fareInfo
            
            $transferIcon 乗換: ${journey.transferCount}回
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        """.trimIndent()
    }

    private fun formatTransferDetails(transferDetails: List<com.enix.kyushu.TransferDetail>): String {
        return transferDetails.joinToString("\n         \n            ") { detail ->
            val depTime = detail.departureTime.format(timeFormatter)
            val arrTime = detail.arrivalTime.format(timeFormatter)
            """
            ${detail.sequence + 1}. ${detail.trainName}
                        ${detail.departureStation.stationName} $depTime → ${detail.arrivalStation.stationName} $arrTime
            """.trimIndent()
        }
    }

    private fun formatFareInfo(fareList: List<FareInfo>): String {
        return fareList.joinToString("\n            ") { fare ->
            val seatTypeName = when (fare.seatType) {
                FareInfo.SeatType.GREEN -> "グリーン車"
                FareInfo.SeatType.SPECIFIC -> "指定席"
                FareInfo.SeatType.FREE -> "自由席"
                FareInfo.SeatType.NORMAL -> "普通車"
            }

            val ticketTypeName = when (fare.ticketType) {
                FareInfo.TicketType.NORMAL -> "通常のきっぷ"
                FareInfo.TicketType.E_TICKET -> "e きっぷ"
                FareInfo.TicketType.KYUSHU_NET_TICKET -> "九州ネットきっぷ"
            }

            val availabilityIcon = when (fare.availability) {
                FareInfo.Availability.AVAILABLE -> "○"
                FareInfo.Availability.SOME -> "△"
                FareInfo.Availability.FULL -> "×"
            }

            "  $availabilityIcon [$seatTypeName] $ticketTypeName: ¥${String.format("%,d", fare.price)}"
        }
    }

    private fun CallToolRequest.argument(
        key: String,
        defaultValue: () -> String = {
            throw IllegalArgumentException("Argument '$key' is required.")
        }
    ) = this.arguments[key]?.jsonPrimitive?.content ?: defaultValue()

}