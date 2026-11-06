#!/usr/bin/env bash
# Real Forge client smoke run; reuses existing JDK, Gradle and optional-mod dependencies.
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"
smoke_tmpdir="$(mktemp -d /tmp/jdte-client-smoke.XXXXXX)"
smoke_display_pid=''
cleanup() {
    if [[ -n "$smoke_display_pid" ]]; then
        kill "$smoke_display_pid" 2>/dev/null || :
        wait "$smoke_display_pid" 2>/dev/null || :
    fi
    rm -rf -- "$smoke_tmpdir"
}
trap cleanup EXIT
if [[ -z "${DISPLAY:-}" ]]; then
    xorg_bin=/usr/lib/xorg/Xorg
    if [[ ! -x "$xorg_bin" || ! -f /usr/lib/xorg/modules/drivers/dummy_drv.so ]]; then
        printf '%s\n' 'A graphical display or Xorg with its dummy driver is required.' >&2
        exit 1
    fi
    cat > "$smoke_tmpdir/xorg.conf" <<'XORG'
Section "ServerFlags"
 Option "AutoAddDevices" "false"
 Option "DontVTSwitch" "true"
EndSection
Section "Device"
 Identifier "JDTE Dummy"
 Driver "dummy"
 VideoRam 256000
EndSection
Section "Monitor"
 Identifier "JDTE Monitor"
 HorizSync 5.0-1000.0
 VertRefresh 5.0-200.0
 Modeline "1280x720" 74.50 1280 1344 1472 1664 720 723 728 748
EndSection
Section "Screen"
 Identifier "JDTE Screen"
 Device "JDTE Dummy"
 Monitor "JDTE Monitor"
 DefaultDepth 24
 SubSection "Display"
  Depth 24
  Modes "1280x720"
 EndSubSection
EndSection
XORG
    for smoke_display_number in {97..107}; do
        if [[ ! -e "/tmp/.X11-unix/X${smoke_display_number}" && ! -e "/tmp/.X${smoke_display_number}-lock" ]]; then
            export DISPLAY=":${smoke_display_number}"
            break
        fi
    done
    if [[ -z "${DISPLAY:-}" ]]; then printf '%s\n' 'No unused smoke display was found.' >&2; exit 1; fi
    "$xorg_bin" "$DISPLAY" -config "$smoke_tmpdir/xorg.conf" -logfile "$smoke_tmpdir/xorg.log" -noreset -nolisten tcp -ac > "$smoke_tmpdir/xorg.stdout" 2>&1 &
    smoke_display_pid=$!
    for smoke_attempt in {1..50}; do
        if xdpyinfo -display "$DISPLAY" >/dev/null 2>&1; then break; fi
        if ! kill -0 "$smoke_display_pid" 2>/dev/null; then cat "$smoke_tmpdir/xorg.stdout" >&2; exit 1; fi
        sleep 0.1
    done
    xdpyinfo -display "$DISPLAY" >/dev/null
fi
export LIBGL_ALWAYS_SOFTWARE="${LIBGL_ALWAYS_SOFTWARE:-1}"
export ALSOFT_DRIVERS="${ALSOFT_DRIVERS:-null}"
python - <<'PY'
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
(client / 'potion-smoke-success.txt').unlink(missing_ok=True)
PY
bash ./gradlew -PjdtePotionSmoke -PjdteCaptureRemoteLaunch=/tmp/jdte-potion-client-launch.json runClient --no-daemon --max-workers=2 "$@"
python - <<'PYCLIENT'
import json,os,subprocess,sys
from pathlib import Path
sys.path.insert(0,str(Path('scripts').resolve()))
from smoke_runtime import filtered_command,KEEP
folder=Path('runs/client').resolve();launch=json.loads(Path('/tmp/jdte-potion-client-launch.json').read_text())
command=filtered_command(launch,folder,KEEP)
if '--gameDir' in command:command[command.index('--gameDir')+1]=str(folder)
with Path('/tmp/jdte-potion-client-runtime.log').open('w') as log:
 result=subprocess.run(command,cwd=folder,env=dict(os.environ,**launch['environment']),stdout=log,stderr=subprocess.STDOUT,timeout=360)
assert result.returncode==0,'Client failed; inspect /tmp/jdte-potion-client-runtime.log'
assert (folder/'potion-smoke-success.txt').is_file(),'No client success marker'
print((folder/'potion-smoke-success.txt').read_text())
PYCLIENT
