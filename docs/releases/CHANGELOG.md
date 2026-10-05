# Changelog — SkyPrivilege Mobile (KMP)

All notable changes to the tablet app. Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/); newest first. One entry per `Implemented` row in [`plans/PLAN_MASTER.md`](../plans/PLAN_MASTER.md) §2.1, landed commit as reference (backend RFC-0001 §5.9).

## Release tags

Annotated SemVer tags on `main`, backfilled from the root commit (Release Taxonomy decision, 2026-10-05). The tablet app versions independently from the backend; the compatibility matrix below is the only link. MAJOR = wire contract break with the backend. MINOR = backwards-compatible feature. PATCH = fix-only. `composeApp/build.gradle.kts` `versionName` must match the tag at release time (currently `1.0.0`, bump to `2.0.0` in the next code commit).

| Tag | Commit | Date | Scope | Requires backend |
|---|---|---|---|---|
| `v2.0.0` | `765a7ca` | 2026-10-04 | Audit Remediation Round 2. Breaking wire contract: static `X-Device-Token` replaced by `X-Timestamp`/`X-Nonce`/`X-Device-Signature`; DTOs carry only masked PNR fields; Detekt; docs guard | `>= v3.0.0` (verified on `v4.0.0`) |
| `v1.1.0` | `2addea9` | 2026-09-25 | Baggage wrapping size selection, dynamic Moka pricing, multiplatform clock, Ktor LLM timeout, E2E scenario IDs | `>= v1.1.0` (D-2 prices on `>= v2.2.0`) |
| `v1.0.0` | `02f16f5` | 2026-09-20 | POS MVP GA: 2-stage checklist gate, camera viewfinder, zero-disk ticket photo, pull-to-refresh, auto shift status, release NSC, Opus blockers closed | `>= v1.0.0` |
| `v0.2.0` | `f6f2726` | 2026-09-19 | `X-Device-Token` auth, GPS attendance map, BCA Mobile style 5-slot nav, flat icons | `v0.x` |
| `v0.1.0` | `9e9f692` | 2026-09-19 | Scaffold, domain models, authenticity checklist, shift management, zero-gallery policy, first runnable `MainActivity` scanner | `v0.x` |

## [Unreleased]

### Added
- Documentation taxonomy per backend RFC-0001: mobile-scoped `PLAN_MASTER`, `DOD_MASTER`, `QA_MASTER` with pinned cross-repo URLs; `releases/CHANGELOG.md`; `docs/README.md`.

### Changed
- Pre-commit docs guard hardened (Opus review H1/H2/M1): code scope now covers `commonMain/`, `androidMain/`, `gradle/`, `build.gradle.kts`, `settings.gradle.kts`, `scripts/`; evidence must live under `docs/dod|qa|plans|releases/` or `commonTest/`; `SKIP_DOCS=1` requires `SKIP_DOCS_REASON` of at least 10 chars.

### Removed
- Empty `docs/superpowers/` folder.

## Mobile KMP — Audit Remediation (Round 2) — 2026-10-04 — `34a2398`

Plan: [`STAR_MOBILE_AUDIT_REMEDIATION.md`](../plans/STAR_MOBILE_AUDIT_REMEDIATION.md). Review: Fable 5.1 + Opus dual audit, `CHANGES_REQUESTED` → `APPROVED`. Commits `60d6ecf` → `aee5aaf` → `465a7c3` → `34a2398`.

### Changed
- Telemetry fields are nullable; hardcoded mock GPS (`-6.1256, 106.6558`), accuracy, and Wi-Fi BSSID/SSID removed (M-1).
- DTOs carry only `pnr_masked`, `masked_display_name`, `pnr_canonical_hash`; plaintext `passenger_name` and PNR removed from wire, memory, toasts, and logs (M-2).
- Emergency voucher sends canonical hash `sha256Hex(pnr.trim().uppercase())`, matching backend `PnrCanonicalizer` (M-3).
- `PiiMasker` splits ICAO `SURNAME/FIRST` names; fixtures use `PAX_A/PAX_B MR`, zero human-name placeholders (MB-11).

### Security
- Keystore signer built on `Dispatchers.Default`; request signing off the main thread; UI gated with "Menyiapkan Kios…" until the signer is ready (M-4, M-5).
- Every mutating request carries `X-Timestamp`, `X-Nonce`, `X-Device-Signature`; static `X-Device-Token` removed (M-6, ADR-0008).

### Fixed
- HTTP 409 on double submit surfaces as typed `NonStackingConflictException`; dialog stays open (M-6).
- Typed `TicketVerificationException` carries reason code and prior redemption (MB-10).
- Guideline ETag 304 with empty cache throws instead of returning a silent empty list (C-F3).

## Pre-registry (September 2026)

| Date | Scope | Commits |
|---|---|---|
| 2026-09-25 | Ktor HTTP timeout for LLM inference; E2E scenario IDs, `bag_count` multiplier, dual-SIM; KMP baggage wrapping and pricing flow tests; multiplatform clock replaces `System.currentTimeMillis` | `2addea9`, `fe30deb`, `eb26871`, `902eaa9` |

## Adding an entry

Every `Implemented` registry row gets an entry here in the same commit. Sections: Added / Changed / Deprecated / Removed / Fixed / Security. Name the plan, the ADRs exercised (backend `decisions/`), and the landed SHA. On tag, write `RELEASE_vX.Y.Z.md` and move `[Unreleased]` under it.
