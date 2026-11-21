package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.common.entities.MachineLightningEffects;
import com.jdte.common.upgrades.SelectiveUpgradeMachine;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;

public class ChargedLightningRodBE extends BaseMachineBE implements PoweredMachineBE, SelectiveUpgradeMachine {
    public static final int ENERGY_PER_STRIKE = 10_000;
    public static final int STRIKE_INTERVAL_TICKS = 200;
    private static final int ENERGY_CAPACITY = 10_000_000;
    private static final String NATURAL_LIGHTNING_TAG = "ae2lt.natural_weather_lightning";
    private static final String MACHINE_POSITION_TAG = "jdte.charged_lightning_rod_pos";
    // AE2LT checks the vanilla rod block itself when resolving a strike ritual.
    private final boolean requiresVanillaRod = ModList.get().isLoaded("ae2lt");

    private final PoweredMachineContainerData poweredData = new PoweredMachineContainerData(this);
    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY);
    private boolean lightningRodSetupAttempted;
    private boolean ownsLightningRod;
    private int strikeCooldown;
    private boolean renderLightning = true;
    private boolean playLightningSound = true;
    private long lastProcessedGameTime = Long.MIN_VALUE;

    public ChargedLightningRodBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.CHARGED_LIGHTNING_ROD.get(), pos, state);
        tickSpeed = 1;
    }

    @Override
    public void tickServer() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        // The common ticker and external time acceleration must not multiply this machine's batch.
        long gameTime = serverLevel.getGameTime();
        if (lastProcessedGameTime == gameTime) return;
        lastProcessedGameTime = gameTime;
        super.tickServer();
        if (!requiresVanillaRod && ownsLightningRod) {
            removeOwnedLightningRod();
            ownsLightningRod = false;
            lightningRodSetupAttempted = false;
            setChanged();
        }
        if (strikeCooldown > 0) {
            strikeCooldown--;
        }
        if (!canOperateInWeather()) return;

        ChargedLightningRodMode mode = getStrikeMode();
        if (mode == ChargedLightningRodMode.NORMAL && strikeCooldown > 0) return;
        if (energy.extractEnergy(mode.energyPerStrike, true) < mode.energyPerStrike) return;
        if (requiresVanillaRod) {
            updateLightningRod(serverLevel);
            if (!serverLevel.getBlockState(getBlockPos().above()).is(Blocks.LIGHTNING_ROD)) return;
        }
        // Put a rodless strike inside the copper contact rather than in the air block above it.
        double strikeY = getBlockPos().getY() + (requiresVanillaRod ? 2.0 : 15.99 / 16.0);
        for (int strike = 0; strike < mode.strikesPerCycle; strike++) {
            if (energy.extractEnergy(mode.energyPerStrike, true) < mode.energyPerStrike) break;
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
            if (lightning == null) break;
            lightning.setPos(getBlockPos().getX() + 0.5, strikeY, getBlockPos().getZ() + 0.5);
            lightning.setVisualOnly(false);
            lightning.getPersistentData().putBoolean(NATURAL_LIGHTNING_TAG, true);
            lightning.getPersistentData().putLong(MACHINE_POSITION_TAG, getBlockPos().asLong());
            applyLightningEffects(lightning);
            if (!serverLevel.addFreshEntity(lightning)) break;
            energy.extractEnergy(mode.energyPerStrike, false);
            strikeCooldown = mode.intervalTicks;
            setChanged();
        }
    }

    public ChargedLightningRodMode getStrikeMode() {
        if (UpgradeHelper.hasUltimateOverclock(this)) return ChargedLightningRodMode.ULTIMATE;
        return UpgradeHelper.hasOverclock(this) ? ChargedLightningRodMode.OVERCLOCK : ChargedLightningRodMode.NORMAL;
    }

    public boolean canOperateInWeather() {
        return UpgradeHelper.countUpgrades(this, UpgradeType.THUNDERSTORM) > 0
                || level != null && level.isRaining() && level.isThundering();
    }

    @Override
    public boolean isUpgradeAllowed(UpgradeType type) {
        return type == UpgradeType.THUNDERSTORM || type == UpgradeType.OVERCLOCK
                || type == UpgradeType.ULTIMATE_OVERCLOCK || type == UpgradeType.CAPACITY
                || type == UpgradeType.ULTIMATE_CAPACITY;
    }

    public boolean isLightningRenderEnabled() { return renderLightning; }
    public boolean isLightningSoundEnabled() { return playLightningSound; }

    public void setLightningEffects(boolean render, boolean sound) {
        if (renderLightning == render && playLightningSound == sound) return;
        renderLightning = render;
        playLightningSound = sound;
        if (level instanceof ServerLevel serverLevel) {
            for (LightningBolt lightning : serverLevel.getEntitiesOfClass(LightningBolt.class,
                    new AABB(getBlockPos()).inflate(4, 16, 4))) {
                CompoundTag data = lightning.getPersistentData();
                if (data.getLong(MACHINE_POSITION_TAG).filter(pos -> pos == getBlockPos().asLong()).isPresent()) {
                    applyLightningEffects(lightning);
                }
            }
        }
        markDirtyClient();
    }

    private void applyLightningEffects(LightningBolt lightning) {
        ((MachineLightningEffects) lightning).jdte$setLightningRenderEnabled(renderLightning);
        lightning.setSilent(!playLightningSound);
    }

    private void updateLightningRod(ServerLevel serverLevel) {
        BlockPos rodPos = getBlockPos().above();
        BlockState rodState = serverLevel.getBlockState(rodPos);
        int cost = getStrikeMode().energyPerStrike;
        boolean hasEnergy = energy.extractEnergy(cost, true) >= cost;

        if (ownsLightningRod && !rodState.is(Blocks.LIGHTNING_ROD)) {
            ownsLightningRod = false;
            lightningRodSetupAttempted = true;
            setChanged();
        }
        if (ownsLightningRod && !hasEnergy) {
            serverLevel.setBlock(rodPos, Blocks.AIR.defaultBlockState(), 3);
            ownsLightningRod = false;
            lightningRodSetupAttempted = false;
            setChanged();
            return;
        }
        if (!lightningRodSetupAttempted && hasEnergy && rodState.isAir()) {
            serverLevel.setBlock(rodPos, Blocks.LIGHTNING_ROD.defaultBlockState()
                    .setValue(LightningRodBlock.FACING, Direction.UP), 3);
            ownsLightningRod = true;
            lightningRodSetupAttempted = true;
            setChanged();
        }
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        removeOwnedLightningRod();
        super.preRemoveSideEffects(pos, state);
    }

    public void removeOwnedLightningRod() {
        if (ownsLightningRod && level instanceof ServerLevel serverLevel) {
            BlockPos rodPos = getBlockPos().above();
            if (serverLevel.getBlockState(rodPos).is(Blocks.LIGHTNING_ROD)) {
                serverLevel.setBlock(rodPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @Override public ContainerData getContainerData() { return poweredData; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energy; }
    @Override public int getStandardEnergyCost() { return getStrikeMode().energyPerStrike; }
    @Override public int getMaxEnergy() { return UpgradeHelper.adjustEnergyCapacity(this, ENERGY_CAPACITY); }

    @Override public boolean isDefaultSettings() {
        return super.isDefaultSettings() && renderLightning && playLightningSound;
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putInt("chargedLightningRodEnergy", energy.getEnergyStored());
        tag.putInt("chargedLightningRodCooldown", strikeCooldown);
        tag.putBoolean("chargedLightningRodSetupAttempted", lightningRodSetupAttempted);
        tag.putBoolean("chargedLightningRodOwnsRod", ownsLightningRod);
        tag.putBoolean("chargedLightningRodRender", renderLightning);
        tag.putBoolean("chargedLightningRodSound", playLightningSound);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        tag.getInt("chargedLightningRodEnergy").ifPresent(energy::setEnergy);
        strikeCooldown = Math.max(0, tag.getIntOr("chargedLightningRodCooldown", 0));
        lightningRodSetupAttempted = tag.getBooleanOr("chargedLightningRodSetupAttempted", false);
        ownsLightningRod = tag.getBooleanOr("chargedLightningRodOwnsRod", false);
        renderLightning = tag.getBooleanOr("chargedLightningRodRender", true);
        playLightningSound = tag.getBooleanOr("chargedLightningRodSound", true);
    }
}
