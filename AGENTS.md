# SkyPrivilege Mobile (Kotlin Multiplatform)

Delta only. Project rules: `../AGENTS.md`. Global rules: `~/.gemini/AGENTS.md`.

## Commands
- Tests: `./gradlew :composeApp:allTests` (shared logic lives in `commonTest`).
- Build check: `./gradlew :composeApp:assemble`.
- Lint: `./gradlew :composeApp:lint`.
- GitNexus pre-commit: `rtk gitnexus detect-changes -C mobile`.
- Static analysis: `rtk ./gradlew detekt` (0 new issues; rules in `config/detekt/detekt.yml`, legacy debt frozen in `config/detekt/baseline.xml`, never regenerate baseline to hide new findings).

## Mechanical Same-Commit Sync & Doc Enforcement (MANDATORY)
- **Rule**: Any change under `composeApp/src/` MUST ship in the same commit as its `DOD_<slug>.md` (or `docs/dod/DOD_MASTER.md`), `QA_<slug>.md`, `CHANGELOG.md`, `composeApp/src/commonTest/`, or other `docs/` evidence.
- **Rejection**: `code-reviewer` and `oracle` reject with `CHANGES_REQUESTED` when code lands without evidence.
- **Hook**: `scripts/pre-commit-docs-guard.sh` linked to `.git/hooks/pre-commit`. Install: `ln -sf ../../scripts/pre-commit-docs-guard.sh .git/hooks/pre-commit`. Rejection prints `[HOOK REJECTED] Code modified without updating docs/ or DoD evidence!`. Bypass: `SKIP_DOCS=1 git commit ...` with justification in body. Never `--no-verify`.

## Invariant Spec Notes (Pillar A)
- Business logic and tests go in `commonMain` / `commonTest`; platform code only in `androidMain` / `iosMain` via `expect` / `actual`.
- Fixer engine is Claude Sonnet only (see `../AGENTS.md`): Gemini Flash produced KMP compiler errors and is disabled for this module.
- Attestation and kill-switch flows (PLAN.md §3.3) are security paths: every change needs a tamper GWT scenario.
- Plan Document Invariant: Any implementation plan written for mobile MUST use the Enhanced STAR structure with a formal DoD table (`DoD ID | Criteria | Evidence (file:line) | Status`). Generic checklists are forbidden.
