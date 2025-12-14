package com.enix.kyushu

interface JRServer {

    /**
     * Check if the server is under maintenance.
     */
    fun isMaintenance(): Boolean

    /**
     * Get the list of stations that match the given keyword.
     *
     * @param stationNameKeyword The keyword to search for.
     */
    fun findStationName(
        stationNameKeyword: String
    ): List<Station>

    /**
     * Search Station
     */
    fun searchStation(
        query: SearchQuery
    ): List<JourneyInfo>

    /**
     * Get the list of Japan holidays.
     */
    fun getJapanHolidayList(): List<JapanHoliday>

    fun verifyStationNameKeyword(
        stationNameKeyword: String
    ) {
        val JAPANESE_KANJI_REGEX = Regex("^[\\p{InCJK_Unified_Ideographs}々〆ヵヶ]+$")
        require(JAPANESE_KANJI_REGEX.matches(stationNameKeyword)) {
            "stationNameKeyword must contain only Japanese kanji characters"
        }
    }

}
