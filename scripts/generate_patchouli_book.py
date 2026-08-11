#!/usr/bin/env python3
"""Generate Patchouli book resources from JDTE's canonical GuideME Markdown."""

from __future__ import annotations

import argparse
import json
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable


RESOURCE_ID_RE = re.compile(r"^[a-z0-9_.-]+:[a-z0-9_./-]+$")
HEADING_RE = re.compile(r"^(#{1,6})\s+(.+?)\s*$")
IMAGE_RE = re.compile(
    r'^<(ItemImage|BlockImage)\s+id="([a-z0-9_.-]+:[a-z0-9_./-]+)"(?:\s+scale="[^"]+")?\s*/>$'
)
RECIPE_RE = re.compile(
    r'^<RecipeFor\s+id="([a-z0-9_.-]+:[a-z0-9_./-]+)"\s*/>$'
)
ITEM_ICON_RE = re.compile(
    r'^<ItemIcon\s+id="([a-z0-9_.-]+:[a-z0-9_./-]+)"\s*/>$'
)
COMPONENT_LINE_RE = re.compile(r"^</?[A-Z][A-Za-z0-9]*(?:\s+[^>]*)?/?>$")
BOLD_RE = re.compile(r"\*\*(.+?)\*\*")
ITALIC_RE = re.compile(r"(?<!\*)\*([^*]+?)\*(?!\*)")
CODE_RE = re.compile(r"`([^`]+?)`")
LINK_RE = re.compile(r"\[([^]]+)]\(([^)]+)\)")
INLINE_IMAGE_RE = re.compile(
    r'<(?:ItemImage|BlockImage)\s+id="([a-z0-9_.-]+:[a-z0-9_./-]+)"(?:\s+scale="[^"]+")?\s*/>'
)
ORDERED_LIST_RE = re.compile(r"^(\d+)\.\s+(.+)$")
TABLE_ROW_RE = re.compile(r"^\|.*\|$")
TABLE_SEPARATOR_CELL_RE = re.compile(r"^:?-{3,}:?$")
TEXT_PAGE_LIMIT = 700
BOOK_ID = "jdte_guide"
BOOK_NAME_KEY = "item.jdte.patchouli_guide.name"
BOOK_LANDING_KEY = "item.jdte.patchouli_guide.landing"
LANGUAGE_DIRECTORIES = {"zh_cn": "", "en_us": "_en_us"}


@dataclass(frozen=True)
class CategoryDefinition:
    slug: str
    icon: str
    sortnum: int
    entries: tuple[str, ...]
    names: dict[str, str]
    descriptions: dict[str, str]


CATEGORIES = (
    CategoryDefinition(
        "upgrades_tools",
        "jdte:capacity_upgrade",
        10,
        (
            "upgrades", "extended-upgrade", "extended-machines", "eclipsealloy-wrench", "boss-essences",
            "ultimate-portal-gun", "big-fluid-tank", "time-multitool",
        ),
        {"zh_cn": "升级与工具", "en_us": "Upgrades & Tools"},
        {
            "zh_cn": "升级卡、扩展机器、扳手与特殊材料。",
            "en_us": "Upgrade cards, extended machines, the wrench, and special materials.",
        },
    ),
    CategoryDefinition(
        "time_energy",
        "jdte:advanced_time_accelerator",
        20,
        ("time-accelerator", "time-freezer", "advanced-energy-transmitter", "solar-panels"),
        {"zh_cn": "时间与能源", "en_us": "Time & Energy"},
        {
            "zh_cn": "时间流体、时间控制和能源传输。",
            "en_us": "Time Fluid, time control, and energy transmission.",
        },
    ),
    CategoryDefinition(
        "logistics_automation",
        "jdte:advanced_item_collector",
        30,
        (
            "advanced-item-collector", "item-sender", "item-receiver", "fluid-sender",
            "fluid-receiver", "fluid-stabilizer", "glue-activator", "gel-generator",
            "factory-packer",
        ),
        {"zh_cn": "物流与自动化", "en_us": "Logistics & Automation"},
        {
            "zh_cn": "物品、流体和工厂自动化设备。",
            "en_us": "Item, fluid, and factory automation devices.",
        },
    ),
    CategoryDefinition(
        "greenhouses_resources",
        "jdte:greenhouse",
        40,
        (
            "crystal-incubator", "greenhouse", "large-greenhouse", "creative-greenhouse",
            "greenhouse-matrix", "mineral-extractor",
        ),
        {"zh_cn": "温室与资源", "en_us": "Greenhouses & Resources"},
        {
            "zh_cn": "作物、晶体和矿物资源生产。",
            "en_us": "Crop, crystal, and mineral resource production.",
        },
    ),
    CategoryDefinition(
        "biology_life",
        "jdte:bio_factory",
        50,
        (
            "bio-crusher", "bio-factory", "life-breeder", "life-extractor",
            "life-synthesis-vat", "infusion-machine", "loot-fabricator",
        ),
        {"zh_cn": "生物与生命", "en_us": "Biology & Life"},
        {
            "zh_cn": "生物加工、生命流体和战利品生产。",
            "en_us": "Biological processing, Life Fluid, and loot production.",
        },
    ),
    CategoryDefinition(
        "control_special",
        "jdte:entity_suppressor",
        60,
        ("entity-suppressor", "range-blocker", "advanced-potion-brewer"),
        {"zh_cn": "控制与特殊机器", "en_us": "Control & Special Machines"},
        {
            "zh_cn": "实体控制、范围限制和药水酿造。",
            "en_us": "Entity control, range restriction, and potion brewing.",
        },
    ),
)


