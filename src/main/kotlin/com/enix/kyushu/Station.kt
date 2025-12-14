package com.enix.kyushu

data class Station(
    val stationName: String,
    val stationCode: String
) {
    init {
        require(stationCode.matches(Regex("^[A-Za-z0-9]+$"))) {
            "stationCode must contain only English letters and digits."
        }
    }
}