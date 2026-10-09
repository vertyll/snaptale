#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

status=0

for entry in CONTENTS.md GLOSSARY.md STANDARDS.md; do
  if ! grep -qE "\]\((\./)?${entry//./\\.}\)" README.md; then
    echo "README.md does not link $entry"
    status=1
  fi
done

while IFS= read -r doc; do
  [ "$doc" = CONTENTS.md ] && continue
  [ -f "$doc" ] || continue
  pattern="\]\((\./)?${doc//./\\.}(#[^)]*)?\)"
  if ! grep -qE "$pattern" CONTENTS.md; then
    echo "CONTENTS.md does not list $doc"
    status=1
  fi
done < <(git ls-files --cached --others --exclude-standard '*.md')

exit "$status"
