package com.enix.kyushu

/**
 * 票價資訊實體，多種『座位種類』X『票券類型』的組合。
 */
data class FareInfo(
    val seatType: SeatType,
    val ticketType: TicketType,
    val availability: Availability,
    val price: Int,
) {

    init {
        require(price >= 0) {
            "price must be non-negative"
        }
    }

    /**
     * 綠色車廂、指定席、自由席、普通車
     */
    enum class SeatType {
        GREEN,
        SPECIFIC,
        FREE,
        NORMAL
    }

    /**
     * eきっぷ、九州ネットきっぷ、JQカード限定
     */
    enum class TicketType {
        NORMAL,
        E_TICKET,
        KYUSHU_NET_TICKET
    }

    /**
     * ○（有空位）、△（少量座位）、×（滿座）
     */
    enum class Availability {
        AVAILABLE,
        SOME,
        FULL
    }
}