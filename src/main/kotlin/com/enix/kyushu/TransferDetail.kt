package com.enix.kyushu

import java.time.LocalDateTime

data class TransferDetail(
    // 轉乘序次
    val sequence: Int,
    // 火車名稱
    val trainName: String,
    val departureStation: Station,
    val departureTime: LocalDateTime,
    val arrivalStation: Station,
    val arrivalTime: LocalDateTime
) {

    init {
        require(sequence >= 0) {
            "sequence must be non-negative"
        }
    }

    var nextDetail: TransferDetail? = null
        set(nextDetail) {
            require(nextDetail != this) {
                "nextDetail must be different from this"
            }
            require(nextDetail?.sequence == sequence + 1) {
                "nextDetail sequence must be ${sequence + 1}"
            }
            require(nextDetail.departureStation == arrivalStation) {
                "nextDetail departureStation must be the same as arrivalStation"
            }
            field = nextDetail
        }
}