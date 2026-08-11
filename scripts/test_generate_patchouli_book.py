#!/usr/bin/env python3
import json
from pathlib import Path
import shutil
import tempfile
import unittest

from generate_patchouli_book import (
    GenerationError,
    TEXT_PAGE_LIMIT,
    build_generated_files,
    check_generated_book,
    parse_guide_document,
    render_entry,
    render_inline,
    render_landing,
    write_generated_book,
)


SAMPLE_MARKDOWN = """---
navigation:
  title: Greenhouse
  icon: "jdte:greenhouse"
  position: 19.5
item_ids:
  - jdte:greenhouse
  - jdte:seed_conversion_upgrade
---

# Greenhouse

<ItemImage id="jdte:greenhouse" scale="2" />

Produces **crops** from `templates`.

## Inputs

- Seeds
- Time Fluid

<ItemGrid>
  <ItemIcon id="jdte:greenhouse" />
  <ItemIcon id="jdte:seed_conversion_upgrade" />
</ItemGrid>

<RecipeFor id="jdte:greenhouse" />
<SubPages />
"""


class GuideDocumentParserTest(unittest.TestCase):
    def test_parse_guide_document_extracts_navigation_items_and_components(self):
        doc = parse_guide_document(SAMPLE_MARKDOWN, "greenhouse.md")

        self.assertEqual("greenhouse", doc.slug)
        self.assertEqual("Greenhouse", doc.title)
        self.assertEqual("jdte:greenhouse", doc.icon)
        self.assertEqual(19.5, doc.position)
        self.assertEqual(
            ("jdte:greenhouse", "jdte:seed_conversion_upgrade"),
            doc.item_ids,
        )
        self.assertEqual(
            [
                "heading",
                "item_image",
                "paragraph",
                "heading",
                "list",
                "item_grid",
                "recipe",
                "subpages",
            ],
            [block.kind for block in doc.blocks],
        )
        self.assertEqual(("Seeds", "Time Fluid"), doc.blocks[4].items)
        self.assertEqual(
            ("jdte:greenhouse", "jdte:seed_conversion_upgrade"),
            doc.blocks[5].items,
        )

    def test_parse_rejects_unknown_guideme_component(self):
        source = SAMPLE_MARKDOWN.replace(
            '<ItemImage id="jdte:greenhouse" scale="2" />',
            '<UnknownWidget id="jdte:greenhouse" />',
        )

        with self.assertRaisesRegex(GenerationError, "UnknownWidget"):
            parse_guide_document(source, "greenhouse.md")

    def test_parse_rejects_incomplete_frontmatter(self):
        source = SAMPLE_MARKDOWN.replace('  icon: "jdte:greenhouse"\n', "")

        with self.assertRaisesRegex(GenerationError, "navigation.icon"):
            parse_guide_document(source, "greenhouse.md")

    def test_lowercase_angle_placeholder_remains_text(self):
        source = SAMPLE_MARKDOWN.replace(
            "Produces **crops** from `templates`.",
            "Files live under data/<namespace>/recipe.",
        )

        doc = parse_guide_document(source, "greenhouse.md")

        paragraph = next(block for block in doc.blocks if block.kind == "paragraph")
        self.assertEqual("Files live under data/<namespace>/recipe.", paragraph.text)

    def test_fenced_code_block_is_parsed_without_its_markers(self):
        source = SAMPLE_MARKDOWN.replace(
            "Produces **crops** from `templates`.",
            "Run:\n\n```\n/jdte timefreezer list\n```",
        )

        doc = parse_guide_document(source, "greenhouse.md")

        code = next(block for block in doc.blocks if block.kind == "code")
        self.assertEqual("/jdte timefreezer list", code.text)

    def test_unclosed_fenced_code_block_is_rejected(self):
        source = SAMPLE_MARKDOWN.replace(
            "Produces **crops** from `templates`.",
            "```\n/jdte timefreezer list",
        )

        with self.assertRaisesRegex(GenerationError, "unclosed fenced code block"):
            parse_guide_document(source, "greenhouse.md")

    def test_ordered_lists_and_tables_are_parsed_structurally(self):
        source = SAMPLE_MARKDOWN.replace(
            "Produces **crops** from `templates`.",
            "1. First step\n2. Second step\n\n| Name | Value |\n|---|---|\n| Speed | 4x |",
        )

        doc = parse_guide_document(source, "greenhouse.md")

        ordered = next(block for block in doc.blocks if block.kind == "ordered_list")
        table = next(block for block in doc.blocks if block.kind == "table")
        self.assertEqual(("First step", "Second step"), ordered.items)
        self.assertEqual(("Name | Value", "Speed | 4x"), table.items)


