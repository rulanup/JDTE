#!/usr/bin/env python3
"""Validate consistency between block registrations, lang files, and GuideME pages.

Checks:
1. en_us.json and zh_cn.json have identical key sets.
2. Every block registered in JDTEBlocks.java has a block.jdte.<name> lang key.
3. GuideME root pages (zh) and _en_us/ pages have matching filenames.
4. Every registered machine maps to a GuideME page (family-normalized).
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LANG_DIR = ROOT / "src/main/resources/assets/jdte/lang"
GUIDE_DIR = ROOT / "src/main/resources/assets/jdte/guides/jdte/guide"
BLOCKS_JAVA = ROOT / "src/main/java/com/jdte/setup/JDTEBlocks.java"

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
}

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


def guide_family(block_name):
    if block_name in GUIDE_ALIASES:
        return GUIDE_ALIASES[block_name]
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
        family = guide_family(block)
        full = block.replace("_", "-")
        if f"{family}.md" not in zh_pages and f"{full}.md" not in zh_pages:
            errors.append(f"no guide page for machine '{block}' (expected {family}.md or {full}.md)")

    if errors:
        fail(errors)
    print(f"OK: {len(en)} lang keys, {len(blocks)} blocks, {len(zh_pages)} guide pages validated")


if __name__ == "__main__":
    main()
