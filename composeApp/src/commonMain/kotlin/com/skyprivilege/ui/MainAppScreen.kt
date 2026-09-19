package com.skyprivilege.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyprivilege.data.remote.KtorClientFactory
import com.skyprivilege.data.remote.dto.AttendanceRecordDto
import com.skyprivilege.data.remote.dto.AuthenticityAcknowledgmentDto
import com.skyprivilege.data.repository.AttendanceRepositoryImpl
import com.skyprivilege.data.repository.GuidelineRepositoryImpl
import com.skyprivilege.data.repository.RedemptionRepositoryImpl
import com.skyprivilege.data.repository.ShiftRepositoryImpl
import com.skyprivilege.data.repository.TicketRepositoryImpl
import com.skyprivilege.domain.model.BarcodeData
import com.skyprivilege.domain.model.LocationContext
import com.skyprivilege.domain.model.RedemptionClaim
import com.skyprivilege.domain.model.Ticket
import com.skyprivilege.domain.model.TicketGuideline
import kotlinx.coroutines.launch

@Composable
fun MainAppScreen(
    initialBaseUrl: String = "http://10.0.2.2:3001"
) {
    var baseUrl by remember { mutableStateOf(initialBaseUrl) }
    val outletId = 2L
    val cashierId = 2L
    val deviceId = "DEV-TABLET-001"

    val httpClient = remember(baseUrl) {
        KtorClientFactory.createHttpClient(
            baseUrl = baseUrl,
            deviceId = deviceId,
            outletId = outletId
        )
    }

    val guidelineRepo = remember(httpClient) { GuidelineRepositoryImpl(httpClient) }
    val shiftRepo = remember(httpClient) { ShiftRepositoryImpl(httpClient) }
    val ticketRepo = remember(httpClient) { TicketRepositoryImpl(httpClient) }
    val redemptionRepo = remember(httpClient) { RedemptionRepositoryImpl(httpClient) }
    val attendanceRepo = remember(httpClient) { AttendanceRepositoryImpl(httpClient) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State
    var activeShiftId by remember { mutableStateOf<Long?>(null) }
    var shiftStatusText by remember { mutableStateOf("Belum ada shift terbuka") }
    var isShiftLoading by remember { mutableStateOf(false) }

    var todayAttendance by remember { mutableStateOf<AttendanceRecordDto?>(null) }
    var hasCheckedIn by remember { mutableStateOf(false) }
    var hasCheckedOut by remember { mutableStateOf(false) }
    var showGpsAttendanceDialog by remember { mutableStateOf(false) }
    var isAttendanceSubmitting by remember { mutableStateOf(false) }
    var attendanceDialogError by remember { mutableStateOf<String?>(null) }
    var showCorrectionDialog by remember { mutableStateOf(false) }
    var isCorrectionSubmitting by remember { mutableStateOf(false) }
    var correctionDialogError by remember { mutableStateOf<String?>(null) }
    var attendanceSuccessToast by remember { mutableStateOf<String?>(null) }

    var showChecklistDialog by remember { mutableStateOf(false) }
    var guidelines by remember { mutableStateOf<List<TicketGuideline>>(emptyList()) }
    var isGuidelinesLoading by remember { mutableStateOf(false) }
    var isChecklistSubmitting by remember { mutableStateOf(false) }
    var checklistConfirmed by remember { mutableStateOf(false) }

    var barcodeInput by remember { mutableStateOf("") }
    var isVerifyingTicket by remember { mutableStateOf(false) }
    var verifiedTicket by remember { mutableStateOf<Ticket?>(null) }
    var claimToken by remember { mutableStateOf<String?>(null) }

    var isClaimingDiscount by remember { mutableStateOf(false) }
    var redemptionSuccessMsg by remember { mutableStateOf<String?>(null) }
    var globalError by remember { mutableStateOf<String?>(null) }

    // Load initial shift and attendance status
    LaunchedEffect(httpClient) {
        shiftRepo.getCurrentShift(cashierId, outletId).onSuccess { res ->
            if (res.success && res.shift != null) {
                activeShiftId = res.shift.id
                shiftStatusText = "Shift #${res.shift.id} Aktif (Total Klaim: ${res.shift.totalRedemptionsCount})"
            }
        }
        attendanceRepo.getTodayAttendance(cashierId, outletId).onSuccess { res ->
            if (res.success) {
                todayAttendance = res.attendance
                hasCheckedIn = res.hasCheckedIn
                hasCheckedOut = res.hasCheckedOut
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SkyPrivilege Scanner POS",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sky Lounge T3 (CGK-T3) • $deviceId",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeShiftId != null) Color(0xFF16A34A) else Color(0xFFE11D48))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (activeShiftId != null) "SHIFT AKTIF" else "OFFLINE SHIFT",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "API Host: $baseUrl",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Error Message Toast
            if (globalError != null) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ " + globalError!!,
                            color = Color(0xFFBE123C),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = { globalError = null },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Tutup", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Attendance Success Toast
            if (attendanceSuccessToast != null) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅ " + attendanceSuccessToast!!,
                            color = Color(0xFF15803D),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = { attendanceSuccessToast = null },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Tutup", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Attendance Card (Matching Reference Image 1)
            AttendanceCard(
                cashierName = "Kasir Terminal 3",
                cashierRole = "Staff Kasir & Operator POS",
                shiftInfo = if (activeShiftId != null) "Shift #${activeShiftId} Aktif" else "Shift Pagi (07:00 - 15:00)",
                todayText = "Sabtu, 19 Sep 2026",
                checkInTime = todayAttendance?.checkInAt?.let { if (it.length >= 16) it.substring(11, 16) else it },
                checkOutTime = todayAttendance?.checkOutAt?.let { if (it.length >= 16) it.substring(11, 16) else it },
                status = todayAttendance?.status,
                onRecordTimeClick = {
                    attendanceDialogError = null
                    showGpsAttendanceDialog = true
                },
                onCorrectionClick = {
                    correctionDialogError = null
                    showCorrectionDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Section 1: Shift Kasir
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Manajemen Shift Kasir",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kasir: Kasir Terminal 3 (CSH-001) | Status: $shiftStatusText",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (activeShiftId == null) {
                            Button(
                                onClick = {
                                    isShiftLoading = true
                                    globalError = null
                                    coroutineScope.launch {
                                        shiftRepo.openShift(
                                            cashierId = cashierId,
                                            outletId = outletId,
                                            deviceId = 1L,
                                            openingCash = 0.0,
                                            openingSelfieKey = null
                                        ).onSuccess { shift ->
                                            activeShiftId = shift.id
                                            shiftStatusText = "Shift #${shift.id} Aktif"
                                            isShiftLoading = false
                                            if (!hasCheckedIn) {
                                                showGpsAttendanceDialog = true
                                            }
                                        }.onFailure { err ->
                                            globalError = "Buka shift gagal: ${err.message}"
                                            isShiftLoading = false
                                        }
                                    }
                                },
                                enabled = !isShiftLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                if (isShiftLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                } else {
                                    Text("Buka Shift Kasir", fontSize = 12.sp)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    isShiftLoading = true
                                    globalError = null
                                    coroutineScope.launch {
                                        shiftRepo.closeShift(
                                            cashierId = cashierId,
                                            outletId = outletId,
                                            closingCash = 0.0,
                                            shiftId = activeShiftId
                                        ).onSuccess {
                                            activeShiftId = null
                                            shiftStatusText = "Shift telah ditutup"
                                            isShiftLoading = false
                                        }.onFailure { err ->
                                            globalError = "Tutup shift gagal: ${err.message}"
                                            isShiftLoading = false
                                        }
                                    }
                                },
                                enabled = !isShiftLoading
                            ) {
                                Text("Tutup Shift (Z-Report)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 2: Authenticity Checklist Gating
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Verifikasi Keaslian Fisik",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (checklistConfirmed) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (checklistConfirmed) "TERKONFIRMASI" else "WAJIB CHECKLIST",
                                color = if (checklistConfirmed) Color(0xFF15803D) else Color(0xFFB45309),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Wajib memeriksa ciri fisik tiket/e-ticket sebelum membuka pemindai barcode.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            isGuidelinesLoading = true
                            globalError = null
                            coroutineScope.launch {
                                guidelineRepo.getGuidelines().onSuccess { list ->
                                    guidelines = list
                                    isGuidelinesLoading = false
                                    showChecklistDialog = true
                                }.onFailure { err ->
                                    globalError = "Gagal memuat panduan: ${err.message}"
                                    isGuidelinesLoading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        if (isGuidelinesLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text(if (checklistConfirmed) "Tinjau Ulang Checklist" else "Buka Checklist Keaslian Tiket", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 3: Barcode Verification
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Input Barcode Boarding Pass (IATA BCBP)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Sample Button
                    OutlinedButton(
                        onClick = {
                            // Sample Garuda BCBP for current Julian date 262
                            barcodeInput = "M1SANTOSO/BUDI MR     EABC1234CGKDPSGA 00410262Y012A00042100"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Isi Sampel Boarding Pass Garuda GA410 (Hari Ini)", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = { barcodeInput = it },
                        label = { Text("Raw Barcode String") },
                        placeholder = { Text("M1SANTOSO/BUDI MR...") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (!checklistConfirmed) {
                                globalError = "Wajib konfirmasi checklist keaslian fisik terlebih dahulu!"
                                return@Button
                            }
                            if (activeShiftId == null) {
                                globalError = "Wajib buka shift kasir terlebih dahulu!"
                                return@Button
                            }
                            isVerifyingTicket = true
                            globalError = null
                            verifiedTicket = null
                            redemptionSuccessMsg = null

                            coroutineScope.launch {
                                ticketRepo.verifyBarcode(
                                    BarcodeData(
                                        rawPayload = barcodeInput.trim(),
                                        format = com.skyprivilege.domain.model.BarcodeFormat.AZTEC
                                    ),
                                    outletId = outletId
                                ).onSuccess { ticket ->
                                    verifiedTicket = ticket
                                    isVerifyingTicket = false
                                }.onFailure { err ->
                                    globalError = "Verifikasi tiket gagal: ${err.message}"
                                    isVerifyingTicket = false
                                }
                            }
                        },
                        enabled = !isVerifyingTicket && barcodeInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isVerifyingTicket) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Verifikasi Tiket ke Anti-Fraud Engine", fontSize = 12.sp)
                        }
                    }

                    // Ticket Verification Result Display
                    if (verifiedTicket != null) {
                        val t = verifiedTicket!!
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "✅ Boarding Pass Sah & Terverifikasi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF166534)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Penumpang: ${t.passengerName}", fontSize = 12.sp, color = Color(0xFF14532D))
                                Text(text = "PNR: ${t.pnr} | Penerbangan: ${t.flightNumber} (${t.fromAirport} -> ${t.toAirport})", fontSize = 12.sp, color = Color(0xFF14532D))
                                Text(text = "Tanggal: ${t.flightDate} | Kursi: ${t.seatNumber} | Kelas: ${t.compartmentCode}", fontSize = 12.sp, color = Color(0xFF14532D))
                                Text(
                                    text = "Hash: ${t.canonicalHash?.take(16)}...",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Claim Button
                        Button(
                            onClick = {
                                isClaimingDiscount = true
                                globalError = null
                                coroutineScope.launch {
                                    val orderId = "ORD-" + (System.currentTimeMillis() % 100000)
                                    val amountCents = 2_500_000L // Rp 25.000

                                    // 1. Request claim token with location telemetry
                                    redemptionRepo.requestClaimToken(
                                        orderId = orderId,
                                        pnrHash = t.canonicalHash.orEmpty(),
                                        outletId = outletId,
                                        cashierId = cashierId,
                                        amountCents = amountCents,
                                        signals = LocationContext(
                                            gps = com.skyprivilege.domain.model.GpsCoordinate(
                                                latitude = -6.1256,
                                                longitude = 106.6558,
                                                accuracyMeters = 15f,
                                                isMock = false
                                            ),
                                            wifi = com.skyprivilege.domain.model.WifiContext(
                                                bssid = "aa:bb:cc:dd:ee:ff",
                                                ssid = "SkyPrivilege_Staff",
                                                rssiDbm = -60
                                            ),
                                            deviceIntegrity = com.skyprivilege.domain.model.DeviceIntegrityContext(
                                                deviceRecognition = "MEETS_BASIC_INTEGRITY",
                                                isRooted = false,
                                                isEmulator = false
                                            )
                                        )
                                    ).onSuccess { token ->
                                        claimToken = token

                                        // 2. Submit redemption
                                        redemptionRepo.submitRedemption(
                                            RedemptionClaim(
                                                ticket = t,
                                                orderId = orderId,
                                                outletId = outletId,
                                                cashierId = cashierId,
                                                amountCents = amountCents,
                                                claimToken = token,
                                                shiftId = activeShiftId
                                            )
                                        ).onSuccess { redId ->
                                            redemptionSuccessMsg = "Klaim Berhasil! ID Transaksi: #$redId (Diskon Rp 25.000 diinjeksi ke Moka POS Order: $orderId)"
                                            isClaimingDiscount = false
                                        }.onFailure { claimErr ->
                                            globalError = "Klaim gagal: ${claimErr.message}"
                                            isClaimingDiscount = false
                                        }
                                    }.onFailure { tokenErr ->
                                        globalError = "Otorisasi token gagal: ${tokenErr.message}"
                                        isClaimingDiscount = false
                                    }
                                }
                            },
                            enabled = !isClaimingDiscount,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isClaimingDiscount) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Text("4. Terapkan Diskon ke Moka POS (Rp 25.000)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Success Message Display
                    if (redemptionSuccessMsg != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🎉 TRANSAKSI SELESAI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF15803D)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = redemptionSuccessMsg!!,
                                    fontSize = 12.sp,
                                    color = Color(0xFF14532D)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Instruksi: Wajib berikan stempel fisik 'CLAIMED - SKYPRIVILEGE' pada boarding pass penumpang sebelum mengembalikan.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Authenticity Checklist Dialog
        if (showChecklistDialog) {
            AuthenticityChecklistDialog(
                guidelines = guidelines,
                isLoading = isGuidelinesLoading,
                isSubmitting = isChecklistSubmitting,
                onDismiss = { showChecklistDialog = false },
                onConfirm = {
                    isChecklistSubmitting = true
                    coroutineScope.launch {
                        guidelineRepo.submitAcknowledgment(
                            AuthenticityAcknowledgmentDto(
                                cashierId = cashierId,
                                outletId = outletId,
                                deviceId = 1L,
                                wifiBssid = "aa:bb:cc:dd:ee:ff",
                                wifiSsid = "SkyPrivilege_Staff",
                                wifiRssi = -60,
                                gpsLatitude = -6.1256,
                                gpsLongitude = 106.6558,
                                gpsAccuracy = 12.5f
                            )
                        ).onSuccess {
                            isChecklistSubmitting = false
                            checklistConfirmed = true
                            showChecklistDialog = false
                        }.onFailure { err ->
                            isChecklistSubmitting = false
                            globalError = "Gagal merekam audit checklist: ${err.message}"
                            showChecklistDialog = false
                        }
                    }
                }
            )
        }

        // GPS Attendance Modal Dialog (Matching Reference Image 0)
        if (showGpsAttendanceDialog) {
            GpsAttendanceDialog(
                cashierName = "Kasir Terminal 3 (CSH-001)",
                deviceId = deviceId,
                clientIp = "192.168.1.45",
                currentDateText = "Sabtu, 19 September 2026",
                currentTimeText = "08:15:30 WIB",
                suggestedType = if (hasCheckedIn && !hasCheckedOut) "check_out" else "check_in",
                latitude = -6.1256,
                longitude = 106.6558,
                accuracyMeters = 15.0f,
                isSubmitting = isAttendanceSubmitting,
                errorMessage = attendanceDialogError,
                onDismiss = { showGpsAttendanceDialog = false },
                onSaveAttendance = { type, lat, lng, acc ->
                    isAttendanceSubmitting = true
                    attendanceDialogError = null
                    coroutineScope.launch {
                        attendanceRepo.recordAttendance(
                            cashierId = cashierId,
                            outletId = outletId,
                            type = type,
                            latitude = lat,
                            longitude = lng,
                            accuracy = acc
                        ).onSuccess { res ->
                            isAttendanceSubmitting = false
                            showGpsAttendanceDialog = false
                            todayAttendance = res.attendance
                            if (res.action == "check_in") hasCheckedIn = true
                            if (res.action == "check_out") hasCheckedOut = true
                            attendanceSuccessToast = res.message ?: "Absensi GPS berhasil dicatat"
                        }.onFailure { err ->
                            isAttendanceSubmitting = false
                            attendanceDialogError = err.message ?: "Gagal mencatat absensi GPS"
                        }
                    }
                }
            )
        }

        // Attendance Correction Request Modal Dialog
        if (showCorrectionDialog) {
            AttendanceCorrectionDialog(
                initialDate = "2026-09-19",
                isSubmitting = isCorrectionSubmitting,
                errorMessage = correctionDialogError,
                onDismiss = { showCorrectionDialog = false },
                onSubmitCorrection = { targetDate, corrType, inTime, outTime, rsn ->
                    isCorrectionSubmitting = true
                    correctionDialogError = null
                    coroutineScope.launch {
                        attendanceRepo.submitCorrectionRequest(
                            cashierId = cashierId,
                            outletId = outletId,
                            targetDate = targetDate,
                            correctionType = corrType,
                            requestedCheckInAt = inTime,
                            requestedCheckOutAt = outTime,
                            reason = rsn
                        ).onSuccess { res ->
                            isCorrectionSubmitting = false
                            showCorrectionDialog = false
                            attendanceSuccessToast = res.message ?: "Pengajuan koreksi absensi berhasil dikirim"
                        }.onFailure { err ->
                            isCorrectionSubmitting = false
                            correctionDialogError = err.message ?: "Gagal mengirim pengajuan koreksi"
                        }
                    }
                }
            )
        }
    }
}
