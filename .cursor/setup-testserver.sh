#!/usr/bin/env bash
# Prepares a local Minecraft test server (Purpur + Vault) used to run SeriousRP.
# Idempotent: only downloads artifacts that are missing so it is safe to re-run.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT/run"
PLUGINS_DIR="$RUN_DIR/plugins"

# Purpur is a Paper/Spigot-compatible server. SeriousRP targets Minecraft 1.21
# and is compiled against the 1.21.9 API, so we pin a matching server build.
PURPUR_VERSION="1.21.9"
PURPUR_BUILD="2505"
# Vault is a hard dependency declared in plugin.yml.
VAULT_VERSION="1.7.3"

mkdir -p "$PLUGINS_DIR"

if [ ! -f "$RUN_DIR/purpur.jar" ]; then
  echo "Downloading Purpur ${PURPUR_VERSION} build ${PURPUR_BUILD}..."
  curl -fsSL -o "$RUN_DIR/purpur.jar" \
    "https://api.purpurmc.org/v2/purpur/${PURPUR_VERSION}/${PURPUR_BUILD}/download"
fi

if [ ! -f "$PLUGINS_DIR/Vault.jar" ]; then
  echo "Downloading Vault ${VAULT_VERSION}..."
  curl -fsSL -o "$PLUGINS_DIR/Vault.jar" \
    "https://github.com/MilkBowl/Vault/releases/download/${VAULT_VERSION}/Vault.jar"
fi

# Accept the Minecraft EULA for this local development test server only.
echo "eula=true" > "$RUN_DIR/eula.txt"

if [ ! -f "$RUN_DIR/server.properties" ]; then
  cat > "$RUN_DIR/server.properties" <<'PROPS'
online-mode=false
level-type=minecraft\:flat
spawn-protection=0
view-distance=4
simulation-distance=4
max-players=5
motd=SeriousRP Dev Server
enable-command-block=false
allow-nether=false
generate-structures=false
PROPS
fi

echo "Test server prepared in ${RUN_DIR}"
