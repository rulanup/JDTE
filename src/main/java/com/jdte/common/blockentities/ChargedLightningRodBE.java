package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.common.entities.MachineLightningEffects;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ChargedLightningRodBE extends BaseMachineBE implements PoweredMachineBE {
    public static final int ENERGY_PER_STRIKE = 10_000;
    public static final int STRIKE_INTERVAL_TICKS = 200;
    private static final int ENERGY_CAPACITY = 10_000_000;
    public static final String NATURAL_LIGHTNING_TAG = "ae2lt.natural_weather_lightning";
    public static final String MACHINE_POSITION_TAG = "jdte.charged_lightning_rod_pos";

    private static final ThreadLocal<Boolean> SPAWNING=ThreadLocal.withInitial(()->false);
    private boolean unloaded;
    private long capabilityGeneration;
    private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.energy.IEnergyStorage> energyPort;
    private Long savedAnchor;
    private String savedDimension;
    public boolean live(){return !unloaded&&!isRemoved()&&level instanceof ServerLevel server&&server.getServer().isSameThread()&&server.getServer().isRunning()&&server.getChunkSource().getChunkNow(worldPosition.getX()>>4,worldPosition.getZ()>>4)!=null&&level.getBlockEntity(worldPosition)==this;}
    private void resetPort(){long generation=++capabilityGeneration;energyPort=net.minecraftforge.common.util.LazyOptional.of(()->new net.minecraftforge.energy.IEnergyStorage(){
        private boolean valid(){return generation==capabilityGeneration&&live();}
        public int receiveEnergy(int n,boolean simulate){if(!valid()||n<=0)return 0;int accepted=energy.receiveEnergy(n,simulate);if(accepted>0&&!simulate)setChanged();return accepted;}
        public int extractEnergy(int n,boolean simulate){if(!valid()||n<=0)return 0;int extracted=energy.extractEnergy(n,simulate);if(extracted>0&&!simulate)setChanged();return extracted;}
        public int getEnergyStored(){return valid()?energy.getEnergyStored():0;}public int getMaxEnergyStored(){return valid()?energy.getMaxEnergyStored():0;}
        public boolean canReceive(){return valid()&&energy.canReceive();}public boolean canExtract(){return valid()&&energy.canExtract();}
    });}
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap,Direction side){if(cap==net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY)return live()?energyPort.cast():net.minecraftforge.common.util.LazyOptional.empty();return super.getCapability(cap,side);}
    @Override public void invalidateCaps(){capabilityGeneration++;if(energyPort!=null)energyPort.invalidate();super.invalidateCaps();}
    @Override public void reviveCaps(){super.reviveCaps();unloaded=false;resetPort();}
    @Override public void onChunkUnloaded(){unloaded=true;super.onChunkUnloaded();}
    @Override public void onLoad(){super.onLoad();unloaded=false;if(energyPort==null||!energyPort.isPresent())resetPort();
        if(ownsLightningRod&&savedAnchor!=null&&(savedAnchor!=worldPosition.asLong()||!level.dimension().location().toString().equals(savedDimension))&&level.hasChunkAt(worldPosition.above())&&level.getBlockState(worldPosition.above()).isAir()){
            ownsLightningRod=false;lightningRodSetupAttempted=false;setChanged();
        }
    }
    public int getStrikeCooldown(){return strikeCooldown;}
    public boolean ownsRod(){return ownsLightningRod;}
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
        resetPort();
    }

    @Override
    public void tickServer() {
        if (!live() || SPAWNING.get() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        // The common ticker and external time acceleration must not multiply this machine's batch.
        long gameTime = serverLevel.getGameTime();
        if (lastProcessedGameTime == gameTime) return;
        lastProcessedGameTime = gameTime;
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        if (strikeCooldown > 0) {
            strikeCooldown--;
        }
        if (!canOperateInWeather()) return;

        ChargedLightningRodMode mode = getStrikeMode();
        if (!mode.canBegin(strikeCooldown)) return;
        if (energy.extractEnergy(mode.energyPerStrike, true) < mode.energyPerStrike) return;
        updateLightningRod(serverLevel);
        if (!serverLevel.getBlockState(getBlockPos().above()).is(Blocks.LIGHTNING_ROD)) {
            return;
        }
        for (int strike = 0, limit=mode.affordableStrikes(energy.getEnergyStored()); strike < limit; strike++) {
            if (energy.extractEnergy(mode.energyPerStrike, true) < mode.energyPerStrike) break;
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (lightning == null) break;
            BlockPos strikePos = getBlockPos().above(2);
            lightning.setPos(strikePos.getX() + 0.5, strikePos.getY(), strikePos.getZ() + 0.5);
            lightning.setVisualOnly(false);
            lightning.getPersistentData().putBoolean(NATURAL_LIGHTNING_TAG, true);
            lightning.getPersistentData().putLong(MACHINE_POSITION_TAG, getBlockPos().asLong());
            applyLightningEffects(lightning);
            int beforeEnergy=energy.getEnergyStored();boolean committed=false;
            SPAWNING.set(true);
            try {
                if(!serverLevel.addFreshEntity(lightning))break;
                if(!live()||lightning.isRemoved()||lightning.level()!=serverLevel||!canOperateInWeather()||getStrikeMode()!=mode
                    ||energy.getEnergyStored()!=beforeEnergy||!serverLevel.getBlockState(worldPosition.above()).is(Blocks.LIGHTNING_ROD))break;
                int paid=energy.extractEnergy(mode.energyPerStrike,false);
                if(paid!=mode.energyPerStrike){energy.setEnergy(energy.getEnergyStored()+paid);break;}
                committed=true;strikeCooldown=mode.intervalTicks;setChanged();
            } finally {
                SPAWNING.remove();
                if(!committed)lightning.discard();
            }
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
                if (data.contains(MACHINE_POSITION_TAG) && data.getLong(MACHINE_POSITION_TAG) == getBlockPos().asLong()) {
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("chargedLightningRodEnergy", energy.getEnergyStored());
        tag.putInt("chargedLightningRodCooldown", strikeCooldown);
        tag.putBoolean("chargedLightningRodSetupAttempted", lightningRodSetupAttempted);
        boolean rodStillPresent=level==null||!level.hasChunkAt(worldPosition.above())||level.getBlockState(worldPosition.above()).is(Blocks.LIGHTNING_ROD);
        tag.putBoolean("chargedLightningRodOwnsRod", ownsLightningRod&&rodStillPresent);
        tag.putLong("chargedLightningRodAnchor",worldPosition.asLong());
        if(level!=null)tag.putString("chargedLightningRodDimension",level.dimension().location().toString());
        tag.putBoolean("chargedLightningRodRender", renderLightning);
        tag.putBoolean("chargedLightningRodSound", playLightningSound);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("chargedLightningRodEnergy")) {
            energy.setEnergy(tag.getInt("chargedLightningRodEnergy"));
        }
        savedAnchor=tag.contains("chargedLightningRodAnchor")?tag.getLong("chargedLightningRodAnchor"):null;
        savedDimension=tag.getString("chargedLightningRodDimension");
        strikeCooldown = Math.max(0, Math.min(STRIKE_INTERVAL_TICKS,tag.getInt("chargedLightningRodCooldown")));
        lightningRodSetupAttempted = tag.getBoolean("chargedLightningRodSetupAttempted");
        ownsLightningRod = tag.getBoolean("chargedLightningRodOwnsRod");
        renderLightning = !tag.contains("chargedLightningRodRender") || tag.getBoolean("chargedLightningRodRender");
        playLightningSound = !tag.contains("chargedLightningRodSound") || tag.getBoolean("chargedLightningRodSound");
    }
}
