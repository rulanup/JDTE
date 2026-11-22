package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.datagen.recipes.GooSpreadRecipe;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import com.direwolf20.justdirethings.common.blocks.gooblocks.GooBlock_Base;
import com.direwolf20.justdirethings.datagen.JustDireItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Item;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Behaviour of the JDTE machine families, driven through their own code paths. */
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class MachineFamilyGameTests {

    /**
     * The gel generator converts through JDT's own goo-spread data recipes. The test discovers
     * one at runtime instead of hardcoding an item pair, installs a Creative card so the
     * conversion needs neither fuel nor FE, and then asserts the mapped output item appears.
     */
    @GameTest(template = "mineral_empty")
    public static void advancedGelGeneratorConvertsARealGooSpreadRecipe(GameTestHelper h) {
        assertGelConversion(h, JDTEBlocks.ADVANCED_GEL_GENERATOR.get(), new BlockPos(2, 1, 2));
    }

    /** The extended tier is the same block entity with eight upgrade slots. */
    @GameTest(template = "mineral_empty")
    public static void extendedGelGeneratorConvertsARealGooSpreadRecipe(GameTestHelper h) {
        assertGelConversion(h, JDTEBlocks.EXTENDED_GEL_GENERATOR.get(), new BlockPos(4, 1, 2));
    }

    private static void assertGelConversion(GameTestHelper h, net.minecraft.world.level.block.Block block,
                                            BlockPos relativePos) {
        var recipes = h.getLevel().getRecipeManager()
                .getAllRecipesFor(Registration.GOO_SPREAD_RECIPE_TYPE.get());
        h.assertTrue(!recipes.isEmpty(), "JDT ships at least one goo spread recipe");
        GooSpreadRecipe recipe = recipes.stream()
                .filter(candidate -> candidate.getTierRequirement() <= 1)
                .findFirst().orElse(null);
        h.assertTrue(recipe != null, "at least one goo spread recipe works with tier-1 gel");

        ItemStack expected = GelGeneratorBE.getOutputItemForState(recipe.getOutput());
        h.assertTrue(!expected.isEmpty(), "the chosen recipe maps to a known output item");

        h.setBlock(relativePos, block);
        GelGeneratorBE generator = (GelGeneratorBE) h.getBlockEntity(relativePos);

        boolean creative = false;
        var upgrades = UpgradeHelper.getUpgradeHandler(generator);
        for (int i = 0; i < upgrades.getSlots(); i++) {
            if (upgrades.isItemValid(i, new ItemStack(JDTEItems.CREATIVE_UPGRADE.get()))) {
                upgrades.setStackInSlot(i, new ItemStack(JDTEItems.CREATIVE_UPGRADE.get()));
                creative = true;
                break;
            }
        }
        h.assertTrue(creative, "the generator has a slot that accepts the creative card");

        var handler = generator.getMachineHandler();
        handler.setStackInSlot(GelGeneratorBE.GEL_SLOT,
                new ItemStack(Registration.GooBlock_Tier1_ITEM.get()));
        handler.setStackInSlot(GelGeneratorBE.INPUT_START_SLOT,
                new ItemStack(recipe.getInput().getBlock().asItem()));

        for (int tick = 0; tick < generator.getConversionProgressMax(); tick++) {
            generator.processConversion();
        }

        boolean produced = false;
        for (int slot = GelGeneratorBE.OUTPUT_START_SLOT;
             slot < GelGeneratorBE.OUTPUT_START_SLOT + GelGeneratorBE.OUTPUT_SLOTS; slot++) {
            ItemStack out = handler.getStackInSlot(slot);
            if (!out.isEmpty() && out.getItem() == expected.getItem()) {
                produced = true;
                break;
            }
        }
        h.assertTrue(produced, "the generator converted " + recipe.getInput().getBlock()
                + " into " + expected.getItem());
        h.succeed();
    }

    /** The glue activator revives a dead goo block in its area using a tagged revive item. */
    @GameTest(template = "mineral_empty")
    public static void basicGlueActivatorRevivesADeadGooBlock(GameTestHelper h) {
        assertGlueRevival(h, JDTEBlocks.BASIC_GLUE_ACTIVATOR.get(), new BlockPos(2, 1, 2), new BlockPos(3, 1, 2));
    }

    @GameTest(template = "mineral_empty")
    public static void advancedGlueActivatorRevivesADeadGooBlock(GameTestHelper h) {
        assertGlueRevival(h, JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get(), new BlockPos(2, 1, 2), new BlockPos(3, 1, 2));
    }

    @GameTest(template = "mineral_empty")
    public static void extendedGlueActivatorRevivesADeadGooBlock(GameTestHelper h) {
        assertGlueRevival(h, JDTEBlocks.EXTENDED_GLUE_ACTIVATOR.get(), new BlockPos(2, 1, 2), new BlockPos(3, 1, 2));
    }

    private static void assertGlueRevival(GameTestHelper h, net.minecraft.world.level.block.Block activatorBlock,
                                          BlockPos machinePos, BlockPos gooPos) {
        ItemStack revive = findReviveItem();
        h.assertTrue(!revive.isEmpty(), "JDT ships at least one goo revive item in its revive tags");

        h.setBlock(gooPos, Registration.GooBlock_Tier1.get().defaultBlockState()
                .setValue(GooBlock_Base.ALIVE, false));
        h.setBlock(machinePos, activatorBlock);
        GlueActivatorBE machine = (GlueActivatorBE) h.getBlockEntity(machinePos);
        machine.setAreaSettings(1, 1, 1, 0, 0, 0, false);
        machine.getItemHandler().setStackInSlot(GlueActivatorBE.REVIVE_SLOT, revive);

        h.assertTrue(!h.getBlockState(gooPos).getValue(GooBlock_Base.ALIVE), "the goo starts dead");
        var gooEntity = h.getBlockEntity(gooPos);
        h.assertTrue(gooEntity instanceof com.direwolf20.justdirethings.common.blockentities.basebe.GooBlockBE_Base,
                "the placed goo carries a goo block entity, actual=" + gooEntity);
        h.assertTrue(machine.isValidRevivalItem(revive),
                "the activator accepts " + revive.getItem() + " as a revive item");

        // The powered tiers refuse to revive without FE, so charge them first.
        if (machine instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered) {
            powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
        }

        // Drive the revival itself: the area scan is covered by the tier area tests, and a
        // direct call keeps this assertion about the machine's own action.
        boolean revived = machine.tryReviveGoo(h.getLevel(), h.absolutePos(gooPos), h.getBlockState(gooPos));
        h.assertTrue(revived, "the activator revived the goo block with " + revive.getItem());
        h.assertTrue(h.getBlockState(gooPos).getValue(GooBlock_Base.ALIVE),
                "the activator revived the goo block with " + revive.getItem());
        h.succeed();
    }

    /** Finds a qualifying revive item from JDT's own tags instead of hardcoding one. */
    private static ItemStack findReviveItem() {
        for (TagKey<Item> tag : java.util.List.of(JustDireItemTags.GOO_REVIVE_TIER_1,
                JustDireItemTags.GOO_REVIVE_TIER_2, JustDireItemTags.GOO_REVIVE_TIER_3,
                JustDireItemTags.GOO_REVIVE_TIER_4)) {
            ItemStack[] items = Ingredient.of(tag).getItems();
            if (items.length > 0) {
                return new ItemStack(items[0].getItem());
            }
        }
        return ItemStack.EMPTY;
    }
}
