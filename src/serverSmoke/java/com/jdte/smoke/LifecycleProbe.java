package com.jdte.smoke;

import com.jdte.common.blockentities.*;
import com.jdte.common.blocks.LargeMineralExtractorStructure;
import com.jdte.common.items.MineralSurveyItem;
import com.jdte.common.minerals.*;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;
import java.util.List;

/** Test-only mod; never part of the production JDTE JAR. */
@Mod("jdte_lifecycle_probe")
public final class LifecycleProbe {
    private static final BlockPos LARGE = new BlockPos(1023, 80, 1024);
    private static final BlockPos NORMAL = new BlockPos(1040, 80, 1040);
    private String mode;
    private MinecraftServer server;
    private int ticks, stage, stagedAt;
    private CompoundTag expected;
    private net.minecraftforge.energy.IEnergyStorage retained;
    private net.minecraft.world.level.block.entity.BlockEntity retainedPart;
    public LifecycleProbe() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new MultiplayerProbe());
        MinecraftForge.EVENT_BUS.register(new MixerMultiplayerProbe());
        MinecraftForge.EVENT_BUS.register(new GeneratorMultiplayerProbe());
        MinecraftForge.EVENT_BUS.register(new BioMultiplayerProbe());
        MinecraftForge.EVENT_BUS.register(new MixerLifecycleProbe());
        MinecraftForge.EVENT_BUS.register(new EnergyUpgradeLifecycleProbe());
        MinecraftForge.EVENT_BUS.register(new GeneratorLifecycleProbe());
        MinecraftForge.EVENT_BUS.register(new BioExtractorLifecycleProbe());
        MinecraftForge.EVENT_BUS.register(new EntityAcceleratorLifecycleProbe());
        MinecraftForge.EVENT_BUS.register(new TransmitterLifecycleProbe());
        MinecraftForge.EVENT_BUS.register(new TransmitterNetworkProbe());
        MinecraftForge.EVENT_BUS.register(new TierConversionProbe());
        MinecraftForge.EVENT_BUS.register(new PortalGunProbe());
        if (net.minecraftforge.fml.ModList.get().isLoaded("ae2") && net.minecraftforge.fml.ModList.get().isLoaded("appflux") && net.minecraftforge.fml.ModList.get().isLoaded("curios"))
            MinecraftForge.EVENT_BUS.register(new AdvancedTransmitterAcceptanceProbe());
        if (net.minecraftforge.fml.ModList.get().isLoaded("ae2"))
            MinecraftForge.EVENT_BUS.register(new AECraftingRestartProbe());
        if (net.minecraftforge.fml.ModList.get().isLoaded("mekanism_extras"))
            MinecraftForge.EVENT_BUS.register(new MekanismFuelProbe());
    }
    private static void check(boolean condition, String message) { if (!condition) throw new IllegalStateException("JDTE lifecycle: " + message); }
    @SubscribeEvent public void started(ServerStartedEvent event) {
        mode = System.getProperty("jdte.lifecycle", ""); server = event.getServer();
        if (mode.isEmpty()) return;
        check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")), "run only in a marked disposable server");
        ServerLevel level = server.overworld();
        level.setChunkForced(LARGE.getX() >> 4, LARGE.getZ() >> 4, true);
        level.setChunkForced((LARGE.getX() + 1) >> 4, LARGE.getZ() >> 4, true);
        level.setChunkForced(NORMAL.getX() >> 4, NORMAL.getZ() >> 4, true);
        level.getChunkAt(LARGE); level.getChunkAt(LARGE.east()); level.getChunkAt(NORMAL);
        if (mode.equals("seed")) {
            for (int y=0;y<2;y++) for(int z=0;z<3;z++) for(int x=-1;x<=1;x++) {
                BlockPos pos=LARGE.offset(x,y,z);level.removeBlockEntity(pos);
                level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2 | 64);
            }
            level.removeBlockEntity(NORMAL); level.setBlock(NORMAL,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);

            level.setBlockAndUpdate(LARGE, JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get().defaultBlockState());
            check(LargeMineralExtractorStructure.placeParts(level, LARGE, Direction.NORTH), "place boundary-spanning structure");
            level.setBlockAndUpdate(NORMAL, JDTEBlocks.MINERAL_EXTRACTOR.get().defaultBlockState());
            fill((MineralExtractorBE) level.getBlockEntity(LARGE)); fill((MineralExtractorBE) level.getBlockEntity(NORMAL));
        } else if (mode.equals("verify")) {
            try { expected = NbtIo.readCompressed(Path.of("lifecycle-expected.nbt").toFile()); }
            catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
        } else throw new IllegalArgumentException("Unknown lifecycle mode: " + mode);
    }
    private void fill(MineralExtractorBE be) {
        UpgradeHelper.getUpgradeHandler(be).insertItem(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false);
        be.getEnergyStorage().setEnergy(50_000_000);
        be.getTimeFluidTank().setFluid(new net.minecraftforge.fluids.FluidStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(), 80_000));
        be.getExperienceFluidTank().setFluid(new net.minecraftforge.fluids.FluidStack(com.direwolf20.justdirethings.setup.Registration.XP_FLUID_SOURCE.get(), 70_000));
        be.getRedstoneControlData().redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;
        for (int i = 0; i < be.surveySlotCount(); i++) {
            ItemStack survey = new ItemStack(JDTEItems.MINERAL_SURVEY.get());
            MineralSurveyItem.setSurvey(survey, MineralSurveyData.create(MineralSurveyIndex.version(server), ResourceLocation.withDefaultNamespace("plains"),
                    ResourceLocation.withDefaultNamespace("overworld"), List.of(new MineralEntry(ResourceLocation.withDefaultNamespace("iron_ore"), 1, -64, 64, 9, MineralEntry.Confidence.EXACT))));
            be.getMachineHandler().setStackInSlot(i, survey);
        }
        for (int i = 0; i < 64; i++) be.getMachineHandler().setStackInSlot(be.outputStartSlot() + i, new ItemStack(Items.IRON_ORE, 6_400_000));
        be.setMultiplier(17);
        var tag = be.saveWithFullMetadata(); tag.putLong("pendingBaseWork", 13); tag.putLong("pendingAcceleratedWork", 7); be.load(tag);
        be.setChanged();
    }
    private CompoundTag snapshot() {
        var level = server.overworld(); var tag = new CompoundTag();
        tag.put("large", level.getBlockEntity(LARGE).saveWithFullMetadata());
        tag.put("normal", level.getBlockEntity(NORMAL).saveWithFullMetadata());
        return tag;
    }
    private void sameState() {
        CompoundTag actual = snapshot();
        for (String machine : List.of("large", "normal")) {
            for (String key : List.of("inventory", "energy", "experienceFluid", "timeFluid", "pendingBaseWork", "pendingAcceleratedWork", "multiplier", "ForgeCaps"))
                check(java.util.Objects.equals(actual.getCompound(machine).get(key), expected.getCompound(machine).get(key)), machine + " persisted " + key);
        }
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || server == null || mode == null || mode.isEmpty()) return;
        try { step(); }
        catch (Throwable failure) {
            try { Files.writeString(Path.of("lifecycle-failure.txt"), failure.toString()); }
            catch (java.io.IOException exception) { failure.addSuppressed(exception); }
            throw new IllegalStateException("Lifecycle probe failed", failure);
        }
    }
    private void step() throws java.io.IOException {
        ticks++;
        if (ticks > 1000) throw new IllegalStateException("lifecycle timeout stage " + stage);
        ServerLevel level = server.overworld();
        if (mode.equals("seed")) {
            if (stage == 0 && ticks > 40) {
                expected = snapshot(); NbtIo.writeCompressed(expected, Path.of("lifecycle-expected.nbt").toFile());
                var part = level.getBlockEntity(LARGE.east()); retainedPart = part;
                retained = part.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);
                server.saveEverything(false, true, true);
                level.setChunkForced(LARGE.getX() >> 4, LARGE.getZ() >> 4, false);
                level.setChunkForced((LARGE.getX() + 1) >> 4, LARGE.getZ() >> 4, false);
                level.setChunkForced(NORMAL.getX() >> 4, NORMAL.getZ() >> 4, false);
                stage = 1;
            } else if (stage == 1 && level.getChunkSource().getChunkNow(LARGE.getX() >> 4, LARGE.getZ() >> 4) == null) {
                check(retained.receiveEnergy(1000, false) == 0, "retained part capability cannot write to unloaded controller");
                level.setChunkForced(LARGE.getX() >> 4, LARGE.getZ() >> 4, true);
                level.getChunkAt(LARGE); level.getChunkAt(NORMAL); sameState();
                level.getChunkAt(LARGE.east());
                var freshPart=level.getBlockEntity(LARGE.east());
                check(retained.getEnergyStored() == (freshPart == retainedPart ? 50_000_000 : 0),
                        "reused live part reconnects; detached part endpoint remains invalid");
                check(freshPart.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).getEnergyStored()==50_000_000, "reloaded part routes to reloaded controller");
                Files.writeString(Path.of("lifecycle-seed-success.txt"), "PASS: real boundary chunk unload/reload, retained capability, all saved machine fields\n");
                server.halt(false); stage = 2;
            }
        } else if (mode.equals("verify")) {
            if (stage == 0 && ticks > 40) {
                sameState();
                var be = (MineralExtractorBE) level.getBlockEntity(LARGE);
                be.getMachineHandler().setStackInSlot(be.outputStartSlot() + 63, new ItemStack(Items.IRON_ORE, 6_399_985));
                be.getRedstoneControlData().redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.IGNORED;
                stage = 1; stagedAt = ticks;
            } else if (stage == 1 && ticks == stagedAt + 1) {
                var be = (MineralExtractorBE) level.getBlockEntity(LARGE);
                check(be.getMachineHandler().getStackInSlot(be.outputStartSlot() + 63).getCount() == 6_399_999, "resume fits seven fortune cycles without overflow");
                check(be.getEnergyStorage().getEnergyStored() == 49_965_000, "resume charges exactly seven cycles");
                check(be.getExperienceFluidTank().getFluidAmount() == 69_825, "resume fortune fluid conservation");
                Files.writeString(Path.of("lifecycle-restart-success.txt"), "PASS: fresh server JVM restored inventory, FE, both fluids, surveys, upgrades, multiplier and pending work; resumed with bounded output/resource costs\n");
                server.halt(false); stage = 2;
            }
        }
    }
}
