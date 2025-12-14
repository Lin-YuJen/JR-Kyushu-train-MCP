package com.enix.kyushu

enum class TimeCondition {
    // 出發時間、到站時間、首班時間、末班時間
    DEPARTURE_TIME,
    ARRIVAL_TIME,
    FIRST_CLASS_TIME,
    LAST_CLASS_TIME;

    companion object {
        fun of(order: Int) = when (order) {
            0 -> DEPARTURE_TIME
            1 -> ARRIVAL_TIME
            2 -> FIRST_CLASS_TIME
            3 -> LAST_CLASS_TIME
            else -> throw IllegalArgumentException("order must be 0 to 3")
        }
    }
}