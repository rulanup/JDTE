#!/usr/bin/env python3
import unittest

from generate_patchouli_book import (
    GenerationError,
    parse_guide_document,
    render_entry,
    render_inline,
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
            "$(thing)jdte:greenhouse$() and the guide "
            "(https://example.invalid/guide).",
            rendered,
        )

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


if __name__ == "__main__":
    unittest.main()
