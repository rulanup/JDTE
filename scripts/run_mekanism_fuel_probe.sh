#!/usr/bin/env bash
# Validate the actual installed Forge Extras recipe with the production JDTE JAR.
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
: "${JDTE_LIFECYCLE_SERVER:?Use a marked disposable Forge server}"
: "${JDTE_MEKE_JAR:?Path to the existing Forge 1.20.1 Mekanism Extras JAR}"
: "${JDTE_MEKANISM_JAR:?Path to the existing Mekanism 10.4.16.80 JAR}"
server_dir="$(realpath -- "$JDTE_LIFECYCLE_SERVER")"
[[ -f "$server_dir/.jdte-lifecycle-fixture" ]] || { printf '%s\n' 'Refusing an unmarked server' >&2; exit 1; }
python - "$server_dir" <<'PY'
from pathlib import Path
import sys
p=Path(sys.argv[1]);props=dict(line.split('=',1) for line in (p/'server.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
assert props.get('server-ip')=='127.0.0.1' and props.get('level-name')=='lifecycle-world'
PY
cd "$repo_dir"
if [[ "${JDTE_SMOKE_PREBUILT:-0}" != 1 ]]; then bash gradlew jar serverSmokeJar --no-daemon --max-workers=2 "$@"; fi
cp 'build/libs/[Forge] jdte-0.5.8-forge-1.20.1.jar' 'build/libs/[Forge] jdte-0.5.8-forge-1.20.1-lifecycle-probe.jar' "$JDTE_MEKE_JAR" "$JDTE_MEKANISM_JAR" "$server_dir/mods/"
python - "$server_dir" "${JAVA_HOME:?Use the existing Java 17 installation}/bin/java" <<'PY'
from pathlib import Path
import subprocess,sys
server=Path(sys.argv[1]);marker=server/'meke-fuel-success.txt';marker.unlink(missing_ok=True)
with (server/'meke-fuel.log').open('w') as log:
 result=subprocess.run([sys.argv[2],'-Xmx3G','-Djdte.mekeFuel=true','@libraries/net/minecraftforge/forge/1.20.1-47.4.10/unix_args.txt','--nogui'],cwd=server,stdout=log,stderr=subprocess.STDOUT,timeout=240)
assert result.returncode==0,f'Server exit {result.returncode}; inspect meke-fuel.log'
assert marker.is_file(),'Fuel probe did not pass; inspect meke-fuel.log'
print(marker.read_text().strip());print((server/'meke-fuel-observations.txt').read_text().strip())
PY
