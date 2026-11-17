package com.jdte.common.blockentities;

import com.jdte.common.items.CraftingPatternData;
import com.jdte.common.items.EssenceConversionUpgradeItem;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/** Server regression tests for the upstream 0.6.5.3/0.6.5.4 deltas on Forge 1.20.1. */
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class UltimateEssenceConversionGameTests {
    private static final BlockPos POS = new BlockPos(3, 1, 2);
    private static final int OVERSIZED_SLOT_LIMIT = 6_400_000;

    private UltimateEssenceConversionGameTests() {
    }

    private static CraftingPatternData capture(GameTestHelper helper, Consumer<List<ItemStack>> filler) {
        List<ItemStack> grid = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
        filler.accept(grid);
        return CraftingPatternData.capture(helper.getLevel(), grid).orElseThrow();
    }

    private static ItemStack ultimateCard(List<CraftingPatternData> patterns) {
        ItemStack card = new ItemStack(JDTEItems.ULTIMATE_ESSENCE_CONVERSION_UPGRADE.get());
        EssenceConversionUpgradeItem.setPatterns(card, patterns);
        return card;
    }

    private static ItemStackHandler oversizedHandler() {
        return new ItemStackHandler(4) {
            @Override public int getSlotLimit(int slot) { return OVERSIZED_SLOT_LIMIT; }
        };
    }

    private static int count(IItemHandler handler, Item item) {
        int total = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    /**
     * Four bound patterns where pattern 3 shares no ingredient with patterns 0-2. Its rotation group
     * key is 3, which overflowed the old fixed {@code int[3]} rotation state.
     */
    @GameTest(templateNamespace = "jdte", template = "mineral_empty")
    public static void ultimateCardRotatesFourPatternsWithoutOverflow(GameTestHelper helper) {
        helper.setBlock(POS, JDTEBlocks.GREENHOUSE.get());
        GreenhouseBE greenhouse = (GreenhouseBE) helper.getBlockEntity(POS);
        List<CraftingPatternData> patterns = List.of(
                capture(helper, grid -> grid.set(0, new ItemStack(Items.OAK_LOG))),
                capture(helper, grid -> {
                    grid.set(0, new ItemStack(Items.OAK_PLANKS));
                    grid.set(3, new ItemStack(Items.OAK_PLANKS));
                }),
                capture(helper, grid -> {
                    grid.set(0, new ItemStack(Items.OAK_PLANKS));
                    grid.set(1, new ItemStack(Items.OAK_PLANKS));
                    grid.set(3, new ItemStack(Items.OAK_PLANKS));
                    grid.set(4, new ItemStack(Items.OAK_PLANKS));
                }),
                capture(helper, grid -> {
                    grid.set(0, new ItemStack(Items.PAPER));
                    grid.set(1, new ItemStack(Items.PAPER));
                    grid.set(2, new ItemStack(Items.PAPER));
                    grid.set(3, new ItemStack(Items.LEATHER));
                }));
        helper.assertTrue(patterns.size() == 4, "four distinct recorded recipes");
        helper.assertTrue(UpgradeHelper.getUpgradeHandler(greenhouse).insertItem(0, ultimateCard(patterns), false).isEmpty(),
                "ultimate card installs in the greenhouse");

        ItemStackHandler buffer = new ItemStackHandler(9);
        buffer.setStackInSlot(0, new ItemStack(Items.OAK_LOG));
        buffer.setStackInSlot(1, new ItemStack(Items.OAK_PLANKS, 8));
        buffer.setStackInSlot(2, new ItemStack(Items.PAPER, 3));
        buffer.setStackInSlot(3, new ItemStack(Items.LEATHER));

        helper.assertTrue(GreenhouseEssenceConversionHelper.convertStored(helper.getLevel(), buffer, greenhouse),
                "four patterns convert the buffered drops");
        helper.assertTrue(greenhouse.getEssencePatternRotation().length >= 4,
                "rotation state grew to the bound pattern count");
        helper.assertTrue(count(buffer, Items.BOOK) == 1 && count(buffer, Items.STICK) == 8
                        && count(buffer, Items.CRAFTING_TABLE) == 2,
                "every rotation group produced its recorded result");
        helper.assertTrue(count(buffer, Items.OAK_LOG) == 0 && count(buffer, Items.OAK_PLANKS) == 0
                        && count(buffer, Items.PAPER) == 0 && count(buffer, Items.LEATHER) == 0,
                "consumed ingredients were withdrawn");
        helper.succeed();
    }

    @GameTest(templateNamespace = "jdte", template = "mineral_empty")
    public static void oversizedCountsSurviveInventoryRoundTrip(GameTestHelper helper) {
        ItemStackHandler source = oversizedHandler();
        source.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 616));
        source.setStackInSlot(1, new ItemStack(Items.STONE, 7620));
        CompoundTag saved = OversizedItemStackHandlerSerialization.serialize(source);
        var items = saved.getList("Items", Tag.TAG_COMPOUND);
        helper.assertTrue(items.size() == 2, "both oversized stacks are written");
        helper.assertTrue(items.getCompound(0).getInt("jdteRealCount") == 616
                        && items.getCompound(1).getInt("jdteRealCount") == 7620,
                "real counts are stored outside the encoded stack");

        ItemStackHandler restored = oversizedHandler();
        OversizedItemStackHandlerSerialization.deserialize(restored, saved);
        helper.assertTrue(restored.getStackInSlot(0).getCount() == 616 && restored.getStackInSlot(1).getCount() == 7620,
                "counts above the codec limit round-trip");
        helper.succeed();
    }

    @GameTest(templateNamespace = "jdte", template = "mineral_empty")
    public static void ultimateCardIsExclusiveAndUnbounded(GameTestHelper helper) {
        helper.setBlock(POS, JDTEBlocks.GREENHOUSE.get());
        GreenhouseBE greenhouse = (GreenhouseBE) helper.getBlockEntity(POS);
        List<CraftingPatternData> patterns = List.of(
                capture(helper, grid -> grid.set(0, new ItemStack(Items.OAK_LOG))),
                capture(helper, grid -> {
                    grid.set(0, new ItemStack(Items.OAK_PLANKS));
                    grid.set(3, new ItemStack(Items.OAK_PLANKS));
                }),
                capture(helper, grid -> {
                    grid.set(0, new ItemStack(Items.PAPER));
                    grid.set(1, new ItemStack(Items.PAPER));
                    grid.set(2, new ItemStack(Items.PAPER));
                    grid.set(3, new ItemStack(Items.LEATHER));
                }));
        ItemStack card = ultimateCard(patterns);
        helper.assertTrue(EssenceConversionUpgradeItem.patterns(card).size() == 3
                        && EssenceConversionUpgradeItem.patterns(card).get(0).equals(patterns.get(0)),
                "the ultimate card keeps every bound pattern in order");
        helper.assertTrue(UpgradeHelper.getUpgradeHandler(greenhouse).insertItem(0, card, false).isEmpty(),
                "ultimate card installs");
        helper.assertTrue(!UpgradeHelper.getUpgradeHandler(greenhouse)
                        .insertItem(1, new ItemStack(JDTEItems.ESSENCE_CONVERSION_UPGRADE.get()), false).isEmpty(),
                "normal card is rejected while the ultimate card is installed");
        helper.assertTrue(!UpgradeHelper.getUpgradeHandler(greenhouse)
                        .insertItem(2, new ItemStack(JDTEItems.ADVANCED_ESSENCE_CONVERSION_UPGRADE.get()), false).isEmpty(),
                "advanced card is rejected while the ultimate card is installed");

        boolean rejected = false;
        try {
            EssenceConversionUpgradeItem.setPatterns(new ItemStack(JDTEItems.ESSENCE_CONVERSION_UPGRADE.get()),
                    patterns.subList(0, 2));
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        helper.assertTrue(rejected, "the normal card still rejects more than one pattern");
        helper.succeed();
    }
}
