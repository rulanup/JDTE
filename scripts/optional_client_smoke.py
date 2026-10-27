#!/usr/bin/env python3
"""Start an actual Forge client with the four target optional mods absent."""
from pathlib import Path
import json,os,subprocess
from smoke_runtime import filtered_command
repo=Path(__file__).resolve().parent.parent
folder=repo/'runs/client-optional';folder.mkdir(exist_ok=True)
(folder/'options.txt').write_text('tutorialStep:none\nrenderDistance:2\nsimulationDistance:5\nmaxFps:30\nguiScale:2\nsoundCategory_master:0.0\n')
marker=folder/'optional-client-success.txt';marker.unlink(missing_ok=True)
launch=json.loads(Path('/tmp/jdte-remote-launch.json').read_text())
command=filtered_command(launch,folder,{'minecraft','forge','justdirethings','guideme','modernfix'})
i=command.index('--quickPlayMultiplayer');del command[i:i+2]
command[command.index('--gameDir')+1]=str(folder)
command=[x.replace('-Djdte.remoteSmoke=true','-Djdte.remoteSmoke=false') for x in command]
command.insert(1,'-Djdte.optionalSmoke=true');command.insert(1,'-Xmx2G')
with (folder/'optional-client.log').open('w') as log:
 result=subprocess.run(command,cwd=folder,env=dict(os.environ,**launch['environment']),stdout=log,stderr=subprocess.STDOUT,timeout=240)
assert result.returncode==0,f'Optional client exited {result.returncode}; inspect {folder}/optional-client.log'
assert marker.is_file(),'Optional client did not complete assertions'
print(marker.read_text().strip())
