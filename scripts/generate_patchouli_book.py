#!/usr/bin/env python3
"""Generate Patchouli book resources from JDTE's canonical GuideME Markdown."""

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path


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
TEXT_PAGE_LIMIT = 700


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
        or stripped.startswith("- ")
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
    text = LINK_RE.sub(lambda match: f"{match.group(1)} ({match.group(2)})", text)
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
    raise GenerationError(f"cannot render {block.kind!r} as text")


def _spotlight_page(item_id: str) -> dict[str, object]:
    return {"type": "patchouli:spotlight", "item": item_id, "text": ""}


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
        combined = fragment if not pending_text else f"{pending_text}$(br2){fragment}"
        if pending_text and len(combined) > TEXT_PAGE_LIMIT:
            flush_text()
            pending_text = fragment
        else:
            pending_text = combined

    for block in document.blocks:
        if block.kind in {"heading", "paragraph", "list"}:
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
