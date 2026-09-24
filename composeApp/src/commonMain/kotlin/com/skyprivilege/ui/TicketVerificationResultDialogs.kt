package com.skyprivilege.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.skyprivilege.domain.model.BaggagePricingCalculator
import com.skyprivilege.domain.model.BaggageSize
import com.skyprivilege.domain.model.Ticket
import com.skyprivilege.domain.model.WrapType

@Composable
fun TicketInvalidWarningDialog(
    errorMessage: String,
    redeemedAt: String? = null,
    redeemedOutlet: String? = null,
    redeemedCashier: String? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Icon Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE4E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚠️", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tiket Dinyatakan Tidak Valid",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Pemeriksaan Anti-Fraud Engine Backend:",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val hasRedemptionHistory = !redeemedAt.isNullOrBlank() || !redeemedOutlet.isNullOrBlank() || !redeemedCashier.isNullOrBlank()
                if (hasRedemptionHistory) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("❌", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tiket Sudah Pernah Digunakan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Klaim ditolak karena tiket telah terdaftar di database:",
                                fontSize = 11.sp,
                                color = Color(0xFF7F1D1D)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            if (!redeemedAt.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🕒 Waktu", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7F1D1D), modifier = Modifier.width(85.dp))
                                    Text(": $redeemedAt", fontSize = 11.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                            }
                            if (!redeemedOutlet.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📍 Lokasi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7F1D1D), modifier = Modifier.width(85.dp))
                                    Text(": $redeemedOutlet", fontSize = 11.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                            }
                            if (!redeemedCashier.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("👤 Petugas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7F1D1D), modifier = Modifier.width(85.dp))
                                    Text(": $redeemedCashier", fontSize = 11.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                } else {
                    // Error Details Card for generic/format/date errors
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "❌ Alasan Penolakan:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBE123C)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage,
                                fontSize = 12.sp,
                                color = Color(0xFF881337),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recommendation text
                Text(
                    text = "Diskon ditolak demi kepatuhan SOP Anti-Fraud. Mohon periksa kembali fisik tiket penumpang.",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Tutup",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TicketValidResultDialog(
    ticket: Ticket,
    discountFormatted: String = "Rp 25.000",
    discountAmountCents: Long = 2_500_000L,
    isClaiming: Boolean = false,
    redemptionSuccessMsg: String? = null,
    initialBagSize: String = "M",
    initialWrapType: String = "standard",
    onDismiss: () -> Unit,
    onApplyToMoka: (
        orderId: String,
        bagSize: String,
        wrapType: String,
        grossAmountCents: Long,
        amountCents: Long,
        netAmountCents: Long
    ) -> Unit
) {
    var selectedBagSize by remember { mutableStateOf(BaggageSize.fromCode(initialBagSize)) }
    var selectedWrapType by remember { mutableStateOf(WrapType.fromCode(initialWrapType)) }
    val orderId = remember(ticket.canonicalHash) { "ORD-" + (System.currentTimeMillis() % 100000) }

    val grossCents = BaggagePricingCalculator.calculateGrossCents(selectedBagSize, selectedWrapType)
    val grossRupiah = BaggagePricingCalculator.calculateGrossRupiah(selectedBagSize, selectedWrapType)
    val discountRupiah = discountAmountCents / 100
    val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, discountAmountCents)
    val netRupiah = BaggagePricingCalculator.calculateNetRupiah(grossRupiah, discountRupiah)

    Dialog(
        onDismissRequest = { if (!isClaiming) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✅", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Tiket Sah & Valid",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = "Lolos verifikasi Anti-Fraud Engine",
                                fontSize = 10.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF0FDF4)
                    ) {
                        Text(
                            text = "VERIFIED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ticket Details Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = ticket.passengerName.ifBlank { "Penumpang Terverifikasi" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "PNR: ${ticket.pnr}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Penerbangan: ${ticket.flightNumber}",
                                fontSize = 11.sp,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "${ticket.fromAirport} ➔ ${ticket.toAirport}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tanggal: ${ticket.flightDate}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Kursi: ${ticket.seatNumber} • Kelas: ${ticket.compartmentCode}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Ukuran Koper
                Text(
                    text = "PILIH UKURAN KOPER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BaggageSize.entries.forEach { size ->
                        val isSelected = selectedBagSize == size
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF005BAC) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !isClaiming && redemptionSuccessMsg == null) {
                                    selectedBagSize = size
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = size.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF005BAC) else Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Rp ${formatRupiah(size.priceRupiah)}",
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF0284C7) else Color(0xFF64748B),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: Tipe Wrap
                Text(
                    text = "TIPE WRAPPING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WrapType.entries.forEach { type ->
                        val isSelected = selectedWrapType == type
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF005BAC) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !isClaiming && redemptionSuccessMsg == null) {
                                    selectedWrapType = type
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = type.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF005BAC) else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = if (type.extraPriceRupiah > 0) "+Rp ${formatRupiah(type.extraPriceRupiah)}" else "+Rp 0",
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF64748B)
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        text = "✓",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF005BAC)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 3: Ringkasan Harga Interaktif (Sesuai Slide 21 Pitch Deck)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MOKA POS · Order Baggage Wrapping",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "${selectedBagSize.code} · ${selectedWrapType.label}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Order Baggage Wrapping (Gross)",
                                fontSize = 11.sp,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Rp ${formatRupiah(grossRupiah)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Promo discount SkyPrivilege",
                                fontSize = 11.sp,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = "-Rp ${formatRupiah(discountRupiah)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Color(0xFFDCFCE7), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total bayar MOKA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Rp ${formatRupiah(netRupiah)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF005BAC)
                            )
                        }
                    }
                }

                // If Already Claimed into Moka POS:
                if (redemptionSuccessMsg != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🎉 Diskon Berhasil Diinjeksi ke Moka POS!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = redemptionSuccessMsg,
                                fontSize = 11.sp,
                                color = Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "⚠️ WAJIB STEMPEL BASAH: Berikan stempel fisik 'CLAIMED - SKYPRIVILEGE' pada tiket penumpang!",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Buttons
                if (redemptionSuccessMsg == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = !isClaiming,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Batal", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onApplyToMoka(
                                    orderId,
                                    selectedBagSize.code,
                                    selectedWrapType.code,
                                    grossCents,
                                    discountAmountCents,
                                    netCents
                                )
                            },
                            modifier = Modifier.weight(1.8f),
                            enabled = !isClaiming,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isClaiming) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Menginjeksi...", fontSize = 12.sp)
                            } else {
                                Text(
                                    text = "Inject ke MOKA POS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005BAC)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Selesai & Tutup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TicketValidResultDialog(
    ticket: Ticket,
    discountFormatted: String = "Rp 25.000",
    isClaiming: Boolean = false,
    redemptionSuccessMsg: String? = null,
    onDismiss: () -> Unit,
    onApplyToMoka: () -> Unit
) {
    TicketValidResultDialog(
        ticket = ticket,
        discountFormatted = discountFormatted,
        discountAmountCents = 2_500_000L,
        isClaiming = isClaiming,
        redemptionSuccessMsg = redemptionSuccessMsg,
        onDismiss = onDismiss,
        onApplyToMoka = { _, _, _, _, _, _ -> onApplyToMoka() }
    )
}
