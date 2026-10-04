# Mobile KMP Audit Remediation Implementation Plan (Enhanced STAR)

- **Target Repo**: `/Users/ichigo/Documents/repo/helmi/sky_privilege/mobile`
- **Branch**: `feat/mobile-fable-opus-perfection`
- **Auditors**: Claude Fable 5.1 & Claude Opus
- **Status**: ROUND 2 REMEDIATION (CHANGES_REQUESTED by Fable 5.1 & Opus)

---

## 1. Enhanced STAR Contract

### S — Situation & Regulatory Context
Opus and Fable 5.1 dual review returned `CHANGES_REQUESTED` on the initial remediation diff:
1. **UU PDP No. 27/2022 Ps. 16 & 44 Statutory Breaches**:
   - `RedemptionHistoryItem.kt:13` & `VerifyTicketDto.kt:25`: DTOs still accept plaintext `passenger_name` and full `pnr_canonical_hash` from server, persisting PII in memory/logs.
   - `PiiMaskerTest.kt:27`: still retains `"SANTOSO/BUDI MR"` (violating zero-human-name invariant).
   - `PiiMasker.kt`: does not split on `/`, causing ICAO boarding pass names (`SURNAME/FIRST`) to format incorrectly.
   - PNR plaintext leaks in toast `MainAppScreen.kt:892` and `RedemptionRepositoryImpl.kt:223`.
2. **Anti-Fraud & Spoofed Telemetry (Critical)**:
   - `MainAppScreen.kt:647-651,737-750` sends hardcoded mock GPS (`-6.1256, 106.6558`), mock accuracy `15.0`, and fake Wi-Fi BSSID/SSID, bypassing `MultiSignalScorer`.
   - `RedemptionClaim.kt:21`: default `ticketPhoto` hardcodes a 1x1 transparent dummy pixel, creating false evidence if photo capture is omitted.
   - `KeystoreSignerProvider` exists but is unwired (0 callers); requests lack `X-Device-Signature`, `X-Timestamp`, `X-Nonce`.
3. **Architecture & Operational Integrity**:
   - `GuidelineRepositoryImpl.kt:30`: returns `emptyList()` silently on 304 when cache is null (swallowing protocol errors).
   - `KtorClientFactory.kt`: `X-Network-Iface` captured statically instead of dynamic provider; 8s timeout applied globally rather than scoped to redemption.
   - `MainActivity.kt`: `PermissionRow` passes `icon = ""` (empty string), dropping icon visual semantics.
   - Emergency voucher failure branch writes to `attendanceSuccessToast` and dismisses dialog prematurely.

### T — Task & Boundary
- **Allowed Blast Radius**: Strictly within `mobile/composeApp/` (`commonMain`, `commonTest`, `androidMain`).
- **Explicit Non-Goals**: No backend migrations; no remote git push; no activation of facial biometric capture without approved DPIA.

### A — Action & RFC 2119 Invariants
1. **MUST**: `RedemptionHistoryItem` and `VerifyTicketDto` MUST NOT contain `passengerName` or full `pnrCanonicalHash`. They SHALL consume `masked_display_name` and `pnr_masked`.
2. **MUST NOT**: `PiiMaskerTest.kt` MUST NOT contain human names (`SANTOSO`). Token `"PAX_A/PAX_B MR"` SHALL be used.
3. **MUST**: `PiiMasker.maskName` MUST split on both `/` and whitespace to format airline ICAO names (`SURNAME/FIRST`) as `F*** S******`.
4. **MUST**: `RedemptionClaim.ticketPhoto` default MUST be `null` (fail-closed).
5. **MUST**: `MainAppScreen` MUST use live `FusedLocationProvider` and `WifiSignalScanner` telemetry for attendance.
6. **MUST**: `KtorClientFactory` MUST wire `X-Device-Signature`, `X-Timestamp`, `X-Nonce` using `KeystoreSignerProvider`.
7. **MUST**: `KtorClientFactory` MUST accept a dynamic `networkIfaceProvider: () -> String`.
8. **MUST**: `GuidelineRepositoryImpl` MUST throw `IllegalStateException` on 304 when cache is null.
9. **MUST**: `MainActivity` `PermissionRow` MUST render flat vector icons (`FlatGpsPinIcon`, `FlatCameraScanIcon`, `FlatShieldIcon`).
10. **MUST**: Emergency voucher error in `MainAppScreen` MUST display an error toast and keep the dialog open.
11. **MUST**: Zero remote push; commits remain local.

### R — Result Criteria & Definition of Done (DoD)

