# QA_MASTER — SkyPrivilege Test Registry, Coverage Matrix & Regression Risk Ledger

- **Owner:** Fable 5.1 (QA Threat Model & Scenarios)
- **Last updated:** 2026-10-04
- **Executor:** `test-engineer` (runtime E2E, verdict `E2E_PASSED | E2E_FAILED`) after `code-reviewer` + `oracle` both `APPROVED`
- **Siblings:** [`docs/plans/PLAN_MASTER.md`](../plans/PLAN_MASTER.md) · [`docs/dod/DOD_MASTER.md`](../dod/DOD_MASTER.md)
- **Related:** [`ROLE_BASED_QA_CHECKLISTS_AND_SCENARIOS.md`](ROLE_BASED_QA_CHECKLISTS_AND_SCENARIOS.md), `backend/docs/QA_E2E_TEST_SCENARIOS.md`

## 1. Universal 6-Category Verification Framework

| Cat | Name | Question answered |
|---|---|---|
| 1 | Adversarial & Abuse Vectors | Can a hostile cashier, device, or outsider forge, replay, escalate, or exfiltrate? |
| 2 | Concurrency & Race Conditions | Do parallel writers corrupt chains, double-spend, or duplicate incidents? |
| 3 | Resiliency & Chaos Recovery | Does the system fail closed and recover under DB deadlock, R2 outage, cloud denial, cold start? |
| 4 | Boundary Values & Extreme Inputs | Do thresholds, deadlines, sizes, and encodings hold at the edge? |
| 5 | Data Integrity, Privacy & Compliance | Does PII stay minimized, retention fire, and statutory invariants hold end to end? |
| 6 | FSM & Lifecycle | Do shift, DSAR, incident, anchor, film roll, and keystore states transition only along legal edges? |

Scenario ID format: `<Wave>-C<cat>-<nn>`. Every `QA_<slug>.md` MUST contain at least one positive and one negative scenario per category or an explicit `N/A` with reason.

## 2. Test Registry

Spec paths relative to `backend/spec/` or `mobile/composeApp/src/commonTest/kotlin/com/skyprivilege/`.

### 2.1 Wave -2 — P0 Legal Hardening

| ID | Cat | Scenario | Expected | Evidence | Status |
|---|---|---|---|---|---|
| WM2-C1-01 | 1 | Client opens shift with blank `auth_method_opened` | 422; no row with `NULL ∧ status='open'` | `shift.rb:25`, `schema.rb:815` | Automated |
| WM2-C1-02 | 1 | Tablet requests redemptions of another outlet | Empty set; scoped to `current_device.outlet_id` | `redemptions_controller_spec.rb` | Automated |
| WM2-C1-03 | 1 | Admin exports airline claim for airline without DPA | 403 | `airline_claims_controller.rb:177` | Automated |
| WM2-C2-01 | 2 | Two purge workers pick same `purge_eligible_at` batch | Idempotent; no double-scrub error | `pdp_auto_purge_worker_spec.rb` | Automated |
| WM2-C3-01 | 3 | Sentry transport raises inside `before_send` | Event dropped, no PII leak, app unaffected | `sentry.rb:45` | Automated |
| WM2-C4-01 | 4 | GPS at ±90/±180 and NaN | geohash6 or nil; never raw lat/lon | `geo_minimizer.rb:8` | Automated |
| WM2-C4-02 | 4 | Record exactly at `purge_eligible_at == now` | Purged (inclusive) | `pdp_auto_purge_worker_spec.rb` | Automated |
| WM2-C5-01 | 5 | PaperTrail version holds `face_embedding` | Scrubbed `object`/`object_changes`; row retained 7 yrs | `pdp_purge_service.rb:45` | Automated |
| WM2-C5-02 | 5 | `has_paper_trail skip:` covers all encrypted + biometric attrs | `versions.object` clean | `cashier.rb:7`, `staff_profile.rb:4` | Automated |
| WM2-C5-03 | 5 | Repo grep `SANTOSO` | 0 | `redemptions_controller_spec.rb:7` | Automated |
| WM2-C6-01 | 6 | Shift `open → closed` with legal-hold flag | Purge blocked while held | `pdp_purge_service.rb` | Automated |

