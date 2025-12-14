package com.enix.kyushu

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.Test

class JourneyInfoTest {

    private val mockedJourneyData = MockedJourneyData()

    private val printF: String?.() -> Unit = {
        println(this)
    }

    @Test
    fun getCorrectTheResult() {
        assertDoesNotThrow {
            JourneyInfo(
                distance = 240.9,
                transferDetailList = mockedJourneyData.getTransferDetailList(),
                fareInfoList = mockedJourneyData.getFareInfo()
            )
        }
    }

    @Test
    fun transferDetailListIsRequired() {
        val exception = assertThrows<IllegalArgumentException> {
            JourneyInfo(
                departureTime = LocalDateTime.now(),
                arrivalTime = LocalDateTime.now().plusHours(1),
                distance = 240.9,
                transferDetailList = emptyList(),
                fareInfoList = mockedJourneyData.getFareInfo()
            )
        }
        print(exception.message)
        assert(exception.message == "transferDetailList must not be empty")
    }

    @Test
    fun fareInfoListIsRequired() {
        val exception = assertThrows<IllegalArgumentException> {
            JourneyInfo(
                distance = 240.9,
                transferDetailList = mockedJourneyData.getTransferDetailList(),
                fareInfoList = emptyList()
            )
        }
        assert(exception.message.apply(printF) == "fareInfoList must not be empty")
    }
}