package com.skyprivilege.domain.repository

import com.skyprivilege.domain.model.BarcodeData
import com.skyprivilege.domain.model.Ticket

interface TicketRepository {
    suspend fun verifyBarcode(barcodeData: BarcodeData, outletId: Long): Result<Ticket>
    suspend fun verifyTicket(
        barcodeData: String? = null,
        imageBase64: String? = null,
        outletId: Long,
        cashierId: Long? = null,
        deviceId: Long? = null,
        shiftId: Long? = null,
        checklistConfirmed: Boolean = true,
        latitude: Double? = null,
        longitude: Double? = null,
        accuracy: Float? = null,
        wifiBssid: String? = null,
        wifiSsid: String? = null
    ): Result<Ticket>
    suspend fun decodeOffline(rawBarcode: String): Result<Ticket>
}
