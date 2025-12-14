package com.enix.app

import com.enix.kyushu.*
import com.fleeksoft.ksoup.Ksoup
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.Test

class GrabHtmlTest {

    private val targetHtml = this::class.java.classLoader
        .getResourceAsStream("test/net-yoyaku-result-min.html")
        ?.bufferedReader()
        ?.use { it.readText() }
        ?: throw IllegalStateException("Cannot find test resource: test/net-yoyaku-result-min.html")
    private val document = Ksoup.parse(targetHtml)

    /**
     * Use `Ksoup` to get the component and its value.
     */
    @Test
    fun getComponent() {
        // Global stations
        val globalDepStation =
            document.selectFirst(".ticketInfoGrid .fromToBox .from .almB")?.text()?.trim() ?: "Unknown"
        val globalArrStation = document.selectFirst(".ticketInfoGrid .fromToBox .to .almB")?.text()?.trim() ?: "Unknown"

        val journeyList = mutableListOf<JourneyInfo>()
        val tables = document.select("div.timeSummary table.tableContent")

        for (table in tables) {
            // Date
            val dateText = table.select(".rideMeta p").firstOrNull()?.text()
                ?: table.select(".rideInfo .rideTime").find { it.text().contains("乗車日") }?.text()
                ?: ""
            val date = parseDate(dateText)

            // Distance
            val distanceText = table.select(".rideTime").find { it.text().contains("距離") }?.text() ?: "0.0Km"
            val distance = parseDistance(distanceText)

            // Segments
            val transferDetails = mutableListOf<TransferDetail>()
            val rideTable = table.selectFirst("div.rideTable")

            if (rideTable != null) { // 有轉乘
                val labels = rideTable.select("div.rideLabel")
                val details = rideTable.select("div.rideDetail")

                for (i in details.indices) {
                    val depLabel = labels[i]
                    val arrLabel = labels[i + 1]
                    val detail = details[i]

                    val depTimeStr = depLabel.selectFirst(".dpt")?.text()?.trim() ?: "00:00"
                    val arrTimeStr = arrLabel.selectFirst(".arr")?.text()?.trim() ?: "00:00"
                    val depStationName = depLabel.selectFirst(".name")?.text()?.trim() ?: ""
                    val arrStationName = arrLabel.selectFirst(".name")?.text()?.trim() ?: ""
                    val trainName = detail.selectFirst(".trainName")?.text()?.trim() ?: ""

                    val depTime = LocalDateTime.of(date, LocalTime.parse(depTimeStr))
                    var arrTime = LocalDateTime.of(date, LocalTime.parse(arrTimeStr))
                    if (arrTime.isBefore(depTime)) {
                        arrTime = arrTime.plusDays(1)
                    }

                    transferDetails.add(
                        TransferDetail(
                            sequence = i,
                            trainName = trainName,
                            departureStation = Station(depStationName, "0000"),
                            departureTime = depTime,
                            arrivalStation = Station(arrStationName, "0000"),
                            arrivalTime = arrTime
                        )
                    )
                }
            } else { // 無轉乘
                val trainName = table.selectFirst(".rideUnit")?.text()?.trim() ?: ""
                val depTimeStr = table.selectFirst(".fromToBox .from")?.text()?.trim() ?: "00:00"
                val arrTimeStr = table.selectFirst(".fromToBox .to")?.text()?.trim() ?: "00:00"

                val depTime = LocalDateTime.of(date, LocalTime.parse(depTimeStr))
                var arrTime = LocalDateTime.of(date, LocalTime.parse(arrTimeStr))
                if (arrTime.isBefore(depTime)) {
                    arrTime = arrTime.plusDays(1)
                }

                transferDetails.add(
                    TransferDetail(
                        sequence = 0,
                        trainName = trainName,
                        departureStation = Station(globalDepStation, "0000"),
                        departureTime = depTime,
                        arrivalStation = Station(globalArrStation, "0000"),
                        arrivalTime = arrTime
                    )
                )
            }

            // Link segments
            for (i in 0 until transferDetails.size - 1) {
                transferDetails[i].nextDetail = transferDetails[i + 1]
            }

            // Fares
            val fareInfos = mutableListOf<FareInfo>()
            val rows = table.select("tbody > tr")
            for (row in rows) {
                val icoCell = row.selectFirst("td.icoCell") ?: continue
                val seatTypeText = icoCell.selectFirst(".txt")?.text() ?: ""
                val seatType = parseSeatType(seatTypeText)

                val markCells = row.select("td.markCell")
                // Map columns: 0->Normal, 1->E_Ticket, 2->Net_Ticket
                val ticketTypes = listOf(
                    FareInfo.TicketType.NORMAL,
                    FareInfo.TicketType.E_TICKET,
                    FareInfo.TicketType.KYUSHU_NET_TICKET
                )

                for (i in 0 until minOf(markCells.size, ticketTypes.size)) {
                    val cell = markCells[i]
                    val markDiv = cell.selectFirst("div.mark")
                    if (markDiv != null) {
                        val statusText = markDiv.selectFirst(".status")?.text() ?: ""
                        val priceText = markDiv.selectFirst(".price")?.text() ?: "0"

                        val availability = parseAvailability(statusText)
                        val price = parsePrice(priceText)

                        fareInfos.add(
                            FareInfo(
                                seatType = seatType,
                                ticketType = ticketTypes[i],
                                availability = availability,
                                price = price
                            )
                        )
                    }
                }
            }

            if (transferDetails.isNotEmpty() && fareInfos.isNotEmpty()) {
                journeyList.add(
                    assertDoesNotThrow {
                        JourneyInfo(
                            distance = distance,
                            transferDetailList = transferDetails,
                            fareInfoList = fareInfos
                        )
                    }
                )
            }
        }

        // Print results
        journeyList.forEach { journey ->
            println("Journey: ${journey.departureTime} -> ${journey.arrivalTime}, Dist: ${journey.distance}")
            journey.transferDetailList.forEach {
                println("  Seg: ${it.departureTime} ${it.departureStation.stationName} -> ${it.arrivalTime} ${it.arrivalStation.stationName} [${it.trainName}]")
            }
            journey.fareInfoList.forEach {
                println("  Fare: ${it.seatType} ${it.ticketType} ${it.price} ${it.availability}")
            }
        }
    }

