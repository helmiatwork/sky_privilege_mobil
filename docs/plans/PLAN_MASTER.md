# PLAN_MASTER — SkyPrivilege Architectural Plan Registry

- **Owner:** Fable 5.1 (Lead Systems Architect, Tri-Document Planning Standard)
- **Standard:** `~/.gemini/AGENTS.md` § Tri-Document Planning Standard & Enhanced STAR Contract
- **Last updated:** 2026-10-04
- **Repos:** `backend/` (Rails API, own git, HEAD `3909f26`), `mobile/` (KMP, own git, HEAD `34a2398`)
- **Sibling registries:** [`docs/dod/DOD_MASTER.md`](../dod/DOD_MASTER.md) · [`docs/qa/QA_MASTER.md`](../qa/QA_MASTER.md)

## 1. Status Legend

| Status | Meaning |
|---|---|
| Draft | Plan authored, Pre-Execution Gate not yet passed |
| In Progress | Fixer dispatched, local commits exist, review gate not closed |
| Implemented | Both reviewers `APPROVED`, local commit landed, DoD evidence backfilled |
| Deprecated | Superseded; retained for audit trail only |

## 2. Plan Index

Legacy plans predate the `PLAN_<slug>.md` naming rule and keep their `STAR_*` filenames. New plans MUST use `PLAN_<slug>.md` with sibling `DOD_<slug>.md` and `QA_<slug>.md`.

| # | Wave / Feature | Plan File | Status | Branch (merged to) | Lead Agent | Landed Commit | Tasks |
|---|---|---|---|---|---|---|---|
| 1 | Wave -2 — P0 Legal Hardening | [`STAR_P0_WAVE_MINUS2_LEGAL_VULNERABILITIES.md`](STAR_P0_WAVE_MINUS2_LEGAL_VULNERABILITIES.md) | Implemented | `backend/main` | Fable 5.1 plan · fixer (Sonnet) · code-reviewer + oracle (Opus) | `83440bf` | T1–T8 |
| 2 | Wave 1 — K-4 Ghost Sale Physical Ledger + K-2 AuditLog Shard | [`STAR_P0_WAVE1_K4_GHOST_SALE_AND_K2_AUDIT_SCALE.md`](STAR_P0_WAVE1_K4_GHOST_SALE_AND_K2_AUDIT_SCALE.md) | Implemented | `backend/main` | Fable 5.1 · fixer (Sonnet) · Opus dual review | `38cf28d` | W1-T1–W1-T14 |
| 3 | Wave 3 — PDP Rights & Breach Governance | [`STAR_P0_WAVE3_PDP_RIGHTS_AND_BREACH_GOVERNANCE.md`](STAR_P0_WAVE3_PDP_RIGHTS_AND_BREACH_GOVERNANCE.md) | Implemented | `backend/main` | Fable 5.1 · fixer (Sonnet) · Opus dual review | `240a0c9` | W3-T1–W3-T6 |
| 4 | Wave 4 — Final Backend & Compliance Artifacts | [`STAR_P0_WAVE4_FINAL_BACKEND_AND_COMPLIANCE.md`](STAR_P0_WAVE4_FINAL_BACKEND_AND_COMPLIANCE.md) | Implemented | `backend/main` | Fable 5.1 · fixer (Sonnet) · Opus dual review | `3909f26` | W4-T1–W4-T3 |
| 5 | Mobile KMP — Audit Remediation (Round 2) | [`../../mobile/docs/plans/STAR_MOBILE_AUDIT_REMEDIATION.md`](../../mobile/docs/plans/STAR_MOBILE_AUDIT_REMEDIATION.md) | Implemented | `feat/mobile-fable-opus-perfection` → `mobile/main` | Fable 5.1 + Opus audit · fixer (Sonnet) | `34a2398` | DoD-1–DoD-15 |
| 6 | Wave -1 — Fixes | [`STAR_P0_WAVE_MINUS1_FIXES.md`](STAR_P0_WAVE_MINUS1_FIXES.md) | Implemented (pre-standard) | `backend/main` | Fable 5.1 | pre-`83440bf` | — |
| 7 | P0 Hotfix — Legal Hold & Trio | [`STAR_P0_HOTFIX_LEGAL_HOLD_AND_TRIO.md`](STAR_P0_HOTFIX_LEGAL_HOLD_AND_TRIO.md) | Implemented (pre-standard) | `backend/main` | Fable 5.1 | pre-`83440bf` | — |
| 8 | P0 Legal & PDP Hardening (original) | [`STAR_P0_LEGAL_AND_PDP_HARDENING.md`](STAR_P0_LEGAL_AND_PDP_HARDENING.md) | Deprecated (absorbed by Wave -2 / Wave 3) | — | Fable 5.1 | — | — |
| 9 | Web Admin — Menus & Logic | [`STAR_WEB_ADMIN_MENUS_AND_LOGIC.md`](STAR_WEB_ADMIN_MENUS_AND_LOGIC.md) | Implemented | `backend/main` | Fable 5.1 | `50ab35d` | — |
| 10 | Web Admin Wave 3 — Fraud Analytics | [`STAR_WEB_ADMIN_WAVE_3_FRAUD_ANALYTICS.md`](STAR_WEB_ADMIN_WAVE_3_FRAUD_ANALYTICS.md) | Implemented | `backend/main` | Fable 5.1 | `50ab35d` | — |
| 11 | Dashboard Charts & Series | [`STAR_DASHBOARD_CHARTS_AND_SERIES.md`](STAR_DASHBOARD_CHARTS_AND_SERIES.md) | Implemented | `backend/main` | Fable 5.1 | `50ab35d` | — |