### 2.2 Wave 1 — Ghost Sale & Audit Scale

| ID | Cat | Scenario | Expected | Evidence | Status |
|---|---|---|---|---|---|
| W1-C1-01 | 1 | ESP32 event with bad HMAC / stale timestamp / replayed nonce | 401; `AUTH_FAIL` audit; 7 failure modes covered | `machine_events_controller.rb:26` | Automated |
| W1-C1-02 | 1 | Upload JPEG with crafted EXIF (GPS, oversized tags) | `UnsafeImageError`; nothing persisted | `fraud_evidence_sanitizer.rb:5,14` | Automated |
| W1-C1-03 | 1 | Cashier weighs with self as witness | Validation error | `film_roll_weighing.rb:24-27` | Automated |
| W1-C1-04 | 1 | Tampered `audit_logs` row mid-chain | `AUDIT_CHAIN_BROKEN` emitted | `chain_verifier.rb:4` | Automated |
| W1-C2-01 | 2 | 200 concurrent audit writes across 2 scopes | No duplicate `prev_hash`; p99 lock-wait < 50 ms | `spec/benchmarks/audit_log_shard_benchmark_spec.rb:1` | Automated |
| W1-C2-02 | 2 | Duplicate ESP32 event `(device, occurred_at, type)` | Second insert rejected by `uq_machine_events_idempotency` | `schema.rb:583` | Automated |
| W1-C3-01 | 3 | `ActiveRecord::Deadlocked` in `Audit::WriteJob` | Polynomial retry up to 25; eventual write | `audit/write_job.rb:7` | Automated |
| W1-C3-02 | 3 | Reshard rake interrupted and re-run | Idempotent; `RESHARDED_START/COMPLETE` audit | `chain_resharder.rb:4` | Automated |
| W1-C4-01 | 4 | Film variance at threshold ±1 g | Flag only strictly over threshold | `film_variance.rb:5` | Automated |
| W1-C4-02 | 4 | Burst window boundary (`machine_burst_window_s` ±1 s) | Correct inclusion | `machine_burst_no_sale.rb:5` | Automated |
| W1-C4-03 | 4 | Weight > film roll gross | Validation error | `film_roll_weighing.rb:20` | Automated |
| W1-C5-01 | 5 | Uploaded seal photo | Stored hash present; EXIF stripped | uploader spec | Automated |
| W1-C5-02 | 5 | `machine_events` older than retention | Pruned; `MACHINE_EVENTS_PRUNED` | `machine_event_prune_job.rb:3` | Automated |
| W1-C6-01 | 6 | Film roll `sealed → opened → weighed → depleted` | Only legal edges; witness required on weigh | `film_roll_weighing.rb:18` | Automated |

### 2.3 Wave 3 — PDP Rights & Breach Governance

| ID | Cat | Scenario | Expected | Evidence | Status |
|---|---|---|---|---|---|
| W3-C1-01 | 1 | Auditor toggles legal hold / creates settlement purge | 403 | `requests/admin/legal_holds_spec.rb` | Automated |
| W3-C1-02 | 1 | Non-superadmin assigns `dpo` role | Refused; `ROLE_ESCALATION_ATTEMPT` | `policies/admin/user_policy_spec.rb` | Automated |
| W3-C1-03 | 1 | DSAR lookup with 1 matching field | No candidates (needs ≥2) | `services/compliance/dsar_lookup_service_spec.rb` | Automated |
| W3-C2-01 | 2 | Two anomaly alerts within 10 min | One incident | `jobs/anomaly_alert_worker_spec.rb` | Automated |
| W3-C2-02 | 2 | Concurrent demotion of two DPOs | Floor keeps ≥1 active | `user.rb:51,264` | Automated |
| W3-C3-01 | 3 | R2 delete fails during selfie purge | Job retries; record not marked purged | `pdp_auto_purge_worker_spec.rb` | Automated |
| W3-C4-01 | 4 | Incident `detected_at` exactly 72 h ago | In `.overdue` | `data_breach_incident.rb:18` | Automated |
| W3-C4-02 | 4 | DSAR `received_at + 30.days` leap/DST | `due_at` correct | `models/data_subject_request_spec.rb` | Automated |
| W3-C4-03 | 4 | Cashier terminated H+29 vs H+30 | Embedding nulled only at H+30 | `jobs/cashier_biometric_purge_job_spec.rb` | Automated |
| W3-C5-01 | 5 | `erase_now` on legal-held subject | Refused | `requests/admin/data_subject_requests_spec.rb` | Automated |
| W3-C5-02 | 5 | DSAR export | Signed R2 URL TTL ≤ 24 h; `DSAR_*` audit | same | Automated |
| W3-C5-03 | 5 | Discarded user in DPO count | Excluded (`User.kept`) | `user.rb:264` | Automated |
| W3-C6-01 | 6 | DSAR `received → verified → fulfilled/rejected` | No skip of identity verification | `data_subject_requests_controller.rb:5` | Automated |
| W3-C6-02 | 6 | Incident `open → contained → notified → closed` | `BREACH_*` audit per edge | `data_breach_incidents_controller.rb:4` | Automated |

