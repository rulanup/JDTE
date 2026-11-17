package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

/**
 * First behavioural coverage for the item/fluid receiver families, which the port matrix lists as
 * partial because their Ultimate cards and dedicated runtime evidence were missing. These tests use
 * real adjacent inventories/tanks and the real block ticker; they assert conservation instead of a
 * theoretical transfer rate.
 */
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class ReceiverValidationGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);
    private static final BlockPos SOURCE = new BlockPos(3, 1, 2);
    private static final int TICKS = 4;

    private ReceiverValidationGameTests() {
    }

    private static ItemReceiverBE placeItemReceiver(GameTestHelper helper, int tier) {
        helper.setBlock(POS, Blocks.AIR);
        helper.setBlock(POS, tier == 0 ? JDTEBlocks.BASIC_ITEM_RECEIVER.get()
                : tier == 1 ? JDTEBlocks.ADVANCED_ITEM_RECEIVER.get() : JDTEBlocks.EXTENDED_ITEM_RECEIVER.get());
        ItemReceiverBE receiver = (ItemReceiverBE) helper.getBlockEntity(POS);
        helper.setBlock(SOURCE, Blocks.AIR);
        helper.setBlock(SOURCE, Blocks.CHEST);
        return receiver;
    }

    private static FluidReceiverBE placeFluidReceiver(GameTestHelper helper, int tier) {
        helper.setBlock(POS, Blocks.AIR);
        helper.setBlock(POS, tier == 0 ? JDTEBlocks.BASIC_FLUID_RECEIVER.get()
                : tier == 1 ? JDTEBlocks.ADVANCED_FLUID_RECEIVER.get() : JDTEBlocks.EXTENDED_FLUID_RECEIVER.get());
        FluidReceiverBE receiver = (FluidReceiverBE) helper.getBlockEntity(POS);
        helper.setBlock(SOURCE, Blocks.AIR);
        helper.setBlock(SOURCE, JDTEBlocks.BASIC_FLUID_SENDER.get());
        return receiver;
    }

    /** The receivers only act while their redstone mode allows it; LOW without a signal is active. */
    private static void allowRedstone(ItemReceiverBE machine) {
        machine.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.LOW;
        machine.getRedstoneControlData().receivingRedstone = false;
    }

    private static void allowRedstone(FluidReceiverBE machine) {
        machine.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.LOW;
        machine.getRedstoneControlData().receivingRedstone = false;
    }

    private static void charge(BaseMachineBE machine) {
        if (machine instanceof PoweredItemReceiverBE powered) powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
        if (machine instanceof PoweredFluidReceiverBE powered) powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
    }

    private static int receiverCount(ItemReceiverBE receiver) {
        int total = 0;
        for (int slot = 0; slot < receiver.getItemHandler().getSlots(); slot++) {
            total += receiver.getItemHandler().getStackInSlot(slot).getCount();
        }
        return total;
    }

    private static int sourceCount(GameTestHelper helper) {
        IItemHandler handler = helper.getLevel().getBlockEntity(helper.absolutePos(SOURCE))
                .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.WEST)
                .orElseThrow(IllegalStateException::new);
        int total = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) total += handler.getStackInSlot(slot).getCount();
        return total;
    }

    /** Puts the whole fixture stock into one source slot so conservation checks stay exact. */
    private static void fillSource(GameTestHelper helper, int count) {
        IItemHandler handler = helper.getLevel().getBlockEntity(helper.absolutePos(SOURCE))
                .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.WEST)
                .orElseThrow(IllegalStateException::new);
        handler.insertItem(0, new ItemStack(Items.STONE, count), false);
    }

    @SuppressWarnings("unchecked")
    private static void tick(GameTestHelper helper, BaseMachineBE machine, int times) {
        var ticker = (net.minecraft.world.level.block.entity.BlockEntityTicker<BlockEntity>)
                machine.getBlockState().getTicker(helper.getLevel(), machine.getType());
        for (int tick = 0; tick < times; tick++) {
            ticker.tick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
        }
    }

    @GameTest(templateNamespace = "jdte", template = "mineral_empty")
    public static void itemReceiversPullFromAdjacentChest(GameTestHelper helper) {
        for (int tier = 0; tier < 3; tier++) {
            ItemReceiverBE receiver = placeItemReceiver(helper, tier);
            allowRedstone(receiver);
            charge(receiver);
            fillSource(helper, 64);
            helper.assertTrue(sourceCount(helper) == 64, "the fixture chest holds exactly 64 items");
            tick(helper, receiver, TICKS);

            int received = receiverCount(receiver);
            int remaining = sourceCount(helper);
            helper.assertTrue(received > 0, "tier " + tier + " receiver pulled items, received=" + received);
            helper.assertTrue(received + remaining == 64,
                    "tier " + tier + " receiver conserved the source stock, received=" + received + " remaining=" + remaining);

            CompoundTag saved = receiver.saveWithFullMetadata();
            receiver.load(saved);
            helper.assertTrue(receiverCount(receiver) == received,
                    "tier " + tier + " receiver keeps its storage across save/load");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "jdte", template = "mineral_empty")
    public static void poweredItemReceiverNeedsEnergy(GameTestHelper helper) {
        ItemReceiverBE receiver = placeItemReceiver(helper, 1);
        allowRedstone(receiver);
        fillSource(helper, 64);
        helper.assertTrue(sourceCount(helper) == 64, "the fixture chest holds exactly 64 items");
        tick(helper, receiver, TICKS);
        helper.assertTrue(receiverCount(receiver) == 0 && sourceCount(helper) == 64,
                "an unpowered advanced receiver moves nothing");

        charge(receiver);
        tick(helper, receiver, TICKS);
        int received = receiverCount(receiver);
        helper.assertTrue(received > 0 && received + sourceCount(helper) == 64,
                "the same receiver pulls once it has energy, received=" + received);
        helper.succeed();
    }

    @GameTest(templateNamespace = "jdte", template = "mineral_empty")
    public static void fluidReceiversPullFromAdjacentTank(GameTestHelper helper) {
        for (int tier = 0; tier < 3; tier++) {
            FluidReceiverBE receiver = placeFluidReceiver(helper, tier);
            allowRedstone(receiver);
            charge(receiver);
            FluidSenderBE source = (FluidSenderBE) helper.getBlockEntity(SOURCE);
            int filled = source.getFluidTank().fill(new FluidStack(Fluids.WATER, 8000), IFluidHandler.FluidAction.EXECUTE);
            helper.assertTrue(filled > 0, "the fixture source tank accepted water, filled=" + filled);

            tick(helper, receiver, TICKS);
            int received = receiver.getFluidTank().getFluidAmount();
            int remaining = source.getFluidTank().getFluidAmount();
            helper.assertTrue(received > 0, "tier " + tier + " receiver pulled fluid, received=" + received);
            helper.assertTrue(received + remaining == filled,
                    "tier " + tier + " receiver conserved the source tank, received=" + received + " remaining=" + remaining);

            CompoundTag saved = receiver.saveWithFullMetadata();
            receiver.load(saved);
            helper.assertTrue(receiver.getFluidTank().getFluidAmount() == received,
                    "tier " + tier + " receiver keeps its tank across save/load");
        }
        helper.succeed();
    }
}
