#!/usr/bin/env python3
from pathlib import Path
import tempfile
import unittest

from validate_docs import (
    GUIDE_DIR,
    ITEMS_JAVA,
    mismatched_localized_item_ids,
    registered_items_from_source,
    undocumented_registered_items,
)


class RegisteredItemsTest(unittest.TestCase):
    def test_parses_direct_and_block_item_registrations(self):
        source = """
        public static final DeferredHolder<Item, Item> DIRECT =
            ITEMS.register("direct", Item::new);
        public static final DeferredHolder<Item, BlockItem> BLOCK =
            blockItem("block", JDTEBlocks.BLOCK);
        """

        self.assertEqual(
            {"direct", "block"},
            registered_items_from_source(source),
        )

    def test_rejects_unrecognized_deferred_item_declaration(self):
        source = """
        public static final DeferredHolder<Item, Item> HIDDEN = makeItem("hidden");
        """

        with self.assertRaisesRegex(ValueError, "unrecognized item registration"):
            registered_items_from_source(source)

    def test_every_static_jdte_item_is_present_in_guide_item_ids(self):
        self.assertEqual(
            [],
            undocumented_registered_items(ITEMS_JAVA, GUIDE_DIR),
        )

    def test_reports_localized_item_id_mismatch(self):
        template = """---
navigation:
  title: {title}
  icon: "jdte:machine"
  position: 1
item_ids:
  - jdte:{item_id}
---

# {title}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            guide_dir = Path(temporary_directory)
            english_dir = guide_dir / "_en_us"
            english_dir.mkdir()
            (guide_dir / "machine.md").write_text(
                template.format(title="机器", item_id="machine"),
                encoding="utf-8",
            )
            (english_dir / "machine.md").write_text(
                template.format(title="Machine", item_id="different_machine"),
                encoding="utf-8",
            )

            self.assertEqual(
                ["machine.md"],
                mismatched_localized_item_ids(guide_dir),
            )


if __name__ == "__main__":
    unittest.main()
