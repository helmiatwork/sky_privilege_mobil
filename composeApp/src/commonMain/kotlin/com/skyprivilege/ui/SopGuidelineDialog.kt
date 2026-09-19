package com.skyprivilege.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class SopItem(
    val title: String,
    val description: String,
    val icon: String
)

@Composable
fun SopGuidelineDialog(
    onDismiss: () -> Unit
) {
    val sops = listOf(
        SopItem(
            title = "1. Verifikasi Barcode Tiket",
            description = "Pindai barcode/Aztec code pada boarding pass atau voucher digital. Pastikan nama penumpang dan tanggal penerbangan valid.",
            icon = "📷"
        ),
        SopItem(
            title = "2. Checklist Keaslian Fisik",
            description = "Wajib memeriksa watermark kertas tiket, cetakan hologram, dan stempel check-in maskapai sebelum memproses diskon.",
            icon = "📋"
        ),
        SopItem(
            title = "3. Absensi GPS Geofence",
            description = "Absensi masuk dan pulang wajib dilakukan di dalam radius outlet Sky Lounge CGK Terminal 3 dengan GPS akurat.",
            icon = "📍"
        ),
        SopItem(
            title = "4. Buka & Tutup Shift",
            description = "Buka shift kasir sebelum transaksi pertama dimulai. Hitung rekonsiliasi kas modal saat serah terima shift.",
            icon = "🔑"
        ),
        SopItem(
            title = "5. Voucher Darurat Offline",
            description = "Hanya gunakan Voucher Darurat saat jaringan bandara offline. Wajib input PNR dan minta otorisasi supervisor.",
            icon = "🎟️"
        ),
        SopItem(
            title = "6. Protokol Keamanan PIN",
            description = "Jangan pernah membagikan PIN kasir kepada siapapun. Ganti PIN secara berkala via menu Akun Saya.",
            icon = "🔒"
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "📖 Panduan SOP Kasir",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Standar Operasional Prosedur Operasional SkyPrivilege",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false).height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sops) { item ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Text(item.icon, fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                                Column {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(item.description, fontSize = 11.sp, color = Color(0xFF475569), lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005BAC))
                    ) {
                        Text("Mengerti", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