| DoD ID | Kriteria | Verifikasi / Evidence | Status |
|---|---|---|---|
| **DoD-1** | Baggage prices S=60k, M=75k, L=90k, XL=120k, PREMIUM=+15k, BUBBLE=+10k. | `domain/model/BaggageWrap.kt:9-12,27-28` | ✅ PASSED |
| **DoD-2** | DTO pricing cascade defaults match 7.5M gross / 5M net. | `RedemptionClaim.kt:26-27`, `ClaimDiscountDto.kt:19-20` | ✅ PASSED |
| **DoD-3** | `ShiftDto.authMethodOpened` default adalah `"pin"`. | `data/remote/dto/ShiftDto.kt:13`, `ShiftDtoTest.kt:18` | ✅ PASSED |
| **DoD-4** | Zero placeholder human names in code & tests (grep `SANTOSO` = 0). | `PiiMaskerTest.kt:27` updated to `"PAX_A/PAX_B MR"` | ⏳ IN PROGRESS |
| **DoD-5** | `PiiMasker` handles `/` and whitespace; wired in all dialogs & toasts. | `pii/PiiMasker.kt`, `MainAppScreen.kt:892` | ⏳ IN PROGRESS |
| **DoD-6** | Emergency voucher memanggil `POST /api/v1/emergency_vouchers` + error handling. | `RedemptionRepositoryImpl.kt:201`, `MainAppScreen.kt:884` | ⏳ IN PROGRESS |
| **DoD-7** | Ktor HTTP timeout scoped to redemption (8.000 ms). | `data/remote/KtorClientFactory.kt` | ⏳ IN PROGRESS |
| **DoD-8** | Header `X-Network-Iface` dynamic provider lambda. | `data/remote/KtorClientFactory.kt` | ⏳ IN PROGRESS |
| **DoD-9** | Emoji sistem dibersihkan; `MainActivity` permission rows use FlatIcons. | `MainActivity.kt:166,172`, `FlatIcons.kt` | ⏳ IN PROGRESS |
| **DoD-10**| Guideline ETag 304 fail-closed on null cache. | `data/repository/GuidelineRepositoryImpl.kt:30` | ⏳ IN PROGRESS |
| **DoD-11**| Unit test suite 100% lulus (0 failure). | `rtk ./gradlew testDebugUnitTest` | ⏳ IN PROGRESS |
| **DoD-12**| Receive-side DTO PII purge (`masked_display_name`, no plaintext). | `RedemptionHistoryItem.kt`, `VerifyTicketDto.kt` | ⏳ IN PROGRESS |
| **DoD-13**| Fail-closed photo in `RedemptionClaim` (null default, no dummy pixel). | `RedemptionClaim.kt:21` | ⏳ IN PROGRESS |
| **DoD-14**| Live GPS & Wi-Fi telemetry wired in `MainAppScreen`. | `MainAppScreen.kt:647,737` | ⏳ IN PROGRESS |
| **DoD-15**| StrongBox Keystore request signing headers (`X-Device-Signature`). | `KtorClientFactory.kt`, `KeystoreSignerProvider.kt` | ⏳ IN PROGRESS |

---

---

### Task 1: Baggage Wrap Catalog + Pricing Cascade

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/domain/model/BaggageWrap.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/domain/model/RedemptionClaim.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/data/remote/dto/ClaimDiscountDto.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/data/repository/RedemptionRepositoryImpl.kt` (SubmitRedemptionRequest defaults)
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/domain/repository/RedemptionRepository.kt` (interface defaults)
- Modify: `composeApp/src/commonTest/kotlin/com/skyprivilege/domain/model/BaggageWrapPricingTest.kt`

**Interfaces:**
- Produces: `BaggageSize.{S,M,L,XL}` with updated `.priceRupiah` / `.priceCents`; `WrapType.{STANDARD,PREMIUM,BUBBLE}` with updated `.extraPriceRupiah` / `.extraPriceCents`

- [ ] **Step 1: Update `BaggageSize` enum entries in `BaggageWrap.kt`**

  Replace the four enum entries:
  ```kotlin
  S("S", "S", 60_000L, 6_000_000L),
  M("M", "M", 75_000L, 7_500_000L),
  L("L", "L", 90_000L, 9_000_000L),
  XL("XL", "XL", 120_000L, 12_000_000L);
  ```

- [ ] **Step 2: Update `WrapType` enum — change BUBBLE surcharge, add PREMIUM**

  Replace the WrapType enum body:
  ```kotlin
  enum class WrapType(
      val code: String,
      val label: String,
      val extraPriceRupiah: Long,
      val extraPriceCents: Long
  ) {
      STANDARD("standard", "Standard", 0L, 0L),
      PREMIUM("premium", "Premium", 15_000L, 1_500_000L),
      BUBBLE("bubble", "Bubble Wrap", 10_000L, 1_000_000L);

      companion object {
          fun fromCode(code: String): WrapType =
              entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: STANDARD
      }
  }
  ```

- [ ] **Step 3: Update `RedemptionClaim` default prices**

  In `RedemptionClaim.kt`, change:
  ```kotlin
  @SerialName("gross_amount_cents") val grossAmountCents: Long = 7_500_000L,
  @SerialName("net_amount_cents") val netAmountCents: Long = 5_000_000L
  ```

- [ ] **Step 4: Update `ClaimDiscountRequest` defaults**

  In `ClaimDiscountDto.kt`:
  ```kotlin
  @SerialName("gross_amount_cents") val grossAmountCents: Long = 7_500_000L,
  @SerialName("net_amount_cents") val netAmountCents: Long = 5_000_000L
  ```

- [ ] **Step 5: Update `SubmitRedemptionRequest` defaults**

  In `RedemptionRepositoryImpl.kt`, the inner `SubmitRedemptionRequest` data class:
  ```kotlin
  @SerialName("gross_amount_cents") val grossAmountCents: Long = 7_500_000L,
  @SerialName("net_amount_cents") val netAmountCents: Long = 5_000_000L
  ```

