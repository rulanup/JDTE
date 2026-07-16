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
import com.jdte.common.network.data.RangeBlockerSyncPayload;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

public class RangeBlockerBE extends BaseMachineBE implements AreaAffectingBE, FilterableBE,
        RedstoneControlledBE, PoweredMachineBE, ExtendedUpgradeMachine {
    public enum Mode { CONTAINMENT, DEMAGNETIZATION, SILENCE }

    private final AreaAffectingData areaData;
    private final FilterData filterData = new FilterData();
    private final RedstoneControlData redstoneData = new RedstoneControlData();
    private final PoweredMachineContainerData poweredData = new PoweredMachineContainerData(this);
    private final MachineEnergyStorage energy = new MachineEnergyStorage(getMaxEnergy());
    private Mode mode = Mode.CONTAINMENT;
    private EntitySuppressorBE.Target target = EntitySuppressorBE.Target.ALL_LIVING;
    private boolean blacklist;
    private boolean fieldActive;
    private long paidGameTime = Long.MIN_VALUE;
    private int filterFingerprint;
    private AABB clientSyncedArea;

    public RangeBlockerBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.RANGE_BLOCKER.get(), pos, state);
        areaData = new AreaAffectingData(state.getValue(BlockStateProperties.FACING).getOpposite());
    }

    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public AreaAffectingData getAreaAffectingData() { return areaData; }
    @Override public FilterData getFilterData() { return filterData; }
    @Override public FilterBasicHandler getFilterHandler() { return getData(Registration.HANDLER_BASIC_FILTER); }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneData; }
    @Override public ContainerData getContainerData() { return poweredData; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energy; }
    @Override public int getStandardEnergyCost() {
        return switch (mode) {
            case CONTAINMENT -> JDTEConfig.COMMON.rangeBlockerContainmentEnergyPerTick.get();
            case DEMAGNETIZATION -> JDTEConfig.COMMON.rangeBlockerDemagnetizationEnergyPerTick.get();
            case SILENCE -> JDTEConfig.COMMON.rangeBlockerSilenceEnergyPerTick.get();
        };
    }
    @Override public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.rangeBlockerEnergyCapacity.get());
    }

    public Mode getMode() { return mode; }
    public EntitySuppressorBE.Target getTarget() { return target; }
    public boolean isBlacklist() { return blacklist; }
    public boolean isFieldActive() {
        return !isRemoved() && fieldActive && level != null
                && level.getBlockEntity(getBlockPos()) == this;
    }

    public void setSettings(int mode, int target, boolean blacklist) {
        Mode newMode = Mode.values()[Math.floorMod(mode, Mode.values().length)];
        this.mode = newMode;
        this.target = EntitySuppressorBE.Target.values()[
                Math.floorMod(target, EntitySuppressorBE.Target.values().length)];
        this.blacklist = blacklist;
        paidGameTime = Long.MIN_VALUE;
        if (level instanceof ServerLevel) fieldActive = canActivateWithoutConsuming();
        RangeBlockerManager.refresh(this);
        markDirtyClient();
        syncClientState();
    }

    public void applyClientSync(int mode, int target, boolean blacklist, boolean active, AABB area) {
        this.mode = Mode.values()[Math.floorMod(mode, Mode.values().length)];
        this.target = EntitySuppressorBE.Target.values()[
                Math.floorMod(target, EntitySuppressorBE.Target.values().length)];
        this.blacklist = blacklist;
        this.fieldActive = active;
        this.clientSyncedArea = area;
        RangeBlockerManager.refresh(this);
    }

    public void applyClientSettings(int mode, int target, boolean blacklist) {
        this.mode = Mode.values()[Math.floorMod(mode, Mode.values().length)];
        this.target = EntitySuppressorBE.Target.values()[
                Math.floorMod(target, EntitySuppressorBE.Target.values().length)];
        this.blacklist = blacklist;
        RangeBlockerManager.refresh(this);
    }

    AABB getIndexedArea() {
        if (level != null && level.isClientSide && clientSyncedArea != null) return clientSyncedArea;
        return getAABB(getBlockPos());
    }

    boolean canApplyEffectThisTick() {
        if (isRemoved() || level == null || !fieldActive || !isActiveRedstone()) return false;
        if (level.isClientSide || UpgradeHelper.hasCreativeUpgrade(this)) return true;
        long gameTime = level.getGameTime();
        if (paidGameTime == gameTime) return true;
        int cost = getStandardEnergyCost();
        if (energy.extractEnergy(cost, true) != cost) {
            setFieldActive(false);
            return false;
        }
        if (cost > 0) energy.extractEnergy(cost, false);
        paidGameTime = gameTime;
        return true;
    }

    private boolean canActivateWithoutConsuming() {
        if (isRemoved() || level == null || !isActiveRedstone()) return false;
        if (UpgradeHelper.hasCreativeUpgrade(this)) return true;
        int configuredCost = getStandardEnergyCost();
        if (configuredCost <= 0) return true;
        int required = Math.max(1, configuredCost);
        return energy.extractEnergy(required, true) == required;
    }

    private boolean consumeSilenceEnergy() {
        if (isRemoved() || level == null || !isActiveRedstone()) return false;
        if (UpgradeHelper.hasCreativeUpgrade(this)) return true;
        int cost = JDTEConfig.COMMON.rangeBlockerSilenceEnergyPerTick.get();
        if (cost <= 0) return true;
        if (energy.extractEnergy(cost, true) != cost) return false;
        energy.extractEnergy(cost, false);
        return true;
    }

    private void setFieldActive(boolean active) {
        if (fieldActive == active) return;
        fieldActive = active;
        markDirtyClient();
        syncClientState();
    }

    @Override public void tickServer() {
        super.tickServer();
        refreshFilterIndexIfNeeded();
        setFieldActive(mode == Mode.SILENCE ? consumeSilenceEnergy() : canActivateWithoutConsuming());
    }

    @Override public void tickClient() {
        super.tickClient();
        refreshFilterIndexIfNeeded();
    }

    private void refreshFilterIndexIfNeeded() {
        int fingerprint = 1;
        FilterBasicHandler handler = getFilterHandler();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            fingerprint = 31 * fingerprint + ItemStack.hashItemAndComponents(handler.getStackInSlot(slot));
        }
        if (fingerprint != filterFingerprint) {
            filterFingerprint = fingerprint;
            RangeBlockerManager.refresh(this);
        }
    }

    @Override public void onLoad() {
        super.onLoad();
        RangeBlockerManager.register(this);
    }

    @Override public void setRemoved() {
        RangeBlockerManager.unregister(this);
        super.setRemoved();
    }

    private void syncClientState() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        AABB area = getAABB(getBlockPos());
        PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(getBlockPos()),
                new RangeBlockerSyncPayload(getBlockPos(), mode.ordinal(), target.ordinal(), blacklist, fieldActive,
                        area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ));
    }

    @Override
    public void setAreaSettings(double xRadius, double yRadius, double zRadius,
                                int xOffset, int yOffset, int zOffset, boolean renderArea) {
        AreaAffectingBE.super.setAreaSettings(xRadius, yRadius, zRadius, xOffset, yOffset, zOffset, renderArea);
        RangeBlockerManager.refresh(this);
        syncClientState();
    }

    @Override
    public void handleRotate(Direction oldDirection, Direction newDirection) {
        AreaAffectingBE.super.handleRotate(oldDirection, newDirection);
        RangeBlockerManager.refresh(this);
        syncClientState();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("rangeBlockerMode", mode.ordinal());
        tag.putInt("rangeBlockerTarget", target.ordinal());
        tag.putBoolean("rangeBlockerBlacklist", blacklist);
        tag.putBoolean("rangeBlockerActive", fieldActive);
        tag.putInt("rangeBlockerEnergy", energy.getEnergyStored());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        mode = Mode.values()[Math.floorMod(tag.getInt("rangeBlockerMode"), Mode.values().length)];
        target = tag.contains("rangeBlockerTarget")
                ? EntitySuppressorBE.Target.values()[Math.floorMod(
                        tag.getInt("rangeBlockerTarget"), EntitySuppressorBE.Target.values().length)]
                : EntitySuppressorBE.Target.ALL_LIVING;
        blacklist = tag.getBoolean("rangeBlockerBlacklist");
        fieldActive = tag.getBoolean("rangeBlockerActive");
        if (tag.contains("rangeBlockerEnergy")) energy.setEnergy(tag.getInt("rangeBlockerEnergy"));
        if (level != null) RangeBlockerManager.refresh(this);
    }
}
