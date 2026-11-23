package com.jdte.common.blockentities;

import com.jdte.common.blocks.SolarPanelBlock;
import com.jdte.common.solar.SolarEnergyTransfer;
import com.jdte.common.solar.SolarGenerationPolicy;
import com.jdte.common.solar.SolarPanelEnergyExportCapability;
import com.jdte.common.solar.SolarPanelEnergyStorage;
import com.jdte.common.solar.SolarPanelTier;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.EnumMap;
import java.util.Map;

public class SolarPanelBE extends BlockEntity {
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_OUTPUT_CURSOR = "OutputCursor";
    private final SolarPanelTier tier;
    private final SolarPanelEnergyStorage energyStorage;
    private final IEnergyStorage exposedEnergyStorage;
    private final Map<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> outputCaches =
            new EnumMap<>(Direction.class);
    private int outputCursor;
    private int lastGeneration;
    private boolean lastCanGenerate;

    public SolarPanelBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.tier = ((SolarPanelBlock) state.getBlock()).tier();
        this.energyStorage = tier.creative()
                ? SolarPanelEnergyStorage.creative()
                : SolarPanelEnergyStorage.finite(capacity());
        this.exposedEnergyStorage = new SolarPanelEnergyExportCapability(
                energyStorage, !tier.creative(), this::setChanged);
    }

    public SolarPanelBE(BlockPos pos, BlockState state) {
        this(JDTEBlockEntities.SOLAR_PANEL.get(), pos, state);
    }

    public SolarPanelTier tier() {
        return tier;
    }

    public IEnergyStorage getEnergyStorage() {
        return exposedEnergyStorage;
    }

    public int baseGeneration() {
        return switch (tier) {
            case CONCENTRATED -> JDTEConfig.COMMON.solarPanel.concentratedGeneration.get();
            case SINGULARITY -> JDTEConfig.COMMON.solarPanel.singularityGeneration.get();
            case STELLAR_FUSION -> JDTEConfig.COMMON.solarPanel.stellarFusionGeneration.get();
            case DIMENSIONAL_COLLAPSE -> JDTEConfig.COMMON.solarPanel.dimensionalCollapseGeneration.get();
            case CREATIVE -> Integer.MAX_VALUE;
        };
    }

    public int capacity() {
        return switch (tier) {
            case CONCENTRATED -> JDTEConfig.COMMON.solarPanel.concentratedCapacity.get();
            case SINGULARITY -> JDTEConfig.COMMON.solarPanel.singularityCapacity.get();
            case STELLAR_FUSION -> JDTEConfig.COMMON.solarPanel.stellarFusionCapacity.get();
            case DIMENSIONAL_COLLAPSE -> JDTEConfig.COMMON.solarPanel.dimensionalCollapseCapacity.get();
            case CREATIVE -> Integer.MAX_VALUE;
        };
    }

    public int currentGeneration() {
        return lastGeneration;
    }

    public boolean canGenerate() {
        return lastCanGenerate;
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, SolarPanelBE panel) {
        if (level instanceof ServerLevel serverLevel) {
            panel.serverTick(serverLevel, pos, state);
        }
    }

    public static boolean shouldPushEnergy(boolean creative, int storedEnergy) {
        return creative || storedEnergy > 0;
    }

    private void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        int storedBefore = energyStorage.getEnergyStored();
        if (!tier.creative()) {
            energyStorage.setCapacity(capacity());
        }
        boolean canGenerate = tier.creative() || (level.isDay()
                && level.canSeeSkyFromBelowWater(pos.above())
                && level.getBlockState(pos.above()).isAir());
        int generation = canGenerate && !tier.creative()
                ? SolarGenerationPolicy.generatedPerTick(baseGeneration(), sameTierNeighbors(level, pos),
                level.getMinBuildHeight(), level.getMaxBuildHeight() - 1, pos.getY())
                : tier.creative() ? Integer.MAX_VALUE : 0;
        lastCanGenerate = canGenerate;
        lastGeneration = generation;

        if (state.getValue(SolarPanelBlock.ACTIVE) != canGenerate) {
            level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, canGenerate), 3);
            state = level.getBlockState(pos);
        }
        if (canGenerate && !tier.creative()) {
            energyStorage.addGeneratedEnergy(generation);
        }
        if (shouldPushEnergy(tier.creative(), energyStorage.getEnergyStored())) {
            pushEnergy(level, pos);
        }
        if (!tier.creative() && energyStorage.getEnergyStored() != storedBefore) {
            setChanged();
        }
    }

    private void pushEnergy(ServerLevel level, BlockPos pos) {
        Direction[] directions = Direction.values();
        int offered = tier.creative() ? Integer.MAX_VALUE : energyStorage.getEnergyStored();
        for (int offset = 0; offset < directions.length && offered > 0; offset++) {
            Direction direction = directions[(outputCursor + offset) % directions.length];
            IEnergyStorage receiver = outputCaches.computeIfAbsent(direction, side ->
                    BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, level,
                            pos.relative(side).immutable(), side.getOpposite())).getCapability();
            SolarEnergyTransfer.Result result = SolarEnergyTransfer.push(energyStorage, receiver, offered);
            if (result.moved() > 0) {
                if (!tier.creative()) offered -= result.moved();
            }
        }
        outputCursor = (outputCursor + 1) % directions.length;
    }

    private int sameTierNeighbors(Level level, BlockPos pos) {
        int count = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                if (level.getBlockState(pos.offset(x, 0, z)).is(getBlockState().getBlock())) count++;
            }
        }
        return count;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (!tier.creative()) tag.putInt(TAG_ENERGY, energyStorage.getEnergyStored());
        tag.putInt(TAG_OUTPUT_CURSOR, outputCursor);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (!tier.creative()) {
            energyStorage.addGeneratedEnergy(tag.getInt(TAG_ENERGY));
            energyStorage.setCapacity(capacity());
        }
        outputCursor = Math.floorMod(tag.getInt(TAG_OUTPUT_CURSOR), Direction.values().length);
    }
}