- [ ] **Step 6: Update `RedemptionRepository` interface defaults**

  In `RedemptionRepository.kt`:
  ```kotlin
  grossAmountCents: Long = 7_500_000L,
  netAmountCents: Long = 5_000_000L
  ```

- [ ] **Step 7: Update `BaggageWrapPricingTest.kt` — all assertions**

  **`testBaggageSizePricing`:**
  ```kotlin
  assertEquals(60_000L, BaggageSize.S.priceRupiah)
  assertEquals(6_000_000L, BaggageSize.S.priceCents)
  assertEquals(75_000L, BaggageSize.M.priceRupiah)
  assertEquals(7_500_000L, BaggageSize.M.priceCents)
  assertEquals(90_000L, BaggageSize.L.priceRupiah)
  assertEquals(9_000_000L, BaggageSize.L.priceCents)
  assertEquals(120_000L, BaggageSize.XL.priceRupiah)
  assertEquals(12_000_000L, BaggageSize.XL.priceCents)
  ```

  **`testWrapTypePricing`** — update BUBBLE, add PREMIUM assertion:
  ```kotlin
  assertEquals(0L, WrapType.STANDARD.extraPriceRupiah)
  assertEquals(0L, WrapType.STANDARD.extraPriceCents)
  assertEquals(15_000L, WrapType.PREMIUM.extraPriceRupiah)
  assertEquals(1_500_000L, WrapType.PREMIUM.extraPriceCents)
  assertEquals(10_000L, WrapType.BUBBLE.extraPriceRupiah)
  assertEquals(1_000_000L, WrapType.BUBBLE.extraPriceCents)
  ```

  **`testBaggagePricingCalculatorDefaultMStandard`** — M(75k)+STANDARD(0)=7_500_000; net=5_000_000:
  ```kotlin
  assertEquals(7_500_000L, grossCents)
  assertEquals(5_000_000L, netCents)
  ```

  **`testBaggagePricingCalculatorSlide21ScenarioLargeStandard`** — L(90k)+STANDARD=9_000_000; Rupiah net=65_000; Cents net=6_500_000:
  ```kotlin
  assertEquals(90_000L, grossRupiah)
  assertEquals(9_000_000L, grossCents)
  assertEquals(65_000L, netRupiah)
  assertEquals(6_500_000L, netCents)
  ```
  Update comment: `// L(90.000) + Standard(0) = 90.000 gross, Net after 25.000 = 65.000`

  **`testBaggagePricingCalculatorMediumBubbleWrap`** — M(75k)+Bubble(10k)=8_500_000; net=6_000_000:
  ```kotlin
  assertEquals(8_500_000L, grossCents)
  assertEquals(6_000_000L, netCents)
  ```
  Update comment: `// Medium (75.000) + Bubble Wrap (10.000) = 85.000 gross, Net after 25.000 = 60.000`

  **`testBaggagePricingCalculatorExtraLargeBubbleWrap`** — XL(120k)+Bubble(10k)=13_000_000; net=10_500_000:
  ```kotlin
  assertEquals(13_000_000L, grossCents)
  assertEquals(10_500_000L, netCents)
  ```
  Update comment: `// XL (120.000) + Bubble (10.000) = 130.000 gross, Net after 25.000 = 105.000`

  **`testBaggagePricingCalculatorSmallStandard`** — S(60k)+Standard=6_000_000; net=3_500_000:
  ```kotlin
  assertEquals(6_000_000L, grossCents)
  assertEquals(3_500_000L, netCents)
  ```
  Update comment: `// S (60.000) + Standard (0) = 60.000 gross, Net after 25.000 = 35.000`

  **`testClaimDiscountRequestDefaultBaggageWrapSerialization`** — default gross=7_500_000, net=5_000_000:
  ```kotlin
  assertEquals(7_500_000L, request.grossAmountCents)
  assertEquals(5_000_000L, request.netAmountCents)
  assertTrue(encoded.contains("\"gross_amount_cents\":7500000"))
  assertTrue(encoded.contains("\"net_amount_cents\":5000000"))
  assertEquals(7_500_000L, decoded.grossAmountCents)
  assertEquals(5_000_000L, decoded.netAmountCents)
  ```

  **`testRedemptionClaimDefaultAndCustomBaggageWrapValues`** — default gross=7_500_000, net=5_000_000; custom L+bubble=10_000_000 gross, 7_500_000 net:
  ```kotlin
  // Default:
  assertEquals(7_500_000L, defaultClaim.grossAmountCents)
  assertEquals(5_000_000L, defaultClaim.netAmountCents)
  // Custom (L=90k + bubble=10k = 100k = 10_000_000; net = 10_000_000 - 2_500_000 = 7_500_000):
  val customClaim = defaultClaim.copy(
      bagSize = "L",
      wrapType = "bubble",
      paymentMethod = "cash",
      grossAmountCents = 10_000_000L,
      netAmountCents = 7_500_000L
  )
  assertEquals(10_000_000L, customClaim.grossAmountCents)
  assertEquals(7_500_000L, customClaim.netAmountCents)
  ```