Audit inputs (not plans, read-only references): `AUDIT_FABLE_ADVERSARIAL_20261003.md`, `AUDIT_FABLE_LEGAL_AND_REGULATORY_COMPLIANCE.md`, `AUDIT_FABLE_ULTRA_RUTHLESS_LEGAL_AND_PLAN.md`, `AUDIT_FABLE_CRITICAL_PERFECTION.md`, `BUSINESS_OWNER_CRITICAL_FRAUD_STRATEGY.md`, `IMPROVEMENT_MASTER_PLAN_PPT_AND_MENUS.md`.

## 3. Task Ledger (Implemented Waves)

### Wave -2 — P0 Legal Hardening (`83440bf`)

| Task | Deliverable | Primary Code Anchor |
|---|---|---|
| T1 | `auth_method_opened` no default; `pin`/`face` only; 422 on blank | `app/models/shift.rb:25`, `db/migrate/20261004000001_change_shifts_auth_method_opened_default_null.rb` |
| T2 | SANTOSO fixture purge; tablet redemption index scoped to `current_device.outlet_id` + active shift; masked-only serializer | `spec/controllers/api/v1/redemptions_controller_spec.rb:7` (negative assertion, grep = 0) |
| T3 | Airline claim export gated on `airlines.dpa_signed_at`; fraud-exclusion query | `app/controllers/admin/exports/airline_claims_controller.rb:10,177` |
| T4 | PaperTrail scrub instead of `delete_all`; legal-hold guard | `app/services/compliance/pdp_purge_service.rb:45` |
| T5 | Auto-purge cutover to `purge_eligible_at` | `app/jobs/pdp_auto_purge_worker.rb:3` |
| T6 | GPS minimization to geohash6 (~1 km) in `audit_logs.metadata` | `app/services/compliance/geo_minimizer.rb:8`, `app/models/audit_log.rb:239` |
| T7 | PaperTrail `skip:` on biometrics + encrypted PII | `app/models/cashier.rb:7`, `app/models/staff_profile.rb:4`, `app/models/ticket_redemption.rb:14`, `app/models/film_roll.rb:4` |
| T8 | `PiiMasker` key union; Sentry `before_send` scrub | `app/services/compliance/pii_masker.rb:4`, `config/initializers/sentry.rb:45` |

### Wave 1 — Ghost Sale & Audit Scale (`38cf28d`)

