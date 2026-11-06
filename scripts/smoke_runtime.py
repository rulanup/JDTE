"""Select actual mod JARs for small reproducible smoke runtimes; keep ordinary Java libraries."""
from pathlib import Path
import os, json, zipfile, tomllib
KEEP={'minecraft','forge','justdirethings','guideme','ae2','curios','jei','jade','modernfix'}
def mod_ids(path):
    p=Path(path)
    if not p.is_file() or p.suffix!='.jar':return set()
    with zipfile.ZipFile(p) as z:
        if 'META-INF/mods.toml' not in z.namelist():return set()
        data=tomllib.loads(z.read('META-INF/mods.toml').decode())
        return {m['modId'] for m in data.get('mods',[])}
def allowed(path,keep):
    ids=mod_ids(path)
    return not ids or ids <= keep

def filtered_command(launch,folder,keep):
    command=list(launch['command'])
    i=command.index('-cp')+1
    command[i]=os.pathsep.join(p for p in command[i].split(os.pathsep) if allowed(p,keep))
    for i,arg in enumerate(command):
        if arg.startswith('-DlegacyClassPath.file='):
            source=Path(arg.split('=',1)[1]);target=Path(folder)/'legacy-classpath.txt'
            target.write_text('\n'.join(p for p in source.read_text().splitlines() if allowed(p,keep))+'\n')
            command[i]='-DlegacyClassPath.file='+str(target)
    return command
