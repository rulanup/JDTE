package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.common.fluids.basefluids.RefinedFuel;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class GeneratorUpgradeGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private static Fluid fuel() {
        return BuiltInRegistries.FLUID.stream().filter(f -> f instanceof RefinedFuel refined && refined.fePerMb() > 0)
                .findFirst().orElseThrow();
    }

    private static GeneratorFluidT1BE fluidGenerator(GameTestHelper h) {
        h.setBlock(POS, Registration.GeneratorFluidT1.get());
        var generator = (GeneratorFluidT1BE) h.getBlockEntity(POS);
        UpgradeHelper.getUpgradeHandler(generator).insertItem(0, new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()), false);
        return generator;
    }

    @GameTest(template = "mineral_empty")
    public static void lastMbSurvivesRepeatedTicksAndSavedReloadUntilSecondMbArrives(GameTestHelper h) {
        var generator = fluidGenerator(h);
        generator.getFluidTank().setFluid(new FluidStack(fuel(), 1));
        for (int i = 0; i < 500; i++) generator.doGenerate();
        h.assertTrue(generator.getFluidStack().getAmount() == 1 && generator.getEnergyStorage().getEnergyStored() == 0,
                "500 insufficient attempts must not debit one mB");
        var restored = (GeneratorFluidT1BE) BlockEntity.loadStatic(generator.getBlockPos(), generator.getBlockState(), generator.saveWithFullMetadata());
        h.getLevel().setBlockEntity(restored);
        restored.doGenerate();
        h.assertTrue(restored.getFluidStack().getAmount() == 1 && UpgradeHelper.hasGeneratorUpgrade(restored), "one mB and card survive reload");
        restored.getFluidTank().setFluid(new FluidStack(fuel(), 2));
        int generated = restored.getFePerFuelTick() * 3;
        restored.doGenerate();
        h.assertTrue(restored.getFluidStack().isEmpty() && restored.getEnergyStorage().getEnergyStored() == generated,
                "two mB buy exactly triple native FE");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void fullAndPartialEnergySpaceAndRedstoneNeverConsumeFluid(GameTestHelper h) {
        var generator = fluidGenerator(h);
        generator.getFluidTank().setFluid(new FluidStack(fuel(), 3));
        int generated = generator.getFePerFuelTick() * 3;
        int capacity = generator.getMaxEnergy();
        for (int stored : new int[]{capacity, capacity - generated + 1}) {
            generator.getEnergyStorage().setEnergy(stored);
            generator.doGenerate();
            h.assertTrue(generator.getFluidStack().getAmount() == 3 && generator.getEnergyStorage().getEnergyStored() == stored,
                    "no partial batch when FE buffer cannot accept all output");
        }
        generator.getEnergyStorage().setEnergy(capacity - generated);
        generator.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.HIGH;
        generator.doGenerate();
        h.assertTrue(generator.getFluidStack().getAmount() == 3 && generator.getEnergyStorage().getEnergyStored() == capacity - generated,
                "disabled redstone cannot spend fluid");
        generator.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.IGNORED;
        generator.doGenerate();
        h.assertTrue(generator.getFluidStack().getAmount() == 1 && generator.getEnergyStorage().getEnergyStored() == capacity,
                "exact headroom accepts one whole batch and preserves odd remainder");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void unexpectedShortEnergyCommitRollsBackOwnedBuffers(GameTestHelper h) {
        var original = fluidGenerator(h);
        var generator = new GeneratorFluidT1BE(original.getBlockPos(), original.getBlockState()) {
            @Override public int insertEnergy(int amount, boolean simulate) {
                int accepted = super.insertEnergy(amount, simulate);
                return !simulate && accepted > 0 ? accepted - 1 : accepted;
            }
        };
        h.getLevel().setBlockEntity(generator);
        UpgradeHelper.getUpgradeHandler(generator).insertItem(0, new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()), false);
        generator.getFluidTank().setFluid(new FluidStack(fuel(), 3));
        generator.getEnergyStorage().setEnergy(123);
        generator.doGenerate();
        h.assertTrue(generator.getFluidStack().getAmount() == 3 && generator.getEnergyStorage().getEnergyStored() == 123,
                "unexpected partial commit restores the machine's exact fluid and FE buffers");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void nativeSingleMbAndSolidFuelUpgradeBehaviorRemainIntact(GameTestHelper h) {
        var generator = fluidGenerator(h);
        UpgradeHelper.getUpgradeHandler(generator).extractItem(0, 1, false);
        generator.getFluidTank().setFluid(new FluidStack(fuel(), 1));
        int nativeOutput = generator.getFePerFuelTick();
        generator.doGenerate();
        h.assertTrue(generator.getFluidStack().isEmpty() && generator.getEnergyStorage().getEnergyStored() == nativeOutput,
                "without card native one-mB generation remains unchanged");
        h.setBlock(POS, Registration.GeneratorT1.get());
        var solid = (GeneratorT1BE) h.getBlockEntity(POS);
        UpgradeHelper.getUpgradeHandler(solid).insertItem(0, new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()), false);
        solid.getMachineHandler().setStackInSlot(0, new ItemStack(Items.COAL));
        solid.doBurn();
        h.assertTrue(solid.getMachineHandler().getStackInSlot(0).getCount() == 1 && solid.feRemaining == 0,
                "one coal remains when upgraded two-item fuel cost cannot be met");
        solid.getMachineHandler().setStackInSlot(0, new ItemStack(Items.COAL, 2));
        int expected = net.minecraftforge.common.ForgeHooks.getBurnTime(new ItemStack(Items.COAL), RecipeType.SMELTING) * solid.getFePerFuelTick() * 3;
        solid.doBurn();
        h.assertTrue(solid.getMachineHandler().getStackInSlot(0).isEmpty() && solid.feRemaining == expected,
                "two coal buy the source tripled pending generation: remaining=" + solid.feRemaining + " expected=" + expected + " slot=" + solid.getMachineHandler().getStackInSlot(0) + " card=" + UpgradeHelper.hasGeneratorUpgrade(solid));
        h.succeed();
    }
}