- [ ] **Step 8: Run pricing tests**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest --tests "com.skyprivilege.domain.model.BaggageWrapPricingTest"
  ```
  Expected: all 7 tests pass.

- [ ] **Step 9: Commit**

  ```bash
  git add composeApp/src/commonMain/kotlin/com/skyprivilege/domain/model/BaggageWrap.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/domain/model/RedemptionClaim.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/data/remote/dto/ClaimDiscountDto.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/data/repository/RedemptionRepositoryImpl.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/domain/repository/RedemptionRepository.kt \
          composeApp/src/commonTest/kotlin/com/skyprivilege/domain/model/BaggageWrapPricingTest.kt
  git commit -m "fix(mobile): update baggage wrap prices (60/75/90/120k) and add PREMIUM wrap type"
  ```

---

### Task 2: Biometric Default = "pin"

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/data/remote/dto/ShiftDto.kt`
- Modify: `composeApp/src/commonTest/kotlin/com/skyprivilege/domain/model/ShiftDtoTest.kt`

- [ ] **Step 1: Write the failing test**

  Add to `ShiftDtoTest.kt` inside `class ShiftDtoTest`:
  ```kotlin
  @Test
  fun testOpenShiftRequestDefaultAuthMethodIsPin() {
      val request = OpenShiftRequest(cashierId = 1L, outletId = 2L)
      assertEquals("pin", request.authMethodOpened)
  }
  ```

- [ ] **Step 2: Run test to confirm it fails**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest --tests "com.skyprivilege.domain.model.ShiftDtoTest.testOpenShiftRequestDefaultAuthMethodIsPin"
  ```
  Expected: FAIL — `expected:<pin> but was:<face>`.

- [ ] **Step 3: Fix the default in `OpenShiftRequest`**

  In `ShiftDto.kt`, line 13:
  ```kotlin
  @SerialName("auth_method_opened") val authMethodOpened: String = "pin"
  ```

- [ ] **Step 4: Run all shift tests**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest --tests "com.skyprivilege.domain.model.ShiftDtoTest"
  ```
  Expected: all tests pass.

- [ ] **Step 5: Commit**

  ```bash
  git add composeApp/src/commonMain/kotlin/com/skyprivilege/data/remote/dto/ShiftDto.kt \
          composeApp/src/commonTest/kotlin/com/skyprivilege/domain/model/ShiftDtoTest.kt
  git commit -m "fix(mobile): change OpenShiftRequest authMethodOpened default from face to pin"
  ```

---

### Task 3: PDP Placeholder Purge

**Files:**
- Modify: `composeApp/src/commonTest/kotlin/com/skyprivilege/ui/TicketPhotoTest.kt`
- Modify: `composeApp/src/commonTest/kotlin/com/skyprivilege/e2e/E2EJourneysTest.kt`

**Note:** `RedemptionDetailDialog.kt` already uses `item.passengerName.ifBlank { "-" }` with no hardcoded human names. No production file change needed for Task 3.

- [ ] **Step 1: Replace `LESTARI/CITRA MS` in `TicketPhotoTest.kt`**

  The string `"LESTARI/CITRA MS"` appears 3 times (in JSON strings and in assertEquals). Replace all three occurrences with `"PAX_SAMPLE_1"`.

- [ ] **Step 2: Replace `PRATAMA/BUDI` in `E2EJourneysTest.kt`**

  In `sampleTicket` (line 47):
  ```kotlin
  passengerName = "PAX_A",
  ```

- [ ] **Step 3: Run affected tests**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest --tests "com.skyprivilege.ui.TicketPhotoTest"
  rtk ./gradlew :composeApp:testDebugUnitTest --tests "com.skyprivilege.e2e.E2EJourneysTest"
  ```
  Expected: all pass.

- [ ] **Step 4: Commit**

  ```bash
  git add composeApp/src/commonTest/kotlin/com/skyprivilege/ui/TicketPhotoTest.kt \
          composeApp/src/commonTest/kotlin/com/skyprivilege/e2e/E2EJourneysTest.kt
  git commit -m "fix(mobile): purge human-name PDP placeholders from tests (PAX_SAMPLE_1, PAX_A)"
  ```

---

### Task 4: PiiMasker — Create, Wire, Test

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/skyprivilege/pii/PiiMasker.kt`
- Create: `composeApp/src/commonTest/kotlin/com/skyprivilege/pii/PiiMaskerTest.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/ui/RedemptionDetailDialog.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/ui/TicketVerificationResultDialogs.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/ui/MainAppScreen.kt`

**Interfaces:**
- Produces:
  ```kotlin
  object PiiMasker {
      fun maskName(name: String?): String   // "DOE/JOHN MR" → "D******* M*"
      fun maskPnr(pnr: String?): String    // "ABCDEF" → "AB***F"
  }
  ```

