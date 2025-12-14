package com.enix.kyushu

import java.time.LocalDateTime

class SearchQuery(
    // 出發站
    val departureStation: Station,
    // 到達站
    val arrivalStation: Station,
    // 時間條件
    val timeCondition: TimeCondition,
    // 搜尋日期時間
    val searchDatetime: LocalDateTime,
    // 大人人數
    val adultCount: Int = 1,
    // 小孩人數
    val childCount: Int = 0,
    // 是否包含特急或急行列車，預設為 true
    val includeExpress: Boolean = true,
    // 是否轉乘，預設為 true
    val allowTransfer: Boolean = true,
    // 是否包含新幹線列車，預設為 true
    val includeShinkansen: Boolean = true,
    // 是否包含小倉到博多之間的新幹線、或九州之外發車的列車，預設為 true
    val includeOuterKyushu: Boolean = true
) {

    init {
        require(departureStation != arrivalStation) {
            "出發站與到達站不可相同"
        }

        val startOfToday = LocalDateTime.now()
            .withHour(0).withMinute(0)
            .withSecond(0).withNano(0)
        require(searchDatetime.isAfter(startOfToday)) {
            "只能查詢當日之後的列車時刻表"
        }

        val theDayIn30Days = startOfToday.plusDays(30)
        require(searchDatetime.isBefore(theDayIn30Days)) {
            "只能查詢未來 30 天內的列車時刻表"
        }

        require(adultCount >= 0) {
            "大人人數，必須 >= 0"
        }
        require(childCount >= 0) {
            "小孩人數，必須 >= 0"
        }
        require(adultCount + childCount >= 1) {
            "至少需有一位乘客"
        }
        require(adultCount + childCount <= 7) {
            "合計最多 7 人"
        }
    }
}