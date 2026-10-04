#!/bin/sh
# Same-Commit Sync guard (AGENTS.md "Mechanical Same-Commit Sync & Doc Enforcement").
# Rejects commits that touch composeApp/src/ without touching docs/, CHANGELOG.md, or commonTest/.
# Install: ln -sf ../../scripts/pre-commit-docs-guard.sh .git/hooks/pre-commit
# Bypass (justify in commit body): SKIP_DOCS=1 git commit ...
set -eu

CODE='^composeApp/src/'
EVIDENCE='^(docs/|CHANGELOG\.md$|composeApp/src/commonTest/)'

[ "${SKIP_DOCS:-}" = "1" ] && exit 0

staged=$(git diff --cached --name-only)
echo "$staged" | grep -Eq "$CODE" || exit 0
echo "$staged" | grep -Eq "$EVIDENCE" && exit 0

{
  echo '[HOOK REJECTED] Code modified without updating docs/ or DoD evidence! Keep code and docs in sync (Same-Commit Sync).'
  echo 'Staged code files:'
  echo "$staged" | grep -E "$CODE" | sed 's/^/  /'
  echo 'Stage the matching DOD_<slug>.md / QA_<slug>.md / docs/ change, or bypass with: SKIP_DOCS=1 git commit ...'
} >&2
exit 1
