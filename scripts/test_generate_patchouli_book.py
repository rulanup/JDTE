#!/usr/bin/env python3
import unittest

from generate_patchouli_book import GenerationError, parse_guide_document


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


if __name__ == "__main__":
    unittest.main()
