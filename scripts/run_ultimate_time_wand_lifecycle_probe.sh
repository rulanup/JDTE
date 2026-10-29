#!/usr/bin/env bash
# Run the separately reobfuscated probe in two actual production Forge server JVMs.
# Requires an existing, dedicated disposable Forge 47.4.10 installation with runtime mods.
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
: "${JDTE_LIFECYCLE_SERVER:?Set JDTE_LIFECYCLE_SERVER to the disposable production Forge server directory}"
server_dir="$(realpath -- "$JDTE_LIFECYCLE_SERVER")"
[[ -f "$server_dir/.jdte-lifecycle-fixture" ]] || { printf '%s\n' 'Refusing an unmarked server; create .jdte-lifecycle-fixture only in a disposable test server.' >&2; exit 1; }
[[ -f "$server_dir/libraries/net/minecraftforge/forge/1.20.1-47.4.10/unix_args.txt" ]] || { printf '%s\n' 'Forge 47.4.10 server installation is required.' >&2; exit 1; }
python - "$server_dir" <<'PY'
from pathlib import Path
import sys
p=Path(sys.argv[1]);props=dict(line.split('=',1) for line in (p/'server.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
assert props.get('server-ip')=='127.0.0.1','Use a loopback-only test server'
assert props.get('level-name')=='wand-lifecycle-world','Use the dedicated wand-lifecycle-world fixture'
PY
cd "$repo_dir"
if [[ "${JDTE_SMOKE_PREBUILT:-0}" != 1 ]]; then
    bash gradlew jar serverSmokeJar --no-daemon --max-workers=2 "$@"
fi
cp 'build/libs/[Forge] jdte-0.5.8-forge-1.20.1.jar' 'build/libs/[Forge] jdte-0.5.8-forge-1.20.1-lifecycle-probe.jar' "$server_dir/mods/"
python - "$server_dir" "${JAVA_HOME:?Use the existing Java 17 installation}/bin/java" <<'PY'
from pathlib import Path
import subprocess,sys,uuid
server=Path(sys.argv[1]);java=sys.argv[2]
# Preserve prior worlds, then isolate every seed from failed prior effect entities.
world=server/'wand-lifecycle-world'
if world.exists():world.rename(server/('wand-lifecycle-world-before-'+uuid.uuid4().hex[:12]))
for mode,marker in [('seed','wand-seed-success'),('verify','wand-verify-success')]:
    success=server/marker;success.unlink(missing_ok=True)
    with (server/f'wand-{mode}.log').open('w') as log:
        completed=subprocess.run([java,'-Xmx3G',f'-Djdte.wandLifecycle={mode}','@libraries/net/minecraftforge/forge/1.20.1-47.4.10/unix_args.txt','--nogui'],cwd=server,stdout=log,stderr=subprocess.STDOUT,timeout=240)
    assert completed.returncode==0,f'{mode} server exited {completed.returncode}; inspect wand-{mode}.log'
    assert success.exists(),f'{mode} probe did not pass; inspect wand-{mode}.log'
    print(success.read_text().strip())
PY