class GenerationError(ValueError):
    """Raised when source content cannot be converted without losing information."""


@dataclass(frozen=True)
class GuideBlock:
    kind: str
    text: str = ""
    level: int = 0
    item_id: str = ""
    items: tuple[str, ...] = ()


@dataclass(frozen=True)
class GuideDocument:
    slug: str
    title: str
    icon: str
    position: float
    item_ids: tuple[str, ...]
    blocks: tuple[GuideBlock, ...]


def _unquote(value: str) -> str:
    value = value.strip()
    if len(value) >= 2 and value[0] == value[-1] and value[0] in {'"', "'"}:
        return value[1:-1]
    return value


def _split_frontmatter(markdown: str, filename: str) -> tuple[list[str], list[str]]:
    normalized = markdown.replace("\r\n", "\n").lstrip("\ufeff")
    lines = normalized.split("\n")
    if not lines or lines[0].strip() != "---":
        raise GenerationError(f"{filename}: missing opening frontmatter delimiter")
    try:
        end = next(index for index in range(1, len(lines)) if lines[index].strip() == "---")
    except StopIteration as exc:
        raise GenerationError(f"{filename}: missing closing frontmatter delimiter") from exc
    return lines[1:end], lines[end + 1 :]


def _parse_frontmatter(lines: list[str], filename: str) -> tuple[str, str, float, tuple[str, ...]]:
    navigation: dict[str, str] = {}
    item_ids: list[str] = []
    section = ""

    for raw_line in lines:
        if not raw_line.strip():
            continue
        stripped = raw_line.strip()
        if not raw_line.startswith(" ") and stripped.endswith(":"):
            section = stripped[:-1]
            if section not in {"navigation", "item_ids"}:
                raise GenerationError(f"{filename}: unsupported frontmatter section {section}")
            continue
        if section == "navigation" and raw_line.startswith("  ") and ":" in stripped:
            key, value = stripped.split(":", 1)
            navigation[key] = _unquote(value)
            continue
        if section == "item_ids" and stripped.startswith("- "):
            item_ids.append(_unquote(stripped[2:]))
            continue
        raise GenerationError(f"{filename}: unsupported frontmatter line {raw_line!r}")

    for key in ("title", "icon", "position"):
        if not navigation.get(key):
            raise GenerationError(f"{filename}: missing navigation.{key}")
    if not RESOURCE_ID_RE.fullmatch(navigation["icon"]):
        raise GenerationError(f"{filename}: invalid navigation.icon {navigation['icon']!r}")
    for item_id in item_ids:
        if not RESOURCE_ID_RE.fullmatch(item_id):
            raise GenerationError(f"{filename}: invalid item id {item_id!r}")
    try:
        position = float(navigation["position"])
    except ValueError as exc:
        raise GenerationError(f"{filename}: navigation.position must be numeric") from exc
    return navigation["title"], navigation["icon"], position, tuple(item_ids)


