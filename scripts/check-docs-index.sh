#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

status=0
while IFS= read -r doc; do
  [ "$doc" = README.md ] && continue
  [ -f "$doc" ] || continue
  pattern="\]\((\./)?${doc//./\\.}(#[^)]*)?\)"
  if ! grep -qE "$pattern" README.md; then
    echo "README.md does not index $doc"
    status=1
  fi
done < <(git ls-files --cached --others --exclude-standard '*.md')

exit "$status"
