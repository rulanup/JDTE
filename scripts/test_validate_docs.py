#!/usr/bin/env python3
import unittest

from validate_docs import (
    GUIDE_DIR,
    ITEMS_JAVA,
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


if __name__ == "__main__":
    unittest.main()
