# QA_MASTER — SkyPrivilege Mobile (KMP) Test Registry, Coverage Matrix & Regression Risk Ledger

- **Owner:** Fable 5.1 (QA Threat Model & Scenarios)
- **Last updated:** 2026-10-04
- **Executor:** `test-engineer` (runtime E2E, verdict `E2E_PASSED | E2E_FAILED`) after `code-reviewer` + `oracle` both `APPROVED`
- **Siblings:** [`docs/plans/PLAN_MASTER.md`](../plans/PLAN_MASTER.md) · [`docs/dod/DOD_MASTER.md`](../dod/DOD_MASTER.md)
- **Backend QA (cross-repo, pinned):** <https://github.com/helmiatwork/sky_privilege/blob/fbb0780/docs/qa/QA_MASTER.md>

## 1. Universal 6-Category Verification Framework

| Cat | Name | Question answered |
|---|---|---|
| 1 | Adversarial & Abuse Vectors | Can a hostile cashier, device, or outsider forge, replay, escalate, or exfiltrate? |
| 2 | Concurrency & Race Conditions | Do parallel writers corrupt chains, double-spend, or duplicate incidents? |
| 3 | Resiliency & Chaos Recovery | Does the system fail closed and recover under network loss, cold start, cache miss? |
| 4 | Boundary Values & Extreme Inputs | Do thresholds, sizes, and encodings hold at the edge? |
| 5 | Data Integrity, Privacy & Compliance | Does PII stay minimised end to end on the device and on the wire? |
| 6 | FSM & Lifecycle | Do attendance, signer, and redemption states transition only along legal edges? |

Scenario ID format: `<Wave>-C<cat>-<nn>`. Every `QA_<slug>.md` MUST contain at least one positive and one negative scenario per category or an explicit `N/A` with reason.

## 2. Test Registry

Spec paths relative to `composeApp/src/commonTest/kotlin/com/skyprivilege/`; source paths relative to `composeApp/src/commonMain/kotlin/com/skyprivilege/`.

### 2.1 Mobile KMP — Audit Remediation (`34a2398`)

| ID | Cat | Scenario | Expected | Evidence | Status |
|---|---|---|---|---|---|
| MB-C1-01 | 1 | Request without `X-Device-Signature` / with stale `X-Timestamp` | Server rejects; client never sends `X-Device-Token` | `data/remote/KtorClientFactoryTest.kt:23` | Automated |
| MB-C1-02 | 1 | Spoofed GPS injection path | No mock constants; null when sensor absent | `dto/VerifyTicketDto.kt:15-19` | Automated |
| MB-C2-01 | 2 | Double-tap redemption submit | 409 → `NonStackingConflictException`; dialog stays open | `RedemptionRepositoryImpl.kt:168-174`, `RedemptionRepositoryTest.kt` | Automated |
| MB-C3-01 | 3 | Cold start before keystore ready | Spinner "Menyiapkan Kios…"; no request leaves | `androidMain/.../MainActivity.kt:134` | Manual (device) |
| MB-C3-02 | 3 | Guideline 304 with null cache | `IllegalStateException`, not silent empty list | `GuidelineRepositoryImpl.kt:30` | Automated |
| MB-C4-01 | 4 | PNR with lowercase, leading/trailing spaces, ICAO `SURNAME/FIRST` | Same canonical hash; mask `F*** S******` | `RedemptionRepositoryImpl.kt:216`, `pii/PiiMaskerTest.kt` | Automated |
| MB-C5-01 | 5 | Serialize every outbound DTO | No `passenger_name`, no plaintext `pnr` | `VerifyTicketDto.kt:25-26`, `ClaimDiscountDto.kt:10` | Automated |
| MB-C5-02 | 5 | Fixture grep `SANTOSO` | 0 | `pii/PiiMaskerTest.kt` | Automated |
| MB-C6-01 | 6 | Attendance FSM `check_in → check_out → update` | Transitions per [`PLAN.md`](../../PLAN.md) §2.1 table | `e2e/E2EJourneysTest.kt` (9 `@Test`) | Automated |
| MB-C6-02 | 6 | Signer `null → ready`; signing on `Dispatchers.Default` | Main thread never blocks | `KtorClientFactory.kt:66`, `MainActivity.kt:129` | Automated |

