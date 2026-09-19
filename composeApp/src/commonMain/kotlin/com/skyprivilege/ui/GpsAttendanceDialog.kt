package com.skyprivilege.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    suggestedType: String = "check_in",
    latitude: Double = -6.1256,
    longitude: Double = 106.6558,
    accuracyMeters: Float = 15.0f,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSaveAttendance: (type: String, latitude: Double, longitude: Double, accuracy: Float) -> Unit
) {
    var selectedType by remember { mutableStateOf(suggestedType) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Employee Attendance",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Pencatatan Kehadiran GPS Geofence",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "GPS READY",
                            color = Color(0xFFD97706),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Map Visualizer Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // Grid lines
                        val step = 30.dp.toPx()
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
                            radius = 60.dp.toPx(),
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFFEA580C),
                            radius = 60.dp.toPx(),
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )

                        // Middle ripple
                        drawCircle(
                            color = Color(0x44F97316),
                            radius = 35.dp.toPx(),
                            center = center
                        )

                        // Center pin
                        drawCircle(
                            color = Color(0xFFEA580C),
                            radius = 12.dp.toPx(),
                            center = center
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
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
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Location accuracy: ${accuracyMeters.toInt()} meters",
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
                            .background(Color(0xCC0F172A))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "📍 CGK Terminal 3 Gate 13 ($latitude, $longitude)",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Employee & Time details card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = cashierName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "IP: $clientIp • Device: $deviceId",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Type Selector (Check In vs Check Out)
                Text(
                    text = "Pilih Aksi Absensi:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isCheckIn = selectedType == "check_in"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCheckIn) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                            .border(
                                width = if (isCheckIn) 2.dp else 1.dp,
                                color = if (isCheckIn) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedType = "check_in" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Masuk (Check In)",
                            fontWeight = if (isCheckIn) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCheckIn) Color(0xFF1D4ED8) else Color(0xFF475569),
                            fontSize = 12.sp
                        )
                    }

                    val isCheckOut = selectedType == "check_out"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCheckOut) Color(0xFFFFF7ED) else Color(0xFFF1F5F9))
                            .border(
                                width = if (isCheckOut) 2.dp else 1.dp,
                                color = if (isCheckOut) Color(0xFFEA580C) else Color(0xFFCBD5E1),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedType = "check_out" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pulang (Check Out)",
                            fontWeight = if (isCheckOut) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCheckOut) Color(0xFFC2410C) else Color(0xFF475569),
                            fontSize = 12.sp
                        )
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

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    ) {
                        Text("Batal", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onSaveAttendance(selectedType, latitude, longitude, accuracyMeters)
                        },
                        modifier = Modifier.weight(1.5f),
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text(
                                text = if (selectedType == "check_in") "Save Check In" else "Save Check Out",
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