| Task | Deliverable | Primary Code Anchor |
|---|---|---|
| W1-T1 | Physical ledger schema: `film_rolls`, `film_roll_weighings`, `machines`, `machine_events` | `db/schema.rb:484,505,574,589` |
| W1-T2 | Shrine uploaders, fail-closed EXIF/GPS strip (`UnsafeImageError`) | `app/services/anti_fraud/fraud_evidence_sanitizer.rb:5,14` |
| W1-T3 | `FilmVariance` detector | `app/services/anti_fraud/detectors/film_variance.rb:5` |
| W1-T4 | `MachineBurstNoSale` detector | `app/services/anti_fraud/detectors/machine_burst_no_sale.rb:5` |
| W1-T5 | `UnderWrapHint` detector | `app/services/anti_fraud/detectors/under_wrap_hint.rb:5` |
| W1-T6 | Dual-witness weighing (witness ≠ cashier) | `app/models/film_roll_weighing.rb:19,24-27` |
| W1-T7 | ESP32 telemetry ingest, HMAC `X-ESP32-Signature` | `app/controllers/api/v1/machine_events_controller.rb:7,26` |
| W1-T8 | `MachineEventPruneJob` | `app/jobs/machine_event_prune_job.rb:3` |
| W1-T9 | `audit_logs.chain_scope` + `(chain_scope, id)` index | `app/models/audit_log.rb:69`, `db/schema.rb:171` |
| W1-T10 | Per-scope `GET_LOCK` advisory lock held across commit | `app/models/audit_log.rb:315` |
| W1-T11 | `Audit::WriteJob` polynomial retry on deadlock | `app/jobs/audit/write_job.rb:7` |
| W1-T12 | `Audit::ChainVerifier` per-scope + nightly job | `app/services/audit/chain_verifier.rb:4`, `app/jobs/audit/chain_verification_job.rb:4` |
| W1-T13 | `Audit::ChainResharder` + rake | `app/services/audit/chain_resharder.rb:4`, `lib/tasks/audit_reshard_chain.rake:5` |
| W1-T14 | Benchmark gate | `spec/benchmarks/audit_log_shard_benchmark_spec.rb:1` |

### Wave 3 — PDP Rights & Breach Governance (`240a0c9`)

| Task | Deliverable | Primary Code Anchor |
|---|---|---|
| W3-T1 | `DataSubjectRequest` model, migration, ≥2-field candidate lookup | `app/models/data_subject_request.rb:3`, `db/migrate/20261004030001_create_data_subject_requests.rb` |
| W3-T2 | `Admin::DataSubjectRequestsController` + policy | `app/controllers/admin/data_subject_requests_controller.rb:5`, `app/policies/data_subject_request_policy.rb:3` |
| W3-T3 | `DataBreachIncident` with 72 h authority-notification SLA | `app/models/data_breach_incident.rb:18,22`, `app/controllers/admin/data_breach_incidents_controller.rb:4` |
| W3-T4 | `AnomalyAlertWorker` auto-opens incident on critical/high | `app/jobs/anomaly_alert_worker.rb:56` |
| W3-T5 | `CashierBiometricPurgeJob` (H+30), `FaceLoginEvent` retention | `app/jobs/cashier_biometric_purge_job.rb:3,23`, `app/models/face_login_event.rb:3` |
| W3-T6 | RBAC hardening, DPO floor (`User.kept`) | `app/models/user.rb:51,264` |

### Wave 4 — Final Backend & Compliance (`3909f26`)

| Task | Deliverable | Primary Code Anchor |
|---|---|---|
| W4-T1 | `AuditAnchor` Merkle root + RFC 3161 simulator, hourly `Audit::AnchorJob` | `app/models/audit_anchor.rb:3`, `app/jobs/audit/anchor_job.rb:4,33` |
| W4-T2 | Fail-closed cloud vision/LLM gate (`CLOUD_DISPATCH_BLOCKED`, TIA ack) | `app/services/vision_ai/router.rb:95`, `app/controllers/admin/integrations_controller.rb:224` |
| W4-T3 | 7 compliance artifacts + README + CHECKSUMS | `docs/compliance/01_…07_*.md`, `docs/compliance/CHECKSUMS.txt` |

### Mobile KMP — Audit Remediation (`34a2398`)

