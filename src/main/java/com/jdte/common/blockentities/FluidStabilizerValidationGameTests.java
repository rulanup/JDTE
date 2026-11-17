package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.datagen.recipes.FluidDropRecipe;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Behavioural coverage for the Fluid Stabilizer family, which had none: it applies JDT's data-driven
 * FluidDrop recipes to real source-fluid blocks in its configured area.
 *
 * <p>The fixture takes an actual loaded FluidDrop recipe from the recipe manager (source fluid block,
 * non-air different output, real catalyst item) instead of hard-coding coordinates that a future JDT
 * data change could invalidate.</p>
 */
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class FluidStabilizerValidationGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);
    private static final BlockPos FLUID = new BlockPos(3, 1, 2);
    /** Basic tiers run every 40 ticks and powered tiers every 20, so one delay covers a full cycle. */
    private static final int DELAY = 60;

    private FluidStabilizerValidationGameTests() {
    }

    private static FluidDropRecipe nativeRecipe(GameTestHelper helper) {
        return helper.getLevel().getRecipeManager()
                .getAllRecipesFor(Registration.FLUID_DROP_RECIPE_TYPE.get()).stream()
                .filter(recipe -> recipe.getInput().getBlock() instanceof LiquidBlock
                        && !recipe.getOutput().isAir()
                        && recipe.getOutput().getBlock() != recipe.getInput().getBlock()
                        && recipe.getCatalyst() != Items.AIR)
                .findFirst()
                .orElseThrow();
    }

    private static void allowRedstone(FluidStabilizerBE machine) {
        machine.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.LOW;
        machine.getRedstoneControlData().receivingRedstone = false;
    }

    private static BlockState fluidAt(GameTestHelper helper) {
        return helper.getLevel().getBlockState(helper.absolutePos(FLUID));
    }

    private static ItemStack catalystSlot(FluidStabilizerBE machine) {
        return machine.getMachineHandler().getStackInSlot(FluidStabilizerBE.CATALYST_SLOT);
    }

    /** The basic tier needs no FE, so the recipe must simply apply and spend exactly one catalyst. */
    @GameTest(templateNamespace = "jdte", template = "mineral_empty")
    public static void basicStabilizerAppliesNativeFluidDropRecipe(GameTestHelper helper) {
        FluidDropRecipe recipe = nativeRecipe(helper);
        helper.setBlock(POS, JDTEBlocks.BASIC_FLUID_STABILIZER.get());
        FluidStabilizerBE machine = (FluidStabilizerBE) helper.getBlockEntity(POS);
        machine.setAreaSettings(1, 1, 1, 0, 0, 0, false);
        allowRedstone(machine);
        machine.getMachineHandler().setStackInSlot(FluidStabilizerBE.CATALYST_SLOT,
                new ItemStack(recipe.getCatalyst()));
        helper.setBlock(FLUID, recipe.getInput());
        helper.assertTrue(fluidAt(helper).equals(recipe.getInput()), "the fixture placed the source fluid");

        helper.runAfterDelay(DELAY, () -> {
            helper.assertTrue(fluidAt(helper).equals(recipe.getOutput()),
                    "the native FluidDrop recipe replaced the source fluid, got=" + fluidAt(helper));
            helper.assertTrue(catalystSlot(machine).isEmpty(),
                    "the single catalyst was spent, left=" + catalystSlot(machine).getCount());
            helper.succeed();
        });
    }

    /** The powered tier must gate on FE: nothing happens without energy, one cycle spends energy. */
    @GameTest(templateNamespace = "jdte", template = "mineral_empty")
    public static void poweredStabilizerNeedsEnergy(GameTestHelper helper) {
        FluidDropRecipe recipe = nativeRecipe(helper);
        helper.setBlock(POS, JDTEBlocks.ADVANCED_FLUID_STABILIZER.get());
        PoweredFluidStabilizerBE machine = (PoweredFluidStabilizerBE) helper.getBlockEntity(POS);
        machine.setAreaSettings(1, 1, 1, 0, 0, 0, false);
        allowRedstone(machine);
        machine.getMachineHandler().setStackInSlot(FluidStabilizerBE.CATALYST_SLOT,
                new ItemStack(recipe.getCatalyst()));
        helper.setBlock(FLUID, recipe.getInput());

        helper.runAfterDelay(DELAY, () -> {
            helper.assertTrue(fluidAt(helper).equals(recipe.getInput()),
                    "an unpowered stabilizer leaves the source fluid untouched");
            helper.assertTrue(catalystSlot(machine).getCount() == 1,
                    "an unpowered stabilizer spends no catalyst");

            machine.getEnergyStorage().setEnergy(machine.getMaxEnergy());
            helper.runAfterDelay(DELAY, () -> {
                helper.assertTrue(fluidAt(helper).equals(recipe.getOutput()),
                        "the charged stabilizer applied the recipe, got=" + fluidAt(helper));
                helper.assertTrue(catalystSlot(machine).isEmpty(),
                        "the charged stabilizer spent its catalyst");
                helper.assertTrue(machine.getEnergyStorage().getEnergyStored() < machine.getMaxEnergy(),
                        "the charged stabilizer spent FE, stored="
                                + machine.getEnergyStorage().getEnergyStored() + "/" + machine.getMaxEnergy());
                helper.succeed();
            });
        });
    }
}