def _is_block_start(line: str) -> bool:
    stripped = line.strip()
    return bool(
        HEADING_RE.fullmatch(stripped)
        or stripped.startswith("```")
        or stripped.startswith("- ")
        or ORDERED_LIST_RE.fullmatch(stripped)
        or TABLE_ROW_RE.fullmatch(stripped)
        or stripped == "<ItemGrid>"
        or IMAGE_RE.fullmatch(stripped)
        or RECIPE_RE.fullmatch(stripped)
        or stripped == "<SubPages />"
        or COMPONENT_LINE_RE.fullmatch(stripped)
    )


def _parse_blocks(lines: list[str], filename: str) -> tuple[GuideBlock, ...]:
    blocks: list[GuideBlock] = []
    index = 0
    while index < len(lines):
        stripped = lines[index].strip()
        if not stripped:
            index += 1
            continue

        if stripped.startswith("```"):
            code_lines: list[str] = []
            index += 1
            while index < len(lines) and lines[index].strip() != "```":
                code_lines.append(lines[index].rstrip())
                index += 1
            if index >= len(lines):
                raise GenerationError(f"{filename}: unclosed fenced code block")
            blocks.append(GuideBlock("code", text="\n".join(code_lines)))
            index += 1
            continue

        heading = HEADING_RE.fullmatch(stripped)
        if heading:
            blocks.append(GuideBlock("heading", text=heading.group(2), level=len(heading.group(1))))
            index += 1
            continue

        image = IMAGE_RE.fullmatch(stripped)
        if image:
            kind = "item_image" if image.group(1) == "ItemImage" else "block_image"
            blocks.append(GuideBlock(kind, item_id=image.group(2)))
            index += 1
            continue

        recipe = RECIPE_RE.fullmatch(stripped)
        if recipe:
            blocks.append(GuideBlock("recipe", item_id=recipe.group(1)))
            index += 1
            continue

        if stripped == "<SubPages />":
            blocks.append(GuideBlock("subpages"))
            index += 1
            continue

        if stripped == "<ItemGrid>":
            grid_items: list[str] = []
            index += 1
            while index < len(lines) and lines[index].strip() != "</ItemGrid>":
                grid_line = lines[index].strip()
                if grid_line:
                    icon = ITEM_ICON_RE.fullmatch(grid_line)
                    if not icon:
                        raise GenerationError(f"{filename}: invalid ItemGrid line {grid_line!r}")
                    grid_items.append(icon.group(1))
                index += 1
            if index >= len(lines):
                raise GenerationError(f"{filename}: unclosed ItemGrid")
            if not grid_items:
                raise GenerationError(f"{filename}: empty ItemGrid")
            blocks.append(GuideBlock("item_grid", items=tuple(grid_items)))
            index += 1
            continue

        if stripped.startswith("- "):
            items: list[str] = []
            while index < len(lines) and lines[index].strip().startswith("- "):
                items.append(lines[index].strip()[2:].strip())
                index += 1
            blocks.append(GuideBlock("list", items=tuple(items)))
            continue

        ordered = ORDERED_LIST_RE.fullmatch(stripped)
        if ordered:
            items: list[str] = []
            while index < len(lines):
                ordered_line = ORDERED_LIST_RE.fullmatch(lines[index].strip())
                if not ordered_line:
                    break
                items.append(ordered_line.group(2).strip())
                index += 1
            blocks.append(GuideBlock("ordered_list", items=tuple(items)))
            continue

        if TABLE_ROW_RE.fullmatch(stripped):
            rows: list[str] = []
            while index < len(lines) and TABLE_ROW_RE.fullmatch(lines[index].strip()):
                cells = [cell.strip() for cell in lines[index].strip().strip("|").split("|")]
                if not all(TABLE_SEPARATOR_CELL_RE.fullmatch(cell) for cell in cells):
                    rows.append(" | ".join(cells))
                index += 1
            if not rows:
                raise GenerationError(f"{filename}: table has no content rows")
            blocks.append(GuideBlock("table", items=tuple(rows)))
            continue

        if COMPONENT_LINE_RE.fullmatch(stripped):
            component = stripped.removeprefix("</").removeprefix("<").split()[0].rstrip("/>")
            raise GenerationError(f"{filename}: unsupported GuideME component {component}")

        paragraph_lines = [stripped]
        index += 1
        while index < len(lines) and lines[index].strip() and not _is_block_start(lines[index]):
            paragraph_lines.append(lines[index].strip())
            index += 1
        blocks.append(GuideBlock("paragraph", text=" ".join(paragraph_lines)))

    return tuple(blocks)


