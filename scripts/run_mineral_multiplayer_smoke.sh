#!/usr/bin/env bash
# Real independent remote clients; reuses the configured JDK, Gradle and Xorg.
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
if [[ "${JDTE_SMOKE_PREBUILT:-0}" != 1 ]]; then
    bash gradlew jar serverSmokeJar -PjdteRemoteSmoke -PjdteCaptureRemoteLaunch=/tmp/jdte-remote-launch.json runClient --no-daemon --max-workers=2 "$@"
else
    [[ -f /tmp/jdte-remote-launch.json && -f build/classes/java/clientSmoke/com/jdte/client/smoke/RemoteMultiplayerSmoke.class && -f 'build/libs/[Forge] jdte-0.5.8-forge-1.20.1.jar' ]] || {
        printf '%s\n' 'Build all source sets and capture the remote launch before requesting a prebuilt run.' >&2
        exit 1
    }
fi
python scripts/mineral_multiplayer_smoke.py
