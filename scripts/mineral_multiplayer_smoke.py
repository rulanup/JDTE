#!/usr/bin/env python3
"""Drive two real Forge clients and two successive dedicated server processes.

Server-side fixture assertions and client actions live in separate test-only source sets.
This coordinator only launches processes and waits for their explicit results.
"""
import json, os, shutil, subprocess, time, uuid
from smoke_runtime import KEEP, filtered_command
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
SERVER = Path(os.environ['JDTE_LIFECYCLE_SERVER']).resolve()
SCENARIO = os.environ.get('JDTE_REMOTE_SCENARIO', 'mineral')
assert SCENARIO in {'mineral','mixer','generator','bio','wand','potion'}
CONTROL = Path(f'/tmp/jdte-{SCENARIO}-remote-control' if SCENARIO != 'mineral' else '/tmp/jdte-remote-control')
assert (SERVER / '.jdte-lifecycle-fixture').is_file(), 'Use the existing marked disposable server'
props = dict(line.split('=', 1) for line in (SERVER / 'server.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
assert props.get('server-ip') == '127.0.0.1' and props.get('server-port') == '25579', 'Use loopback port 25579'
if SCENARIO in {'bio','wand','potion'}:
    # Preserve prior worlds/evidence and isolate absolute cooldown/resource ledgers on every seed.
    world = SCENARIO + '-live-' + uuid.uuid4().hex[:12]
    file = SERVER / 'server.properties'
    lines = [line for line in file.read_text().splitlines() if not line.startswith('level-name=')]
    file.write_text('\n'.join(lines + ['level-name=' + world]) + '\n')
    print('Disposable ' + SCENARIO + ' world:', world, flush=True)

if CONTROL.exists():
    assert (CONTROL / '.fixture').is_file(), 'Refusing an unmarked control directory'
    shutil.rmtree(CONTROL)
CONTROL.mkdir(); (CONTROL / '.fixture').touch()
for jar in ['[Forge] jdte-0.5.8-forge-1.20.1.jar', '[Forge] jdte-0.5.8-forge-1.20.1-lifecycle-probe.jar']:
    shutil.copy2(REPO / 'build/libs' / jar, SERVER / 'mods' / jar)
launch = json.loads(Path('/tmp/jdte-remote-launch.json').read_text())
assert launch['command'][0] and '--quickPlayMultiplayer' in launch['command'], 'Capture a configured remote launch first'
processes = []; logs = []
env = dict(os.environ, **launch['environment'])
def start_server(mode):
    log = (CONTROL / f'server-{mode}.log').open('w'); logs.append(log)
    process = subprocess.Popen([str(Path(os.environ['JAVA_HOME']) / 'bin/java'), '-Xmx3G', f'-Djdte.{(SCENARIO+"Multiplayer") if SCENARIO != "mineral" else "multiplayer"}={mode}', f'-Djdte.remoteControl={CONTROL}', '@libraries/net/minecraftforge/forge/1.20.1-47.4.10/unix_args.txt', '--nogui'], cwd=SERVER, stdout=log, stderr=subprocess.STDOUT)
    processes.append(process); return process

def start_client(role, round_number):
    folder = CONTROL / f'client-{role}'; folder.mkdir(exist_ok=True)
    (folder / 'options.txt').write_text('tutorialStep:none\nrenderDistance:2\nsimulationDistance:5\nmaxFps:30\nguiScale:2\nsoundCategory_master:0.0\n')
    command = filtered_command(launch,folder,KEEP)
    for key, value in [('--username', 'JDTE' + role), ('--gameDir', str(folder))]:
        command[command.index(key) + 1] = value
    if SCENARIO in {'mixer','generator','bio','wand','potion'}:
        command = [arg.replace('-Djdte.remoteSmoke=true','-Djdte.remoteSmoke=false') for arg in command]
        command.insert(1,f'-Djdte.{SCENARIO}RemoteSmoke=true')
        command.insert(1,f'-Djdte.remoteControl={CONTROL}')
    command.insert(1, '-Xmx3G')
    log = (CONTROL / f'client-{role}-{round_number}.log').open('w'); logs.append(log)
    process = subprocess.Popen(command, cwd=folder, env=env, stdout=log, stderr=subprocess.STDOUT)
    processes.append(process); return process

def wait_for(predicate, label, timeout=300):
    until = time.monotonic() + timeout
    while time.monotonic() < until:
        for process in processes:
            if process.poll() is not None and process.returncode != 0:
                raise RuntimeError(f'Test JVM {process.pid} exited {process.returncode}; inspect {CONTROL}')
        failures = [p for p in CONTROL.glob('*failure*') if p.is_file()]
        if failures:
            raise RuntimeError('; '.join(p.read_text() for p in failures))
        if predicate():
            print('PASS:', label, flush=True); return
        time.sleep(0.5)
    raise TimeoutError(f'{label}; inspect {CONTROL}')

def phase(value):
    p = CONTROL / 'phase'
    return p.exists() and p.read_text().strip() == str(value)

try:
    server = start_server('seed'); wait_for(lambda: phase(0), 'dedicated server ready', 150)
    one = start_client('One', 0); two = start_client('Two', 0)
    wait_for(lambda: phase(7), 'two-client simultaneous interactions and first disconnect', 420)
    wait_for(lambda: (CONTROL / 'seed-success').is_file(), 'reconnect, chunk unload and saved shutdown')
    assert server.wait(timeout=90) == 0
    server = start_server('restart'); wait_for(lambda: phase(9), 'fresh dedicated server ready', 150)
    wait_for(lambda: (CONTROL / 'restart-success').is_file(), 'disk restoration and two-client synchronized menus')
    assert server.wait(timeout=90) == 0
    assert one.wait(timeout=60) == 0 and two.wait(timeout=60) == 0
    print((CONTROL / 'seed-success').read_text().strip(), flush=True)
    print((CONTROL / 'restart-success').read_text().strip(), flush=True)
finally:
    for process in processes:
        if process.poll() is None: process.terminate()
    for process in processes:
        try: process.wait(timeout=30)
        except subprocess.TimeoutExpired: process.kill(); process.wait()
    for log in logs: log.close()