class PatchouliRendererTest(unittest.TestCase):
    def setUp(self):
        self.document = parse_guide_document(SAMPLE_MARKDOWN, "greenhouse.md")

    def test_render_inline_preserves_markdown_meaning(self):
        rendered = render_inline(
            "Use **fast mode**, *carefully*, with `jdte:greenhouse` and "
            "[the guide](https://example.invalid/guide)."
        )
        self.assertEqual(
            "Use $(bold)fast mode$(), $(italic)carefully$(), with "
            "$(thing)jdte:greenhouse$() and "
            "$(l:https://example.invalid/guide)the guide$(/l).",
            rendered,
        )

    def test_internal_links_and_inline_images_do_not_leak_guideme_markup(self):
        rendered = render_inline(
            'Use <ItemImage id="jdte:extended_upgrade" scale="1" /> '
            "[Extended Upgrade](extended-upgrade.md)."
        )

        self.assertEqual(
            "Use $(thing)jdte:extended_upgrade$() "
            "$(l:jdte:extended-upgrade)Extended Upgrade$(/l).",
            rendered,
        )

    def test_ordered_lists_and_tables_keep_their_layout(self):
        source = SAMPLE_MARKDOWN.replace(
            "Produces **crops** from `templates`.",
            "1. First step\n2. Second step\n\n| Name | Value |\n|---|---|\n| Speed | 4x |",
        )
        document = parse_guide_document(source, "greenhouse.md")

        entry = render_entry(document, "greenhouses_resources", recipe_index={})

        rendered = str(entry["pages"])
        self.assertIn("1. First step$(br)2. Second step", rendered)
        self.assertIn("$(bold)Name | Value$()$(br)Speed | 4x", rendered)

    def test_index_body_becomes_the_patchouli_landing_text(self):
        landing = render_landing(self.document)

        self.assertIn("Produces $(bold)crops$()", landing)
        self.assertIn("$(bold)Inputs$()", landing)
        self.assertNotIn("# Greenhouse", landing)

    def test_fenced_code_renders_as_patchouli_styled_text(self):
        source = SAMPLE_MARKDOWN.replace(
            "Produces **crops** from `templates`.",
            "```\n/jdte timefreezer list\n```",
        )
        document = parse_guide_document(source, "greenhouse.md")

        entry = render_entry(document, "greenhouses_resources", recipe_index={})

        rendered = str(entry["pages"])
        self.assertIn("$(thing)/jdte timefreezer list$()", rendered)
        self.assertNotIn("```", rendered)

    def test_render_entry_preserves_text_spotlights_and_recipes(self):
        entry = render_entry(
            self.document,
            "greenhouses_resources",
            recipe_index={"jdte:greenhouse": "jdte:greenhouse"},
        )

        self.assertEqual("Greenhouse", entry["name"])
        self.assertEqual("jdte:greenhouse", entry["icon"])
        self.assertEqual("jdte:greenhouses_resources", entry["category"])
        self.assertEqual(1950, entry["sortnum"])
        self.assertEqual("patchouli:spotlight", entry["pages"][0]["type"])
        self.assertEqual("jdte:greenhouse", entry["pages"][0]["item"])
        self.assertIn("$(bold)crops$()", str(entry["pages"]))
        self.assertIn("$(li)Seeds", str(entry["pages"]))
        self.assertEqual("patchouli:crafting", entry["pages"][-1]["type"])
        self.assertEqual("jdte:greenhouse", entry["pages"][-1]["recipe"])
        self.assertEqual(0, entry["extra_recipe_mappings"]["jdte:greenhouse"])

    def test_render_entry_falls_back_to_spotlight_for_missing_recipe(self):
        entry = render_entry(
            self.document,
            "greenhouses_resources",
            recipe_index={},
        )

        self.assertEqual("patchouli:spotlight", entry["pages"][-1]["type"])
        self.assertEqual("jdte:greenhouse", entry["pages"][-1]["item"])

    def test_item_grid_keeps_every_item(self):
        entry = render_entry(
            self.document,
            "greenhouses_resources",
            recipe_index={},
        )

        spotlight_items = [
            page["item"]
            for page in entry["pages"]
            if page["type"] == "patchouli:spotlight"
        ]
        self.assertGreaterEqual(spotlight_items.count("jdte:greenhouse"), 2)
        self.assertIn("jdte:seed_conversion_upgrade", spotlight_items)

    def test_single_long_paragraph_is_split_into_readable_pages(self):
        source = SAMPLE_MARKDOWN.replace(
            "Produces **crops** from `templates`.",
            " ".join(["long-paragraph-content"] * 100),
        )
        document = parse_guide_document(source, "greenhouse.md")

        entry = render_entry(document, "greenhouses_resources", recipe_index={})

        text_pages = [page["text"] for page in entry["pages"] if page["type"] == "patchouli:text"]
        self.assertGreater(len(text_pages), 1)
        self.assertTrue(all(len(text) <= TEXT_PAGE_LIMIT for text in text_pages))


class PatchouliBookGenerationTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root = Path(__file__).resolve().parents[1]
        cls.generated = build_generated_files(cls.root)

    def test_generates_book_categories_and_every_localized_entry(self):
        relative_paths = {path.relative_to(self.root).as_posix() for path in self.generated}

        self.assertIn(
            "src/main/resources/data/jdte/patchouli_books/jdte_guide/book.json",
            relative_paths,
        )
        self.assertEqual(87, len(relative_paths))
        for language in ("zh_cn", "en_us"):
            prefix = (
                "src/main/resources/assets/jdte/patchouli_books/"
                f"jdte_guide/{language}"
            )
            self.assertEqual(
                6,
                sum(path.startswith(f"{prefix}/categories/") for path in relative_paths),
            )
            self.assertEqual(
                37,
                sum(path.startswith(f"{prefix}/entries/") for path in relative_paths),
            )

    def test_new_entries_use_expected_categories(self):
        expected_categories = {
            "ultimate-portal-gun": "upgrades_tools",
            "big-fluid-tank": "upgrades_tools",
            "time-multitool": "upgrades_tools",
            "solar-panels": "time_energy",
            "creative-greenhouse": "greenhouses_resources",
        }

        for language in ("zh_cn", "en_us"):
            for slug, category in expected_categories.items():
                path = self.root / (
                    "src/main/resources/assets/jdte/patchouli_books/"
                    f"jdte_guide/{language}/entries/{slug}.json"
                )
                entry = json.loads(self.generated[path])
                self.assertEqual(f"jdte:{category}", entry["category"])

    def test_languages_have_identical_category_and_entry_paths(self):
        def localized_paths(language):
            marker = f"/jdte_guide/{language}/"
            return {
                path.as_posix().split(marker, 1)[1]
                for path in self.generated
                if marker in path.as_posix()
            }

        self.assertEqual(localized_paths("zh_cn"), localized_paths("en_us"))

    def test_every_generated_resource_is_valid_json(self):
        for path, content in self.generated.items():
            with self.subTest(path=path):
                self.assertIsInstance(json.loads(content), dict)
                self.assertNotRegex(content, r"<(?:ItemImage|BlockImage|RecipeFor|SubPages)")

    def test_generated_text_pages_respect_the_readability_limit(self):
        for path, content in self.generated.items():
            resource = json.loads(content)
            for page in resource.get("pages", []):
                if page.get("type") == "patchouli:text":
                    with self.subTest(path=path):
                        self.assertLessEqual(len(page["text"]), TEXT_PAGE_LIMIT)
                        styled_starts = sum(
                            page["text"].count(marker)
                            for marker in ("$(bold)", "$(italic)", "$(thing)")
                        )
                        self.assertEqual(styled_starts, page["text"].count("$()"))

    def test_generation_is_deterministic(self):
        self.assertEqual(self.generated, build_generated_files(self.root))

    def test_book_recipe_is_patchouli_conditional_and_binds_book_component(self):
        recipe_path = (
            self.root / "src/main/resources/data/jdte/recipe/jdte_guide.json"
        )
        recipe = json.loads(recipe_path.read_text(encoding="utf-8"))

        self.assertEqual("minecraft:crafting_shapeless", recipe["type"])
        self.assertEqual(
            {"type": "neoforge:mod_loaded", "modid": "patchouli"},
            recipe["neoforge:conditions"][0],
        )
        self.assertEqual("patchouli:guide_book", recipe["result"]["id"])
        self.assertEqual(
            "jdte:jdte_guide",
            recipe["result"]["components"]["patchouli:book"],
        )

    def test_check_mode_reports_source_changes_as_stale_generated_entries(self):
        with tempfile.TemporaryDirectory() as temporary_directory:
            temporary_root = Path(temporary_directory)
            shutil.copy2(self.root / "gradle.properties", temporary_root / "gradle.properties")
            for relative_directory in (
                "src/main/resources/assets/jdte/guides",
                "src/main/resources/assets/jdte/lang",
                "src/main/resources/data/jdte/recipe",
            ):
                shutil.copytree(
                    self.root / relative_directory,
                    temporary_root / relative_directory,
                )
            write_generated_book(temporary_root)
            source = (
                temporary_root
                / "src/main/resources/assets/jdte/guides/jdte/guide/greenhouse.md"
            )
            source.write_text(
                source.read_text(encoding="utf-8") + "\n生成漂移测试。\n",
                encoding="utf-8",
            )

            errors = check_generated_book(temporary_root)

            self.assertTrue(
                any("stale generated Patchouli resource" in error for error in errors),
                errors,
            )


if __name__ == "__main__":
    unittest.main()