- [ ] **Step 1: Write failing tests in `PiiMaskerTest.kt`**

  Create the file:
  ```kotlin
  package com.skyprivilege.pii

  import kotlin.test.Test
  import kotlin.test.assertEquals

  class PiiMaskerTest {

      @Test
      fun maskName_null_returnsDash() = assertEquals("-", PiiMasker.maskName(null))

      @Test
      fun maskName_blank_returnsDash() = assertEquals("-", PiiMasker.maskName("   "))

      @Test
      fun maskName_dash_returnsDash() = assertEquals("-", PiiMasker.maskName("-"))

      @Test
      fun maskName_singleChar_returnsAsIs() = assertEquals("J", PiiMasker.maskName("J"))

      @Test
      fun maskName_multiWord_masksEachPart() {
          // split on whitespace: ["DOE/JOHN", "MR"]
          assertEquals("D******* M*", PiiMasker.maskName("DOE/JOHN MR"))
      }

      @Test
      fun maskName_fullIataName_masksCorrectly() {
          // ["SANTOSO/BUDI", "MR"] -> first+stars per token
          assertEquals("S*********** M*", PiiMasker.maskName("SANTOSO/BUDI MR"))
      }

      @Test
      fun maskPnr_null_returnsDash() = assertEquals("-", PiiMasker.maskPnr(null))

      @Test
      fun maskPnr_blank_returnsDash() = assertEquals("-", PiiMasker.maskPnr(""))

      @Test
      fun maskPnr_short_returnsAsIs() = assertEquals("AB", PiiMasker.maskPnr("AB"))

      @Test
      fun maskPnr_normal_masksMiddle() {
          // "ABCDEF" (len=6): take(2)="AB", middle stars = "*".repeat(6-3=3)="***", takeLast(1)="F"
          assertEquals("AB***F", PiiMasker.maskPnr("ABCDEF"))
      }
  }
  ```

- [ ] **Step 2: Run tests to confirm they fail**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest --tests "com.skyprivilege.pii.PiiMaskerTest"
  ```
  Expected: compilation error — `PiiMasker` not found.

- [ ] **Step 3: Create `PiiMasker.kt`**

  ```kotlin
  package com.skyprivilege.pii

  object PiiMasker {
      fun maskName(name: String?): String {
          if (name.isNullOrBlank() || name.trim() == "-") return "-"
          val parts = name.trim().split(Regex("\\s+"))
          return parts.joinToString(" ") { part ->
              if (part.length <= 1) part
              else part.first() + "*".repeat(part.length - 1)
          }
      }

      fun maskPnr(pnr: String?): String {
          if (pnr.isNullOrBlank()) return "-"
          val clean = pnr.trim()
          return if (clean.length <= 3) clean
          else clean.take(2) + "*".repeat((clean.length - 3).coerceAtLeast(1)) + clean.takeLast(1)
      }
  }
  ```

- [ ] **Step 4: Run tests to confirm they pass**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest --tests "com.skyprivilege.pii.PiiMaskerTest"
  ```
  Expected: all 10 tests pass.

- [ ] **Step 5: Wire `PiiMasker.maskName` in `RedemptionDetailDialog.kt`**

  Add import:
  ```kotlin
  import com.skyprivilege.pii.PiiMasker
  ```

  Find the passenger name display (currently `item.passengerName.ifBlank { "-" }`):
  ```kotlin
  text = PiiMasker.maskName(item.passengerName),
  ```

- [ ] **Step 6: Wire `PiiMasker.maskName` in `TicketVerificationResultDialogs.kt`**

  Add import:
  ```kotlin
  import com.skyprivilege.pii.PiiMasker
  ```
  Find any `passengerName` or `ticket.passengerName` text display and wrap with `PiiMasker.maskName(...)`.

- [ ] **Step 7: Wire `PiiMasker.maskName` in `MainAppScreen.kt`**

  Add import:
  ```kotlin
  import com.skyprivilege.pii.PiiMasker
  ```
  Find any `passengerName` text display in scan result rendering and wrap with `PiiMasker.maskName(...)`.

- [ ] **Step 8: Run full test suite**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest
  ```
  Expected: 0 failures.

- [ ] **Step 9: Commit**

  ```bash
  git add composeApp/src/commonMain/kotlin/com/skyprivilege/pii/PiiMasker.kt \
          composeApp/src/commonTest/kotlin/com/skyprivilege/pii/PiiMaskerTest.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/ui/RedemptionDetailDialog.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/ui/TicketVerificationResultDialogs.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/ui/MainAppScreen.kt
  git commit -m "feat(mobile): add PiiMasker and apply maskName to all passenger name display sites"
  ```

---

### Task 5: Emergency Voucher via Backend POST

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/domain/repository/RedemptionRepository.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/data/repository/RedemptionRepositoryImpl.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/ui/MainAppScreen.kt`

**Interfaces:**
- Produces:
  ```kotlin
  suspend fun issueEmergencyVoucher(
      serialNumber: String, pnr: String, supervisorPin: String,
      reason: String, cashierId: Long, outletId: Long
  ): Result<String>
  ```

- [ ] **Step 1: Add `issueEmergencyVoucher` to `RedemptionRepository` interface**

  ```kotlin
  suspend fun issueEmergencyVoucher(
      serialNumber: String,
      pnr: String,
      supervisorPin: String,
      reason: String,
      cashierId: Long,
      outletId: Long
  ): Result<String>
  ```

- [ ] **Step 2: Add request/response DTOs in `RedemptionRepositoryImpl.kt`**

  Before the `RedemptionRepositoryImpl` class definition, add:
  ```kotlin
  @Serializable
  data class EmergencyVoucherRequest(
      @SerialName("serial_number") val serialNumber: String,
      val pnr: String,
      @SerialName("supervisor_pin") val supervisorPin: String,
      val reason: String,
      @SerialName("cashier_id") val cashierId: Long,
      @SerialName("outlet_id") val outletId: Long
  )

  @Serializable
  data class EmergencyVoucherResponse(
      val success: Boolean,
      val message: String? = null,
      val error: String? = null
  )
  ```

