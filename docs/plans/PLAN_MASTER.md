# PLAN_MASTER — SkyPrivilege Mobile (KMP) Plan Registry

- **Owner:** Fable 5.1 (Lead Systems Architect, Tri-Document Planning Standard)
- **Standard:** `~/.gemini/AGENTS.md` § Tri-Document Planning Standard & Enhanced STAR Contract; taxonomy per backend RFC-0001
- **Last updated:** 2026-10-04
- **Repo:** `mobile/` (Kotlin Multiplatform, Compose Multiplatform, module `:composeApp`), HEAD `fe0478e`
- **Sibling registries:** [`docs/dod/DOD_MASTER.md`](../dod/DOD_MASTER.md) · [`docs/qa/QA_MASTER.md`](../qa/QA_MASTER.md)
- **Backend registry (cross-repo, pinned):** <https://github.com/helmiatwork/sky_privilege/blob/fbb0780/docs/plans/PLAN_MASTER.md>

This registry is mobile-scoped (RFC-0001 §2.1). A plan lives in the repo that owns the majority of changed code. Backend-owned waves that touch the tablet contract get one row here with a pinned URL, never a relative path across the repo boundary.

## 1. Status Legend

| Status | Meaning |
|---|---|
| Draft | Plan authored, Pre-Execution Gate not yet passed |
| In Progress | Fixer dispatched, local commits exist, review gate not closed |
| Implemented | Both reviewers `APPROVED`, local commit landed, DoD evidence backfilled |
| Deprecated | Superseded; retained for audit trail only |

## 2. Plan Index

Legacy plans keep their `STAR_*` filenames. New plans MUST use `PLAN_<slug>.md` with sibling `DOD_<slug>.md` and `QA_<slug>.md`, Enhanced STAR structure, and a formal DoD table (`mobile/AGENTS.md` Plan Document Invariant).

### 2.1 Mobile-owned

| # | Wave / Feature | Plan File | Status | Branch (merged to) | Lead Agent | Landed Commit | Tasks |
|---|---|---|---|---|---|---|---|
| M1 | Mobile KMP — Audit Remediation (Round 2) | [`STAR_MOBILE_AUDIT_REMEDIATION.md`](STAR_MOBILE_AUDIT_REMEDIATION.md) | Implemented | `feat/mobile-fable-opus-perfection` → `main` | Fable 5.1 + Opus audit · fixer (Sonnet) | `34a2398` | DoD-1 … DoD-15 |

### 2.2 Backend-owned, tablet contract affected (pinned URLs)

| # | Wave | Plan (backend, pinned) | Status | Landed (backend) | Tablet-facing contract |
|---|---|---|---|---|---|
| B1 | Wave -2 — P0 Legal Hardening | <https://github.com/helmiatwork/sky_privilege/blob/fbb0780/docs/plans/STAR_P0_WAVE_MINUS2_LEGAL_VULNERABILITIES.md> | Implemented | `83440bf` | T1 `auth_method_opened` ∈ {`pin`,`face`}, blank → 422; T2 redemption index scoped to device outlet + active shift, masked-only serializer |
| B2 | Wave 1 — Ghost Sale Ledger + Audit Scale | <https://github.com/helmiatwork/sky_privilege/blob/fbb0780/docs/plans/STAR_P0_WAVE1_K4_GHOST_SALE_AND_K2_AUDIT_SCALE.md> | Implemented | `38cf28d` | Evidence uploads fail closed on unsafe EXIF (`UnsafeImageError`); film-roll weighing requires witness ≠ cashier |
| B3 | Wave 3 — PDP Rights & Breach Governance | <https://github.com/helmiatwork/sky_privilege/blob/fbb0780/docs/plans/STAR_P0_WAVE3_PDP_RIGHTS_AND_BREACH_GOVERNANCE.md> | Implemented | `240a0c9` | Biometric purge H+30; `FaceLoginEvent` retention; consent gate unchanged |
| B4 | Wave 4 — Final Backend & Compliance | <https://github.com/helmiatwork/sky_privilege/blob/fbb0780/docs/plans/STAR_P0_WAVE4_FINAL_BACKEND_AND_COMPLIANCE.md> | Implemented | `3909f26` | No tablet API change; cloud OCR may return `CLOUD_DISPATCH_BLOCKED` (local path continues) |

Pinned SHA `fbb0780` is the backend commit that completed RFC-0001 §5.8; URLs resolve once that commit is pushed.

## 3. Task Ledger — Mobile KMP Audit Remediation (`34a2398`)

Paths relative to `composeApp/src/commonMain/kotlin/com/skyprivilege/` unless prefixed `androidMain/` or `commonTest/`.

