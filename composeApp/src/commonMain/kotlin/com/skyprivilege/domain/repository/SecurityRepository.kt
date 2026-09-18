package com.skyprivilege.domain.repository

import com.skyprivilege.domain.model.LocationContext

interface SecurityRepository {
    suspend fun collectCurrentLocationContext(): LocationContext
    suspend fun signPayload(payload: ByteArray): ByteArray
    suspend fun attestDeviceIntegrity(nonce: String): Result<String>
}
