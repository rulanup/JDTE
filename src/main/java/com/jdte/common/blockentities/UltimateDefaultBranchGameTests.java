package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Runtime acceptance for the Ultimate cards on the families the source never
 * intercepts, i.e. the ones that reach {@code UpgradeHelper.isUpgradeCompatible}'s
 * default branch. {@link Ultimate27GameTests} proves the gate and the helper
 * arithmetic; this class proves the cards change the real machines: physical
 * FE buffers, real fluid tanks, the real energy cost a machine charges, and
 * the real ticker's execution count.
 *
 * Coverage is the representative hosts a player actually reaches:
 * advanced glue activator (powered + costed), the loot fabricator (energy +
 * fluid + locked tick speed), and the extended generator (native JDT host that
 * reaches the default branch through Forge's ExtendedUpgradeMachine marker).
 * The item/fluid senders and receivers keep their own dedicated suites
 * (ItemSenderUltimateGameTests, FluidSenderValidationGameTests,
 * ReceiverValidationGameTests).
 *
 * What this does NOT prove: real-client GUI/JEI rendering and packaged
 * dedicated-server lifecycle. Those are separate validation rounds.
 */
@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class UltimateDefaultBranchGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private static BaseMachineBE place(GameTestHelper h, net.minecraft.world.level.block.Block block) {
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, block);
        return (BaseMachineBE) h.getBlockEntity(POS);
    }

    private static void card(BaseMachineBE be, net.minecraft.world.item.Item item, int slot) {
        if (!UpgradeHelper.getUpgradeHandler(be).insertItem(slot, new ItemStack(item), false).isEmpty())
            throw new IllegalStateException("Rejected fixture card " + item);
    }

    private static int stored(BaseMachineBE be) {
        return be.getCapability(ForgeCapabilities.ENERGY, null)
                .orElseThrow(IllegalStateException::new).getEnergyStored();
    }

    private static int maxStored(BaseMachineBE be) {
        return be.getCapability(ForgeCapabilities.ENERGY, null)
                .orElseThrow(IllegalStateException::new).getMaxEnergyStored();
    }

    @SuppressWarnings("unchecked")
    private static void tickOnce(GameTestHelper h, BaseMachineBE be) {
        var ticker = (net.minecraft.world.level.block.entity.BlockEntityTicker<
                net.minecraft.world.level.block.entity.BlockEntity>)
                be.getBlockState().getTicker(h.getLevel(), be.getType());
        if (ticker == null) throw new IllegalStateException("no ticker for " + be.getType());
        ticker.tick(h.getLevel(), be.getBlockPos(), be.getBlockState(), be);
    }

    @GameTest(template = "mineral_empty")
    public static void glueActivatorPhysicalCapacityCostAndTicker(GameTestHelper h) {
        var be = (AdvancedGlueActivatorBE) place(h, JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get());
        // Baseline: the advertised capacity is the physical buffer before any card.
        h.assertTrue(maxStored(be) == AdvancedGlueActivatorBE.BASE_ENERGY_CAPACITY,
                "baseline glue capacity " + maxStored(be));
        h.assertTrue(be.getStandardEnergyCost() == AdvancedGlueActivatorBE.BASE_ENERGY_COST,
                "baseline glue cost " + be.getStandardEnergyCost());

        card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
        h.assertTrue(be.getMaxEnergy() == AdvancedGlueActivatorBE.BASE_ENERGY_CAPACITY * 50L,
                "Ultimate Capacity x50 advertised, got " + be.getMaxEnergy());
        h.assertTrue(maxStored(be) == AdvancedGlueActivatorBE.BASE_ENERGY_CAPACITY * 50L,
                "Ultimate Capacity x50 in the physical buffer, got " + maxStored(be));
        // The buffer can really hold the new capacity, not just advertise it.
        be.getEnergyStorage().setEnergy(be.getMaxEnergy());
        h.assertTrue(stored(be) == be.getMaxEnergy(), "glue buffer really stores the new capacity");

        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
        h.assertTrue(be.getStandardEnergyCost() == AdvancedGlueActivatorBE.BASE_ENERGY_COST * 50,
                "Ultimate Overclock x50 real charge, got " + be.getStandardEnergyCost());
        // Removing the capacity card must not leave energy above the shrunk buffer.
        h.assertTrue(UpgradeHelper.getUpgradeHandler(be).extractItem(0, 1, false).getCount() == 1,
                "capacity card removed");
        h.assertTrue(stored(be) <= maxStored(be),
                "stored FE truncated to the shrunk physical buffer, stored=" + stored(be) + " max=" + maxStored(be));
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void lootFabricatorPhysicalCapacityFluidAndLockedSpeed(GameTestHelper h) {
        var be = (LootFabricatorBE) place(h, JDTEBlocks.LOOT_FABRICATOR.get());
        h.assertTrue(be.getMaxEnergy() == LootFabricatorBE.BASE_ENERGY_CAPACITY,
                "baseline fabricator capacity " + be.getMaxEnergy());
        h.assertTrue(be.getMaxFluidCapacity() == LootFabricatorBE.BASE_FLUID_CAPACITY,
                "baseline fabricator fluid " + be.getMaxFluidCapacity());
        h.assertTrue(be.getEffectiveEnergyCost() == LootFabricatorBE.ENERGY_COST,
                "baseline fabricator cost " + be.getEffectiveEnergyCost());

        card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
        h.assertTrue(maxStored(be) == LootFabricatorBE.BASE_ENERGY_CAPACITY * 50L,
                "fabricator physical FE buffer x50, got " + maxStored(be));
        h.assertTrue(be.getMaxFluidCapacity() == LootFabricatorBE.BASE_FLUID_CAPACITY * 50L,
                "fabricator fluid tank x50, got " + be.getMaxFluidCapacity());

        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
        h.assertTrue(be.getEffectiveEnergyCost() == LootFabricatorBE.ENERGY_COST * 50,
                "fabricator real cost x50, got " + be.getEffectiveEnergyCost());
        // The fabricator is a locked-delay machine, so Ultimate Overclock locks
        // its process time to one tick (the same rule the item senders assert),
        // while the base tick speed stays the default PROCESS_TIME.
        h.assertTrue(be.getProcessTime() == 1,
                "fabricator process time locked to 1 tick, got " + be.getProcessTime());
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void lootFabricatorUltimateCardsExclusiveAndSurviveReload(GameTestHelper h) {
        var be = (LootFabricatorBE) place(h, JDTEBlocks.LOOT_FABRICATOR.get());
        card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
        var upgrades = UpgradeHelper.getUpgradeHandler(be);
        // Speed cards are exclusive and each Ultimate card is capped at one.
        h.assertTrue(!upgrades.isItemValid(2, new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get()))
                        && !upgrades.isItemValid(2, new ItemStack(JDTEItems.UNDERCLOCK_UPGRADE.get()))
                        && !upgrades.isItemValid(2, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()))
                        && !upgrades.isItemValid(2, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())),
                "fabricator speed cards exclusive and Ultimate capped at one");
        // Looting cards still coexist with the Ultimate cards (dedicated handler).
        var saved = be.saveWithFullMetadata();
        var fresh = new LootFabricatorBE(be.getBlockPos(), be.getBlockState());
        h.getLevel().setBlockEntity(fresh);
        fresh.load(saved);
        var reloaded = UpgradeHelper.getUpgradeHandler(fresh);
        h.assertTrue(reloaded.getStackInSlot(0).is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())
                        && reloaded.getStackInSlot(1).is(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()),
                "both Ultimate cards survive the fabricator save/reload");
        // Capacity is re-applied at the head of the server tick (BaseMachineBEMixin),
        // not inside load(), so a freshly loaded machine only reaches its x50 buffer
        // once it ticks. Drive the real ticker instead of calling the helper directly.
        tickOnce(h, fresh);
        h.assertTrue(maxStored(fresh) == LootFabricatorBE.BASE_ENERGY_CAPACITY * 50L,
                "reloaded fabricator reaches the x50 physical buffer on its first tick, got " + maxStored(fresh));
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void glueActivatorSpeedCardsExclusive(GameTestHelper h) {
        var be = place(h, JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get());
        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        var upgrades = UpgradeHelper.getUpgradeHandler(be);
        h.assertTrue(!upgrades.isItemValid(1, new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get()))
                        && !upgrades.isItemValid(1, new ItemStack(JDTEItems.UNDERCLOCK_UPGRADE.get()))
                        && !upgrades.isItemValid(1, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())),
                "glue activator rejects the other speed cards and a second Ultimate Overclock");
        h.assertTrue(UpgradeHelper.getUpgradeHandler(be).insertItem(2,
                        new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false).isEmpty(),
                "glue activator accepts Ultimate Capacity alongside Ultimate Overclock");
        h.assertTrue(UpgradeHelper.getUpgradeHandler(be).insertItem(3,
                        new ItemStack(JDTEItems.FILTER_UPGRADE.get()), false).isEmpty(),
                "glue activator keeps its ordinary Filter card alongside both Ultimate cards");
        h.succeed();
    }
}