- [ ] **Step 3: Implement `issueEmergencyVoucher` in `RedemptionRepositoryImpl`**

  ```kotlin
  override suspend fun issueEmergencyVoucher(
      serialNumber: String,
      pnr: String,
      supervisorPin: String,
      reason: String,
      cashierId: Long,
      outletId: Long
  ): Result<String> {
      return runCatching {
          val response = httpClient.post("/api/v1/emergency_vouchers") {
              contentType(ContentType.Application.Json)
              setBody(EmergencyVoucherRequest(
                  serialNumber = serialNumber,
                  pnr = pnr,
                  supervisorPin = supervisorPin,
                  reason = reason,
                  cashierId = cashierId,
                  outletId = outletId
              ))
          }.body<EmergencyVoucherResponse>()

          if (response.success) {
              response.message ?: "Voucher Darurat #$serialNumber berhasil diterbitkan untuk PNR $pnr"
          } else {
              throw IllegalStateException(response.error ?: "Penerbitan voucher darurat gagal")
          }
      }
  }
  ```

- [ ] **Step 4: Wire up in `MainAppScreen.kt` (around line 879)**

  The current block (local simulation):
  ```kotlin
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
  ```

  Replace with (locate `cashierId` and `outletId` in the enclosing `MainAppScreen` composable scope — they are the same Long state vars used for shift operations):
  ```kotlin
  onSubmit = { serial, pnr, pin, rsn ->
      isEmergencySubmitting = true
      coroutineScope.launch {
          redemptionRepo.issueEmergencyVoucher(
              serialNumber = serial,
              pnr = pnr,
              supervisorPin = pin,
              reason = rsn,
              cashierId = cashierId,
              outletId = outletId
          ).onSuccess { msg ->
              isEmergencySubmitting = false
              showEmergencyDialog = false
              attendanceSuccessToast = msg
          }.onFailure {
              isEmergencySubmitting = false
              showEmergencyDialog = false
              attendanceSuccessToast = "Koneksi ke backend gagal. Coba lagi."
          }
      }
  }
  ```

- [ ] **Step 5: Run full test suite**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest
  ```
  Expected: 0 failures.

- [ ] **Step 6: Commit**

  ```bash
  git add composeApp/src/commonMain/kotlin/com/skyprivilege/domain/repository/RedemptionRepository.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/data/repository/RedemptionRepositoryImpl.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/ui/MainAppScreen.kt
  git commit -m "fix(mobile): replace emergency voucher local simulation with POST /api/v1/emergency_vouchers"
  ```

---

### Task 6: Strict 8s Online Timeout

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/data/remote/KtorClientFactory.kt`

- [ ] **Step 1: Update all timeouts to 8s**

  Replace the `HttpTimeout` block:
  ```kotlin
  install(HttpTimeout) {
      requestTimeoutMillis = 8_000L
      connectTimeoutMillis = 8_000L
      socketTimeoutMillis = 8_000L
  }
  ```

- [ ] **Step 2: Run full test suite**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest
  ```
  Expected: 0 failures.

- [ ] **Step 3: Commit**

  ```bash
  git add composeApp/src/commonMain/kotlin/com/skyprivilege/data/remote/KtorClientFactory.kt
  git commit -m "fix(mobile): set ktor http timeout to 8s strict online (request/connect/socket)"
  ```

---

### Task 7: Dual-SIM `network_iface` Telemetry Header

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/data/remote/KtorClientFactory.kt`

- [ ] **Step 1: Add `networkIface` parameter and header**

  Update function signature (valid values: `"wifi" | "cellular_sim1" | "cellular_sim2" | "none"`):
  ```kotlin
  fun createHttpClient(
      baseUrl: String = "http://10.0.2.2:3001",
      deviceId: String = "DEV-TABLET-001",
      outletId: Long = 2L,
      authTokenProvider: (() -> String?)? = null,
      // wifi | cellular_sim1 | cellular_sim2 | none
      networkIface: String = "wifi"
  ): HttpClient {
  ```

  In `defaultRequest` block, add:
  ```kotlin
  header("X-Network-Iface", networkIface)
  ```

- [ ] **Step 2: Run full test suite**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest
  ```
  Expected: 0 failures.

- [ ] **Step 3: Commit**

  ```bash
  git add composeApp/src/commonMain/kotlin/com/skyprivilege/data/remote/KtorClientFactory.kt
  git commit -m "feat(mobile): add X-Network-Iface telemetry header (wifi|cellular_sim1|cellular_sim2|none)"
  ```

---

### Task 8: Emoji Removal — Flat Vector Icons

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/ui/components/FlatIcons.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/ui/MainAppScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/ui/TicketCameraDialog.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/ui/RedemptionDetailDialog.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/ui/TicketVerificationResultDialogs.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/ui/GpsAttendanceDialog.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/ui/AttendanceCard.kt`
- Modify: `composeApp/src/androidMain/kotlin/com/skyprivilege/MainActivity.kt`

