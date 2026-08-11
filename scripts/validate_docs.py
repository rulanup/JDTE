#!/usr/bin/env python3
"""Validate consistency between block registrations, lang files, and GuideME pages.

Checks:
1. en_us.json and zh_cn.json have identical key sets.
2. Every block registered in JDTEBlocks.java has a block.jdte.<name> lang key.
3. GuideME root pages (zh) and _en_us/ pages have matching filenames.
4. Every registered machine maps to a GuideME page (family-normalized).
5. Every statically registered JDTE item appears in GuideME item_ids.
6. Checked-in Patchouli resources exactly match their GuideME sources.
"""
import json
import re
import sys
from pathlib import Path

from generate_patchouli_book import check_generated_book, parse_guide_document

ROOT = Path(__file__).resolve().parent.parent
LANG_DIR = ROOT / "src/main/resources/assets/jdte/lang"
GUIDE_DIR = ROOT / "src/main/resources/assets/jdte/guides/jdte/guide"
BLOCKS_JAVA = ROOT / "src/main/java/com/jdte/setup/JDTEBlocks.java"
ITEMS_JAVA = ROOT / "src/main/java/com/jdte/setup/JDTEItems.java"

ITEM_DECLARATION = re.compile(
    r"public\s+static\s+final\s+DeferredHolder<Item,"
)
ITEM_REGISTRATION = re.compile(
    r"public\s+static\s+final\s+DeferredHolder<Item,.*?>\s+[A-Z0-9_]+\s*=\s*"
    r'(?:ITEMS\.register|blockItem)\s*\(\s*"([a-z0-9_]+)"',
    re.DOTALL,
)

# Machines documented on a shared page instead of a family page of their own.
GUIDE_ALIASES = {
    "extended_clicker": "extended-machines",
    "extended_block_breaker": "extended-machines",
    "extended_block_placer": "extended-machines",
    "extended_block_swapper": "extended-machines",
    "extended_dropper": "extended-machines",
    "extended_sensor": "extended-machines",
    "extended_fluid_collector": "extended-machines",
    "extended_fluid_placer": "extended-machines",
    "large_mineral_extractor": "mineral-extractor",
}

# Internal structure blocks are registered for world state, not as standalone machines.
NON_GUIDE_BLOCKS = {"large_greenhouse_part", "life_synthesis_part", "large_mineral_extractor_part"}

TIER_PREFIXES = ("basic_", "advanced_", "extended_")


def fail(errors):
    for err in errors:
        print(f"ERROR: {err}")
    sys.exit(1)


def load_lang(name):
    path = LANG_DIR / name
    with path.open(encoding="utf-8") as fh:
        return json.load(fh)


def registered_blocks():
    text = BLOCKS_JAVA.read_text(encoding="utf-8")
    return re.findall(r'register\("([a-z0-9_]+)"', text)


def registered_items_from_source(source):
    declaration_count = len(ITEM_DECLARATION.findall(source))
    item_ids = ITEM_REGISTRATION.findall(source)
    if len(item_ids) != declaration_count or len(set(item_ids)) != len(item_ids):
        raise ValueError(
            "unrecognized item registration or duplicate item id in JDTEItems.java"
        )
    return set(item_ids)


def guide_item_ids(guide_dir):
    item_ids = set()
    for path in sorted(guide_dir.glob("*.md")):
        document = parse_guide_document(path.read_text(encoding="utf-8"), path.name)
        item_ids.update(
            item.removeprefix("jdte:")
            for item in document.item_ids
            if item.startswith("jdte:")
        )
    return item_ids


def undocumented_registered_items(items_java, guide_dir):
    registered = registered_items_from_source(items_java.read_text(encoding="utf-8"))
    return sorted(registered - guide_item_ids(guide_dir))


def guide_family(block_name):
    if block_name in GUIDE_ALIASES:
        return GUIDE_ALIASES[block_name]
    if block_name.startswith("greenhouse_matrix_"):
        return "greenhouse-matrix"
    name = block_name
    for prefix in TIER_PREFIXES:
        if name.startswith(prefix):
            name = name[len(prefix):]
            break
    return name.replace("_", "-")


def main():
    errors = []

    en = load_lang("en_us.json")
    zh = load_lang("zh_cn.json")
    only_en = sorted(set(en) - set(zh))
    only_zh = sorted(set(zh) - set(en))
    for key in only_en:
        errors.append(f"lang key only in en_us.json: {key}")
    for key in only_zh:
        errors.append(f"lang key only in zh_cn.json: {key}")

    blocks = registered_blocks()
    if not blocks:
        errors.append("no block registrations parsed from JDTEBlocks.java")
    for block in blocks:
        key = f"block.jdte.{block}"
        if key not in en:
            errors.append(f"missing lang key: {key}")

    zh_pages = {p.name for p in GUIDE_DIR.glob("*.md")}
    en_pages = {p.name for p in (GUIDE_DIR / "_en_us").glob("*.md")}
    for page in sorted(zh_pages - en_pages):
        errors.append(f"guide page missing English translation: _en_us/{page}")
    for page in sorted(en_pages - zh_pages):
        errors.append(f"guide page missing Chinese original: {page}")

    for block in blocks:
        if block in NON_GUIDE_BLOCKS:
            continue
        family = guide_family(block)
        full = block.replace("_", "-")
        if f"{family}.md" not in zh_pages and f"{full}.md" not in zh_pages:
            errors.append(f"no guide page for machine '{block}' (expected {family}.md or {full}.md)")

    try:
        missing_item_docs = undocumented_registered_items(ITEMS_JAVA, GUIDE_DIR)
    except ValueError as exception:
        errors.append(str(exception))
    else:
        for item_id in missing_item_docs:
            errors.append(f"undocumented registered item: jdte:{item_id}")

    errors.extend(check_generated_book(ROOT))

    if errors:
        fail(errors)
    print(f"OK: {len(en)} lang keys, {len(blocks)} blocks, {len(zh_pages)} guide pages validated")


if __name__ == "__main__":
    main()
