# DOD_MASTER — SkyPrivilege Mobile (KMP) Definition of Done Registry

- **Owner:** Fable 5.1 (paired with `test-engineer` + Opus review)
- **Last updated:** 2026-10-04
- **Scope:** Mobile-owned plans registered in [`docs/plans/PLAN_MASTER.md`](../plans/PLAN_MASTER.md) §2.1, plus mobile mechanical gates
- **Header contract (mandatory in every `DOD_<slug>.md`):** `DoD ID | Criteria | Evidence (file:line) | Status`
- **Status enum:** `PASSED | FAILED | PARTIAL | NOT VERIFIED | N/A`
- **Backend DoD (cross-repo, pinned):** <https://github.com/helmiatwork/sky_privilege/blob/fbb0780/docs/dod/DOD_MASTER.md>

Paths relative to `composeApp/src/commonMain/kotlin/com/skyprivilege/` unless prefixed `androidMain/` or `commonTest/`. Evidence verified at mobile `a81fec4` unless stated.

## 1. Completion Overview

| Wave | Plan | DoD File | DoD IDs | Passed | Partial | Not Verified | Completion |
|---|---|---|---|---|---|---|---|
| Mobile Audit Remediation | `STAR_MOBILE_AUDIT_REMEDIATION.md` | inline §R (DoD-1 … DoD-15) | MB-01 … MB-12 | 11 | 0 | 1 | 92 % |
| Mechanical gates | — | this file §3 | G-M1 … G-M4 | 4 | 0 | 0 | 100 % |

Backend-owned waves (B1 … B4 in `PLAN_MASTER.md` §2.2) carry their DoD evidence in the backend registry (RFC-0001 §2.2). Evidence for files in this repo that a backend plan touched is recorded here, not in the owning plan; none exists today.

## 2. Mobile KMP — Audit Remediation (`34a2398`)

| DoD ID | Criteria | Evidence (file:line) | Status |
|---|---|---|---|
| MB-01 | Telemetry fields nullable; no hardcoded GPS/BSSID | `data/remote/dto/VerifyTicketDto.kt:15-19`; `data/repository/TicketRepositoryImpl.kt:72-76` | PASSED |
| MB-02 | DTOs expose only `pnr_masked`, `masked_display_name`, `pnr_hash` | `VerifyTicketDto.kt:25-26,41`; `ClaimDiscountDto.kt:10` | PASSED |
| MB-03 | Redemption and emergency voucher send `pnr_canonical_hash` only | `data/repository/RedemptionRepositoryImpl.kt:27,63` | PASSED |
| MB-04 | Canonical hash = `sha256Hex(pnr.trim().uppercase())` | `RedemptionRepositoryImpl.kt:216`; `security/Sha256.kt:3`; `androidMain/.../security/Sha256.kt:5-8` | PASSED |
| MB-05 | `KeystoreSignerProvider` built on `Dispatchers.Default` | `androidMain/.../MainActivity.kt:129` | PASSED |
| MB-06 | Request signing off main thread | `data/remote/KtorClientFactory.kt:66` | PASSED |
| MB-07 | UI gated until keystore ready | `androidMain/.../MainActivity.kt:125,134` | PASSED |
| MB-08 | `X-Timestamp`, `X-Nonce`, `X-Device-Signature` on every request; `X-Device-Token` absent | `KtorClientFactory.kt:57-72`; `commonTest/.../KtorClientFactoryTest.kt:23` | PASSED |
| MB-09 | HTTP 409 → `NonStackingConflictException` | `RedemptionRepositoryImpl.kt:168-174` | PASSED |
| MB-10 | Typed `TicketVerificationException` carries reason code and prior redemption | `TicketRepositoryImpl.kt:53-59` | PASSED |
| MB-11 | Zero `SANTOSO` in mobile code and tests | `commonTest/.../PiiMaskerTest.kt` (`PAX_A/PAX_B MR`) | PASSED |
| MB-12 | Emergency voucher hash covered by unit tests | `commonTest/.../RedemptionRepositoryTest.kt` (10 `@Test`), commit `34a2398` | NOT VERIFIED (not re-run) |

## 3. Mechanical Gates

| Gate ID | Gate | Command | Reported Result | Verified | Status |
|---|---|---|---|---|---|
| G-M1 | Unit tests | `rtk ./gradlew :composeApp:testDebugUnitTest` | 104/104 passing | Yes (reconciled: 104 tests across 20 XML test classes) | PASSED |
| G-M2 | Static analysis | `rtk ./gradlew detekt` | 0 issues | detekt 1.23.7 wired in `build.gradle.kts`, `composeApp/build.gradle.kts`, `gradle/libs.versions.toml`, rules in `config/detekt/`; BUILD SUCCESSFUL, 0 issues. Wiring is in the working tree, not yet committed at `fe0478e`. | PASSED |
| G-M3 | Zero plaintext PII grep | `rg -n "passengerName|\"pnr\"" composeApp/src/commonMain` | 0 hits | Yes (DTO sweep) | PASSED |
| G-M4 | `SANTOSO` grep | `rg -n SANTOSO composeApp` | 0 | Yes | PASSED |

## 4. Static Security Gates

| Gate | Criteria | Evidence | Status |
|---|---|---|---|
| SEC-M1 | Requests ECDSA-signed; no static device token | `KtorClientFactory.kt:57-72`; `KtorClientFactoryTest.kt:23` | PASSED |
| SEC-M2 | Signer initialised off main thread and gated before first request | `MainActivity.kt:129,134` | PASSED |
| SEC-M3 | No plaintext PII in outbound DTOs | `VerifyTicketDto.kt:25-26`, `ClaimDiscountDto.kt:10` | PASSED |

## 5. Addition Rule

Every new mobile plan ships `DOD_<slug>.md` with the header contract above, a row in §1, and its mechanical gates re-run with the result recorded here in the same commit. Backend-owned plans that change files in this repo add their mobile-file evidence rows here.
