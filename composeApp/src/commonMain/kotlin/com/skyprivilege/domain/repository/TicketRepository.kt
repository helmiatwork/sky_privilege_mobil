package com.skyprivilege.domain.repository

import com.skyprivilege.domain.model.BarcodeData
import com.skyprivilege.domain.model.Ticket

interface TicketRepository {
    suspend fun verifyBarcode(barcodeData: BarcodeData, outletId: Long): Result<Ticket>
    suspend fun decodeOffline(rawBarcode: String): Result<Ticket>
}
