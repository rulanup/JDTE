package com.jdte.smoke;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Opt-in probe for the actual reobfuscated JAR, excluded from the production mod. */
public final class EnergyUpgradeLifecycleProbe {
    private static final BlockPos BREWER = new BlockPos(2040, 90, 2040);
    private static final BlockPos TRANSMITTER = BREWER.east(4);
    private String mode = "";
    private MinecraftServer server;
    private int ticks;

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("JDTE energy upgrade: " + message);
    }

    private AdvancedPotionBrewerBE brewer() {
        return (AdvancedPotionBrewerBE) server.overworld().getBlockEntity(BREWER);
    }

    private EnergyTransmitterBE transmitter() {
        return (EnergyTransmitterBE) server.overworld().getBlockEntity(TRANSMITTER);
    }

    private CompoundTag snapshot() {
        var result = new CompoundTag();
        result.put("brewer", brewer().saveWithFullMetadata());
        result.put("transmitter", transmitter().saveWithFullMetadata());
        return result;
    }

    @SubscribeEvent
    public void started(ServerStartedEvent event) throws IOException {
        mode = System.getProperty("jdte.energyUpgradeLifecycle", "");
        if (mode.isEmpty()) return;
        check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")), "requires a marked disposable server");
        server = event.getServer();
        var level = server.overworld();
        level.setChunkForced(BREWER.getX() >> 4, BREWER.getZ() >> 4, true);
        level.getChunkAt(BREWER);
        if (mode.equals("seed")) {
            for (var pos : List.of(BREWER, TRANSMITTER)) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(BREWER, JDTEBlocks.ADVANCED_POTION_BREWER.get().defaultBlockState());
            level.setBlockAndUpdate(TRANSMITTER, Registration.EnergyTransmitter.get().defaultBlockState());
            var b = brewer();
            check(UpgradeHelper.getUpgradeHandler(b).insertItem(0, new ItemStack(JDTEItems.ENERGY_BREWING_UPGRADE.get()), false).isEmpty(), "brewing card insertion");
            check(UpgradeHelper.getUpgradeHandler(transmitter()).insertItem(0, new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get()), false).isEmpty(), "overload insertion");
            b.getMachineHandler().setStackInSlot(AdvancedPotionBrewerBE.FUEL_SLOT, new ItemStack(Items.BLAZE_POWDER, 5));
            b.getMachineHandler().setStackInSlot(AdvancedPotionBrewerBE.BOTTLE_SLOT_0, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
            b.getMachineHandler().setStackInSlot(AdvancedPotionBrewerBE.INGREDIENT_SLOT, new ItemStack(Items.NETHER_WART));
            b.getEnergyStorage().setEnergy(50_000);
        } else if (mode.equals("verify")) {
            var expected = NbtIo.readCompressed(Path.of("energy-upgrades-expected.nbt").toFile());
            var actual = snapshot();
            for (String machine : List.of("brewer", "transmitter")) {
                for (String key : List.of("ForgeCaps", "energy", "inventory", "fuel", "brewProgress", "brewActiveTime", "brewSequenceActive", "brewActiveIngredientOrder")) {
                    check(Objects.equals(expected.getCompound(machine).get(key), actual.getCompound(machine).get(key)), machine + " restored " + key);
                }
            }
            check(brewer().hasEnergyBrewingUpgrade(), "saved brewing card active");
            var handler = UpgradeHelper.getUpgradeHandler(transmitter());
            var card = handler.extractItem(0, 1, false);
            check(card.is(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get()) && transmitter().fePerTick() == Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.get(), "removal restores configured native cap");
            check(handler.insertItem(0, card, false).isEmpty(), "reinstall saved overload without duplication");
        } else throw new IllegalArgumentException("Unknown energy upgrade probe mode: " + mode);
        check(transmitter().fePerTick() == Integer.MAX_VALUE, "production Mixin removes the native batch cap");
        for (String recipe : List.of("energy_brewing_upgrade", "energy_overload_upgrade")) {
            check(level.getRecipeManager().byKey(com.jdte.JDTE.id(recipe)).isPresent(), "production recipe " + recipe);
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) throws IOException {
        if (mode.isEmpty() || event.phase != TickEvent.Phase.END || event.getServer() != server) return;
        check(++ticks < 650, "timed out in " + mode);
        var b = brewer();
        var data = b.saveWithFullMetadata();
        if (mode.equals("seed") && data.getInt("brewProgress") >= 175) {
            check(data.getInt("brewProgress") == 175 && data.getInt("fuel") == 19 && b.getEnergyStorage().getEnergyStored() == 45_000, "one fuel purchase before a partially completed real brew");
            NbtIo.writeCompressed(snapshot(), Path.of("energy-upgrades-expected.nbt").toFile());
            finish("energy-upgrades-seed-success.txt", "PASS: production cards/recipes/Mixin active; natural brew saved at 175/400 ticks, 19 credits, 45000 FE and five retained Blaze Powder");
        } else if (mode.equals("verify") && !b.getMachineHandler().getStackInSlot(AdvancedPotionBrewerBE.OUTPUT_SLOT_START).isEmpty()) {
            var output = b.getMachineHandler().getStackInSlot(AdvancedPotionBrewerBE.OUTPUT_SLOT_START);
            check(output.getCount() == 1 && output.is(Items.POTION) && PotionUtils.getPotion(output) == Potions.AWKWARD, "one real resumed brewing product");
            check(data.getInt("fuel") == 19 && b.getEnergyStorage().getEnergyStored() == 44_700, "no repurchase after restart; normal completion costs 300 FE");
            check(b.getMachineHandler().getStackInSlot(AdvancedPotionBrewerBE.FUEL_SLOT).getCount() == 5 && b.getMachineHandler().getStackInSlot(AdvancedPotionBrewerBE.INGREDIENT_SLOT).isEmpty(), "powder conserved and ingredient consumed exactly once");
            finish("energy-upgrades-restart-success.txt", "PASS: fresh production JVM restored both cards, FE, fuel and progress; resumed exactly one awkward potion for 300 FE without a second fuel purchase; native overload removal/reinsertion restored cap");
        }
    }

    private void finish(String marker, String result) throws IOException {
        Files.writeString(Path.of(marker), result + "\n");
        mode = "";
        server.halt(false);
    }
}
