# Changelog — SkyPrivilege Mobile (KMP)

All notable changes to the tablet app. Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/); newest first. One entry per `Implemented` row in [`plans/PLAN_MASTER.md`](../plans/PLAN_MASTER.md) §2.1, landed commit as reference (backend RFC-0001 §5.9).

No git tag exists yet; version labels are logical until the first tag.

## [Unreleased]

### Added
- Documentation taxonomy per backend RFC-0001: mobile-scoped `PLAN_MASTER`, `DOD_MASTER`, `QA_MASTER` with pinned cross-repo URLs; `releases/CHANGELOG.md`; `docs/README.md`.

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
