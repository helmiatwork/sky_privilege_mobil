# SkyPrivilege Mobile (KMP) — Documentation Map

Standard: backend RFC-0001 (<https://github.com/helmiatwork/sky_privilege/blob/fbb0780/docs/rfcs/RFC-0001-documentation-taxonomy-and-lifecycle.md>). Same skeleton as `backend/docs/`; this repo owns mobile documents only. `compliance/` is backend-only (the data controller lives there): <https://github.com/helmiatwork/sky_privilege/blob/fbb0780/docs/compliance/README.md>.

## Tree

| Folder | Question it answers | Lifecycle | Start here |
|---|---|---|---|
| `architecture/` | What **is** | Living | Not yet created. The mobile architecture document is [`../PLAN.md`](../PLAN.md) (Konsep & Spesifikasi Teknis); it moves to `architecture/MOBILE_ARCHITECTURE.md` on its next rewrite (`PLAN_MASTER.md` §6). |
| `decisions/` | **Why** | Frozen | Mobile decisions ADR-0008 (device signing) and ADR-0015 (offline outbox) are recorded in the backend `decisions/`: <https://github.com/helmiatwork/sky_privilege/blob/fbb0780/docs/decisions/README.md>. Created here with the first mobile-only ADR. |
| `rfcs/` | What is **proposed** | Draft → Accepted / Rejected | on demand |
| [`plans/`](plans/PLAN_MASTER.md) · [`dod/`](dod/DOD_MASTER.md) · [`qa/`](qa/QA_MASTER.md) | What is **contracted** (Tri-Document) | Draft → In Progress → Implemented | [`PLAN_MASTER.md`](plans/PLAN_MASTER.md) · [`DOD_MASTER.md`](dod/DOD_MASTER.md) · [`QA_MASTER.md`](qa/QA_MASTER.md) |
| `audits/` | What is **proven** | Frozen | on demand |
| `runbooks/` | How to **operate** (engineers) | Living | on demand (device enrolment, keystore reset) |
| [`releases/`](releases/CHANGELOG.md) | What **shipped** | Frozen per release | [`CHANGELOG.md`](releases/CHANGELOG.md) |
| `histories/` | What **was** | Frozen | on demand; `README.md` index required with the first file |
| `security/` · `incidents/` · `feedback/` | Threat & proof · What went wrong · Voice of user | on demand | Created with the first document. No placeholders. |

## How to add a document

1. Pick the folder by the question it answers. If none fits, it is probably an RFC.
2. Copy the frontmatter block from RFC-0001 §4.2.
3. Plans: Enhanced STAR structure with a formal DoD table (`DoD ID | Criteria | Evidence (file:line) | Status`) is mandatory (`../AGENTS.md`). Ship `PLAN_<slug>.md` + `DOD_<slug>.md` + `QA_<slug>.md` and a row in each `*_MASTER.md` in one commit.
4. A plan lives in the repo that owns most of the changed code. Backend-owned waves that affect the tablet get one row in `PLAN_MASTER.md` §2.2 with a URL pinned to a backend SHA; never a relative path across the repo boundary.
5. Every `Implemented` row gets a `releases/CHANGELOG.md` entry in the same commit.
6. Retiring a document: `git mv` into `histories/`, add its index row, set `superseded_by`. Never delete.

## Status enums

Identical to the backend map (RFC-0001 §4.1): RFC Draft/Accepted/Rejected/Superseded · ADR Proposed/Accepted/Deprecated/Superseded · Plan Draft/In Progress/Implemented/Deprecated · DoD PASSED/FAILED/PARTIAL/NOT VERIFIED/N/A · QA Automated/Manual/Blocked/N/A · Release Draft/Released/Yanked · Living doc Current/Stale.

## Stale

Living documents whose `review_by` has passed. Must be empty before a release is tagged.

- None as of 2026-10-04. `../PLAN.md` carries no `review_by`; set one when it moves to `architecture/`.

## Language

Engineering documents in English. Field-facing strings and SOPs are backend `sop/` (Bahasa Indonesia). `../PLAN.md` is in Bahasa Indonesia and is not retro-fitted.