| Item | Deliverable | Primary Code Anchor |
|---|---|---|
| M-1 | Nullable live telemetry (no mock GPS / Wi-Fi) | `data/remote/dto/VerifyTicketDto.kt:15-19`, `data/repository/TicketRepositoryImpl.kt:72-76` |
| M-2 | Zero plaintext PNR / passenger name on wire | `VerifyTicketDto.kt:25-26,41`, `ClaimDiscountDto.kt:10`, `RedemptionRepositoryImpl.kt:27,63` |
| M-3 | Emergency voucher PNR canonical hash `sha256Hex(pnr.trim().uppercase())` | `RedemptionRepositoryImpl.kt:216`, `security/Sha256.kt:3` (expect), `androidMain/.../Sha256.kt:5-8` |
| M-4 | Keystore sign off main thread | `KtorClientFactory.kt:66`, `androidMain/.../MainActivity.kt:129` |
| M-5 | Keystore cold-start gate | `androidMain/.../MainActivity.kt:125,134` |
| M-6 | Device signature handshake; 409 → `NonStackingConflictException` | `KtorClientFactory.kt:57-72`, `RedemptionRepositoryImpl.kt:168-174` |

## 4. Mobile Architectural Contracts

Binding on every mobile plan. Backend-side contracts (`C-S*`, `C-D*`) live in the backend registry §4; the rows below are the ones the tablet enforces. Any plan that weakens one MUST say so in Pillar A and obtain `oracle` `APPROVED`.

| ID | Contract | Statute / Source | Enforcing Code |
|---|---|---|---|
| C-S1 (mobile side) | Plaintext PNR and passenger name MUST NOT leave the device or persist; DTOs carry only `pnr_canonical_hash`, `pnr_masked`, `masked_display_name` | UU PDP Ps. 16, 44 | `VerifyTicketDto.kt:25-26`, `ClaimDiscountDto.kt:10` |
| C-S11 | No human-name placeholder fixtures (`SANTOSO` grep = 0) | Zero-fiction rule | `commonTest/.../PiiMaskerTest.kt` |
| C-C6 | Keystore signing runs on `Dispatchers.Default`; UI blocks until signer initialised | ADR-0008 | `KtorClientFactory.kt:66`, `MainActivity.kt:134` |
| C-C7 | Redemption double-submit surfaces HTTP 409 as typed `NonStackingConflictException` | ADR-0001 non-stacking | `RedemptionRepositoryImpl.kt:168-174` |
| C-F2 | `RedemptionClaim.ticketPhoto` defaults to `null`; no dummy pixel | Fail-closed | DTO default |
| C-F3 | Guideline ETag 304 with null cache throws `IllegalStateException`; never silent empty list | Fail-closed | `GuidelineRepositoryImpl.kt:30` |
| C-M1 | Every mutating request carries `X-Timestamp`, `X-Nonce`, `X-Device-Signature`; `X-Device-Token` is never sent | ADR-0008 | `KtorClientFactory.kt:57-72` |
| C-M2 | Business logic and tests live in `commonMain` / `commonTest`; platform code only via `expect` / `actual` | `mobile/AGENTS.md` | module layout |
| C-M3 | Zero-disk camera: frames processed in memory, never written to local storage | UU PDP Ps. 16; `PLAN.md` §3.4 | camera pipeline |
| C-M4 | Attestation and kill-switch flows are security paths; every change needs a tamper GWT scenario | `mobile/AGENTS.md`; `PLAN.md` §3.3 | attestation module |

ADR references resolve in the backend repo: <https://github.com/helmiatwork/sky_privilege/blob/fbb0780/docs/decisions/README.md>.

## 5. Pipeline Binding

Plan → `fixer` (Claude Sonnet only; Gemini Flash disabled for KMP) TDD → `code-reviewer` + `oracle` (Opus) parallel on `rtk git diff HEAD` → ≤ 3 combined rounds → `test-engineer` E2E against `QA_<slug>.md` → `E2E_PASSED`. Fable never reviews diffs.

## 6. Open Backlog

- Offline outbox per ADR-0015 (SQLDelight over SQLCipher, `OutboxSyncWorker`); current code queues acknowledgments and audit only (`PLAN.md` §3.1).
- Face login / liveness / selfie upload not implemented; `auth_method_opened` stays `pin` until DPIA approved (backend MASTER_CONCEPT §2.2 status note, 3 Oct 2026).
- `docs/architecture/MOBILE_ARCHITECTURE.md` per RFC-0001 §2: today the architecture document is [`../../PLAN.md`](../../PLAN.md); move when it is next rewritten.
- Per-feature `DOD_<slug>.md` / `QA_<slug>.md` backfill for `STAR_MOBILE_AUDIT_REMEDIATION.md`.
