#!/usr/bin/env bash
# Builds SeriousRP, deploys it into the local test server, and runs the server.
# Console commands (e.g. "seriousrp version") can be typed into this terminal.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

# Ensure the server + Vault are present (no-op when already downloaded).
"$ROOT/.cursor/setup-testserver.sh"

echo "Building SeriousRP..."
./gradlew build --no-daemon -q

cp build/libs/SeriousRP-*.jar run/plugins/

cd run
exec java -Xms1G -Xmx2G -jar purpur.jar nogui
