package com.skyprivilege.ui

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Strictly in-memory decoder from Base64 string / data URI to ImageBitmap.
 * Zero-Disk Storage compliant (UU PDP No. 27/2022).
 */
expect fun decodeBase64ToBitmap(base64Data: String): ImageBitmap?
