#!/usr/bin/env bash
# Real Forge client sender smoke run under Xvfb + Mesa software rendering.
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"
smoke_display_pid=''
cleanup() {
    if [[ -n "$smoke_display_pid" ]]; then
        kill "$smoke_display_pid" 2>/dev/null || :
        wait "$smoke_display_pid" 2>/dev/null || :
    fi
}
trap cleanup EXIT
if [[ -z "${DISPLAY:-}" ]]; then
    for smoke_display_number in {97..107}; do
        if [[ ! -e "/tmp/.X11-unix/X${smoke_display_number}" && ! -e "/tmp/.X${smoke_display_number}-lock" ]]; then
            export DISPLAY=":${smoke_display_number}"
            break
        fi
    done
    if [[ -z "${DISPLAY:-}" ]]; then printf '%s\n' 'No unused smoke display was found.' >&2; exit 1; fi
    Xvfb "$DISPLAY" -screen 0 1280x720x24 > /tmp/jdte-sender-xvfb.log 2>&1 &
    smoke_display_pid=$!
    sleep 2
fi
export LIBGL_ALWAYS_SOFTWARE="${LIBGL_ALWAYS_SOFTWARE:-1}"
export ALSOFT_DRIVERS="${ALSOFT_DRIVERS:-null}"
python3 - <<'PY'
from pathlib import Path
import os, shutil
client = Path('runs/client')
world = client / 'saves/jdte-mineral-client-smoke'
source = Path(os.environ.get('JDTE_SMOKE_SOURCE_WORLD', 'runs/server/jdte-port-smoke'))
if not (source / 'level.dat').is_file():
    source = Path('runs/gameTestServer/world')
if not (source / 'level.dat').is_file():
    raise SystemExit('Create a Forge development server world first, or set JDTE_SMOKE_SOURCE_WORLD to a test world directory.')
marker = world / '.jdte-client-smoke-world'
if world.exists():
    if not marker.exists():
        raise SystemExit(f'Refusing to replace an unmarked world: {world}. Inspect or move it before running this harness.')
    shutil.rmtree(world)
world.parent.mkdir(parents=True, exist_ok=True)
shutil.copytree(source, world)
marker.touch()
options = client / 'options.txt'
settings = {'tutorialStep': 'none', 'renderDistance': '2', 'simulationDistance': '2', 'maxFps': '60', 'guiScale': '2', 'overrideWidth': '1280', 'overrideHeight': '720', 'soundCategory_master': '0.0'}
lines = options.read_text().splitlines() if options.exists() else []
lines = [line for line in lines if line.split(':', 1)[0] not in settings]
options.write_text('\n'.join(lines + [key + ':' + value for key, value in settings.items()]) + '\n')
(client / 'sender-client-smoke-success.txt').unlink(missing_ok=True)
PY
python3 ~/workspace/run-gradle.py -PjdteSenderSmoke runClient > ~/workspace/jdte-sender-client.log 2>&1; echo "EXIT:$?"
test -s runs/client/sender-client-smoke-success.txt
cat runs/client/sender-client-smoke-success.txt
