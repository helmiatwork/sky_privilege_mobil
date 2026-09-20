package com.skyprivilege.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class SelectedTicketData(
    val barcodeData: String? = null,
    val imageBase64: String? = null,
    val previewLabel: String,
    val passengerName: String,
    val flightNumber: String,
    val flightDate: String,
    val isSimulationInvalid: Boolean = false
)

@Composable
fun TicketCameraDialog(
    cashierName: String = "Kasir Terminal 3 (CSH-001)",
    outletName: String = "Sky Lounge Terminal 3 CGK",
    isVerifying: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSubmitTicket: (barcodeData: String?, imageBase64: String?) -> Unit
) {
    // Current date calculations for sample barcodes
    val todayDate = "2026-09-20"
    // Julian day for 20 Sep is approx 263
    val todayJulian = "263"
    val validSampleBarcode = "M1SANTOSO/BUDI MR     EABC1234CGKDPSGA 00410${todayJulian}Y012A00042100"
    val expiredSampleBarcode = "M1DOE/JOHN MR         EABC1234CGKDPSGA 00402255Y012A00042100" // 5 days earlier
    val redeemedSampleBarcode = "M1TEST/ANDHIKA         EABC1234CGKDPSGA 0410${todayJulian}Y012A00042100"

    var selectedTicket by remember {
        mutableStateOf<SelectedTicketData?>(
            SelectedTicketData(
                barcodeData = validSampleBarcode,
                previewLabel = "Boarding Pass Fisik GA410 (Terdeteksi)",
                passengerName = "SANTOSO/BUDI MR",
                flightNumber = "GA410 (CGK -> DPS)",
                flightDate = todayDate
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition()
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Dialog(
        onDismissRequest = { if (!isVerifying) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Foto & Pindai Tiket",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tahap 2: Ambil foto boarding pass penumpang",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "✅ Checklist OK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Viewfinder Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Grid Lines
                        val step = 24.dp.toPx()
                        var x = 0f
                        while (x < w) {
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(x, 0f),
                                end = Offset(x, h),
                                strokeWidth = 1f
                            )
                            x += step
                        }
                        var y = 0f
                        while (y < h) {
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1f
                            )
                            y += step
                        }

                        // Viewfinder Reticle Frame
                        val cornerLen = 22.dp.toPx()
                        val inset = 16.dp.toPx()
                        val strokeW = 3.dp.toPx()
                        val reticleColor = Color(0xFF38BDF8)

                        // Top-Left
                        drawLine(reticleColor, Offset(inset, inset), Offset(inset + cornerLen, inset), strokeW)
                        drawLine(reticleColor, Offset(inset, inset), Offset(inset, inset + cornerLen), strokeW)
                        // Top-Right
                        drawLine(reticleColor, Offset(w - inset, inset), Offset(w - inset - cornerLen, inset), strokeW)
                        drawLine(reticleColor, Offset(w - inset, inset), Offset(w - inset, inset + cornerLen), strokeW)
                        // Bottom-Left
                        drawLine(reticleColor, Offset(inset, h - inset), Offset(inset + cornerLen, h - inset), strokeW)
                        drawLine(reticleColor, Offset(inset, h - inset), Offset(inset, h - inset - cornerLen), strokeW)
                        // Bottom-Right
                        drawLine(reticleColor, Offset(w - inset, h - inset), Offset(w - inset - cornerLen, h - inset), strokeW)
                        drawLine(reticleColor, Offset(w - inset, h - inset), Offset(w - inset, h - inset - cornerLen), strokeW)

                        // Animated Laser Scan Line
                        val laserY = h * scanLineProgress
                        drawLine(
                            color = Color(0xFF00B14F),
                            start = Offset(inset + 4.dp.toPx(), laserY),
                            end = Offset(w - inset - 4.dp.toPx(), laserY),
                            strokeWidth = 2.5f
                        )
                    }

                    // Top Status Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC0F172A))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Arahkan kamera tepat pada tiket fisik atau barcode",
                            color = Color(0xFFE2E8F0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Bottom Telemetry Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Color(0xD90F172A))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "📍 GPS Geofence: Aktif (±15m) • 📶 Wi-Fi: SkyPrivilege_Staff",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ticket Selection / Quick Simulation Options for Testing
                Text(
                    text = "Pilih / Ambil Foto Tiket:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Button(
                        onClick = {
                            selectedTicket = SelectedTicketData(
                                barcodeData = validSampleBarcode,
                                previewLabel = "Boarding Pass Fisik Garuda GA410 (Sah)",
                                passengerName = "SANTOSO/BUDI MR",
                                flightNumber = "GA410 (CGK -> DPS)",
                                flightDate = todayDate
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTicket?.barcodeData == validSampleBarcode)
                                Color(0xFF005BAC) else Color(0xFFF1F5F9)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚡ Valid",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTicket?.barcodeData == validSampleBarcode)
                                Color.White else Color(0xFF334155)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            selectedTicket = SelectedTicketData(
                                barcodeData = expiredSampleBarcode,
                                previewLabel = "Tiket Kedaluwarsa 5 Hari Lalu (Uji Tolak)",
                                passengerName = "DOE/JOHN MR",
                                flightNumber = "GA402 (CGK -> DPS)",
                                flightDate = "2026-09-15",
                                isSimulationInvalid = true
                            )
                        },
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedTicket?.barcodeData == expiredSampleBarcode)
                                Color(0xFFFEE2E2) else Color.Transparent
                        )
                    ) {
                        Text(
                            text = "⚠️ Kedaluwarsa",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            selectedTicket = SelectedTicketData(
                                barcodeData = redeemedSampleBarcode,
                                previewLabel = "Tiket Pernah Diklaim (Uji Double Claim)",
                                passengerName = "TEST/ANDHIKA",
                                flightNumber = "SGA410 (CGK -> DPS)",
                                flightDate = todayDate,
                                isSimulationInvalid = true
                            )
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedTicket?.barcodeData == redeemedSampleBarcode)
                                Color(0xFFFEF3C7) else Color.Transparent
                        )
                    ) {
                        Text(
                            text = "♻️ Pernah Dipakai",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selected Ticket Preview Card
                if (selectedTicket != null) {
                    val ticket = selectedTicket!!
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (ticket.isSimulationInvalid) Color(0xFFFFF1F2) else Color(0xFFF8FAFC)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (ticket.isSimulationInvalid) Color(0xFFFECDD3) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ticket.previewLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ticket.isSimulationInvalid) Color(0xFFBE123C) else Color(0xFF0F172A)
                                )
                                Text(
                                    text = ticket.flightDate,
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Penumpang: ${ticket.passengerName}",
                                fontSize = 11.sp,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Penerbangan: ${ticket.flightNumber}",
                                fontSize = 11.sp,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ $errorMessage",
                        color = Color(0xFFBE123C),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isVerifying,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (selectedTicket != null) {
                                onSubmitTicket(selectedTicket!!.barcodeData, selectedTicket!!.imageBase64)
                            }
                        },
                        modifier = Modifier.weight(1.8f),
                        enabled = !isVerifying && selectedTicket != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF005BAC)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Memverifikasi...", fontSize = 12.sp)
                        } else {
                            Text(
                                text = "Kirim ke Backend",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
