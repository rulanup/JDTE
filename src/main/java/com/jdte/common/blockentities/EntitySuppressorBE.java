package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.network.data.EntitySuppressorSyncPayload;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

public class EntitySuppressorBE extends BaseMachineBE implements AreaAffectingBE, FilterableBE,
        RedstoneControlledBE, PoweredMachineBE, ExtendedUpgradeMachine {
    public enum Mode { SUPPRESS_TICK, BLOCK_ENTITY, DISABLE_PARTICLES }
    public enum Target { HOSTILE, PASSIVE, ALL_LIVING, SELECTED_TYPES, NON_LIVING, ALL_TYPES }

    private final AreaAffectingData areaData;
    private final FilterData filterData = new FilterData();
    private final RedstoneControlData redstoneData = new RedstoneControlData();
    private final PoweredMachineContainerData poweredData = new PoweredMachineContainerData(this);
    private final MachineEnergyStorage energy = new MachineEnergyStorage(getMaxEnergy());
    private Mode mode = Mode.SUPPRESS_TICK;
    private Target target = Target.HOSTILE;
    private boolean blacklist;
    private long paidGameTime = Long.MIN_VALUE;
    private int filterFingerprint;
    private boolean particleActive;
    private boolean entitySuppressionActive;
    private AABB clientSyncedArea;

    public EntitySuppressorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ENTITY_SUPPRESSOR.get(), pos, state);
        areaData = new AreaAffectingData(state.getValue(BlockStateProperties.FACING).getOpposite());
    }

    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public AreaAffectingData getAreaAffectingData() { return areaData; }
    @Override public FilterData getFilterData() { return filterData; }
    @Override public FilterBasicHandler getFilterHandler() { return getData(Registration.HANDLER_BASIC_FILTER); }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneData; }
    @Override public ContainerData getContainerData() { return poweredData; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energy; }
    @Override public int getStandardEnergyCost() { return JDTEConfig.COMMON.entitySuppressorEnergyPerTick.get(); }
    @Override public int getMaxEnergy() { return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.entitySuppressorEnergyCapacity.get()); }

    public Mode getMode() { return mode; }
    public Target getTarget() { return target; }
    public boolean isBlacklist() { return blacklist; }

    public void setSettings(int mode, int target, boolean blacklist) {
        this.mode = Mode.values()[Math.floorMod(mode, Mode.values().length)];
        this.target = Target.values()[Math.floorMod(target, Target.values().length)];
        this.blacklist = blacklist;
        paidGameTime = Long.MIN_VALUE;
        if (level instanceof ServerLevel) {
            particleActive = this.mode == Mode.DISABLE_PARTICLES && canOperateThisTick();
            entitySuppressionActive = this.mode == Mode.SUPPRESS_TICK && canActivateWithoutConsuming();
        }
        EntitySuppressorManager.refresh(this);
        markDirtyClient();
        syncClientState();
    }

    public void applyClientSync(int mode, int target, boolean blacklist,
                                boolean particleActive, boolean entitySuppressionActive, AABB area) {
        this.mode = Mode.values()[Math.floorMod(mode, Mode.values().length)];
        this.target = Target.values()[Math.floorMod(target, Target.values().length)];
        this.blacklist = blacklist;
        this.particleActive = particleActive;
        this.entitySuppressionActive = entitySuppressionActive;
        this.clientSyncedArea = area;
        EntitySuppressorManager.refresh(this);
    }

    public void applyClientSettings(int mode, int target, boolean blacklist) {
        this.mode = Mode.values()[Math.floorMod(mode, Mode.values().length)];
        this.target = Target.values()[Math.floorMod(target, Target.values().length)];
        this.blacklist = blacklist;
        EntitySuppressorManager.refresh(this);
    }

    AABB getIndexedArea() {
        if (level != null && level.isClientSide && clientSyncedArea != null) return clientSyncedArea;
        return getAABB(getBlockPos());
    }

    boolean canOperateThisTick() {
        if (isRemoved() || level == null || !isActiveRedstone()) return false;
        if (UpgradeHelper.hasCreativeUpgrade(this)) return true;
        long gameTime = level.getGameTime();
        if (paidGameTime == gameTime) return true;
        int cost = getStandardEnergyCost();
        if (energy.extractEnergy(cost, true) != cost) return false;
        energy.extractEnergy(cost, false);
        paidGameTime = gameTime;
        return true;
    }

    private boolean canActivateWithoutConsuming() {
        if (isRemoved() || level == null || !isActiveRedstone()) return false;
        return UpgradeHelper.hasCreativeUpgrade(this)
                || energy.extractEnergy(getStandardEnergyCost(), true) == getStandardEnergyCost();
    }

    boolean canSuppressParticlesClient() {
        return !isRemoved() && particleActive;
    }

    boolean canSuppressEntitiesClient() {
        return !isRemoved() && entitySuppressionActive;
    }

    @Override public void tickServer() {
        super.tickServer();
        refreshFilterIndexIfNeeded();
        boolean particles = mode == Mode.DISABLE_PARTICLES && canOperateThisTick();
        boolean entities = mode == Mode.SUPPRESS_TICK && canActivateWithoutConsuming();
        if (particles != particleActive || entities != entitySuppressionActive) {
            particleActive = particles;
            entitySuppressionActive = entities;
            markDirtyClient();
            syncClientState();
        }
        if (mode == Mode.BLOCK_ENTITY && JDTEConfig.COMMON.entitySuppressorRemoveExisting.get()
                && Math.floorMod(level.getGameTime() + getBlockPos().asLong(), 20L) == 0L) {
            EntitySuppressorManager.removeExistingEntities(this);
        }
    }
    @Override public void tickClient() { super.tickClient(); refreshFilterIndexIfNeeded(); }

    private void refreshFilterIndexIfNeeded() {
        int fingerprint = 1;
        FilterBasicHandler handler = getFilterHandler();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            fingerprint = 31 * fingerprint + ItemStack.hashItemAndComponents(handler.getStackInSlot(slot));
        }
        if (fingerprint != filterFingerprint) {
            filterFingerprint = fingerprint;
            EntitySuppressorManager.refresh(this);
        }
    }

    @Override public void onLoad() { super.onLoad(); EntitySuppressorManager.register(this); }
    @Override public void setRemoved() { EntitySuppressorManager.unregister(this); super.setRemoved(); }

    private void syncClientState() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        AABB area = getAABB(getBlockPos());
        PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(getBlockPos()),
                new EntitySuppressorSyncPayload(getBlockPos(), mode.ordinal(), target.ordinal(), blacklist,
                        particleActive, entitySuppressionActive,
                        area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ));
    }

    @Override
    public void setAreaSettings(double xRadius, double yRadius, double zRadius,
                                int xOffset, int yOffset, int zOffset, boolean renderArea) {
        AreaAffectingBE.super.setAreaSettings(xRadius, yRadius, zRadius, xOffset, yOffset, zOffset, renderArea);
        EntitySuppressorManager.refresh(this);
        syncClientState();
    }

    @Override
    public void handleRotate(Direction oldDirection, Direction newDirection) {
        AreaAffectingBE.super.handleRotate(oldDirection, newDirection);
        EntitySuppressorManager.refresh(this);
        syncClientState();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("entitySuppressorMode", mode.ordinal());
        tag.putInt("entitySuppressorTarget", target.ordinal());
        tag.putBoolean("entitySuppressorBlacklist", blacklist);
        tag.putInt("entitySuppressorEnergy", energy.getEnergyStored());
        tag.putBoolean("entitySuppressorParticleActive", particleActive);
        tag.putBoolean("entitySuppressorEntityActive", entitySuppressionActive);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        mode = Mode.values()[Math.floorMod(tag.getInt("entitySuppressorMode"), Mode.values().length)];
        target = Target.values()[Math.floorMod(tag.getInt("entitySuppressorTarget"), Target.values().length)];
        blacklist = tag.getBoolean("entitySuppressorBlacklist");
        if (tag.contains("entitySuppressorEnergy")) energy.setEnergy(tag.getInt("entitySuppressorEnergy"));
        particleActive = tag.getBoolean("entitySuppressorParticleActive");
        entitySuppressionActive = tag.getBoolean("entitySuppressorEntityActive");
        if (level != null) EntitySuppressorManager.refresh(this);
    }
}
