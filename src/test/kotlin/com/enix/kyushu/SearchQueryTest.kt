package com.enix.kyushu

import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.Test

class SearchQueryTest {

    private val hakataStation = Station("博多", "00F00B261279")
    private val kumamotoStation = Station("熊本", "00F00B261299")

    @Test
    fun departureStationEqualsArrivalStation() {
        // Throw exception when creating SearchQuery
        val exception = assertThrows<IllegalArgumentException> {
            SearchQuery(
                departureStation = hakataStation,
                arrivalStation = hakataStation,
                timeCondition = TimeCondition.DEPARTURE_TIME,
                searchDatetime = LocalDateTime.now()
            )
        }
        assert(exception.message == "出發站與到達站不可相同")
    }

    @Test
    fun searchDatetimeLimit_BeforeToday() {
        // Throw exception when creating SearchQuery
        val exception = assertThrows<IllegalArgumentException> {
            SearchQuery(
                departureStation = hakataStation,
                arrivalStation = kumamotoStation,
                timeCondition = TimeCondition.DEPARTURE_TIME,
                searchDatetime = LocalDateTime.now().minusDays(1)
            )
        }
        assert(exception.message == "只能查詢當日之後的列車時刻表")
    }

    @Test
    fun searchDatetimeLimit_InOneMonth() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchQuery(
                departureStation = hakataStation,
                arrivalStation = kumamotoStation,
                timeCondition = TimeCondition.DEPARTURE_TIME,
                searchDatetime = LocalDateTime.now().plusDays(30)
            )
        }
        assert(exception.message == "只能查詢未來 30 天內的列車時刻表")
    }

    @Test
    fun childCountAndAdultCountMaxLimit() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchQuery(
                departureStation = hakataStation,
                arrivalStation = kumamotoStation,
                timeCondition = TimeCondition.DEPARTURE_TIME,
                searchDatetime = LocalDateTime.now(),
                childCount = 8,
                adultCount = 0
            )
        }
        assert(exception.message == "合計最多 7 人")
    }


    @Test
    fun childCountAndAdultCountMinLimit() {
        val exception = assertThrows<IllegalArgumentException> {
            SearchQuery(
                departureStation = hakataStation,
                arrivalStation = kumamotoStation,
                timeCondition = TimeCondition.DEPARTURE_TIME,
                searchDatetime = LocalDateTime.now(),
                childCount = 0,
                adultCount = 0
            )
        }
        assert(exception.message == "至少需有一位乘客")
    }
}