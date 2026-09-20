package com.skyprivilege.ui

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun decodeBase64ToBitmap(base64Data: String): ImageBitmap? {
    if (base64Data.isBlank()) return null
    return try {
        val cleanBase64 = if (base64Data.contains(",")) {
            base64Data.substringAfter(",")
        } else {
            base64Data
        }.trim()

        val decodedBytes = try {
            android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
        } catch (t: Throwable) {
            java.util.Base64.getDecoder().decode(cleanBase64)
        }

        if (decodedBytes == null || decodedBytes.isEmpty()) return null
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
