package com.skyprivilege.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.skyprivilege.domain.model.BarcodeData
import com.skyprivilege.domain.model.BarcodeFormat

class BarcodeAnalyzer(
    private val onBarcodeDetected: (BarcodeData) -> Unit,
    private val onError: (Exception) -> Unit
) : ImageAnalysis.Analyzer {

    // Optimize ML Kit scanner specifically for IATA PDF417 and Aztec / QR boarding pass standards
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_PDF417,
            Barcode.FORMAT_AZTEC,
            Barcode.FORMAT_QR_CODE
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue ?: continue
                        val format = when (barcode.format) {
                            Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF417
                            Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
                            Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
                            else -> BarcodeFormat.UNKNOWN
                        }
                        onBarcodeDetected(
                            BarcodeData(
                                rawPayload = rawValue,
                                format = format,
                                timestampEpochMs = System.currentTimeMillis()
                            )
                        )
                    }
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
