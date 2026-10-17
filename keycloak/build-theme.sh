#!/usr/bin/env bash
# Packs the Keycloak theme into a JAR, which a Keycloak shared by several applications loads from its providers folder
# (keycloak/target/snaptale-keycloak-theme.jar).
set -euo pipefail
cd "$(dirname "$0")"

out="$PWD/target/snaptale-keycloak-theme.jar"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

mkdir -p "$work/META-INF" "$work/theme" target
cp -R "themes/snaptale" "$work/theme/"
cat > "$work/META-INF/keycloak-themes.json" <<'JSON'
{
  "themes": [
    {
      "name": "snaptale",
      "types": [
        "login",
        "email"
      ]
    }
  ]
}
JSON

rm -f "$out"
(cd "$work" && zip -q -r -X "$out" META-INF theme)
echo "$out"
