package com.skyprivilege.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun GpsAttendanceDialog(
    cashierName: String = "Kasir Terminal 3 (CSH-001)",
    deviceId: String = "DEV-TABLET-001",
    clientIp: String = "192.168.1.45",
    currentDateText: String = "Sabtu, 19 September 2026",
    currentTimeText: String = "08:15:30 WIB",
    checkInTime: String? = null,
    checkOutTime: String? = null,
    latitude: Double = -6.1256,
    longitude: Double = 106.6558,
    accuracyMeters: Float = 15.0f,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSaveAttendance: (type: String, latitude: Double, longitude: Double, accuracy: Float) -> Unit
) {
    val hasCheckIn = !checkInTime.isNullOrBlank() && checkInTime != "--:--"
    val hasCheckOut = !checkOutTime.isNullOrBlank() && checkOutTime != "--:--"

    // Determine 1-button logic:
    // 1st press (empty): check_in
    // 2nd press (has check_in, empty check_out): check_out
    // 3rd press (both exist): check_out (replaces end time)
    val actionType = if (!hasCheckIn) "check_in" else "check_out"
    val actionTitle = when {
        !hasCheckIn -> "Catat Jam Masuk (Start Time)"
        !hasCheckOut -> "Catat Jam Pulang (End Time)"
        else -> "Perbarui Jam Pulang (Replace End Time)"
    }
    val actionDescription = when {
        !hasCheckIn -> "Mencatat jam mulai shift kerja hari ini."
        !hasCheckOut -> "Jam masuk: $checkInTime. Tekan untuk mencatat jam kepulangan."
        else -> "Jam pulang saat ini: $checkOutTime. Tekan untuk memperbarui dengan jam terbaru."
    }
    val buttonText = when {
        !hasCheckIn -> "Catat Jam Masuk"
        !hasCheckOut -> "Catat Jam Pulang"
        else -> "Perbarui Jam Pulang"
    }
    val badgeBg = when {
        !hasCheckIn -> Color(0xFFDCFCE7)
        !hasCheckOut -> Color(0xFFFFEDD5)
        else -> Color(0xFFFEF3C7)
    }
    val badgeColor = when {
        !hasCheckIn -> Color(0xFF15803D)
        !hasCheckOut -> Color(0xFFC2410C)
        else -> Color(0xFFB45309)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header (Clean, without redundant GPS READY badge)
                Column {
                    Text(
                        text = "Employee Attendance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pencatatan Kehadiran GPS Geofence Outlet",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Map Visualizer Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(145.dp)) {
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // Grid lines
                        val step = 28.dp.toPx()
                        var x = 0f
                        while (x < size.width) {
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1f
                            )
                            x += step
                        }
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f
                            )
                            y += step
                        }

                        // Geofence outer radius
                        drawCircle(
                            color = Color(0x33F97316),
                            radius = 55.dp.toPx(),
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFFEA580C),
                            radius = 55.dp.toPx(),
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )

                        // Middle ripple
                        drawCircle(
                            color = Color(0x44F97316),
                            radius = 32.dp.toPx(),
                            center = center
                        )

                        // Center pin
                        drawCircle(
                            color = Color(0xFFEA580C),
                            radius = 11.dp.toPx(),
                            center = center
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.5.dp.toPx(),
                            center = center
                        )
                    }

                    // Accuracy Badge at top right
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC059669))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Akurasi: ${accuracyMeters.toInt()} meter",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Coordinates at bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Color(0xD90F172A))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "📍 CGK Terminal 3 Gate 13 ($latitude, $longitude)",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Employee & Time details card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = cashierName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentTimeText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFEA580C)
                            )
                            Text(
                                text = currentDateText,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "IP: $clientIp • Device: $deviceId",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dynamic Action Indicator Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = badgeBg.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (!hasCheckIn) "🟢" else if (!hasCheckOut) "🟠" else "🔄",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = actionTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = actionDescription,
                                fontSize = 10.sp,
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

                // Bottom Action Buttons (Single Action Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onSaveAttendance(actionType, latitude, longitude, accuracyMeters)
                        },
                        modifier = Modifier.weight(1.8f),
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text(
                                text = buttonText,
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