**Existing icons available in `FlatIcons.kt`:**
- `FlatCameraScanIcon` — replaces 📷
- `FlatGpsPinIcon` — replaces 📍
- No shield icon exists → add `FlatShieldIcon` for 🛡️
- No lab icon → drop 🧪 from button text (keep plain text)

- [ ] **Step 1: Add `SHIELD` to `FlatIconType` enum and create `FlatShieldIcon` composable**

  Add `SHIELD` to the enum:
  ```kotlin
  enum class FlatIconType {
      HOME, TRANSACTION, SCAN, ABSEN, PROFILE,
      CAMERA_SCAN, GPS_PIN, EDIT_NOTE, LOCK_SHIFT,
      CHECKLIST, VOUCHER_TICKET, HISTORY_CLOCK, BOOK_SOP, GARUDA_LOGO,
      SHIELD
  }
  ```

  Add the composable after `FlatGpsPinIcon`:
  ```kotlin
  @Composable
  fun FlatShieldIcon(
      modifier: Modifier = Modifier,
      tint: Color = Color(0xFF005BAC),
      size: Dp = 24.dp
  ) {
      Canvas(modifier = modifier.size(size)) {
          val w = this.size.width
          val h = this.size.height
          val shieldPath = Path().apply {
              moveTo(w * 0.50f, h * 0.05f)
              lineTo(w * 0.90f, h * 0.22f)
              cubicTo(w * 0.90f, h * 0.55f, w * 0.72f, h * 0.82f, w * 0.50f, h * 0.95f)
              cubicTo(w * 0.28f, h * 0.82f, w * 0.10f, h * 0.55f, w * 0.10f, h * 0.22f)
              close()
          }
          drawPath(shieldPath, color = tint, style = Fill)
          val checkPath = Path().apply {
              moveTo(w * 0.33f, h * 0.52f)
              lineTo(w * 0.46f, h * 0.65f)
              lineTo(w * 0.67f, h * 0.42f)
          }
          drawPath(
              checkPath,
              color = Color.White,
              style = Stroke(width = h * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round)
          )
      }
  }
  ```

- [ ] **Step 2: Replace emojis in `MainAppScreen.kt`**

  At line ~1968 — replace `text = "📷 Buka Kamera & Foto Tiket"` with a `Row`:
  ```kotlin
  Row(verticalAlignment = Alignment.CenterVertically) {
      FlatCameraScanIcon(tint = Color.White, size = 16.dp)
      Spacer(modifier = Modifier.width(6.dp))
      Text("Buka Kamera & Foto Tiket", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
  }
  ```

  At line ~1986 — replace `Text("🛡️", fontSize = 12.sp)` with:
  ```kotlin
  FlatShieldIcon(tint = Color(0xFF334155), size = 14.dp)
  ```

  Add imports:
  ```kotlin
  import com.skyprivilege.ui.components.FlatCameraScanIcon
  import com.skyprivilege.ui.components.FlatShieldIcon
  ```

- [ ] **Step 3: Replace emojis in `TicketCameraDialog.kt`**

  Line ~214 — drop `📍 ` prefix from the GPS geofence status string:
  `"GPS Geofence: Aktif (±15m) • Wi-Fi: SkyPrivilege_Staff"`

  Line ~263 — replace `"🧪 Isi Sampel Demo (Garuda GA410)"` with `"Isi Sampel Demo (Garuda GA410)"`.

  Line ~329 — replace `"📷 Ambil Foto Boarding Pass"` with a `Row`:
  ```kotlin
  Row(verticalAlignment = Alignment.CenterVertically) {
      FlatCameraScanIcon(tint = Color.White, size = 16.dp)
      Spacer(modifier = Modifier.width(6.dp))
      Text("Ambil Foto Boarding Pass", ...)
  }
  ```

  Add import:
  ```kotlin
  import com.skyprivilege.ui.components.FlatCameraScanIcon
  ```

- [ ] **Step 4: Replace 📷 in `RedemptionDetailDialog.kt` (line 161)**

  The badge currently shows `text = "📷 FOTO FISIK TIKET (GEOFENCE AUDITED)"`.
  Replace the whole `Text(...)` with a `Row`:
  ```kotlin
  Row(verticalAlignment = Alignment.CenterVertically) {
      FlatCameraScanIcon(tint = Color(0xFF38BDF8), size = 10.dp)
      Spacer(modifier = Modifier.width(4.dp))
      Text("FOTO FISIK TIKET (GEOFENCE AUDITED)", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
  }
  ```

  Add import:
  ```kotlin
  import com.skyprivilege.ui.components.FlatCameraScanIcon
  ```

- [ ] **Step 5: Replace 📍 in `TicketVerificationResultDialogs.kt` (line 143)**

  Replace:
  ```kotlin
  Text("📍 Lokasi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7F1D1D), modifier = Modifier.width(85.dp))
  ```
  With:
  ```kotlin
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(85.dp)) {
      FlatGpsPinIcon(tint = Color(0xFF7F1D1D), size = 12.dp)
      Spacer(modifier = Modifier.width(3.dp))
      Text("Lokasi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7F1D1D))
  }
  ```

  Add import:
  ```kotlin
  import com.skyprivilege.ui.components.FlatGpsPinIcon
  ```