### 2.2 Backend-owned waves

Scenarios for Waves -2, 1, 3, 4 are registered in the backend QA_MASTER (pinned link above). Tablet-visible expectations re-stated for the mobile executor:

| Backend ID | Tablet-visible expectation | Mobile check |
|---|---|---|
| WM2-C1-01 | Blank `auth_method_opened` → 422 | Clock-in always sends `pin` explicitly |
| WM2-C1-02 | Redemptions of another outlet → empty set | History screen shows only own outlet |
| W1-C1-02 | Crafted EXIF upload → `UnsafeImageError` | Client strips EXIF before upload; server rejection surfaces as typed error |
| W4-C1-01 | Cloud OCR without TIA → `CLOUD_DISPATCH_BLOCKED` | Scan falls back to local path; no crash |

## 3. Coverage Matrix

Cells: number of registered scenarios / qualitative coverage. `●` strong (≥ 2 automated), `◐` single or manual, `○` gap.

| Wave | C1 Adversarial | C2 Concurrency | C3 Resiliency | C4 Boundary | C5 Privacy & Compliance | C6 FSM |
|---|---|---|---|---|---|---|
| Mobile Audit Remediation | 2 ● | 1 ◐ | 2 ◐ (1 manual) | 1 ◐ | 2 ● | 2 ● |

Weakest column: C3 (cold-start gate is device-only). Runtime E2E (`test-engineer`) has not been recorded against the mobile wave; rows above are unit-level automation or manual.

## 4. Runtime E2E Ledger

| Wave | QA Doc | `test-engineer` Verdict | Date | Notes |
|---|---|---|---|---|
| Mobile Audit Remediation | `STAR_MOBILE_AUDIT_REMEDIATION.md` (inline) | — | — | Cold-start gate requires physical tablet |

## 5. Regression Risk Ledger

| Risk ID | Area | Risk | Likelihood | Impact | Guard | Owner Gate |
|---|---|---|---|---|---|---|
| RM-01 | PII | New DTO adds `passenger_name` or plaintext `pnr` | Medium | Critical (UU PDP Ps. 16/44) | DTO grep gate G-M3; `MB-C5-01` | code-reviewer |
| RM-02 | Signing | Keystore gate removed or signer made optional for "offline mode" | Low | Critical | `MainActivity.kt:134`; `KtorClientFactoryTest.kt:23` | oracle |
| RM-03 | Tests | Reported test count drifts from Gradle XML report | Medium | Medium | Publish Gradle XML report; reconcile (DOD G-M1) | test-engineer |
| RM-04 | Telemetry | Mock GPS / BSSID constants reintroduced for demo builds | Medium | Critical (bypasses `MultiSignalScorer`) | `MB-C1-02`; grep for literal coordinates | code-reviewer |
| RM-05 | Hashing | Canonicalisation diverges from backend `PnrCanonicalizer` (trim / uppercase / title strip) | Medium | High (409 or duplicate claims) | `MB-C4-01`; shared fixture vector with backend spec | fable |
| RM-06 | Offline | Outbox implementation (ADR-0015) stores plaintext PII locally | Low | Critical | SQLCipher + masked-only columns; review gate | oracle |
| RM-07 | Attestation | Kill-switch or attestation flow changed without tamper GWT scenario | Low | Critical | `mobile/AGENTS.md` C-M4 | oracle |

## 6. Addition Rule

For every new mobile plan: create `docs/qa/QA_<slug>.md` with scenario IDs `<slug>-C<cat>-<nn>`, add a row block in §2, update §3 counts, add an E2E ledger row in §4, and register new risks in §5 before `fixer` dispatch. `fixer` returns `INTAKE_REJECTED: missing [QA|Masters]` otherwise.
