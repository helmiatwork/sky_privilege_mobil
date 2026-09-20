package com.skyprivilege.ui

import com.skyprivilege.domain.model.RedemptionHistoryItem
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TicketPhotoTest {

    @Test
    fun testDefaultBoardingPassPhotoDataUriIsValidDataUri() {
        val uri = TicketPhotoDefaults.DEFAULT_BOARDING_PASS_PHOTO_DATA_URI
        assertTrue(uri.startsWith("data:image/jpeg;base64,"), "URI must start with data:image/jpeg;base64,")
        assertTrue(uri.length > 5000, "In-memory photo data URI must contain realistic compressed image data")
    }

    @Test
    fun testRedemptionHistoryItemDeserializesTicketPhotoUrl() {
        val jsonString = """
            {
                "id": 123,
                "flight_number": "GA0410",
                "passenger_name": "SANTOSO/BUDI MR",
                "discount_amount": 25000.0,
                "status": "approved",
                "ticket_photo_url": "data:image/jpeg;base64,sample_photo_base64_stream"
            }
        """.trimIndent()

        val json = Json { ignoreUnknownKeys = true }
        val item = json.decodeFromString<RedemptionHistoryItem>(jsonString)

        assertEquals(123L, item.id)
        assertEquals("GA0410", item.flightNumber)
        assertEquals("SANTOSO/BUDI MR", item.passengerName)
        assertEquals(25000.0, item.discountAmount)
        assertNotNull(item.ticketPhotoUrl)
        assertEquals("data:image/jpeg;base64,sample_photo_base64_stream", item.ticketPhotoUrl)
    }

    @Test
    fun testRedemptionHistoryItemDefaultsTicketPhotoUrlToNull() {
        val jsonString = """
            {
                "id": 124,
                "flight_number": "GA0410",
                "passenger_name": "SANTOSO/BUDI MR"
            }
        """.trimIndent()

        val json = Json { ignoreUnknownKeys = true }
        val item = json.decodeFromString<RedemptionHistoryItem>(jsonString)

        assertEquals(null, item.ticketPhotoUrl)
    }
}