| Item | Deliverable | Primary Code Anchor |
|---|---|---|
| M-1 | Nullable live telemetry (no mock GPS/Wi-Fi) | `data/remote/dto/VerifyTicketDto.kt:15-19`, `data/repository/TicketRepositoryImpl.kt:72-76` |
| M-2 | Zero plaintext PNR / passenger name on wire | `VerifyTicketDto.kt:25-26,41`, `ClaimDiscountDto.kt:10`, `RedemptionRepositoryImpl.kt:27,63` |
| M-3 | Emergency voucher PNR canonical hash `sha256Hex(pnr.trim().uppercase())` | `RedemptionRepositoryImpl.kt:216`, `security/Sha256.kt:3` (expect), `androidMain/.../Sha256.kt:5-8` |
| M-4 | Keystore sign off main thread | `KtorClientFactory.kt:66`, `MainActivity.kt:129` |
| M-5 | Keystore cold-start gate | `MainActivity.kt:125,134` |
| M-6 | Device signature handshake, 409 → `NonStackingConflictException` | `KtorClientFactory.kt:57-72`, `RedemptionRepositoryImpl.kt:168-174` |

All mobile paths relative to `mobile/composeApp/src/commonMain/kotlin/com/skyprivilege/` unless prefixed `androidMain`.

## 4. Cross-Feature Architectural Contracts

Binding on every current and future plan. Any plan that weakens a contract MUST say so explicitly in Pillar A and obtain `oracle` `APPROVED`.

### 4.1 Statutory Invariants (UU PDP No. 27/2022, UU ITE, KUHP, OWASP Top 10)

| ID | Contract | Statute | Enforcing Code |
|---|---|---|---|
| C-S1 | Plaintext PNR and passenger name MUST NOT cross any wire or persist outside encrypted columns; only `pnr_canonical_hash`, `pnr_masked`, `masked_display_name` | UU PDP Ps. 16, 44 | mobile `VerifyTicketDto.kt:25-26`; backend tablet serializer (T2) |
| C-S2 | GPS in audit metadata MUST be minimized to geohash6; no `gps_latitude`/`gps_longitude` plaintext | UU PDP Ps. 16 (minimization) | `geo_minimizer.rb:8`, `audit_log.rb:239` |
| C-S3 | Biometric templates MUST be purged H+30 after termination/deactivation; MUST NOT be versioned by PaperTrail | UU PDP Ps. 16, 43 | `cashier_biometric_purge_job.rb:3`, `cashier.rb:7` |
| C-S4 | Audit versions MUST be scrubbed, never `delete_all`; 7-year retention of the audit record itself | UU PDP Ps. 46(3) | `pdp_purge_service.rb:45` |
| C-S5 | Purge MUST be blocked while `legal_held?`, `flagged`, or `fraud_confirmed` | KUHP evidence preservation; UU ITE | `pdp_purge_service.rb`, `docs/compliance/07_RUNBOOK_ELECTRONIC_EVIDENCE_LEGAL_HOLD.md` |
| C-S6 | DSAR MUST complete within `received_at + 30.days`; lookup requires ≥2 matching identity fields | UU PDP Ps. 5–13 | `data_subject_request.rb`, DSAR lookup service |
| C-S7 | Breach MUST be notified to authority within `detected_at + 72.hours` | UU PDP Ps. 46 | `data_breach_incident.rb:18,22` |
| C-S8 | Cross-border cloud dispatch MUST fail closed unless TIA acknowledged (`CLOUD_DISPATCH_BLOCKED`) | UU PDP Ps. 56 | `vision_ai/router.rb:95` |
| C-S9 | Airline claim export MUST be refused (403) when `airlines.dpa_signed_at IS NULL` | UU PDP Ps. 51 (processor agreement) | `airline_claims_controller.rb:10,177` |
| C-S10 | Error telemetry MUST pass `PiiMasker.scrub` before leaving the process | UU PDP Ps. 16; OWASP A09 | `config/initializers/sentry.rb:45` |
| C-S11 | No human-name placeholder fixtures (`SANTOSO` grep = 0 in both repos) | Internal zero-fiction rule | `redemptions_controller_spec.rb:7`; mobile `PiiMaskerTest.kt` |

