#!/usr/bin/env python3
"""One real remote client, running-server offline interval, real ME grid and two production JVMs."""
from pathlib import Path
import os,json,shutil,subprocess,time,zipfile
from smoke_runtime import filtered_command
REPO=Path(__file__).resolve().parent.parent
CONTROL=Path('/tmp/jdte-advanced-transmitter-control');SERVER=Path('/tmp/jdte-advanced-transmitter-server')
for folder,marker in [(CONTROL,'.fixture'),(SERVER,'.jdte-lifecycle-fixture')]:
    if folder.exists():assert (folder/marker).is_file(),f'Refusing unmarked directory {folder}'
    folder.mkdir(exist_ok=True);(folder/marker).touch()
for name in ['phase','client-success.txt','server-failure','advanced-transmitter-seed-success.txt','advanced-transmitter-restart-success.txt']:(CONTROL/name).unlink(missing_ok=True)
source=Path('/tmp/jdte-remote-minimal');assert (source/'.jdte-lifecycle-fixture').is_file()
if not (SERVER/'libraries').exists():(SERVER/'libraries').symlink_to((source/'libraries').resolve(),target_is_directory=True)
(SERVER/'eula.txt').write_text('eula=true\n')
(SERVER/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25579\nlevel-name=lifecycle-world\nlevel-type=minecraft:flat\nonline-mode=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\nmax-players=2\n')
mods=SERVER/'mods';mods.mkdir(exist_ok=True)
for p in (source/'mods').glob('*.jar'):
    if not p.name.startswith('[Forge] jdte-'):shutil.copy2(p,mods/p.name)
for prefix in ['applied-flux-','glodium-']:
    p=next(Path('/tmp/jdte-production-server/mods').glob(prefix+'*.jar'));shutil.copy2(p,mods/p.name)
for name in ['[Forge] jdte-0.5.8-forge-1.20.1.jar','[Forge] jdte-0.5.8-forge-1.20.1-lifecycle-probe.jar']:shutil.copy2(REPO/'build/libs'/name,mods/name)
launch=json.loads(Path('/tmp/jdte-remote-launch.json').read_text());processes=[];logs=[]
env=dict(os.environ,**launch['environment'])
def server(mode):
    log=(CONTROL/f'server-{mode}.log').open('w');logs.append(log)
    p=subprocess.Popen([str(Path(os.environ['JAVA_HOME'])/'bin/java'),'-Xmx3G',f'-Djdte.advancedTransmitterAcceptance={mode}',f'-Djdte.advancedTransmitterControl={CONTROL}','@libraries/net/minecraftforge/forge/1.20.1-47.4.10/unix_args.txt','--nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT);processes.append(p);return p
def wait(predicate,label,timeout=240):
    end=time.monotonic()+timeout
    while time.monotonic()<end:
        if (CONTROL/'server-failure').exists():raise RuntimeError((CONTROL/'server-failure').read_text())
        for p in processes:
            if p.poll() is not None and p.returncode!=0:raise RuntimeError(f'JVM exited {p.returncode}; {CONTROL}')
        if predicate():print('PASS:',label,flush=True);return
        time.sleep(.5)
    raise TimeoutError(label)
try:
    first=server('seed');wait(lambda:(CONTROL/'phase').exists(),'dedicated source/grid fixture ready')
    folder=CONTROL/'client';folder.mkdir(exist_ok=True);(folder/'options.txt').write_text('tutorialStep:none\nrenderDistance:2\nsimulationDistance:2\nmaxFps:30\nguiScale:2\nsoundCategory_master:0.0\n')
    command=filtered_command(launch,folder,{'minecraft','forge','justdirethings','guideme','ae2','appflux','glodium','curios','jei','jade','modernfix'})
    command=[x.replace('-Djdte.remoteSmoke=true','-Djdte.remoteSmoke=false') for x in command]
    command[command.index('--gameDir')+1]=str(folder);command[command.index('--username')+1]='JDTEAdvanced'
    command.insert(1,'-Xmx3G');command.insert(1,'-Djdte.advancedTransmitterRemote=true');command.insert(1,f'-Djdte.advancedTransmitterControl={CONTROL}')
    log=(CONTROL/'client.log').open('w');logs.append(log);client=subprocess.Popen(command,cwd=folder,env=env,stdout=log,stderr=subprocess.STDOUT);processes.append(client)
    wait(lambda:(CONTROL/'client-success.txt').exists() and (CONTROL/'advanced-transmitter-seed-success.txt').exists(),'real dedicated logout/offline ticks/unload/rejoin/Curios and real ME demand batch',300)
    assert first.wait(timeout=60)==0 and client.wait(timeout=60)==0
    second=server('verify');wait(lambda:(CONTROL/'advanced-transmitter-restart-success.txt').exists(),'installed ME disk/node recover in second JVM and deliver remaining FE exactly once')
    assert second.wait(timeout=60)==0
    for name in ['client-success.txt','advanced-transmitter-seed-success.txt','advanced-transmitter-restart-success.txt']:print((CONTROL/name).read_text().strip())
finally:
    for p in processes:
        if p.poll() is None:p.terminate()
    for p in processes:
        if p.poll() is None:
            try:p.wait(timeout=30)
            except subprocess.TimeoutExpired:p.kill();p.wait()
    for log in logs:log.close()