### 2.4 Wave 4 — Final Backend & Compliance

| ID | Cat | Scenario | Expected | Evidence | Status |
|---|---|---|---|---|---|
| W4-C1-01 | 1 | Cloud vision dispatch without TIA ack | Blocked; `CLOUD_DISPATCH_BLOCKED` | `vision_ai/router.rb:95` | Automated |
| W4-C1-02 | 1 | Tamper `audit_anchors.merkle_root` | Verification failure metric | `audit_anchor.rb:3` | Automated |
| W4-C2-01 | 2 | Two `AnchorJob` runs overlap | Advisory lock; single anchor per `last_audit_log_id` | `anchor_job.rb:10`, `schema.rb:149` | Automated |
| W4-C3-01 | 3 | `CLOUD_DISPATCH_BLOCKED` raised mid-request | Rescued safely; local path continues | commit `add62a7` | Automated |
| W4-C4-01 | 4 | Anchor with `entry_count = 0` | CHECK violation | `schema.rb:152-156` | Automated |
| W4-C5-01 | 5 | Compliance docs drift vs `CHECKSUMS.txt` | `spec/compliance/documents_spec.rb` fails | `docs/compliance/CHECKSUMS.txt` | Automated |
| W4-C5-02 | 5 | Each doc cites statute + control-to-code mapping | Spec green | `spec/compliance/documents_spec.rb` | Automated |
| W4-C6-01 | 6 | Anchor `pending → signed → verified` | Status enum CHECK; no reverse edge | `schema.rb:152-156` | Automated |

### 2.5 Mobile KMP — Audit Remediation

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
| MB-C6-01 | 6 | Attendance FSM `check_in → check_out → update` | Transitions per `PLAN.md` §2.1 table | `e2e/E2EJourneysTest.kt` (9 `@Test`) | Automated |
| MB-C6-02 | 6 | Signer `null → ready`; signing on `Dispatchers.Default` | Main thread never blocks | `KtorClientFactory.kt:66`, `MainActivity.kt:129` | Automated |

## 3. Coverage Matrix

Cells: number of registered scenarios / qualitative coverage. `●` strong (≥2 automated), `◐` single or manual, `○` gap.

| Wave | C1 Adversarial | C2 Concurrency | C3 Resiliency | C4 Boundary | C5 Privacy & Compliance | C6 FSM |
|---|---|---|---|---|---|---|
| Wave -2 | 3 ● | 1 ◐ | 1 ◐ | 2 ● | 3 ● | 1 ◐ |
| Wave 1 | 4 ● | 2 ● | 2 ● | 3 ● | 2 ● | 1 ◐ |
| Wave 3 | 3 ● | 2 ● | 1 ◐ | 3 ● | 3 ● | 2 ● |
| Wave 4 | 2 ● | 1 ◐ | 1 ◐ | 1 ◐ | 2 ● | 1 ◐ |
| Mobile | 2 ● | 1 ◐ | 2 ◐ (1 manual) | 1 ◐ | 2 ● | 2 ● |
| **Total** | 14 | 7 | 7 | 10 | 12 | 7 |

Weakest columns: C2 and C3 for Wave 4, C3 for Mobile (device-only). Runtime E2E (`test-engineer`) has not been recorded against any wave in this registry; all rows above are unit/integration-level automation or manual.

