package com.skyprivilege.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import skyprivilegemobile.composeapp.generated.resources.Res
import skyprivilegemobile.composeapp.generated.resources.card_promo_garuda
import skyprivilegemobile.composeapp.generated.resources.logo_garuda
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.skyprivilege.domain.model.TicketVerificationException
import com.skyprivilege.data.remote.dto.CashierProfileDto
import com.skyprivilege.data.repository.ProfileRepositoryImpl
import com.skyprivilege.ui.components.FlatAbsenIcon
import com.skyprivilege.ui.components.FlatAirplaneIcon
import com.skyprivilege.ui.components.FlatGarudaLogo
import com.skyprivilege.ui.components.FlatGpsPinIcon
import com.skyprivilege.ui.components.FlatHomeIcon
import com.skyprivilege.ui.components.FlatLogoutIcon
import com.skyprivilege.ui.components.FlatProfileIcon
import com.skyprivilege.ui.components.FlatScanIcon
import com.skyprivilege.ui.components.FlatTransactionIcon
import com.skyprivilege.ui.components.FlatVoucherTicketIcon
import com.skyprivilege.ui.components.SkyPullRefreshBox
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    HOME("Home"),
    HISTORY("Transaksi"),
    SCAN("Scan"),
    ABSEN("Absen"),
    AKUN_SAYA("Akun Saya")
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
    val profileRepo = remember(httpClient) { ProfileRepositoryImpl(httpClient) }

    val coroutineScope = rememberCoroutineScope()

    // Navigation state
    var currentTab by remember { mutableStateOf(AppTab.HOME) }

    // Cashier Profile state
    var cashierProfile by remember {
        mutableStateOf(
            CashierProfileDto(
                id = cashierId,
                name = "ANDHIKA PUTRA",
                employeeId = "CSH-001",
                role = "cashier",
                outletId = outletId,
                outletName = "Sky Lounge Terminal 3 CGK",
                active = true
            )
        )
    }
    val profileRefreshController = remember { PullRefreshController() }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var editProfileError by remember { mutableStateOf<String?>(null) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var changePasswordError by remember { mutableStateOf<String?>(null) }
    var showSopDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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
    var showCameraScanDialog by remember { mutableStateOf(false) }
    var showTicketInvalidDialog by remember { mutableStateOf(false) }
    var ticketInvalidError by remember { mutableStateOf<String?>(null) }
    var ticketInvalidRedeemedAt by remember { mutableStateOf<String?>(null) }
    var ticketInvalidRedeemedOutlet by remember { mutableStateOf<String?>(null) }
    var ticketInvalidRedeemedCashier by remember { mutableStateOf<String?>(null) }
    var showTicketValidDialog by remember { mutableStateOf(false) }
    var capturedPhotoBase64 by remember { mutableStateOf<String?>(null) }

    // Shift gating alert dialogs
    var showShiftNotStartedDialog by remember { mutableStateOf(false) }
    var showShiftCompletedDialog by remember { mutableStateOf(false) }

    val isCheckedIn = (todayAttendance?.checkInAt != null && todayAttendance?.checkInAt != "--:--") ||
                      (todayAttendance?.startTime != null && todayAttendance?.startTime != "--:--") ||
                      todayAttendance?.status in listOf("present", "late", "completed") ||
                      hasCheckedIn

    val isCheckedOut = (todayAttendance?.checkOutAt != null && todayAttendance?.checkOutAt != "--:--") ||
                       (todayAttendance?.endTime != null && todayAttendance?.endTime != "--:--") ||
                       todayAttendance?.status == "completed" ||
                       hasCheckedOut

    val isShiftActive = isCheckedIn && !isCheckedOut
    val isShiftCompleted = isCheckedOut
    val isShiftNotStarted = !isCheckedIn && !isCheckedOut

    // History data
    var redemptionHistoryList by remember { mutableStateOf<List<RedemptionHistoryItem>>(emptyList()) }
    val redemptionsRefreshController = remember { PullRefreshController() }
    var selectedRedemptionDetail by remember { mutableStateOf<RedemptionHistoryItem?>(null) }

    var attendanceHistoryList by remember { mutableStateOf<List<AttendanceHistoryItem>>(emptyList()) }
    val attendancesRefreshController = remember { PullRefreshController() }

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
        coroutineScope.launch {
            redemptionsRefreshController.execute {
                redemptionRepo.getRedemptions(cashierId, todayOnly = true).onSuccess { list ->
                    redemptionHistoryList = list
                }
            }
        }
    }

    val homeRefreshController = remember { PullRefreshController() }

    fun refreshHome() {
        coroutineScope.launch {
            homeRefreshController.execute {
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
                redemptionRepo.getRedemptions(cashierId, todayOnly = true).onSuccess { list ->
                    redemptionHistoryList = list
                }
                profileRepo.getProfile(cashierId).onSuccess { prof ->
                    cashierProfile = prof
                }
            }
        }
    }

    fun refreshAttendances() {
        coroutineScope.launch {
            attendancesRefreshController.execute {
                attendanceRepo.getTodayAttendance(cashierId, outletId).onSuccess { res ->
                    if (res.success) {
                        todayAttendance = res.attendance
                        hasCheckedIn = res.hasCheckedIn
                        hasCheckedOut = res.hasCheckedOut
                    }
                }
                attendanceRepo.getAttendanceHistory(cashierId).onSuccess { list ->
                    attendanceHistoryList = list
                }
            }
        }
    }

    fun refreshProfile() {
        coroutineScope.launch {
            profileRefreshController.execute {
                profileRepo.getProfile(cashierId).onSuccess { prof ->
                    cashierProfile = prof
                }
            }
        }
    }

    fun startScanFlow() {
        if (!checklistConfirmed) {
            if (guidelines.isEmpty()) {
                isGuidelinesLoading = true
                coroutineScope.launch {
                    guidelineRepo.getGuidelines().onSuccess { list ->
                        guidelines = list
                        isGuidelinesLoading = false
                        showChecklistDialog = true
                    }.onFailure {
                        guidelines = listOf(
                            TicketGuideline(1, "Kertas Fisik Asli", "Tiket thermal cetak asli bandara, tidak ada bekas tempelan/potongan.", "physical_print", 1),
                            TicketGuideline(2, "E-Ticket Digital Resmi", "Dibuka langsung dari aplikasi resmi maskapai atau OTA (bukan screenshot WA).", "digital_eticket", 2),
                            TicketGuideline(3, "Masa Berlaku Penerbangan", "Tanggal penerbangan harus hari ini atau besok (Hari H s/d H+1).", "flight_date", 3),
                            TicketGuideline(4, "Kualitas Barcode", "Barcode PDF417 / Aztec terlihat tajam dan tidak buram.", "barcode", 4)
                        )
                        isGuidelinesLoading = false
                        showChecklistDialog = true
                    }
                }
            } else {
                showChecklistDialog = true
            }
        } else {
            showCameraScanDialog = true
        }
    }

    fun onAttemptScan() {
        when {
            isShiftCompleted -> {
                showShiftCompletedDialog = true
            }
            isShiftNotStarted -> {
                showShiftNotStartedDialog = true
            }
            else -> {
                startScanFlow()
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
        refreshProfile()
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                color = Color.White,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SkyPrivilege",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF005BAC)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "mobile",
                            fontSize = 17.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color(0xFF0284C7)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .clickable { showLogoutDialog = true }
                                .padding(4.dp)
                        ) {
                            FlatLogoutIcon(
                                tint = Color(0xFF64748B),
                                size = 20.dp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            BcaBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    if (tab == AppTab.SCAN) {
                        onAttemptScan()
                    } else {
                        currentTab = tab
                        if (tab == AppTab.HISTORY) refreshRedemptions()
                        if (tab == AppTab.ABSEN) refreshAttendances()
                        if (tab == AppTab.AKUN_SAYA) refreshProfile()
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = if (currentTab == AppTab.HOME) Color(0xFF072146) else BgLight
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
                        val outlet = cashierProfile.outletName.orEmpty()
                        val locationName = if (outlet.contains("Terminal 3")) "Terminal 3 Soetta" else outlet.ifEmpty { "Terminal 3 Soetta" }
                        val shiftScheduleTime = "Shift 08:00 - 11:00"
                        val todayRedemptions = redemptionHistoryList.filter { item ->
                            item.isToday || item.dateFormatted.contains("20 Sep 2026")
                        }
                        HomeTabContent(
                            cashierName = cashierProfile.name,
                            detectedLocationText = "$locationName | $shiftScheduleTime",
                            activeShiftId = activeShiftId,
                            shiftStatusText = shiftStatusText,
                            todayAttendance = todayAttendance,
                            hasCheckedIn = hasCheckedIn,
                            hasCheckedOut = hasCheckedOut,
                            recentRedemptions = todayRedemptions.take(1),
                            isRefreshing = homeRefreshController.isRefreshing,
                            onRefresh = { refreshHome() },
                            onNavigateToScan = { onAttemptScan() },
                            onNavigateToHistory = {
                                currentTab = AppTab.HISTORY
                                refreshRedemptions()
                            },
                            onRedemptionClick = { item ->
                                selectedRedemptionDetail = item
                            }
                        )
                    }

                    AppTab.HISTORY -> {
                        HistoryTabContent(
                            redemptions = redemptionHistoryList,
                            isLoading = redemptionsRefreshController.isRefreshing,
                            onRefresh = { refreshRedemptions() },
                            onItemClick = { item ->
                                selectedRedemptionDetail = item
                            }
                        )
                    }

                    AppTab.SCAN -> {
                        // User requirement: "halaman ini g perlu, kalau scan gagal balik ke home aja"
                        LaunchedEffect(Unit) {
                            currentTab = AppTab.HOME
                            onAttemptScan()
                        }
                    }

                    AppTab.ABSEN -> {
                        AbsenTabContent(
                            todayAttendance = todayAttendance,
                            attendances = attendanceHistoryList,
                            corrections = correctionHistoryList,
                            isLoading = attendancesRefreshController.isRefreshing,
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

                    AppTab.AKUN_SAYA -> {
                        AkunSayaTabContent(
                            profile = cashierProfile,
                            isLoading = profileRefreshController.isRefreshing,
                            deviceId = deviceId,
                            onRefresh = { refreshProfile() },
                            onOpenEditProfile = {
                                editProfileError = null
                                showEditProfileDialog = true
                            },
                            onOpenChangePassword = {
                                changePasswordError = null
                                showChangePasswordDialog = true
                            },
                            onLogout = {
                                showLogoutDialog = true
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
                    isSubmitting = false,
                    onDismiss = {
                        showChecklistDialog = false
                        currentTab = AppTab.HOME
                    },
                    onConfirm = {
                        // User requirement: "itu jangan di post ke backend, habis dia checklist udah ada dan valid secara manual, nanti kmudian akan di bukakan kamera untuk foto tiket"
                        checklistConfirmed = true
                        showChecklistDialog = false
                        showCameraScanDialog = true
                    }
                )
            }

            if (showCameraScanDialog) {
                TicketCameraDialog(
                    cashierName = cashierProfile.name,
                    outletName = cashierProfile.outletName.orEmpty(),
                    isVerifying = isVerifyingTicket,
                    errorMessage = globalError,
                    showDemoAffordance = false,
                    onDismiss = {
                        showCameraScanDialog = false
                        capturedPhotoBase64 = null
                        currentTab = AppTab.HOME
                    },
                    onSubmitTicket = { barcodeData, imageBase64 ->
                        capturedPhotoBase64 = imageBase64
                        isVerifyingTicket = true
                        globalError = null
                        coroutineScope.launch {
                            ticketRepo.verifyTicket(
                                barcodeData = barcodeData,
                                imageBase64 = imageBase64,
                                outletId = outletId,
                                cashierId = cashierId,
                                deviceId = 1L,
                                shiftId = activeShiftId,
                                checklistConfirmed = true,
                                latitude = -6.1256,
                                longitude = 106.6558,
                                accuracy = 15.0f,
                                wifiBssid = "aa:bb:cc:dd:ee:ff",
                                wifiSsid = "SkyPrivilege_Staff"
                            ).onSuccess { ticket ->
                                isVerifyingTicket = false
                                showCameraScanDialog = false
                                verifiedTicket = ticket
                                barcodeInput = barcodeData.orEmpty()
                                redemptionSuccessMsg = null
                                showTicketValidDialog = true
                            }.onFailure { err ->
                                isVerifyingTicket = false
                                showCameraScanDialog = false
                                capturedPhotoBase64 = null
                                if (err is TicketVerificationException) {
                                    ticketInvalidError = err.message
                                    ticketInvalidRedeemedAt = err.redeemedAt
                                    ticketInvalidRedeemedOutlet = err.redeemedOutlet
                                    ticketInvalidRedeemedCashier = err.redeemedCashier
                                } else {
                                    ticketInvalidError = err.message ?: "Tiket tidak valid atau melanggar aturan Anti-Fraud"
                                    ticketInvalidRedeemedAt = null
                                    ticketInvalidRedeemedOutlet = null
                                    ticketInvalidRedeemedCashier = null
                                }
                                showTicketInvalidDialog = true
                            }
                        }
                    }
                )
            }

            if (showTicketInvalidDialog && ticketInvalidError != null) {
                TicketInvalidWarningDialog(
                    errorMessage = ticketInvalidError!!,
                    redeemedAt = ticketInvalidRedeemedAt,
                    redeemedOutlet = ticketInvalidRedeemedOutlet,
                    redeemedCashier = ticketInvalidRedeemedCashier,
                    onDismiss = {
                        showTicketInvalidDialog = false
                        ticketInvalidError = null
                        ticketInvalidRedeemedAt = null
                        ticketInvalidRedeemedOutlet = null
                        ticketInvalidRedeemedCashier = null
                        checklistConfirmed = false
                        capturedPhotoBase64 = null
                        currentTab = AppTab.HOME
                    }
                )
            }

            if (showTicketValidDialog && verifiedTicket != null) {
                TicketValidResultDialog(
                    ticket = verifiedTicket!!,
                    discountFormatted = "Rp 25.000",
                    isClaiming = isClaimingDiscount,
                    redemptionSuccessMsg = redemptionSuccessMsg,
                    onDismiss = {
                        showTicketValidDialog = false
                        capturedPhotoBase64 = null
                        currentTab = AppTab.HOME
                        if (redemptionSuccessMsg != null) {
                            checklistConfirmed = false
                            verifiedTicket = null
                            barcodeInput = ""
                            redemptionSuccessMsg = null
                        }
                    },
                    onApplyToMoka = { orderId, bagSize, wrapType, grossAmountCents, amountCents, netAmountCents ->
                        val t = verifiedTicket!!
                        val photoToSend = capturedPhotoBase64
                        isClaimingDiscount = true
                        coroutineScope.launch {
                            redemptionRepo.requestClaimToken(
                                orderId = orderId,
                                pnrHash = t.canonicalHash.orEmpty(),
                                outletId = outletId,
                                cashierId = cashierId,
                                amountCents = amountCents,
                                bagSize = bagSize,
                                wrapType = wrapType,
                                grossAmountCents = grossAmountCents,
                                netAmountCents = netAmountCents,
                                ticketPhotoData = photoToSend,
                                signals = com.skyprivilege.domain.model.LocationContext(
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
                                    com.skyprivilege.domain.model.RedemptionClaim(
                                        ticket = t,
                                        orderId = orderId,
                                        outletId = outletId,
                                        cashierId = cashierId,
                                        amountCents = amountCents,
                                        claimToken = token,
                                        shiftId = activeShiftId,
                                        ticketPhotoData = photoToSend,
                                        bagSize = bagSize,
                                        wrapType = wrapType,
                                        grossAmountCents = grossAmountCents,
                                        netAmountCents = netAmountCents
                                    )
                                ).onSuccess { redId ->
                                    isClaimingDiscount = false
                                    // ZERO-DISK: clear in-memory photo buffer immediately after transmission
                                    capturedPhotoBase64 = null
                                    val netRp = formatRupiah(netAmountCents / 100)
                                    redemptionSuccessMsg = "Klaim Berhasil! ID Transaksi: #$redId (Order $orderId: $bagSize/$wrapType, Net Rp $netRp)"
                                    refreshRedemptions()
                                }.onFailure { claimErr ->
                                    isClaimingDiscount = false
                                    globalError = "Klaim gagal: ${claimErr.message}"
                                }
                            }.onFailure { tokenErr ->
                                isClaimingDiscount = false
                                globalError = "Otorisasi token gagal: ${tokenErr.message}"
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
                    checkInTime = todayAttendance?.checkInAt?.let { if (it.length >= 16) it.substring(11, 16) else it } ?: todayAttendance?.startTime,
                    checkOutTime = todayAttendance?.checkOutAt?.let { if (it.length >= 16) it.substring(11, 16) else it } ?: todayAttendance?.endTime,
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
                                if (res.action == "check_in" || type == "check_in") {
                                    hasCheckedIn = true
                                }
                                if (res.action == "check_out" || type == "check_out") {
                                    hasCheckedOut = true
                                }
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

            if (showEditProfileDialog) {
                EditProfileDialog(
                    currentName = cashierProfile.name,
                    isSubmitting = isEditingProfile,
                    errorMessage = editProfileError,
                    onDismiss = { showEditProfileDialog = false },
                    onSubmit = { newName ->
                        isEditingProfile = true
                        editProfileError = null
                        coroutineScope.launch {
                            profileRepo.updateProfile(cashierId, newName).onSuccess { msg ->
                                isEditingProfile = false
                                showEditProfileDialog = false
                                attendanceSuccessToast = msg
                                cashierProfile = cashierProfile.copy(name = newName)
                            }.onFailure { err ->
                                isEditingProfile = false
                                editProfileError = err.message ?: "Gagal memperbarui profil"
                            }
                        }
                    }
                )
            }

            if (showChangePasswordDialog) {
                ChangePasswordDialog(
                    isSubmitting = isChangingPassword,
                    errorMessage = changePasswordError,
                    onDismiss = { showChangePasswordDialog = false },
                    onSubmit = { oldPin, newPin, confirmation ->
                        isChangingPassword = true
                        changePasswordError = null
                        coroutineScope.launch {
                            profileRepo.changePassword(cashierId, oldPin, newPin, confirmation).onSuccess { msg ->
                                isChangingPassword = false
                                showChangePasswordDialog = false
                                attendanceSuccessToast = msg
                            }.onFailure { err ->
                                isChangingPassword = false
                                changePasswordError = err.message ?: "Gagal mengubah password/PIN"
                            }
                        }
                    }
                )
            }

            if (showSopDialog) {
                SopGuidelineDialog(
                    onDismiss = { showSopDialog = false }
                )
            }

            if (showLogoutDialog) {
                LogoutConfirmDialog(
                    onDismiss = { showLogoutDialog = false },
                    onConfirm = {
                        showLogoutDialog = false
                        attendanceSuccessToast = "Sesi kasir berhasil diakhiri"
                    }
                )
            }

            if (selectedRedemptionDetail != null) {
                RedemptionDetailDialog(
                    item = selectedRedemptionDetail!!,
                    onDismiss = { selectedRedemptionDetail = null }
                )
            }

            if (showShiftNotStartedDialog) {
                Dialog(
                    onDismissRequest = { showShiftNotStartedDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .clip(RoundedCornerShape(20.dp)),
                        color = Color.White,
                        shadowElevation = 16.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚠️", fontSize = 26.sp)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Shift Belum Dibuka",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Silakan lakukan Absensi Masuk (Check-in) terlebih dahulu sebelum dapat memindai tiket atau melakukan transaksi.",
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showShiftNotStartedDialog = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Batal", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        showShiftNotStartedDialog = false
                                        currentTab = AppTab.ABSEN
                                        showGpsAttendanceDialog = true
                                    },
                                    modifier = Modifier.weight(1.4f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005BAC)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Buka Absen GPS",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showShiftCompletedDialog) {
                Dialog(
                    onDismissRequest = { showShiftCompletedDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .clip(RoundedCornerShape(20.dp)),
                        color = Color.White,
                        shadowElevation = 16.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF2F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔒", fontSize = 26.sp)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Shift Hari Ini Telah Selesai",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val checkOutFormatted = todayAttendance?.checkOutAt?.let {
                                if (it.length >= 16) it.substring(11, 16) else it
                            } ?: todayAttendance?.endTime ?: "--:--"

                            Text(
                                text = "Anda telah melakukan check-out pada $checkOutFormatted WIB.\n\nAplikasi dinonaktifkan dari transaksi baru sampai shift kerja berikutnya besok.",
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { showShiftCompletedDialog = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Mengerti",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. HOME TAB CONTENT (BCA Mobile Style)
// ==========================================
@Composable
fun HomeTabContent(
    cashierName: String,
    detectedLocationText: String = "Terminal 3 Soetta | Shift 08:00 - 11:00",
    activeShiftId: Long?,
    shiftStatusText: String,
    todayAttendance: AttendanceRecordDto?,
    hasCheckedIn: Boolean = false,
    hasCheckedOut: Boolean = false,
    recentRedemptions: List<RedemptionHistoryItem>,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onNavigateToScan: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onRedemptionClick: (RedemptionHistoryItem) -> Unit = {},
    onOpenAttendance: () -> Unit = {},
    onOpenCorrection: () -> Unit = {},
    onOpenChecklist: () -> Unit = {},
    onToggleShift: () -> Unit = {},
    onOpenEmergencyVoucher: () -> Unit = {},
    onOpenSop: () -> Unit = {}
) {
    val isCheckedIn = (todayAttendance?.checkInAt != null && todayAttendance?.checkInAt != "--:--") ||
                      (todayAttendance?.startTime != null && todayAttendance?.startTime != "--:--") ||
                      todayAttendance?.status in listOf("present", "late", "completed") ||
                      hasCheckedIn

    val isCheckedOut = (todayAttendance?.checkOutAt != null && todayAttendance?.checkOutAt != "--:--") ||
                       (todayAttendance?.endTime != null && todayAttendance?.endTime != "--:--") ||
                       todayAttendance?.status == "completed" ||
                       hasCheckedOut

    val isShiftActive = isCheckedIn && !isCheckedOut
    val isShiftCompleted = isCheckedOut
    val isShiftNotStarted = !isCheckedIn && !isCheckedOut

    SkyPullRefreshBox(
        refreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF072146)),
        indicatorColor = Color(0xFF005BAC)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // BCA Greeting Section with Auto-Detected GPS Location & Shift
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Text(
                    text = "Selamat datang,",
                    color = Color(0xFF93C5FD),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cashierName.uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlatGpsPinIcon(
                        tint = Color(0xFF38BDF8),
                        cutoutColor = Color(0xFF072146),
                        size = 14.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = detectedLocationText,
                        color = Color(0xFF7DD3FC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Quick Status & Shift Card (Adaptive to Shift Attendance State)
        // User rule: "kalau udah checkin otomatis itu hilang, kalau sudah checkout aplikasi ga bisa di gunakan"
        if (isShiftNotStarted) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Status Operasional Kasir", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Shift Belum Dibuka",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFE11D48)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFE4E6))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("BELUM CHECK-IN", color = Color(0xFFBE123C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Silakan lakukan Absensi Masuk (Check-in) pada tab Absen saat memulai shift kerja.",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        } else if (isShiftCompleted) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔒", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Shift Hari Ini Telah Selesai",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF334155)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE2E8F0))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("SELESAI", color = Color(0xFF475569), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val checkOutFormatted = todayAttendance?.checkOutAt?.let {
                            if (it.length >= 16) it.substring(11, 16) else it
                        } ?: todayAttendance?.endTime ?: "--:--"
                        Text(
                            text = "Check-out tercatat pada $checkOutFormatted WIB. Seluruh fungsi transaksi dan scan dinonaktifkan sampai shift hari berikutnya.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Promo Banner Card (Exact User Mockup with Full-Bleed Airline Logo & Sparkle)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF005BAC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(Res.drawable.card_promo_garuda),
                    contentDescription = "Promo Maskapai SkyPrivilege",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
        }

        // Recent Scans
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Terakhir",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Text(
                    text = "Lihat Semua ›",
                    color = Color(0xFF93C5FD),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToHistory() }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (recentRedemptions.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D3268).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FlatVoucherTicketIcon(
                            tint = Color(0xFF93C5FD),
                            size = 28.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Belum ada pemindaian tiket hari ini", fontSize = 11.sp, color = Color(0xFF93C5FD))
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentRedemptions.forEach { item ->
                        RedemptionHistoryCard(
                            item = item,
                            onClick = { onRedemptionClick(item) }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
    }
}

@Composable
fun BcaGridItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (tint: Color) -> Unit = {}
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0066B3),
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color(0xFF0072CE), Color(0xFF004F98))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon(Color.White)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BcaBottomNavigationBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom bar surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slot 1: Home
                BcaBottomNavItem(
                    modifier = Modifier.weight(1f),
                    title = "Home",
                    selected = currentTab == AppTab.HOME,
                    onClick = { onTabSelected(AppTab.HOME) },
                    icon = { tint -> FlatHomeIcon(tint = tint, size = 22.dp) }
                )

                // Slot 2: Transaksi
                BcaBottomNavItem(
                    modifier = Modifier.weight(1f),
                    title = "Transaksi",
                    selected = currentTab == AppTab.HISTORY,
                    onClick = { onTabSelected(AppTab.HISTORY) },
                    icon = { tint -> FlatTransactionIcon(tint = tint, size = 22.dp) }
                )

                // Center Spacer for elevated protruding button
                Spacer(modifier = Modifier.weight(1.1f))

                // Slot 4: Absen
                BcaBottomNavItem(
                    modifier = Modifier.weight(1f),
                    title = "Absen",
                    selected = currentTab == AppTab.ABSEN,
                    onClick = { onTabSelected(AppTab.ABSEN) },
                    icon = { tint -> FlatAbsenIcon(tint = tint, size = 22.dp) }
                )

                // Slot 5: Akun Saya
                BcaBottomNavItem(
                    modifier = Modifier.weight(1f),
                    title = "Akun Saya",
                    selected = currentTab == AppTab.AKUN_SAYA,
                    onClick = { onTabSelected(AppTab.AKUN_SAYA) },
                    icon = { tint -> FlatProfileIcon(tint = tint, size = 22.dp) }
                )
            }
        }

        // Slot 3 (Center Elevated Protruding Scan Button)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-8).dp)
                .clickable { onTabSelected(AppTab.SCAN) },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF005BAC),
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(3.dp, Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color(0xFF0066B3), Color(0xFF004482))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        FlatScanIcon(
                            tint = Color.White,
                            size = 26.dp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "SCAN",
                    color = if (currentTab == AppTab.SCAN) Color(0xFF005BAC) else Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BcaBottomNavItem(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (tint: Color) -> Unit
) {
    val tintColor = if (selected) Color(0xFF005BAC) else Color(0xFF64748B)
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(tintColor)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = tintColor
        )
    }
}

@Composable
fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Konfirmasi Keluar",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Apakah Anda yakin ingin keluar dari sesi kasir SkyPrivilege?",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Text("Ya, Keluar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. HISTORY TAB CONTENT
// ==========================================
@Composable
fun HistoryTabContent(
    redemptions: List<RedemptionHistoryItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (RedemptionHistoryItem) -> Unit = {}
) {
    // Strictly show today's transactions only (no yesterday history)
    val todayRedemptions = redemptions.filter { it.isToday || it.dateFormatted.contains("20 Sep 2026") }
    val totalApproved = todayRedemptions.count { it.status.equals("approved", ignoreCase = true) }
    val totalDiscount = todayRedemptions.sumOf { it.discountAmount.toLong() }
    val displayedRedemptions = todayRedemptions

    SkyPullRefreshBox(
        refreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        indicatorColor = GrabGreen
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Title Header (Clean without reload button)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Riwayat Pemindaian Tiket",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SlateDark
                    )
                    Text(
                        text = "Daftar klaim diskon boarding pass hari ini",
                        fontSize = 12.sp,
                        color = SlateSubtle
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(2.dp)) }

            // Summary Metric Cards (Total Klaim & Total Diskon Hari Ini)
            item {
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
                            Text("Total Diskon Hari Ini", fontSize = 11.sp, color = SlateSubtle)
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
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Redemption List or Empty State
            if (displayedRedemptions.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📜", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum Ada Riwayat Hari Ini",
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
                }
            } else {
                items(displayedRedemptions) { item ->
                    RedemptionHistoryCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun RedemptionHistoryCard(
    item: RedemptionHistoryItem,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
    onOpenCamera: () -> Unit,
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

        // Camera Scanner Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pemindai Kamera Boarding Pass",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Verifikasi keaslian fisik tiket, barcode IATA BCBP, dan stempel via Vision AI",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dedicated Camera Button
                    Button(
                        onClick = onOpenCamera,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005BAC)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "📷 Buka Kamera & Foto Tiket",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Anti-Fraud Policy Card (Immutable / Read-Only Notice)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🛡️", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Kebijakan Anti-Fraud Vision AI:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Input manual dinonaktifkan demi integritas audit & kepatuhan SOP.\n" +
                                       "• Tiket fisik/digital wajib dipindai langsung melalui sensor kamera.\n" +
                                       "• Vision AI otomatis memverifikasi keabsahan boarding pass & stempel outlet.",
                                fontSize = 10.sp,
                                color = Color(0xFF475569),
                                lineHeight = 14.sp
                            )
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

    SkyPullRefreshBox(
        refreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        indicatorColor = Color(0xFF005BAC)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Title Header (Clean without reload button)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
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
            }

        // GreatDay HR Style Attendance Card
        item {
            AttendanceCard(
                cashierName = "Kasir Terminal 3",
                cashierRole = "Staff Kasir & Operator POS",
                shiftInfo = "Shift Pagi (06:00 - 15:00)",
                todayText = "Sabtu, 19 Sep 2026",
                checkInTime = todayAttendance?.checkInAt?.let { if (it.length >= 16) it.substring(11, 16) else it } ?: todayAttendance?.startTime,
                checkOutTime = todayAttendance?.checkOutAt?.let { if (it.length >= 16) it.substring(11, 16) else it } ?: todayAttendance?.endTime,
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