def parse_guide_document(markdown: str, filename: str) -> GuideDocument:
    """Parse one GuideME Markdown page without silently discarding content."""
    frontmatter_lines, body_lines = _split_frontmatter(markdown, filename)
    title, icon, position, item_ids = _parse_frontmatter(frontmatter_lines, filename)
    return GuideDocument(
        slug=Path(filename).stem,
        title=title,
        icon=icon,
        position=position,
        item_ids=item_ids,
        blocks=_parse_blocks(body_lines, filename),
    )


def render_inline(text: str) -> str:
    """Convert the supported inline Markdown subset to Patchouli markup."""
    text = INLINE_IMAGE_RE.sub(lambda match: f"$(thing){match.group(1)}$()", text)

    def replace_link(match: re.Match[str]) -> str:
        label, target = match.groups()
        if target.endswith(".md"):
            target = f"jdte:{Path(target).stem}"
        if RESOURCE_ID_RE.fullmatch(target) or target.startswith(("https://", "http://")):
            return f"$(l:{target}){label}$(/l)"
        return f"{label} ({target})"

    text = LINK_RE.sub(replace_link, text)
    text = BOLD_RE.sub(lambda match: f"$(bold){match.group(1)}$()", text)
    text = ITALIC_RE.sub(lambda match: f"$(italic){match.group(1)}$()", text)
    return CODE_RE.sub(lambda match: f"$(thing){match.group(1)}$()", text)


def _text_fragment(block: GuideBlock, document_title: str) -> str:
    if block.kind == "heading":
        if block.level == 1 and block.text.strip() == document_title.strip():
            return ""
        return f"$(bold){render_inline(block.text)}$()"
    if block.kind == "paragraph":
        return render_inline(block.text)
    if block.kind == "list":
        return "".join(f"$(li){render_inline(item)}" for item in block.items)
    if block.kind == "ordered_list":
        return "$(br)".join(
            f"{index}. {render_inline(item)}" for index, item in enumerate(block.items, 1)
        )
    if block.kind == "table":
        return "$(br)".join(
            f"$(bold){render_inline(row)}$()" if index == 0 else render_inline(row)
            for index, row in enumerate(block.items)
        )
    if block.kind == "code":
        return f"$(thing){block.text.replace(chr(10), '$(br)')}$()"
    raise GenerationError(f"cannot render {block.kind!r} as text")


def _spotlight_page(item_id: str) -> dict[str, object]:
    return {"type": "patchouli:spotlight", "item": item_id, "text": ""}


def render_landing(document: GuideDocument) -> str:
    fragments = [
        _text_fragment(block, document.title)
        for block in document.blocks
        if block.kind in {"heading", "paragraph", "list", "ordered_list", "table", "code"}
    ]
    landing = "$(br2)".join(fragment for fragment in fragments if fragment)
    if not landing:
        raise GenerationError(f"{document.slug}: landing page has no text")
    return landing


def _split_long_text(fragment: str) -> list[str]:
    pieces: list[str] = []
    remaining = fragment
    while len(remaining) > TEXT_PAGE_LIMIT:
        candidates = [
            (remaining.rfind("$(br2)", 1, TEXT_PAGE_LIMIT + 1), "br2"),
            (remaining.rfind("$(br)", 1, TEXT_PAGE_LIMIT + 1), "br"),
            (remaining.rfind("$(li)", 1, TEXT_PAGE_LIMIT + 1), "li"),
            (remaining.rfind(". ", 1, TEXT_PAGE_LIMIT + 1), "sentence"),
            (remaining.rfind(" ", 1, TEXT_PAGE_LIMIT + 1), "space"),
        ]
        split_at, boundary = max(candidates)
        if split_at <= 0:
            split_at, boundary = TEXT_PAGE_LIMIT, "hard"
        if boundary == "sentence":
            split_at += 1
        piece = remaining[:split_at].rstrip()
        if not piece:
            split_at, boundary = TEXT_PAGE_LIMIT, "hard"
            piece = remaining[:split_at]
        pieces.append(piece)
        if boundary == "br2":
            remaining = remaining[split_at + len("$(br2)") :].lstrip()
        elif boundary == "br":
            remaining = remaining[split_at + len("$(br)") :].lstrip()
        elif boundary == "li":
            remaining = remaining[split_at:]
        else:
            remaining = remaining[split_at:].lstrip()
    if remaining:
        pieces.append(remaining)
    return pieces


