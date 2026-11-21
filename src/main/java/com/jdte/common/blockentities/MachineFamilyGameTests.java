package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.recipes.GooSpreadRecipe;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
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
        var recipes = h.getLevel().getRecipeManager()
                .getAllRecipesFor(Registration.GOO_SPREAD_RECIPE_TYPE.get());
        h.assertTrue(!recipes.isEmpty(), "JDT ships at least one goo spread recipe");
        GooSpreadRecipe recipe = recipes.stream()
                .filter(candidate -> candidate.getTierRequirement() <= 1)
                .findFirst().orElse(null);
        h.assertTrue(recipe != null, "at least one goo spread recipe works with tier-1 gel");

        ItemStack expected = GelGeneratorBE.getOutputItemForState(recipe.getOutput());
        h.assertTrue(!expected.isEmpty(), "the chosen recipe maps to a known output item");

        h.setBlock(new BlockPos(2, 1, 2), JDTEBlocks.ADVANCED_GEL_GENERATOR.get());
        GelGeneratorBE generator = (GelGeneratorBE) h.getBlockEntity(new BlockPos(2, 1, 2));

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
}