    private fun parseDate(text: String): LocalDate {
        val regex = Regex("(\\d+)月(\\d+)日")
        val match = regex.find(text)
        return if (match != null) {
            val (month, day) = match.destructured
            LocalDate.of(2025, month.toInt(), day.toInt()) // Assuming 2025
        } else {
            LocalDate.now()
        }
    }

    private fun parseDistance(text: String): Double {
        val regex = Regex("([\\d.]+)Km")
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.toDouble() ?: 0.0
    }

    private fun parsePrice(text: String): Int {
        return text.replace(",", "").replace("円", "").trim().toIntOrNull() ?: 0
    }

    private fun parseAvailability(text: String): FareInfo.Availability {
        return when (text) {
            "○" -> FareInfo.Availability.AVAILABLE
            "△" -> FareInfo.Availability.SOME
            "×" -> FareInfo.Availability.FULL
            else -> FareInfo.Availability.FULL
        }
    }

    private fun parseSeatType(text: String): FareInfo.SeatType {
        return when {
            text.contains("グリーン車") -> FareInfo.SeatType.GREEN
            text.contains("指定席") -> FareInfo.SeatType.SPECIFIC
            text.contains("自由席") -> FareInfo.SeatType.FREE
            else -> FareInfo.SeatType.NORMAL
        }
    }

    private fun minOf(a: Int, b: Int): Int = if (a <= b) a else b
}