def render_entry(
    document: GuideDocument,
    category: str,
    recipe_index: dict[str, str],
) -> dict[str, object]:
    """Render a parsed GuideME document as one localized Patchouli entry."""
    pages: list[dict[str, object]] = []
    pending_text = ""

    def flush_text() -> None:
        nonlocal pending_text
        if pending_text:
            pages.append({"type": "patchouli:text", "text": pending_text})
            pending_text = ""

    def append_text(fragment: str) -> None:
        nonlocal pending_text
        if not fragment:
            return
        fragments = _split_long_text(fragment)
        for index, piece in enumerate(fragments):
            combined = piece if not pending_text else f"{pending_text}$(br2){piece}"
            if pending_text and len(combined) > TEXT_PAGE_LIMIT:
                flush_text()
                pending_text = piece
            else:
                pending_text = combined
            if index < len(fragments) - 1:
                flush_text()

    for block in document.blocks:
        if block.kind in {"heading", "paragraph", "list", "ordered_list", "table", "code"}:
            append_text(_text_fragment(block, document.title))
            continue
        if block.kind in {"item_image", "block_image"}:
            flush_text()
            pages.append(_spotlight_page(block.item_id))
            continue
        if block.kind == "item_grid":
            flush_text()
            pages.extend(_spotlight_page(item_id) for item_id in block.items)
            continue
        if block.kind == "recipe":
            flush_text()
            recipe_id = recipe_index.get(block.item_id)
            if recipe_id:
                pages.append({"type": "patchouli:crafting", "recipe": recipe_id})
            else:
                pages.append(_spotlight_page(block.item_id))
            continue
        if block.kind == "subpages":
            continue
        raise GenerationError(f"{document.slug}: unsupported block kind {block.kind!r}")
    flush_text()

    if not pages:
        pages.append({"type": "patchouli:text", "text": document.title})

    mappings: dict[str, int] = {}
    for item_id in document.item_ids:
        mapped_page = 0
        for page_index, page in enumerate(pages):
            if page.get("item") == item_id:
                mapped_page = page_index
                break
        mappings[item_id] = mapped_page

    return {
        "name": document.title,
        "icon": document.icon,
        "category": f"jdte:{category}",
        "sortnum": int(round(document.position * 100)),
        "pages": pages,
        "extra_recipe_mappings": mappings,
    }


def _json_text(data: dict[str, object]) -> str:
    return json.dumps(data, ensure_ascii=False, indent=2) + "\n"


def _mod_version(root: Path) -> str:
    properties = (root / "gradle.properties").read_text(encoding="utf-8")
    for line in properties.splitlines():
        if line.startswith("mod_version="):
            return line.split("=", 1)[1].strip()
    raise GenerationError("gradle.properties: missing mod_version")


def _recipe_index(root: Path) -> dict[str, str]:
    recipe_root = root / "src/main/resources/data/jdte/recipe"
    return {
        resource_id: resource_id
        for recipe_path in recipe_root.rglob("*.json")
        for resource_id in [
            "jdte:" + recipe_path.relative_to(recipe_root).with_suffix("").as_posix()
        ]
    }


def _category_by_entry() -> dict[str, CategoryDefinition]:
    result: dict[str, CategoryDefinition] = {}
    for category in CATEGORIES:
        for entry in category.entries:
            if entry in result:
                raise GenerationError(f"entry {entry!r} belongs to multiple categories")
            result[entry] = category
    return result


def _read_documents(root: Path, language: str) -> dict[str, GuideDocument]:
    guide_root = root / "src/main/resources/assets/jdte/guides/jdte/guide"
    language_directory = LANGUAGE_DIRECTORIES[language]
    source_root = guide_root / language_directory if language_directory else guide_root
    documents = {
        path.stem: parse_guide_document(path.read_text(encoding="utf-8"), str(path))
        for path in sorted(source_root.glob("*.md"))
    }
    if "index" not in documents:
        raise GenerationError(f"{language}: missing index.md")
    return documents


