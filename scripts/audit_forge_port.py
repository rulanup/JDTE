#!/usr/bin/env python3
"""Rebuild the complete literal JDTE item inventory; curated statuses never follow registration alone."""
from pathlib import Path
import argparse,collections,json,re,subprocess
ROOT=Path(__file__).resolve().parent.parent
META=ROOT/'docs/forge-1.20.1-audit-classification.json'
def declaration(text,item):
 m=re.search(r'public static final\s+[^;]+?\b(\w+)\s*=\s*ITEMS\.register\(\s*"'+re.escape(item)+r'"[^;]*;',text,re.S)
 return m.group(0) if m else ''
def main():
 p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');args=p.parse_args()
 curated=json.loads(META.read_text());ref=curated['reference'];src=subprocess.check_output(['git','show',f'{ref}:src/main/java/com/jdte/setup/JDTEItems.java'],cwd=ROOT,text=True)
 forge=(ROOT/'src/main/java/com/jdte/setup/JDTEItems.java').read_text();source_ids=re.findall(r'ITEMS\.register\(\s*"([^"]+)"',src);forge_ids=set(re.findall(r'ITEMS\.register\(\s*"([^"]+)"',forge))
 assert len(source_ids)==len(set(source_ids))==120
 rows=curated['rows'];assert {r['id'] for r in rows}==set(source_ids) and len(rows)==120
 java=collections.defaultdict(list)
 for path in (ROOT/'src/main/java').rglob('*.java'):java[path.stem].append(str(path.relative_to(ROOT)))
 be=(ROOT/'src/main/java/com/jdte/setup/JDTEBlockEntities.java').read_text();menus=(ROOT/'src/main/java/com/jdte/setup/JDTEMenus.java').read_text();blocks=(ROOT/'src/main/java/com/jdte/setup/JDTEBlocks.java').read_text()
 lang=json.loads((ROOT/'src/main/resources/assets/jdte/lang/en_us.json').read_text())
 source_lang=json.loads(subprocess.check_output(['git','show',f'{ref}:src/main/resources/assets/jdte/lang/en_us.json'],cwd=ROOT,text=True))
 for r in rows:
  i=r['id'];present=i in forge_ids;decl=declaration(forge,i);block='BlockItem' in decl
  # Prefer explicit block/item factories; generic UpgradeCardItem stays explicit in the source expression.
  classes=re.findall(r'(\w+)(?:::new|\()',decl);classes=[c for c in classes if c in java and c not in ['Item','BlockItem']]
  # Custom BlockItem factories are still block items, even when the declaration names their subclass.
  block=block or any(re.search(r'\bextends\s+(?:net\.minecraft\.world\.item\.)?BlockItem\b',(ROOT/f).read_text()) for c in classes for f in java[c])
  if block:
   field=re.search(r'JDTEBlocks\.(\w+)',decl)
   if field:
    bd=re.search(r'\b'+field.group(1)+r'\s*=\s*BLOCKS\.register\([^;]+;',blocks,re.S)
    if bd:classes+= [c for c in re.findall(r'(\w+)(?:::new|\()',bd.group(0)) if c in java]
  r.update(registered=present,kind='block item' if block else 'item',name=source_lang.get('item.jdte.'+i,source_lang.get('block.jdte.'+i,i)),implementation_files=sorted({f for c in classes for f in java[c]}),block_entity=bool(re.search(r'BLOCK_ENTITIES\.register\(\s*"'+i+r'"',be)),menu=bool(re.search(r'MENUS\.register\(\s*"'+i+r'"',menus)),model=(ROOT/f'src/main/resources/assets/jdte/models/item/{i}.json').is_file(),blockstate=(ROOT/f'src/main/resources/assets/jdte/blockstates/{i}.json').is_file(),translation=('item.jdte.'+i in lang or 'block.jdte.'+i in lang),named_recipe=(ROOT/f'src/main/resources/data/jdte/recipes/{i}.json').is_file())
  if not present:r['status']='absent';r['note']='No Forge item registration, corresponding gameplay implementation or complete resource wiring. Stray copied assets do not count as a port.'
  assert r['status'] in ['verified','implemented_unverified','partial','absent']
  if r['status']=='verified':assert present and r['model'] and r['translation'],i
 counts=dict(collections.Counter(r['status'] for r in rows))
 result={'reference':ref,'starting_commit':curated['starting_commit'],'source_item_count':120,'forge_item_count':len(forge_ids),'counts':counts,'rows':rows}
 out=ROOT/'docs/forge-1.20.1-feature-inventory.json';md=ROOT/'docs/forge-1.20.1-feature-matrix.md'
 labels={'verified':'V','implemented_unverified':'U','partial':'P','absent':'N'}
 text='''# Forge 1.20.1 feature migration matrix

Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Audit started at `020109b`; version stays 0.5.8. This inventory covers **all 120 literal item registrations**, including all entries in the original migration checklist, not just the initial registry delta. The unused `blockItem(name, ...)` helper adds no dynamic registrations.

## Classification and limits

- **V — implemented with behavioral verification:** concrete code and retained tests/runtime evidence for the stated paths. This does not certify every optional backend, recipe pack or long-running workload.
- **U — implemented, unverified:** concrete implementation and wiring, but dedicated behavioral evidence is absent or acceptance coverage remains incomplete. Build/registration alone does not upgrade U to V.
- **P — partial:** known source behavior or integration is missing, even if its core machine works and has tests.
- **N — not implemented:** no item registration/gameplay wiring. Copied textures alone are not implementation.

The machine family and individual upgrade rows are separate: a verified new card does not complete every feature of its host machine. Names come from the pinned English language file. `BE/Menu` and `Model/Language/Recipe` are static presence signals, not proof of working behavior. `Recipe` refers to an exact-name JSON only; source intentionally recipe-less or dynamic recipes are not inferred missing from this flag. JSON contains actual implementation file paths and blockstate presence. Source/Forge API renames and inherited methods are not classified as missing behavior solely from method-name differences.

'''
 text+=f"Current inventory: source **120**, Forge **{len(forge_ids)}**. Status counts: "+', '.join(f'{labels[k]} **{counts.get(k,0)}**' for k in labels)+'.\n\n'
 text+='''## Evidence and cross-cutting systems

| System | Status and evidence | Remaining boundary |
| --- | --- | --- |
| Mineral core, survey, normal/large machines | V: mineral unit/GameTests, lifecycle/network evidence folders | Public-server stress and all data packs |
| Bags, copier, patterns, conversion and AE cards | V for tested paths: upgrades/production-chain/AE runtime folders | Future absent-machine codecs do not exist yet |
| Time Accelerators | P: native AE path verified; shared wand/proxy support is verified separately, while remaining area timing/proxy/filter-cache parity is incomplete | Real installed JDTE-AE whole-grid backend remains unverified |
| Greenhouse family | P: core harvest/seed/recorded crafting verified | Source matrix integration and broader Ultimate support |
| Mixer family | V: production-chain and AE-runtime evidence, real clients/jobs/restart/Extras fuel | Complete reactor operation remains unverified |
| Energy Brewing / Energy Overload | V for tested brewer and native/JDTE transmitter paths: energy-upgrade/transmitter evidence | New graphical card checks; JDTE transmitter acceptance is tracked separately |
| Energy Transmitters | Both V: scoped source contracts, graphical/Curios/Draconic, native mixed network, dedicated offline/rejoin and installed ME production-JVM recovery evidence | Prolonged multiplayer, full fusion/reactor operation and external JDTE-AE backend are separate unverified scope |
| Large Fuel Canister / Extended generators / Generator Upgrade | V: generators evidence; real handheld/Curios/generator GUIs, conversion/rollback, production unload/restart and independent two-client concurrency/conservation | Source container-fuel exception retained; Portable siblings are covered by separate acceptance; prolonged multiplayer and arbitrary recipe packs not certified |
| Bio Extractors | Both V: all nine recipes, GUI/JEI/Jade, preserving lifecycle; two independent clients operate shared naturally producing machines, race upgrades/outputs and restore exact FE/milk/inventories/cooldowns across reconnect/unload/restart | Prolonged uptime and arbitrary data packs remain unverified |
| Entity Accelerators | Both machines V: retained 150/217 machine checkpoint plus current wand regression; real GUI/JEI/Jade, requested resources and packaged lifecycle | Independent two-client machine menus, prolonged uptime and arbitrary mod creatures remain unverified |
| Ultimate Time Wand / Entity Acceleration Upgrade | Both V: 167 JUnit/238 GameTests; actual two-client inventory/modes/renderer/JEI, shared block/entity effects and exact FE/fraction/card ledger; real unload/rejoin and two production JVMs restore exact state, including minimal optional-absence runtime | Default600-tick boundaries are tested separately; production fixture uses source6000-tick config. Arbitrary third-party tickers/entities, prolonged uptime and external JDTE-AE backend remain unverified |
| Charged Lightning Rod / Thunderstorm Upgrade | Both V: 170 JUnit/252 GameTests, actual GUI/JEI and client flash/sound events; complete vanilla rod/pig effects and AE2LT marker; two production JVMs verify exact FE, wrench relocation, owned/player rod handling, actual unload and nonzero-cooldown recovery | Weather deterministically set through real APIs; random weather arrival, real NeoForge-only AE2LT, cross-dimensional transport, independent multiplayer/prolonged load remain unverified |
| Extended Experience Holder / Extended Upgrade | V:173 JUnit/270 GameTests, actual native GUI/JEI/Jade and real-player target37 XP ledger; all20 pinned mappings plus the Forge-added route; paired matching-JAR production conversion/transport/actual unload/fresh BE/exact NBT restart and merged-orb work | Source conservation/marker repairs documented; independent multiplayer, cross-dimensional transport, prolonged load and arbitrary mod event handlers remain unverified |
| Time Multitool | V:196 JUnit/324 GameTests; real graphical mode/input/controller/native Hammer controls/JEI and exact player save/rejoin/resume; matching-JAR minimal production actual harvest callbacks/XP/native breaker/unload/fresh BE/two-JVM resource recovery | Generated input/material fixtures explicit; independent multiplayer/prolonged/cross-dimensional/unsaved-crash/arbitrary mod blocks/protection and every ability/JEI layout remain unverified |
| Large Pocket Generator | V:180 JUnit/302 GameTests, real held/worn GUI/native toggle/G/JEI, natural inventory+Curios receiver credit/FE ledger and real player/world save/rejoin/resume; matching-JAR minimal production actual unload/fresh BE/exact NBT/two-JVM recovery | Explicit setup/FakePlayer work distinguished; independent multiplayer, prolonged uptime, cross-dimensional/unsaved-crash/arbitrary-mod behavior unverified |
| Large Potion Canister | V:176 JUnit/284 GameTests, real handheld/Curios/P/JEI/C2S native bow, exact full effects/color; matching-JAR minimal production unload/fresh BE/restart and two independent clients with stale replay/handoff/cursor drop/actual pickup/two-server recovery | Controlled setup/handoff documented; cross-dimensional/prolonged/arbitrary potion mods and abrupt unsaved crash remain unverified |
| Inherited other machines | P where known Ultimate/operation gaps; otherwise U | Dedicated parity and behavior checks are not replaced by bootstrap |
| Repair Talisman, apples, eggs/essences | U unless an explicit behavioral test is linked later | Functional behavior not established by registration/assets |
| Optional JEI/Jade/Curios integrations | Implemented, selected client/presence/absence checks retained | Not every new card GUI/every mod combination |
| Network/save/capabilities | Tested by subsystem, Forge replacements for NeoForge attachments | Absence of a global migration proof; scoped coverage only |

Evidence: [lifecycle](validation/forge-1.20.1-lifecycle/README.md), [network](validation/forge-1.20.1-network/README.md), [upgrades](validation/forge-1.20.1-upgrades/README.md), [production chain](validation/forge-1.20.1-production-chain/README.md), [AE runtime](validation/forge-1.20.1-ae-runtime/README.md), [energy upgrades](validation/forge-1.20.1-energy-upgrades/README.md), [transmitter recovery](validation/forge-1.20.1-energy-transmitters/README.md), [fuel and generators](validation/forge-1.20.1-generators/README.md), [Bio Extractors](validation/forge-1.20.1-bio-extractors/README.md), [Entity Accelerators](validation/forge-1.20.1-entity-accelerators/README.md), [Ultimate Time Wand](validation/forge-1.20.1-ultimate-time-wand/README.md), [Lightning](validation/forge-1.20.1-lightning/README.md), [Experience Holder](validation/forge-1.20.1-experience-holder/README.md), [Large Potion Canister](validation/forge-1.20.1-potion-canister/README.md), [Large Pocket Generator](validation/forge-1.20.1-pocket-generator/README.md), [Time Multitool](validation/forge-1.20.1-time-multitool/README.md). Remaining3N Fumo source findings are [read-only](forge-1.20.1-fumo-investigation.md). JDTE-AE whole-grid execution, prolonged multiplayer uptime and complete operating reactors remain **unverified**.

## Complete item mapping

`Initial` marks membership in the original progress checklist. `BE/Menu` = matching named registry entries; `Assets` = model/language/exact-name recipe.

| Registry ID | Source name | Initial | Status | BE/Menu | Assets | Behavior / outstanding work |
| --- | --- | --- | --- | --- | --- | --- |
'''
 for r in sorted(rows,key=lambda x:x['id']):
  yn=lambda v:'yes' if v else 'no'
  text+=f"| `jdte:{r['id']}` | {r['name']} | {yn(r['initial_list'])} | **{labels[r['status']]}** | {yn(r['block_entity'])}/{yn(r['menu'])} | {yn(r['model'])}/{yn(r['translation'])}/{yn(r['named_recipe'])} | {r['note']} |\n"
 text+='''
## Rebuild and maintain

Curated behavioral classifications and notes live in `forge-1.20.1-audit-classification.json`; the generated machine-readable inventory is `forge-1.20.1-feature-inventory.json`. Review source and evidence before changing a classification, then run `python scripts/audit_forge_port.py`. Use `--check` to detect stale tables without executing Minecraft or Gradle. This intentionally does not infer completed gameplay from a registered class or count of tests.
'''
 rendered=json.dumps(result,indent=2,ensure_ascii=False)+'\n'
 if args.check:
  assert out.read_text()==rendered and md.read_text()==text,'Audit output stale; regenerate after reviewing classifications'
  port=(ROOT/'docs/forge-1.20.1-port.md').read_text()
  summary=port.split('<!-- current-status:start -->',1)[1].split('<!-- current-status:end -->',1)[0]
  expected='- 120-item status: '+', '.join(f'{labels[k]} **{counts.get(k,0)}**' for k in labels)+f'; **{len(forge_ids)}** source items registered in Forge.'
  assert expected in summary,'Current Status inventory counts differ from the authoritative matrix'
 else:out.write_text(rendered);md.write_text(text)
 print(f'Audited {len(rows)} source items, {len(forge_ids)} Forge items: {counts}')
if __name__=='__main__':main()
