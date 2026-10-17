#!/usr/bin/env bash
# Fails when Keycloak's own login template, in the Keycloak image docker-compose.local.yml runs, is no longer the one
# each theme's template.ftl was copied from (parentTemplateSha256 in its theme.properties). The copy is then brought up
# to date with the new release's changes, and the hash updated, as the authentication document describes.
set -euo pipefail
cd "$(dirname "$0")"

engine=$(command -v docker || command -v podman)
image=$(grep -o -m1 'quay.io/keycloak/keycloak:[0-9][0-9.]*' "$(git rev-parse --show-toplevel)/docker-compose.local.yml")
version=${image##*:}
work=$(mktemp -d)
container=$("$engine" create "$image")
trap '"$engine" rm "$container" >/dev/null; rm -rf "$work"' EXIT

"$engine" cp "$container:/opt/keycloak/lib/lib/main/org.keycloak.keycloak-themes-$version.jar" "$work/themes.jar"
unzip -p "$work/themes.jar" theme/keycloak.v2/login/template.ftl > "$work/template.ftl"
actual=$(sha256sum "$work/template.ftl" 2>/dev/null || shasum -a 256 "$work/template.ftl")
actual=${actual%% *}

status=0
for properties in themes/*/login/theme.properties; do
  expected=$(sed -n 's/^parentTemplateSha256=//p' "$properties")
  if [[ "$expected" != "$actual" ]]; then
    echo "${properties%/theme.properties}/template.ftl was copied from another keycloak.v2 template than Keycloak $version's ($actual)"
    status=1
  fi
done
exit "$status"