def build_generated_files(root: Path) -> dict[Path, str]:
    """Build every checked-in Patchouli JSON resource in memory."""
    root = root.resolve()
    asset_root = root / f"src/main/resources/assets/jdte/patchouli_books/{BOOK_ID}"
    book_path = root / f"src/main/resources/data/jdte/patchouli_books/{BOOK_ID}/book.json"
    category_by_entry = _category_by_entry()
    recipe_index = _recipe_index(root)
    documents_by_language = {
        language: _read_documents(root, language) for language in LANGUAGE_DIRECTORIES
    }
    expected_entries = set(category_by_entry)
    localized_entry_sets = {
        language: set(documents) - {"index"}
        for language, documents in documents_by_language.items()
    }
    for language, actual_entries in localized_entry_sets.items():
        missing = sorted(expected_entries - actual_entries)
        extra = sorted(actual_entries - expected_entries)
        if missing or extra:
            raise GenerationError(
                f"{language}: category mapping mismatch; missing={missing}, extra={extra}"
            )
    if len(set(map(frozenset, localized_entry_sets.values()))) != 1:
        raise GenerationError("localized GuideME entry sets do not match")

    generated: dict[Path, str] = {
        book_path: _json_text(
            {
                "name": BOOK_NAME_KEY,
                "landing_text": BOOK_LANDING_KEY,
                "version": _mod_version(root),
                "subtitle": "JDT Extras",
                "model": "jdte:capacity_upgrade",
                "creative_tab": "jdte:jdte",
                "use_resource_pack": True,
                "text_overflow_mode": "resize",
                "show_progress": False,
                "pause_game": False,
            }
        )
    }
    for language, documents in documents_by_language.items():
        localized_root = asset_root / language
        for category in CATEGORIES:
            generated[localized_root / "categories" / f"{category.slug}.json"] = _json_text(
                {
                    "name": category.names[language],
                    "description": category.descriptions[language],
                    "icon": category.icon,
                    "sortnum": category.sortnum,
                }
            )
        for slug in sorted(expected_entries):
            category = category_by_entry[slug]
            generated[localized_root / "entries" / f"{slug}.json"] = _json_text(
                render_entry(documents[slug], category.slug, recipe_index)
            )
    return generated


def _existing_generated_json(root: Path) -> set[Path]:
    asset_root = root / f"src/main/resources/assets/jdte/patchouli_books/{BOOK_ID}"
    book_root = root / f"src/main/resources/data/jdte/patchouli_books/{BOOK_ID}"
    return {
        path.resolve()
        for generated_root in (asset_root, book_root)
        if generated_root.exists()
        for path in generated_root.rglob("*.json")
    }


def check_generated_book(root: Path) -> list[str]:
    root = root.resolve()
    expected = build_generated_files(root)
    errors: list[str] = []
    for path, content in expected.items():
        if not path.is_file():
            errors.append(f"missing generated Patchouli resource: {path.relative_to(root)}")
        elif path.read_text(encoding="utf-8") != content:
            errors.append(f"stale generated Patchouli resource: {path.relative_to(root)}")
    for path in sorted(_existing_generated_json(root) - set(expected)):
        errors.append(f"unexpected generated Patchouli resource: {path.relative_to(root)}")
    for language in LANGUAGE_DIRECTORIES:
        lang_path = root / f"src/main/resources/assets/jdte/lang/{language}.json"
        lang = json.loads(lang_path.read_text(encoding="utf-8"))
        if not lang.get(BOOK_NAME_KEY):
            errors.append(f"missing Patchouli book name translation: {language}:{BOOK_NAME_KEY}")
        expected_landing = render_landing(_read_documents(root, language)["index"])
        if lang.get(BOOK_LANDING_KEY) != expected_landing:
            errors.append(
                f"stale Patchouli landing translation: {language}:{BOOK_LANDING_KEY}"
            )
    return errors


def write_generated_book(root: Path) -> None:
    root = root.resolve()
    generated = build_generated_files(root)
    for stale_path in _existing_generated_json(root) - set(generated):
        stale_path.unlink()
    for path, content in generated.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8", newline="\n")


def main(argv: Iterable[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="fail if generated files drift")
    args = parser.parse_args(argv)
    root = Path(__file__).resolve().parents[1]
    if args.check:
        errors = check_generated_book(root)
        for error in errors:
            print(error)
        return 1 if errors else 0
    write_generated_book(root)
    print(f"Generated {len(build_generated_files(root))} Patchouli resources.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
