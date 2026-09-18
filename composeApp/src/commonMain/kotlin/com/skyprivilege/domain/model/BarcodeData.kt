package com.skyprivilege.domain.model

import kotlinx.serialization.Serializable

enum class BarcodeFormat {
    PDF417,
    AZTEC,
    QR_CODE,
    UNKNOWN
}

@Serializable
data class BarcodeData(
    val rawPayload: String,
    val format: BarcodeFormat,
    val timestampEpochMs: Long = 0L
)
