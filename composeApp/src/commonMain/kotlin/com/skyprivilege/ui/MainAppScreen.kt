package com.skyprivilege.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextAlign
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
import com.skyprivilege.domain.model.AttendanceHistoryItem
import com.skyprivilege.domain.model.BarcodeData
import com.skyprivilege.domain.model.LocationContext
import com.skyprivilege.domain.model.RedemptionClaim
import com.skyprivilege.domain.model.RedemptionHistoryItem
import com.skyprivilege.domain.model.Ticket
import com.skyprivilege.domain.model.TicketGuideline
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val iconText: String) {
    HOME("Home", "🏠"),
    HISTORY("History", "📜"),
    SCAN("Scan", "📷"),
    ABSEN("Absen", "⏱️")
}

data class LocalCorrectionItem(
    val id: Long,
    val targetDate: String,
    val correctionType: String,
    val requestedTime: String,
    val reason: String,
    val status: String
)

// Grab Style Palette
val GrabGreen = Color(0xFF00B14F)
val GrabGreenDark = Color(0xFF00873D)
val GrabGreenLight = Color(0xFFE8F8F0)
val SlateDark = Color(0xFF0F172A)
val SlateCard = Color(0xFF1E293B)
val SlateSubtle = Color(0xFF64748B)
val BgLight = Color(0xFFF8FAFC)

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

    // Navigation state
    var currentTab by remember { mutableStateOf(AppTab.HOME) }

    // Shift state
    var activeShiftId by remember { mutableStateOf<Long?>(null) }
    var shiftStatusText by remember { mutableStateOf("Belum ada shift terbuka") }
    var isShiftLoading by remember { mutableStateOf(false) }

    // Attendance state
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

    // Emergency Voucher Dialog state
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var isEmergencySubmitting by remember { mutableStateOf(false) }
    var emergencySuccessMsg by remember { mutableStateOf<String?>(null) }

    // Checklist state
    var showChecklistDialog by remember { mutableStateOf(false) }
    var guidelines by remember { mutableStateOf<List<TicketGuideline>>(emptyList()) }
    var isGuidelinesLoading by remember { mutableStateOf(false) }
    var isChecklistSubmitting by remember { mutableStateOf(false) }
    var checklistConfirmed by remember { mutableStateOf(false) }

    // Scan & Redemption state
    var barcodeInput by remember { mutableStateOf("") }
    var isVerifyingTicket by remember { mutableStateOf(false) }
    var verifiedTicket by remember { mutableStateOf<Ticket?>(null) }
    var claimToken by remember { mutableStateOf<String?>(null) }
    var isClaimingDiscount by remember { mutableStateOf(false) }
    var redemptionSuccessMsg by remember { mutableStateOf<String?>(null) }
    var globalError by remember { mutableStateOf<String?>(null) }

    // History data
    var redemptionHistoryList by remember { mutableStateOf<List<RedemptionHistoryItem>>(emptyList()) }
    var isRedemptionsLoading by remember { mutableStateOf(false) }

    var attendanceHistoryList by remember { mutableStateOf<List<AttendanceHistoryItem>>(emptyList()) }
    var isAttendancesLoading by remember { mutableStateOf(false) }

    val correctionHistoryList = remember {
        mutableStateListOf(
            LocalCorrectionItem(
                id = 101L,
                targetDate = "2026-09-18",
                correctionType = "Check-In",
                requestedTime = "06:00",
                reason = "Jaringan bandara drop saat pergantian shift",
                status = "APPROVED"
            )
        )
    }

    // Helper to refresh history
    fun refreshRedemptions() {
        isRedemptionsLoading = true
        coroutineScope.launch {
            redemptionRepo.getRedemptions(cashierId).onSuccess { list ->
                redemptionHistoryList = list
                isRedemptionsLoading = false
            }.onFailure {
                isRedemptionsLoading = false
            }
        }
    }

    fun refreshAttendances() {
        isAttendancesLoading = true
        coroutineScope.launch {
            attendanceRepo.getAttendanceHistory(cashierId).onSuccess { list ->
                attendanceHistoryList = list
                isAttendancesLoading = false
            }.onFailure {
                isAttendancesLoading = false
            }
        }
    }

    // Initial Load
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
        refreshRedemptions()
        refreshAttendances()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                AppTab.values().forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            currentTab = tab
                            if (tab == AppTab.HISTORY) refreshRedemptions()
                            if (tab == AppTab.ABSEN) refreshAttendances()
                        },
                        icon = {
                            Text(
                                text = tab.iconText,
                                fontSize = if (selected) 20.sp else 18.sp
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) GrabGreen else SlateSubtle
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GrabGreen,
                            selectedTextColor = GrabGreen,
                            indicatorColor = GrabGreenLight,
                            unselectedIconColor = SlateSubtle,
                            unselectedTextColor = SlateSubtle
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = BgLight
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Global Error Banner
                if (globalError != null) {
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️ $globalError",
                                color = Color(0xFFBE123C),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = { globalError = null },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Tutup", fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Global Success Toast
                if (attendanceSuccessToast != null) {
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅ $attendanceSuccessToast",
                                color = Color(0xFF15803D),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = { attendanceSuccessToast = null },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Tutup", fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Main Tab Content
                when (currentTab) {
                    AppTab.HOME -> {
                        HomeTabContent(
                            activeShiftId = activeShiftId,
                            shiftStatusText = shiftStatusText,
                            todayAttendance = todayAttendance,
                            recentRedemptions = redemptionHistoryList.take(3),
                            onNavigateToScan = { currentTab = AppTab.SCAN },
                            onNavigateToHistory = {
                                currentTab = AppTab.HISTORY
                                refreshRedemptions()
                            },
                            onOpenAttendance = {
                                attendanceDialogError = null
                                showGpsAttendanceDialog = true
                            },
                            onOpenCorrection = {
                                correctionDialogError = null
                                showCorrectionDialog = true
                            },
                            onOpenChecklist = {
                                isGuidelinesLoading = true
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
                            onToggleShift = {
                                isShiftLoading = true
                                coroutineScope.launch {
                                    if (activeShiftId == null) {
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
                                            if (!hasCheckedIn) showGpsAttendanceDialog = true
                                        }.onFailure { err ->
                                            globalError = "Buka shift gagal: ${err.message}"
                                            isShiftLoading = false
                                        }
                                    } else {
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
                                }
                            },
                            onOpenEmergencyVoucher = {
                                showEmergencyDialog = true
                            }
                        )
                    }

                    AppTab.HISTORY -> {
                        HistoryTabContent(
                            redemptions = redemptionHistoryList,
                            isLoading = isRedemptionsLoading,
                            onRefresh = { refreshRedemptions() }
                        )
                    }

                    AppTab.SCAN -> {
                        ScanTabContent(
                            checklistConfirmed = checklistConfirmed,
                            activeShiftId = activeShiftId,
                            barcodeInput = barcodeInput,
                            onBarcodeInputChanged = { barcodeInput = it },
                            isVerifying = isVerifyingTicket,
                            verifiedTicket = verifiedTicket,
                            isClaiming = isClaimingDiscount,
                            redemptionSuccessMsg = redemptionSuccessMsg,
                            onOpenChecklist = {
                                isGuidelinesLoading = true
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
                            onVerifyTicket = {
                                if (!checklistConfirmed) {
                                    globalError = "Wajib konfirmasi checklist keaslian fisik terlebih dahulu!"
                                    return@ScanTabContent
                                }
                                if (activeShiftId == null) {
                                    globalError = "Wajib buka shift kasir terlebih dahulu!"
                                    return@ScanTabContent
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
                            onClaimDiscount = { t ->
                                isClaimingDiscount = true
                                globalError = null
                                coroutineScope.launch {
                                    val orderId = "ORD-" + (System.currentTimeMillis() % 100000)
                                    val amountCents = 2_500_000L // Rp 25.000

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
                                            refreshRedemptions()
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
                            onResetScan = {
                                verifiedTicket = null
                                barcodeInput = ""
                                redemptionSuccessMsg = null
                            }
                        )
                    }

                    AppTab.ABSEN -> {
                        AbsenTabContent(
                            todayAttendance = todayAttendance,
                            attendances = attendanceHistoryList,
                            corrections = correctionHistoryList,
                            isLoading = isAttendancesLoading,
                            onRefresh = { refreshAttendances() },
                            onOpenRecordTime = {
                                attendanceDialogError = null
                                showGpsAttendanceDialog = true
                            },
                            onOpenCorrection = {
                                correctionDialogError = null
                                showCorrectionDialog = true
                            }
                        )
                    }
                }
            }

            // Dialogs
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
                                refreshAttendances()
                            }.onFailure { err ->
                                isAttendanceSubmitting = false
                                attendanceDialogError = err.message ?: "Gagal mencatat absensi GPS"
                            }
                        }
                    }
                )
            }

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
                                correctionHistoryList.add(
                                    0,
                                    LocalCorrectionItem(
                                        id = res.correctionRequest?.id ?: System.currentTimeMillis(),
                                        targetDate = targetDate,
                                        correctionType = corrType.uppercase(),
                                        requestedTime = inTime ?: outTime ?: "--:--",
                                        reason = rsn,
                                        status = res.correctionRequest?.status?.uppercase() ?: "PENDING"
                                    )
                                )
                            }.onFailure { err ->
                                isCorrectionSubmitting = false
                                correctionDialogError = err.message ?: "Gagal mengirim pengajuan koreksi"
                            }
                        }
                    }
                )
            }

            if (showEmergencyDialog) {
                EmergencyVoucherDialog(
                    isSubmitting = isEmergencySubmitting,
                    onDismiss = { showEmergencyDialog = false },
                    onSubmit = { serial, pnr, pin, rsn ->
                        isEmergencySubmitting = true
                        coroutineScope.launch {
                            // Local simulation for offline voucher issuance
                            kotlinx.coroutines.delay(500)
                            isEmergencySubmitting = false
                            showEmergencyDialog = false
                            attendanceSuccessToast = "Voucher Darurat #$serial berhasil diterbitkan untuk PNR $pnr"
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// 1. HOME TAB CONTENT (Grab Style)
// ==========================================
@Composable
fun HomeTabContent(
    activeShiftId: Long?,
    shiftStatusText: String,
    todayAttendance: AttendanceRecordDto?,
    recentRedemptions: List<RedemptionHistoryItem>,
    onNavigateToScan: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenCorrection: () -> Unit,
    onOpenChecklist: () -> Unit,
    onToggleShift: () -> Unit,
    onOpenEmergencyVoucher: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Top Header Card (Grab Slate & Emerald)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SlateDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF7ED))
                                    .border(1.5.dp, Color(0xFFEA580C), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("KT", color = Color(0xFFEA580C), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Kasir Terminal 3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Sky Lounge Terminal 3 CGK", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }

                        // Shift Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeShiftId != null) GrabGreen else Color(0xFFE11D48))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (activeShiftId != null) "OPEN SHIFT" else "OFFLINE SHIFT",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SlateCard)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status Kasir:",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (activeShiftId != null) "Shift #$activeShiftId Siap Transaksi" else "Shift Belum Dibuka",
                                color = if (activeShiftId != null) Color(0xFF34D399) else Color(0xFFF87171),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Quick Status Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ringkasan Kasir Hari Ini", fontSize = 11.sp, color = SlateSubtle)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = todayAttendance?.checkInAt?.let { "Jam Masuk: ${if (it.length >= 16) it.substring(11, 16) else it} WIB" } ?: "Belum Check-In Hari Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SlateDark
                        )
                    }

                    val (statusText, statusBg, statusCol) = when (todayAttendance?.status) {
                        "completed" -> Triple("SELESAI", Color(0xFFDCFCE7), Color(0xFF15803D))
                        "present" -> Triple("HADIR", Color(0xFFDBEAFE), Color(0xFF1D4ED8))
                        "late" -> Triple("TERLAMBAT", Color(0xFFFEF3C7), Color(0xFFB45309))
                        else -> Triple("BELUM ABSEN", Color(0xFFF1F5F9), Color(0xFF64748B))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(statusText, color = statusCol, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Service Menu Grid (Grab Style 6-Icon Grid)
        item {
            Text(
                text = "Layanan & Operasional",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SlateDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickMenuItem(
                            iconText = "📷",
                            iconBg = GrabGreenLight,
                            label = "Scan Tiket",
                            onClick = onNavigateToScan
                        )
                        QuickMenuItem(
                            iconText = "📍",
                            iconBg = Color(0xFFFFEDD5),
                            label = "Absensi GPS",
                            onClick = onOpenAttendance
                        )
                        QuickMenuItem(
                            iconText = "✏️",
                            iconBg = Color(0xFFDBEAFE),
                            label = "Ajukan Koreksi",
                            onClick = onOpenCorrection
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickMenuItem(
                            iconText = "📋",
                            iconBg = Color(0xFFFEF3C7),
                            label = "Checklist Fisik",
                            onClick = onOpenChecklist
                        )
                        QuickMenuItem(
                            iconText = if (activeShiftId != null) "🔒" else "🔓",
                            iconBg = Color(0xFFCCFBF1),
                            label = if (activeShiftId != null) "Tutup Shift" else "Buka Shift",
                            onClick = onToggleShift
                        )
                        QuickMenuItem(
                            iconText = "🎟️",
                            iconBg = Color(0xFFF3E8FF),
                            label = "Emergency",
                            onClick = onOpenEmergencyVoucher
                        )
                    }
                }
            }
        }

        // Promo Banner Card (Grab Style)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GrabGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("PROMO AKTIF", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Diskon SkyPrivilege Rp 25.000",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Potongan langsung per boarding pass Garuda, Citilink & Lion Group di Sky Lounge T3.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                    Text("✈️", fontSize = 32.sp)
                }
            }
        }

        // Recent Activity Snippet
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Terakhir",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SlateDark
                )
                Text(
                    text = "Lihat Semua >",
                    color = GrabGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToHistory() }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (recentRedemptions.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎟️", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Belum ada pemindaian tiket hari ini", fontSize = 12.sp, color = SlateSubtle)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onNavigateToScan,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Mulai Scan Tiket", fontSize = 11.sp, color = GrabGreen)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentRedemptions.forEach { item ->
                        RedemptionHistoryCard(item)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun QuickMenuItem(
    iconText: String,
    iconBg: Color,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(iconText, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = SlateDark,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// 2. HISTORY TAB CONTENT
// ==========================================
@Composable
fun HistoryTabContent(
    redemptions: List<RedemptionHistoryItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    val totalApproved = redemptions.count { it.status.equals("approved", ignoreCase = true) }
    val totalDiscount = redemptions.sumOf { it.discountAmount.toLong() }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat Pemindaian Tiket",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SlateDark
                )
                Text(
                    text = "Daftar klaim diskon boarding pass kasir",
                    fontSize = 12.sp,
                    color = SlateSubtle
                )
            }
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.height(32.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), color = GrabGreen)
                } else {
                    Text("🔄 Muat Ulang", fontSize = 11.sp, color = GrabGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Metric Cards (Total Klaim & Total Diskon)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Klaim Hari Ini", fontSize = 11.sp, color = SlateSubtle)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalApproved Tiket",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SlateDark
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Diskon Diberikan", fontSize = 11.sp, color = SlateSubtle)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rp ${formatRupiah(totalDiscount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GrabGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Redemption List
        if (redemptions.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📜", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum Ada Riwayat Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lakukan pemindaian tiket di tab Scan untuk memulai klaim diskon penumpang.",
                        fontSize = 12.sp,
                        color = SlateSubtle,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(redemptions) { item ->
                    RedemptionHistoryCard(item)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun RedemptionHistoryCard(item: RedemptionHistoryItem) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.flightNumber.ifBlank { "GA-TICKET" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${item.outletName.ifBlank { "Sky Lounge T3" }}",
                        fontSize = 11.sp,
                        color = SlateSubtle
                    )
                }

                // Badge status hijau APPROVED / kuning OVERRIDE
                val (badgeText, badgeBg, badgeCol) = when {
                    item.isOverridden -> Triple("OVERRIDE", Color(0xFFFEF3C7), Color(0xFFB45309))
                    item.status.equals("approved", ignoreCase = true) -> Triple("APPROVED", GrabGreenLight, GrabGreenDark)
                    else -> Triple(item.status.uppercase(), Color(0xFFF1F5F9), Color(0xFF475569))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeCol,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // PNR Masked
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PNR: ${item.pnrMasked.ifBlank { "f7f12381..." }}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )

                Text(
                    text = "-Rp ${formatRupiah(item.discountAmount.toLong())}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GrabGreen
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.timeFormatted.ifBlank { "15:43 WIB" }} • ${item.dateFormatted.ifBlank { "19 Sep 2026" }}",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )

                if (item.cashierName.isNotBlank()) {
                    Text(
                        text = item.cashierName,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. SCAN TAB CONTENT
// ==========================================
@Composable
fun ScanTabContent(
    checklistConfirmed: Boolean,
    activeShiftId: Long?,
    barcodeInput: String,
    onBarcodeInputChanged: (String) -> Unit,
    isVerifying: Boolean,
    verifiedTicket: Ticket?,
    isClaiming: Boolean,
    redemptionSuccessMsg: String?,
    onOpenChecklist: () -> Unit,
    onVerifyTicket: () -> Unit,
    onClaimDiscount: (Ticket) -> Unit,
    onResetScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Column {
                Text(
                    text = "Pemindai Tiket Boarding Pass",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SlateDark
                )
                Text(
                    text = "Verifikasi keaslian fisik, validasi barcode & klaim diskon POS",
                    fontSize = 12.sp,
                    color = SlateSubtle
                )
            }
        }

        // Checklist Banner Gate
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (checklistConfirmed) GrabGreenLight else Color(0xFFFFFBEB)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (checklistConfirmed) GrabGreen else Color(0xFFFCD34D)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (checklistConfirmed) "✅ Checklist Fisik Terkonfirmasi" else "⚠️ Wajib Checklist Keaslian Tiket",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (checklistConfirmed) GrabGreenDark else Color(0xFFB45309)
                        )
                        OutlinedButton(
                            onClick = onOpenChecklist,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (checklistConfirmed) "Tinjau Ulang" else "Buka Checklist",
                                fontSize = 11.sp,
                                color = if (checklistConfirmed) GrabGreenDark else Color(0xFFB45309)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (checklistConfirmed)
                            "4 kriteria keaslian boarding pass telah dicentang dan diaudit."
                        else
                            "Kasir wajib memeriksa kertas fisik/watermark/tanggal sebelum melakukan scan.",
                        fontSize = 11.sp,
                        color = if (checklistConfirmed) GrabGreenDark else Color(0xFF92400E)
                    )
                }
            }
        }

        // Camera & Barcode Input Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Input Barcode Boarding Pass (IATA BCBP)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Sample Button
                    Button(
                        onClick = {
                            onBarcodeInputChanged("M1SANTOSO/BUDI MR     EABC1234CGKDPSGA 00410262Y012A00042100")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚡ Isi Sampel Boarding Pass GA410 (Hari Ini)", fontSize = 11.sp, color = SlateDark)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = onBarcodeInputChanged,
                        label = { Text("Raw Barcode / BCBP String") },
                        placeholder = { Text("M1SANTOSO/BUDI MR...") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onVerifyTicket,
                        enabled = !isVerifying && barcodeInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Verifikasi Tiket ke Anti-Fraud Engine", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Ticket Verification Result
                    if (verifiedTicket != null) {
                        val t = verifiedTicket
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = GrabGreenLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "✅ Boarding Pass Sah & Terverifikasi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = GrabGreenDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Penumpang: ${t.passengerName}", fontSize = 12.sp, color = Color(0xFF14532D))
                                Text("PNR: ${t.pnr} | Penerbangan: ${t.flightNumber} (${t.fromAirport} -> ${t.toAirport})", fontSize = 12.sp, color = Color(0xFF14532D))
                                Text("Tanggal: ${t.flightDate} | Kursi: ${t.seatNumber} | Kelas: ${t.compartmentCode}", fontSize = 12.sp, color = Color(0xFF14532D))
                                Text(
                                    text = "Hash: ${t.canonicalHash?.take(16)}...",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = GrabGreenDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onClaimDiscount(t) },
                            enabled = !isClaiming,
                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isClaiming) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Text("Terapkan Diskon ke Moka POS (Rp 25.000)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Success Message & Stempel Basah Banner
                    if (redemptionSuccessMsg != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, GrabGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🎉 KLAIM DISKON BERHASIL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF15803D)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = redemptionSuccessMsg,
                                    fontSize = 12.sp,
                                    color = Color(0xFF14532D)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "⚠️ WAJIB STEMPEL BASAH: Berikan stempel fisik 'CLAIMED - SKYPRIVILEGE' pada boarding pass penumpang sebelum mengembalikan.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = onResetScan,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Scan Tiket Berikutnya", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ==========================================
// 4. ABSEN TAB CONTENT (GreatDay HR Style)
// ==========================================
@Composable
fun AbsenTabContent(
    todayAttendance: AttendanceRecordDto?,
    attendances: List<AttendanceHistoryItem>,
    corrections: List<LocalCorrectionItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onOpenRecordTime: () -> Unit,
    onOpenCorrection: () -> Unit
) {
    var subTab by remember { mutableStateOf(0) } // 0: Riwayat Absensi, 1: Riwayat Koreksi

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Absensi & Kehadiran Kasir",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SlateDark
                    )
                    Text(
                        text = "GreatDay HR Style Presensi & Geofencing",
                        fontSize = 12.sp,
                        color = SlateSubtle
                    )
                }
                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.height(30.dp),
                    enabled = !isLoading
                ) {
                    Text("🔄 Refresh", fontSize = 10.sp)
                }
            }
        }

        // GreatDay HR Style Attendance Card
        item {
            AttendanceCard(
                cashierName = "Kasir Terminal 3",
                cashierRole = "Staff Kasir & Operator POS",
                shiftInfo = "Shift Pagi (06:00 - 15:00)",
                todayText = "Sabtu, 19 Sep 2026",
                checkInTime = todayAttendance?.checkInAt?.let { if (it.length >= 16) it.substring(11, 16) else it },
                checkOutTime = todayAttendance?.checkOutAt?.let { if (it.length >= 16) it.substring(11, 16) else it },
                status = todayAttendance?.status,
                onRecordTimeClick = onOpenRecordTime,
                onCorrectionClick = onOpenCorrection
            )
        }

        // In-page Sub-Tab Switcher (Riwayat Absensi vs Riwayat Koreksi)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (subTab == 0) Color.White else Color.Transparent)
                        .clickable { subTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Riwayat Absensi",
                        fontWeight = if (subTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (subTab == 0) GrabGreenDark else SlateSubtle
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (subTab == 1) Color.White else Color.Transparent)
                        .clickable { subTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Riwayat Koreksi",
                        fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (subTab == 1) GrabGreenDark else SlateSubtle
                    )
                }
            }
        }

        // SubTab 0: Riwayat Absensi
        if (subTab == 0) {
            if (attendances.isEmpty() && !isLoading) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⏱️", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Belum ada riwayat absensi", fontSize = 12.sp, color = SlateSubtle)
                        }
                    }
                }
            } else {
                items(attendances) { item ->
                    AttendanceHistoryCard(item)
                }
            }
        } else {
            // SubTab 1: Riwayat Koreksi
            if (corrections.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📝", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Belum ada pengajuan koreksi", fontSize = 12.sp, color = SlateSubtle)
                        }
                    }
                }
            } else {
                items(corrections) { item ->
                    CorrectionHistoryCard(item)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun AttendanceHistoryCard(item: AttendanceHistoryItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = item.dateFormatted.ifBlank { item.date },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SlateDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("In: ${item.checkInAt ?: "--:--"}", fontSize = 11.sp, color = Color(0xFF15803D))
                    Text("•", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("Out: ${item.checkOutAt ?: "--:--"}", fontSize = 11.sp, color = Color(0xFFC2410C))
                }
                if (item.checkInAccuracy != null) {
                    Text(
                        text = "GPS: ±${item.checkInAccuracy.toInt()}m",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            val (badgeText, badgeBg, badgeCol) = when (item.status) {
                "completed" -> Triple("SELESAI", Color(0xFFDCFCE7), Color(0xFF15803D))
                "present" -> Triple("HADIR", Color(0xFFDBEAFE), Color(0xFF1D4ED8))
                "late" -> Triple("TERLAMBAT", Color(0xFFFEF3C7), Color(0xFFB45309))
                else -> Triple(item.status.uppercase(), Color(0xFFF1F5F9), Color(0xFF64748B))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(badgeText, color = badgeCol, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CorrectionHistoryCard(item: LocalCorrectionItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.correctionType} • ${item.targetDate}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SlateDark
                )

                val (bg, col) = when (item.status) {
                    "APPROVED" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
                    "REJECTED" -> Pair(Color(0xFFFFE4E6), Color(0xFFBE123C))
                    else -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(item.status, color = col, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Jam Diajukan: ${item.requestedTime}", fontSize = 11.sp, color = SlateDark)
            Text("Alasan: ${item.reason}", fontSize = 11.sp, color = SlateSubtle)
        }
    }
}

fun formatRupiah(amount: Long): String {
    return amount.toString().reversed().chunked(3).joinToString(".").reversed()
}
