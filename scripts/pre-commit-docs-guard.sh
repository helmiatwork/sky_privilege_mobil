#!/bin/sh
# Same-Commit Sync guard (AGENTS.md "Mechanical Same-Commit Sync & Doc Enforcement").
# Rejects commits that touch code without touching DoD/QA/plan/release docs or commonTest/.
# Install: ln -sf ../../scripts/pre-commit-docs-guard.sh .git/hooks/pre-commit
# Bypass (justify in commit body too): SKIP_DOCS=1 SKIP_DOCS_REASON="..." git commit ...
set -eu

CODE='^(composeApp/src/(commonMain|androidMain)/|gradle/|build\.gradle\.kts|settings\.gradle\.kts|scripts/)'
EVIDENCE='^(docs/(dod/|qa/|plans/|releases/)|composeApp/src/commonTest/)'

if [ "${SKIP_DOCS:-}" = "1" ]; then
  reason="${SKIP_DOCS_REASON:-}"
  if [ -z "$reason" ] || [ "${#reason}" -lt 10 ]; then
    echo '[HOOK REJECTED] SKIP_DOCS=1 requires SKIP_DOCS_REASON="..." (at least 10 chars justification).' >&2
    exit 1
  fi
  echo "[HOOK BYPASS] Docs guard bypassed. Reason: $reason"
  exit 0
fi

staged=$(git diff --cached --name-only)
echo "$staged" | grep -Eq "$CODE" || exit 0
echo "$staged" | grep -Eq "$EVIDENCE" && exit 0

{
  echo '[HOOK REJECTED] Code modified without updating docs/ or DoD evidence! Keep code and docs in sync (Same-Commit Sync).'
  echo 'Staged code files:'
  echo "$staged" | grep -E "$CODE" | sed 's/^/  /'
  echo 'Stage the matching docs/dod/, docs/qa/, docs/plans/, docs/releases/, or commonTest/ change,'
  echo 'or bypass with: SKIP_DOCS=1 SKIP_DOCS_REASON="<why, >=10 chars>" git commit ...'
} >&2
exit 1