- [ ] **Step 6: Drop 📍 prefix strings in `GpsAttendanceDialog.kt` and `AttendanceCard.kt`**

  `GpsAttendanceDialog.kt` line ~226: change `"📍 CGK Terminal 3 Gate 13 ($latitude, $longitude)"` to `"CGK Terminal 3 Gate 13 ($latitude, $longitude)"`.

  `AttendanceCard.kt` line ~241: change `"📍 Shift Selesai"` → `"Shift Selesai"` and `"📍 Record Time"` → `"Record Time"`.

- [ ] **Step 7: Replace emojis in `MainActivity.kt` (androidMain)**

  Line ~135: `Text("🛡️", fontSize = 36.sp)` → `FlatShieldIcon(tint = Color.White, size = 36.dp)`.

  Add import:
  ```kotlin
  import com.skyprivilege.ui.components.FlatShieldIcon
  ```

  Lines ~165 and ~171: `PermissionRow` is called with `icon = "📍"` and `icon = "📷"`. Read the `PermissionRow` composable definition in `MainActivity.kt`. If it does `Text(icon)`, simply change the string to `""` or remove the icon slot. If the composable accepts a `@Composable () -> Unit` icon slot already, pass `{ FlatGpsPinIcon(...) }` and `{ FlatCameraScanIcon(...) }` respectively. Use the least-invasive approach.

- [ ] **Step 8: Run full test suite**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest
  ```
  Expected: 0 failures.

- [ ] **Step 9: Commit**

  ```bash
  git add composeApp/src/commonMain/kotlin/com/skyprivilege/ui/components/FlatIcons.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/ui/MainAppScreen.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/ui/TicketCameraDialog.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/ui/RedemptionDetailDialog.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/ui/TicketVerificationResultDialogs.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/ui/GpsAttendanceDialog.kt \
          composeApp/src/commonMain/kotlin/com/skyprivilege/ui/AttendanceCard.kt \
          composeApp/src/androidMain/kotlin/com/skyprivilege/MainActivity.kt
  git commit -m "fix(mobile): replace system emojis with FlatShieldIcon, FlatCameraScanIcon, FlatGpsPinIcon"
  ```

---

### Task 9: Guideline ETag / If-None-Match 304 Caching

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/skyprivilege/data/repository/GuidelineRepositoryImpl.kt`

- [ ] **Step 1: Add in-memory ETag + cache fields, rewrite `getGuidelines`**

  The current `getGuidelines` does `httpClient.get("/api/v1/guidelines").body<GuidelinesResponse>()` in one chain. Replace the class body:

  ```kotlin
  class GuidelineRepositoryImpl(
      private val httpClient: HttpClient
  ) : GuidelineRepository {

      private var cachedETag: String? = null
      private var cachedGuidelines: List<TicketGuideline>? = null

      override suspend fun getGuidelines(): Result<List<TicketGuideline>> {
          return runCatching {
              val response = httpClient.get("/api/v1/guidelines") {
                  cachedETag?.let { etag ->
                      header(io.ktor.http.HttpHeaders.IfNoneMatch, etag)
                  }
              }

              if (response.status == io.ktor.http.HttpStatusCode.NotModified) {
                  return@runCatching cachedGuidelines
                      ?: throw IllegalStateException("304 received with no cached guidelines")
              }

              val etag = response.headers[io.ktor.http.HttpHeaders.ETag]
              val body = response.body<GuidelinesResponse>()
              val guidelines = body.data.ifEmpty { body.guidelines }
              cachedETag = etag
              cachedGuidelines = guidelines
              guidelines
          }
      }

      override suspend fun submitAcknowledgment(
          request: AuthenticityAcknowledgmentDto
      ): Result<AuthenticityAcknowledgmentResponse> {
          return runCatching {
              httpClient.post("/api/v1/acknowledgments") {
                  setBody(request)
              }.body<AuthenticityAcknowledgmentResponse>()
          }
      }
  }
  ```

  Add imports:
  ```kotlin
  import io.ktor.client.request.header
  import io.ktor.client.statement.body
  ```

- [ ] **Step 2: Run full test suite**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest
  ```
  Expected: 0 failures.

- [ ] **Step 3: Commit**

  ```bash
  git add composeApp/src/commonMain/kotlin/com/skyprivilege/data/repository/GuidelineRepositoryImpl.kt
  git commit -m "feat(mobile): add If-None-Match ETag caching to GuidelineRepositoryImpl (304 short-circuit)"
  ```

---

### Task 10: Final Verification

- [ ] **Step 1: Run full test suite**

  ```
  rtk ./gradlew :composeApp:testDebugUnitTest
  ```
  Expected: 0 failures.

- [ ] **Step 2: Build check**

  ```
  rtk ./gradlew :composeApp:assemble
  ```
  Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Lint**

  ```
  rtk ./gradlew :composeApp:lint
  ```
  Fix any errors; log warnings.

- [ ] **Step 4: Confirm no human-name placeholders remain**

  ```
  rtk grep -rn "SANTOSO\|PRATAMA\|BUDI\|LESTARI\|CITRA" composeApp/src/
  ```
  Expected: 0 results.

- [ ] **Step 5: Confirm no system emojis remain in commonMain**

  ```
  rtk grep -rn "🛡️\|📍\|📷\|🧪" composeApp/src/commonMain/
  ```
  Expected: 0 results.