## 4. Runtime E2E Ledger

| Wave | QA Doc | `test-engineer` Verdict | Date | Notes |
|---|---|---|---|---|
| Wave -2 | inline in STAR plan | — | — | Pre-standard; no `QA_<slug>.md` |
| Wave 1 | inline in STAR plan §5 | — | — | Benchmark spec counts as C2 runtime proxy |
| Wave 3 | inline §4 TDD specs | — | — | — |
| Wave 4 | inline §3.5 | — | — | — |
| Mobile | `STAR_MOBILE_AUDIT_REMEDIATION.md` | — | — | Cold-start gate requires physical tablet |

## 5. Regression Risk Ledger

| Risk ID | Area | Risk | Likelihood | Impact | Guard | Owner Gate |
|---|---|---|---|---|---|---|
| R-01 | Audit chain | Future caller bypasses `record_async` and writes `AuditLog` directly without `GET_LOCK` | Medium | Critical (chain break) | `chain_verifier.rb:4` nightly; `gitnexus impact --target AuditLog#record!` before merge | oracle |
| R-02 | Audit chain | Reshard on live traffic races with anchor job | Low | Critical | Advisory lock in `anchor_job.rb:10`; runbook `docs/runbooks/audit_chain_reshard.md` | oracle |
| R-03 | PII | New serializer or DTO adds `passenger_name` plaintext | Medium | Critical (UU PDP Ps. 16/44) | Mobile DTO grep gate G-M3; backend serializer spec | code-reviewer |
| R-04 | PII | New `audit_logs.metadata` writer stores raw GPS | Medium | High | `audit_log.rb:239` sanitizer; gate G5 grep `gps_latitude` | code-reviewer |
| R-05 | Retention | New model with PII not added to `MODELS_WITH_RETENTION` | Medium | High | ROPA review (`docs/compliance/03_*`) per new table | fable |
| R-06 | Retention | Purge runs on legal-held or flagged record | Low | Critical (evidence destruction) | `pdp_purge_service.rb` guard; W3-C5-01 | test-engineer |
| R-07 | RBAC | Seed drift grants auditor `legal_holds:*` | Low | High | `spec/seeds/role_permissions_spec.rb` | code-reviewer |
| R-08 | RBAC | Last DPO demoted via bulk update path bypassing validation | Low | High | `user.rb:51` validate on update; add callback-free path check | oracle |
| R-09 | Cloud gate | New AI provider added outside `VisionAi::Router#dispatch` | Medium | Critical (Ps. 56) | `gitnexus impact --target VisionAi::Router#dispatch` | oracle |
| R-10 | Compliance docs | Doc edited without `CHECKSUMS.txt` update | High | Medium | `spec/compliance/documents_spec.rb` | CI |
| R-11 | Coverage | SimpleCov threshold not enforced; coverage regresses silently | High | Medium | Add `SimpleCov.minimum_coverage 90` (DOD G-B3) | fixer |
| R-12 | Mobile tests | Reported 104 vs static 96 `@Test`; gate not reproducible | Medium | Medium | Publish Gradle XML report; reconcile (DOD G-M1) | test-engineer |
| R-13 | Mobile signing | Keystore gate removed or signer made optional for "offline mode" | Low | Critical | `MainActivity.kt:134`; `KtorClientFactoryTest.kt:23` | oracle |
| R-14 | ESP32 | Device clock skew widens accepted timestamp window | Medium | High | HMAC + timestamp window spec; W1-C1-01 | code-reviewer |
| R-15 | Shift auth | Migration re-introduces default `'face'` | Low | Critical (biometric fiction) | `schema.rb:815` CHECK; gate G1 grep | code-reviewer |
| R-16 | Breach SLA | Timezone handling on `detected_at` shifts 72 h boundary | Low | High | W3-C4-01 boundary spec | test-engineer |

## 6. Addition Rule

For every new plan: create `docs/qa/QA_<slug>.md` with scenario IDs `<slug>-C<cat>-<nn>`, add a row block in §2, update §3 counts, add E2E ledger row in §4, and register new risks in §5 before `fixer` dispatch. `fixer` returns `INTAKE_REJECTED: missing [QA|Masters]` otherwise.
