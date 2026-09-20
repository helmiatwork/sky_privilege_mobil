package com.skyprivilege.ui

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AttendanceCard(
    cashierName: String = "Kasir Terminal 3",
    cashierRole: String = "Staff Kasir & Operator POS",
    shiftInfo: String = "Shift Pagi (07:00 - 15:00)",
    todayText: String = "Sabtu, 19 Sep 2026",
    checkInTime: String? = null,
    checkOutTime: String? = null,
    status: String? = null,
    onRecordTimeClick: () -> Unit,
    onCorrectionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Profile Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF7ED))
                        .border(1.5.dp, Color(0xFFEA580C), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "KT",
                        color = Color(0xFFEA580C),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cashierName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = cashierRole,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Status Badge (Omit if belum absen)
                val badgeInfo = when (status) {
                    "completed" -> Triple(Color(0xFFDCFCE7), "SELESAI", Color(0xFF15803D))
                    "present" -> Triple(Color(0xFFDBEAFE), "HADIR", Color(0xFF1D4ED8))
                    "late" -> Triple(Color(0xFFFEF3C7), "TERLAMBAT", Color(0xFFB45309))
                    else -> null
                }

                if (badgeInfo != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeInfo.first)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeInfo.second,
                            color = badgeInfo.third,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-card: Shift & Today Information
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hari Ini • $todayText",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = shiftInfo,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0284C7)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2-Column Time Blocks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Start Time Column
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A))
                                    )
                                    Text(
                                        text = "Start Time",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = checkInTime ?: "--:--",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (checkInTime != null) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                )
                            }
                        }

                        // End Time Column
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEA580C))
                                    )
                                    Text(
                                        text = "End Time",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = checkOutTime ?: "--:--",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (checkOutTime != null) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val hasCheckOut = (!checkOutTime.isNullOrBlank() && checkOutTime != "--:--") || status == "completed"
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Record Time Button (Orange Primary or Slate when completed)
                Button(
                    onClick = onRecordTimeClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasCheckOut) Color(0xFF64748B) else Color(0xFFEA580C)
                    ),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text(
                        text = if (hasCheckOut) "📍 Shift Selesai" else "📍 Record Time",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Koreksi Absensi Button
                OutlinedButton(
                    onClick = onCorrectionClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Ajukan Koreksi",
                        fontSize = 11.sp,
                        color = Color(0xFF2563EB)
                    )
                }
            }
        }
    }
}
