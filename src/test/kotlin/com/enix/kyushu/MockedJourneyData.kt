package com.enix.kyushu

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MockedJourneyData {

    val datetimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

    fun String.toLocalDateTime(): LocalDateTime =
        datetimeFormatter.parse(this, LocalDateTime::from)

    fun getTransferDetailList(
        index: Int = 0
    ) = when (index) {
        0 -> listOf(
            TransferDetail(
                sequence = 1,
                trainName = "新幹線こだま８３８号",
                departureStation = Station("博多", "00F00B261279"),
                departureTime = "2025/12/02 06:07".toLocalDateTime(),
                arrivalStation = Station("小倉", "00F00B261267"),
                arrivalTime = "2025/12/02 06:24".toLocalDateTime()
            ),
            TransferDetail(
                sequence = 2,
                trainName = "特急にちりん３号",
                departureStation = Station("小倉", "00F00B261267"),
                departureTime = "2025/12/02 06:39".toLocalDateTime(),
                arrivalStation = Station("大分", "00F00B38144B"),
                arrivalTime = "2025/12/02 08:14".toLocalDateTime()
            ),
            TransferDetail(
                sequence = 3,
                trainName = "特急ゆふ２号",
                departureStation = Station("大分", "00F00B38144B"),
                departureTime = "2025/12/02 08:20".toLocalDateTime(),
                arrivalStation = Station("由布院", "00F00B361329"),
                arrivalTime = "2025/12/02 09:06".toLocalDateTime()
            )
        )

        1 -> listOf(
            TransferDetail(
                sequence = 1,
                trainName = "特急ゆふ１号",
                departureStation = Station("博多", "00F00B261279"),
                departureTime = "2025/12/02 07:43".toLocalDateTime(),
                arrivalStation = Station("由布院", "00F00B361329"),
                arrivalTime = "2025/12/02 10:02".toLocalDateTime()
            )
        )

        2 -> listOf(
            TransferDetail(
                sequence = 1,
                trainName = "特急ゆふいんの森１号",
                departureStation = Station("博多", "00F00B261279"),
                departureTime = "2025/12/02 09:17".toLocalDateTime(),
                arrivalStation = Station("由布院", "00F00B361329"),
                arrivalTime = "2025/12/02 11:31".toLocalDateTime()
            )
        )

        else -> throw IllegalArgumentException("index must be 0 to 2")
    }

    fun getFareInfo(
        index: Int = 0
    ) = when (index) {
        0 -> listOf(
            FareInfo(
                seatType = FareInfo.SeatType.NORMAL,
                ticketType = FareInfo.TicketType.E_TICKET,
                availability = FareInfo.Availability.AVAILABLE,
                price = 5100
            )
        )
        else -> throw IllegalArgumentException("index must be 0 or 1")
    }
}