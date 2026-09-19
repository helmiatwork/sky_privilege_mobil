package com.skyprivilege.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AttendanceCorrectionDialog(
    initialDate: String = "2026-09-19",
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSubmitCorrection: (
        targetDate: String,
        correctionType: String,
        requestedCheckInAt: String?,
        requestedCheckOutAt: String?,
        reason: String
    ) -> Unit
) {
    var targetDate by remember { mutableStateOf(initialDate) }
    var correctionType by remember { mutableStateOf("check_in") }
    var requestedCheckInAt by remember { mutableStateOf("2026-09-19 07:00:00") }
    var requestedCheckOutAt by remember { mutableStateOf("2026-09-19 15:00:00") }
    var reason by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

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
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = "Pengajuan Koreksi Absensi",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ajukan koreksi waktu kehadiran kepada Supervisor / Operasional.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Alert info banner
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ℹ️ Pengajuan akan ditinjau oleh Manajer Outlet dan dicatat ke Audit Trail.",
                        fontSize = 11.sp,
                        color = Color(0xFF1E40AF),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Date Field
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Tanggal Kehadiran (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Correction Type Radio Buttons
                Text(
                    text = "Tipe Koreksi:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf(
                        "check_in" to "Jam Masuk",
                        "check_out" to "Jam Pulang",
                        "both" to "Keduanya"
                    )

                    types.forEach { (typeKey, label) ->
                        val isSelected = correctionType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFFEF3C7) else Color(0xFFF1F5F9))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFD97706) else Color(0xFFCBD5E1),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { correctionType = typeKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFB45309) else Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Requested Check-In Field
                if (correctionType == "check_in" || correctionType == "both") {
                    OutlinedTextField(
                        value = requestedCheckInAt,
                        onValueChange = { requestedCheckInAt = it },
                        label = { Text("Jam Masuk Sebenarnya (YYYY-MM-DD HH:MM:SS)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Requested Check-Out Field
                if (correctionType == "check_out" || correctionType == "both") {
                    OutlinedTextField(
                        value = requestedCheckOutAt,
                        onValueChange = { requestedCheckOutAt = it },
                        label = { Text("Jam Pulang Sebenarnya (YYYY-MM-DD HH:MM:SS)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Reason Field
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Alasan Pengajuan Koreksi *") },
                    placeholder = { Text("Contoh: Terkendala jaringan saat scan GPS check-in") },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

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
                            val inTime = if (correctionType == "check_in" || correctionType == "both") requestedCheckInAt else null
                            val outTime = if (correctionType == "check_out" || correctionType == "both") requestedCheckOutAt else null
                            onSubmitCorrection(targetDate, correctionType, inTime, outTime, reason)
                        },
                        modifier = Modifier.weight(1.5f),
                        enabled = !isSubmitting && reason.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Kirim Pengajuan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
