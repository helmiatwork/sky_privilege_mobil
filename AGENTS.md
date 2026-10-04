# SkyPrivilege Mobile (Kotlin Multiplatform)

Delta only. Project rules: `../AGENTS.md`. Global rules: `~/.gemini/AGENTS.md`.

## Commands
- Tests: `./gradlew :composeApp:allTests` (shared logic lives in `commonTest`).
- Build check: `./gradlew :composeApp:assemble`.
- Lint: `./gradlew :composeApp:lint`.
- GitNexus pre-commit: `rtk gitnexus detect-changes -C mobile`.

## Invariant Spec Notes (Pillar A)
- Business logic and tests go in `commonMain` / `commonTest`; platform code only in `androidMain` / `iosMain` via `expect` / `actual`.
- Fixer engine is Claude Sonnet only (see `../AGENTS.md`): Gemini Flash produced KMP compiler errors and is disabled for this module.
- Attestation and kill-switch flows (PLAN.md §3.3) are security paths: every change needs a tamper GWT scenario.
- Plan Document Invariant: Any implementation plan written for mobile MUST use the Enhanced STAR structure with a formal DoD table (`DoD ID | Criteria | Evidence (file:line) | Status`). Generic checklists are forbidden.