### 4.2 Database Constraints

| ID | Contract | Location |
|---|---|---|
| C-D1 | `shifts.auth_method_opened` CHECK `IN ('pin','face') OR NULL`; no column default | `db/schema.rb:815` |
| C-D2 | `machine_events` unique `(esp32_device_id, occurred_at, event_type)` idempotency key | `db/schema.rb:583` |
| C-D3 | `film_rolls.code` unique | `db/schema.rb:519` |
| C-D4 | `audit_logs` index `(chain_scope, id)` for ordered per-scope chain reads | `db/schema.rb:171` |
| C-D5 | `audit_anchors.last_audit_log_id` unique; 5 CHECK constraints (digest enum, `entry_count > 0`, log-id order, period order, status enum) | `db/schema.rb:149,152-156` |
| C-D6 | `data_subject_requests` and `data_breach_incidents` carry 3 CHECK constraints each; indexes on `status`, `due_at`/`detected_at`, `pnr_canonical_hash` | `db/schema.rb:322,347` |
| C-D7 | Every migration MUST be reversible (`db:migrate:redo`) and CHECK constraints MUST survive `db:schema:load` | Wave 3 D1-c, D6-e |

### 4.3 Concurrency Guards

| ID | Contract | Location |
|---|---|---|
| C-C1 | Audit hash-chain append MUST hold MySQL `GET_LOCK` per `chain_scope` across the commit boundary | `audit_log.rb:315` |
| C-C2 | Async audit writes MUST retry `ActiveRecord::Deadlocked` with `wait: :polynomially_longer, attempts: 25`; `fraud_confirmed` stays synchronous | `audit/write_job.rb:7` |
| C-C3 | Anchor job MUST serialize via advisory lock; one anchor per `last_audit_log_id` | `audit/anchor_job.rb:10`, `db/schema.rb:149` |
| C-C4 | `AnomalyAlertWorker` MUST dedupe incident creation within 10 min | `anomaly_alert_worker.rb:56` |
| C-C5 | ESP32 events MUST be idempotent by unique key; replay MUST be rejected by HMAC + timestamp | `machine_events_controller.rb:26`, `db/schema.rb:583` |
| C-C6 | Mobile keystore signing MUST run on `Dispatchers.Default`; UI MUST block until signer initialized | `KtorClientFactory.kt:66`, `MainActivity.kt:134` |
| C-C7 | Redemption double-submit MUST surface HTTP 409 as typed `NonStackingConflictException` | `RedemptionRepositoryImpl.kt:168-174` |

### 4.4 Fail-Closed Handling

| ID | Contract |
|---|---|
| C-F1 | Image evidence with unparseable or unsafe EXIF raises `UnsafeImageError`; never stored (`fraud_evidence_sanitizer.rb:5,14`) |
| C-F2 | `RedemptionClaim.ticketPhoto` defaults to `null`; no dummy pixel |
| C-F3 | Guideline ETag 304 with null cache throws `IllegalStateException`; never silent empty list |
| C-F4 | Cloud dispatch without TIA ack blocks and audits; never degrades to unauthorized transfer |
| C-F5 | Missing `auth_method_opened` returns 422; never defaults to `face` |

## 5. Pipeline Binding

Plan → `fixer` (Sonnet only, Flash disabled) TDD → `code-reviewer` + `oracle` (Opus) parallel on `rtk git diff HEAD` → ≤3 combined rounds → `test-engineer` E2E against `QA_<slug>.md` → `E2E_PASSED`. Fable never reviews diffs.

## 6. Open Backlog (not planned, from Wave 4 §4.6 and audits)

- Production RFC 3161 TSA integration replacing `Audit::Rfc3161Simulator`.
- `SimpleCov.minimum_coverage 90` enforcement in `spec/spec_helper.rb` (currently absent, see DOD_MASTER G-B3).
- Mobile static analysis (detekt/ktlint) — no config present.
- Per-feature `DOD_<slug>.md` / `QA_<slug>.md` backfill for legacy `STAR_*` plans.
