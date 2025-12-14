package com.enix.kyushu

import java.time.Duration
import java.time.LocalDateTime

data class JourneyInfo(
    // 乘車時間，預設明細第一筆乘車時間
    val departureTime: LocalDateTime = transferDetailList.first().departureTime,
    // 到達時間，預設最後一筆到達時間
    val arrivalTime: LocalDateTime = transferDetailList.last().arrivalTime,
    // 總時間
    val duration: Duration = Duration.between(departureTime, arrivalTime),
    // 總距離
    val distance: Double,
    // 轉搭明細
    val transferDetailList: List<TransferDetail>,
    // 座位與價位
    val fareInfoList: List<FareInfo>
) {
    val transferCount: Int = transferDetailList.size - 1
    val isTransfer: Boolean = transferCount > 0

    init {
        require(arrivalTime.isAfter(departureTime)) {
            "arrivalTime must be after departureTime"
        }
        require(duration >= Duration.ZERO) {
            "duration must be positive"
        }

        val durationSumOfDetails = transferDetailList
            .takeIf { it.isNotEmpty() }
            ?.map { Duration.between(it.departureTime, it.arrivalTime) }
            ?.reduce { acc, duration -> acc.plus(duration) }
            ?: Duration.ZERO
        require(duration >= durationSumOfDetails) {
            "duration must greater than sum of transferDetailList"
        }

        require(distance >= 0.0) {
            "distance must be non-negative"
        }
        require(transferDetailList.isNotEmpty()) {
            "transferDetailList must not be empty"
        }
        require(transferDetailList.size <= 6) {
            "transferDetailList must not be greater than 6"
        }
        require(fareInfoList.isNotEmpty()) {
            "fareInfoList must not be empty"
        }
    }
}