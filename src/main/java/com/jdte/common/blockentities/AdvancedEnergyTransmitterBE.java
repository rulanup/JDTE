package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.client.particles.itemparticle.ItemFlowParticleData;
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
import com.jdte.common.integrations.ae2.AdvancedEnergyTransmitterEnergySource;
import com.jdte.common.integrations.ae2.AdvancedEnergyTransmitterEnergySources;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdvancedEnergyTransmitterBE extends BaseMachineBE
        implements PoweredMachineBE, RedstoneControlledBE, AreaAffectingBE,
                   FilterableBE, ExtendedUpgradeMachine {

    private record EnergyTarget(BlockPos pos, Direction side,
                                BlockCapabilityCache<IEnergyStorage, Direction> cache) {
    }

    private record ReceivingCapability(Direction side, IEnergyStorage receiver) {
    }

    private static final class PlannedTransfer {
        private EnergyTarget target;
        private IEnergyStorage receiver;
        private int demand;

        private void set(EnergyTarget target, IEnergyStorage receiver, int demand) {
            this.target = target;
            this.receiver = receiver;
            this.demand = demand;
        }
    }

    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public final PoweredMachineContainerData poweredMachineData;
    public AreaAffectingData areaAffectingData;
    public FilterData filterData = new FilterData();
    private boolean showParticles;
    private UUID boundPlayerId;
    private String boundPlayerName = "";

    private final MachineEnergyStorage energyStorage;
    private final AdvancedEnergyTransmitterEnergySource energyNetworkSource;
    /**
     * Applied Flux 中已实际提取、但尚未发送或成功回存的 FE。
     * 使用 long 且独立持久化，避免旧存档的小型内部缓存导致回存失败时能量丢失。
     */
    private long networkEnergyReserve;
    private final ItemStackHandler machineHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            IEnergyStorage itemEnergy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            return itemEnergy != null && itemEnergy.canExtract();
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final List<EnergyTarget> targets = new ArrayList<>();
    private final Map<BlockPos, EnergyTarget> pendingTargets = new LinkedHashMap<>();
    private final IdentityHashMap<IEnergyStorage, Boolean> pendingReceiverIdentities = new IdentityHashMap<>();
    private final List<PlannedTransfer> transferPlan = new ArrayList<>();
    private final IdentityHashMap<IEnergyStorage, Boolean> transferReceiverIdentities = new IdentityHashMap<>();
    private final AdvancedEnergyTransmitterPlayerCharger playerCharger =
            new AdvancedEnergyTransmitterPlayerCharger();
    private final ContainerData transmitterData;

    private int targetCursor;
    private final BlockPos.MutableBlockPos scanCursorPos = new BlockPos.MutableBlockPos();
    private long nextTargetRefreshTick;
    private boolean scanActive;
    private long scanIndex;
    private long scanVolume;
    private int scanMinX;
    private int scanMinY;
    private int scanMinZ;
    private int scanSizeX;
    private int scanSizeY;
    private int scanSizeZ;
    private int settingsFingerprint = Integer.MIN_VALUE;

    private int syncedTargetCount;
    private int syncedScanProgress;
    private int syncedEnergyNetworkStatus;
    private boolean syncedPlayerBound;
    private boolean syncedBoundPlayerOnline;
    private int lastAttemptedTargets;
    private int lastTransferred;

    public AdvancedEnergyTransmitterBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_ENERGY_TRANSMITTER.get(), pos, state);
        MACHINE_SLOTS = 1;
        tickSpeed = JDTEConfig.COMMON.advancedEnergyTransmitterBaseTickDelay.get();
        areaAffectingData = new AreaAffectingData();
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        energyNetworkSource = AdvancedEnergyTransmitterEnergySources.create(this);
        poweredMachineData = new PoweredMachineContainerData(this);
        showParticles = JDTEConfig.COMMON.advancedEnergyTransmitterShowParticlesByDefault.get();
        transmitterData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> targets.size();
                    case 1 -> getScanProgress();
                    case 2 -> lastAttemptedTargets;
                    case 3 -> lastTransferred;
                    case 4 -> showParticles ? 1 : 0;
                    case 5 -> energyNetworkSource.getStatus().ordinal();
                    case 6 -> boundPlayerId != null ? 1 : 0;
                    case 7 -> isBoundPlayerOnline() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> syncedTargetCount = value;
                    case 1 -> syncedScanProgress = value;
                    case 2 -> lastAttemptedTargets = value;
                    case 3 -> lastTransferred = value;
                    case 4 -> showParticles = value != 0;
                    case 5 -> syncedEnergyNetworkStatus = value;
                    case 6 -> syncedPlayerBound = value != 0;
                    case 7 -> syncedBoundPlayerOnline = value != 0;
                    default -> { }
                }
            }

            @Override
            public int getCount() {
                return 8;
            }
        };
    }

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this,
                JDTEConfig.COMMON.advancedEnergyTransmitterEnergyCapacity.get());
    }

    @Override
    public ContainerData getContainerData() {
        return poweredMachineData;
    }

    public ContainerData getTransmitterData() {
        return transmitterData;
    }

    @Override
    public MachineEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public int getStandardEnergyCost() {
        return 0;
    }

    @Override
    public RedstoneControlData getRedstoneControlData() {
        return redstoneControlData;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public AreaAffectingData getAreaAffectingData() {
        return areaAffectingData;
    }

    @Override
    public FilterBasicHandler getFilterHandler() {
        return getData(Registration.HANDLER_BASIC_FILTER);
    }

    @Override
    public FilterData getFilterData() {
        return filterData;
    }

    @Override
    public ItemStackHandler getMachineHandler() {
        return machineHandler;
    }

    public AdvancedEnergyTransmitterEnergySource getEnergyNetworkSource() {
        return energyNetworkSource;
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        energyNetworkSource.ensureReady(serverLevel, getBlockPos());
        updateTargetDiscovery(serverLevel);
        if (!isActiveRedstone()) {
            returnNetworkEnergyReserve();
            lastAttemptedTargets = 0;
            lastTransferred = 0;
            return;
        }
        if (!canRun()) {
            returnNetworkEnergyReserve();
            lastAttemptedTargets = 0;
            lastTransferred = 0;
            return;
        }
        providePlayerPower();
        providePower(serverLevel);
    }

    @Override
    public void setAreaSettings(double xRadius, double yRadius, double zRadius,
                                int xOffset, int yOffset, int zOffset, boolean renderArea) {
        AreaAffectingBE.super.setAreaSettings(
                xRadius, yRadius, zRadius, xOffset, yOffset, zOffset, renderArea);
        invalidateTargets(true);
    }

    @Override
    public void handleRotate(Direction oldDirection, Direction newDirection) {
        AreaAffectingBE.super.handleRotate(oldDirection, newDirection);
        invalidateTargets(true);
    }

    public void setShowParticles(boolean show) {
        if (showParticles == show) {
            return;
        }
        showParticles = show;
        markDirtyClient();
    }

    public enum BindingResult {
        BOUND,
        UNBOUND,
        DENIED
    }

    public BindingResult togglePlayerBinding(ServerPlayer actor) {
        if (boundPlayerId == null) {
            bindPlayer(actor);
            return BindingResult.BOUND;
        }
        boolean authorized = boundPlayerId.equals(actor.getUUID())
                || actor.isCreative() && actor.hasPermissions(2);
        if (!authorized) {
            return BindingResult.DENIED;
        }
        boundPlayerId = null;
        boundPlayerName = "";
        markDirtyClient();
        return BindingResult.UNBOUND;
    }

    private void bindPlayer(ServerPlayer player) {
        boundPlayerId = player.getUUID();
        boundPlayerName = player.getGameProfile().getName();
        markDirtyClient();
    }

    public boolean hasBoundPlayer() {
        return level != null && level.isClientSide ? syncedPlayerBound : boundPlayerId != null;
    }

    public boolean isBoundPlayerOnline() {
        if (level != null && level.isClientSide) {
            return syncedBoundPlayerOnline;
        }
        return getBoundPlayer() != null;
    }

    public String getBoundPlayerName() {
        return boundPlayerName;
    }

    public ServerPlayer getBoundPlayer() {
        if (boundPlayerId == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(boundPlayerId);
    }

    public boolean isShowingParticles() {
        return showParticles;
    }

    public int getTargetCount() {
        return level != null && level.isClientSide ? syncedTargetCount : targets.size();
    }

    public int getScanProgress() {
        if (level != null && level.isClientSide) {
            return syncedScanProgress;
        }
        if (!scanActive || scanVolume <= 0) {
            return 100;
        }
        return (int) Math.min(99L, scanIndex * 100L / scanVolume);
    }

    public int getLastAttemptedTargets() {
        return lastAttemptedTargets;
    }

    public int getLastTransferred() {
        return lastTransferred;
    }

    public AdvancedEnergyTransmitterEnergySource.Status getEnergyNetworkStatus() {
        int status = level != null && level.isClientSide
                ? syncedEnergyNetworkStatus : energyNetworkSource.getStatus().ordinal();
        AdvancedEnergyTransmitterEnergySource.Status[] values =
                AdvancedEnergyTransmitterEnergySource.Status.values();
        return values[Math.floorMod(status, values.length)];
    }

    private void drainEnergyItem(int requested) {
        ItemStack stack = machineHandler.getStackInSlot(0);
        if (requested <= 0 || stack.isEmpty()
                || energyStorage.getEnergyStored() >= energyStorage.getMaxEnergyStored()) {
            return;
        }
        IEnergyStorage itemEnergy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (itemEnergy == null || !itemEnergy.canExtract()) {
            return;
        }

        int missing = Math.min(requested,
                energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
        int extractable = itemEnergy.extractEnergy(missing, true);
        if (extractable <= 0) {
            return;
        }
        int accepted = energyStorage.receiveEnergy(extractable, true);
        if (accepted <= 0) {
            return;
        }
        int extracted = itemEnergy.extractEnergy(accepted, false);
        if (extracted > 0) {
            energyStorage.receiveEnergy(extracted, false);
            setChanged();
        }
    }

    private void updateTargetDiscovery(ServerLevel serverLevel) {
        int currentFingerprint = calculateSettingsFingerprint();
        if (currentFingerprint != settingsFingerprint) {
            settingsFingerprint = currentFingerprint;
            beginTargetScan(serverLevel, true);
        } else if (!scanActive && serverLevel.getGameTime() >= nextTargetRefreshTick) {
            beginTargetScan(serverLevel, false);
        }

        if (scanActive) {
            scanTargetBatch(serverLevel);
        }
    }

    private void beginTargetScan(ServerLevel serverLevel, boolean discardCurrentTargets) {
        if (discardCurrentTargets) {
            targets.clear();
            targetCursor = 0;
        }
        pendingTargets.clear();
        pendingReceiverIdentities.clear();

        AABB area = getAABB(getBlockPos());
        scanMinX = (int) Math.floor(area.minX);
        scanMinY = Math.max(serverLevel.getMinBuildHeight(), (int) Math.floor(area.minY));
        scanMinZ = (int) Math.floor(area.minZ);
        int maxX = (int) Math.ceil(area.maxX);
        int maxY = Math.min(serverLevel.getMaxBuildHeight(), (int) Math.ceil(area.maxY));
        int maxZ = (int) Math.ceil(area.maxZ);
        scanSizeX = Math.max(0, maxX - scanMinX);
        scanSizeY = Math.max(0, maxY - scanMinY);
        scanSizeZ = Math.max(0, maxZ - scanMinZ);
        scanVolume = (long) scanSizeX * scanSizeY * scanSizeZ;
        scanIndex = 0;
        scanActive = scanVolume > 0;

        if (!scanActive) {
            finishTargetScan(serverLevel);
        }
    }

    private void scanTargetBatch(ServerLevel serverLevel) {
        long end = AdvancedEnergyTransmitterScheduler.scanBatchEnd(
                scanIndex, scanVolume,
                JDTEConfig.COMMON.advancedEnergyTransmitterScanBlocksPerTick.get());
        for (; scanIndex < end; scanIndex++) {
            int x = (int) (scanIndex % scanSizeX);
            long yz = scanIndex / scanSizeX;
            int z = (int) (yz % scanSizeZ);
            int y = (int) (yz / scanSizeZ);
            scanPosition(serverLevel, scanCursorPos.set(
                    scanMinX + x, scanMinY + y, scanMinZ + z));
        }
        if (scanIndex >= scanVolume) {
            finishTargetScan(serverLevel);
        }
    }

    private void scanPosition(ServerLevel serverLevel, BlockPos pos) {
        if (pos.equals(getBlockPos()) || isEnergyInputSourcePosition(pos) || !serverLevel.isLoaded(pos)) {
            return;
        }
        BlockState state = serverLevel.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
        if (blockEntity == null
                || JDTEConfig.COMMON.advancedEnergyTransmitterExcludeTransmitters.get()
                && blockEntity instanceof AdvancedEnergyTransmitterBE) {
            return;
        }
        ItemStack filterStack = state.getCloneItemStack(
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false),
                serverLevel, pos, null);
        if (!isStackValidFilter(filterStack)) {
            return;
        }

        Direction preferredSide = sideFacingTransmitter(pos);
        ReceivingCapability receiving = findReceivingCapability(serverLevel, pos, preferredSide);
        if (receiving == null
                || pendingReceiverIdentities.put(receiving.receiver(), Boolean.TRUE) != null) {
            return;
        }
        BlockPos immutablePos = pos.immutable();
        BlockCapabilityCache<IEnergyStorage, Direction> cache = BlockCapabilityCache.create(
                Capabilities.EnergyStorage.BLOCK, serverLevel, immutablePos, receiving.side());
        pendingTargets.put(immutablePos, new EnergyTarget(immutablePos, receiving.side(), cache));
    }

    private boolean isEnergyInputSourcePosition(BlockPos pos) {
        Direction inputSide = getBlockState().getValue(BlockStateProperties.FACING);
        return pos.equals(getBlockPos().relative(inputSide));
    }

    private ReceivingCapability findReceivingCapability(ServerLevel serverLevel, BlockPos pos,
                                                        Direction preferredSide) {
        if (preferredSide != null) {
            IEnergyStorage preferred = serverLevel.getCapability(
                    Capabilities.EnergyStorage.BLOCK, pos, preferredSide);
            if (preferred != null && preferred.canReceive()) {
                return new ReceivingCapability(preferredSide, preferred);
            }
        }
        for (Direction side : Direction.values()) {
            if (side == preferredSide) {
                continue;
            }
            IEnergyStorage receiver = serverLevel.getCapability(
                    Capabilities.EnergyStorage.BLOCK, pos, side);
            if (receiver != null && receiver.canReceive()) {
                return new ReceivingCapability(side, receiver);
            }
        }
        return null;
    }

    private Direction sideFacingTransmitter(BlockPos target) {
        int dx = getBlockPos().getX() - target.getX();
        int dy = getBlockPos().getY() - target.getY();
        int dz = getBlockPos().getZ() - target.getZ();
        int ax = Math.abs(dx);
        int ay = Math.abs(dy);
        int az = Math.abs(dz);
        if (ax >= ay && ax >= az && dx != 0) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        if (ay >= az && dy != 0) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        }
        if (dz != 0) {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return null;
    }

    private void finishTargetScan(ServerLevel serverLevel) {
        targets.clear();
        targets.addAll(pendingTargets.values());
        pendingTargets.clear();
        pendingReceiverIdentities.clear();
        scanActive = false;
        scanIndex = scanVolume;
        targetCursor = AdvancedEnergyTransmitterScheduler.normalizeCursor(targetCursor, targets.size());
        nextTargetRefreshTick = serverLevel.getGameTime()
                + JDTEConfig.COMMON.advancedEnergyTransmitterTargetRefreshInterval.get();
    }

    private void providePlayerPower() {
        ServerPlayer player = getBoundPlayer();
        if (player == null) {
            return;
        }
        long planned = playerCharger.plan(player,
                JDTEConfig.COMMON.advancedEnergyTransmitterPlayerChargeMaxItemsPerTick.get());
        if (planned <= 0L) {
            return;
        }

        boolean creative = UpgradeHelper.hasCreativeUpgrade(this);
        long reserveBefore = networkEnergyReserve;
        if (!creative) {
            preparePlayerEnergyBatch(planned);
        }
        long available = creative
                ? planned
                : AdvancedEnergyTransmitterScheduler.saturatingAdd(
                        energyStorage.getEnergyStored(), networkEnergyReserve);
        long transferred = playerCharger.charge(available,
                JDTEConfig.COMMON.advancedEnergyTransmitterPlayerChargeMaxCallsPerItem.get());
        if (!creative && transferred > 0L) {
            consumePreparedEnergy(transferred);
        }
        if (!creative && (transferred > 0L || networkEnergyReserve != reserveBefore)) {
            setChanged();
        }
    }

    private void preparePlayerEnergyBatch(long plannedDemand) {
        long available = AdvancedEnergyTransmitterScheduler.saturatingAdd(
                energyStorage.getEnergyStored(), networkEnergyReserve);
        long missing = Math.max(0L, plannedDemand - available);
        if (missing <= 0L) {
            return;
        }

        drainEnergyItem(AdvancedEnergyTransmitterScheduler.clampToInt(missing));
        available = AdvancedEnergyTransmitterScheduler.saturatingAdd(
                energyStorage.getEnergyStored(), networkEnergyReserve);
        missing = Math.max(0L, plannedDemand - available);
        if (missing <= 0L) {
            return;
        }
        long extracted = AdvancedEnergyTransmitterScheduler.clampExternalResult(
                missing, energyNetworkSource.extract(missing));
        networkEnergyReserve = AdvancedEnergyTransmitterScheduler.saturatingAdd(
                networkEnergyReserve, extracted);
    }

    private void consumePreparedEnergy(long amount) {
        int fromInternal = energyStorage.extractEnergy(
                AdvancedEnergyTransmitterScheduler.clampToInt(
                        Math.min(amount, energyStorage.getEnergyStored())), false);
        networkEnergyReserve = AdvancedEnergyTransmitterScheduler.consumeReserve(
                networkEnergyReserve, amount - fromInternal);
    }

    private void providePower(ServerLevel serverLevel) {
        boolean creative = UpgradeHelper.hasCreativeUpgrade(this);
        int transferBudget = effectiveBudget(
                JDTEConfig.COMMON.advancedEnergyTransmitterTransferBudgetPerTick.get());
        if (transferBudget <= 0 || targets.isEmpty()) {
            returnNetworkEnergyReserve();
            lastAttemptedTargets = 0;
            lastTransferred = 0;
            return;
        }

        int targetCount = targets.size();
        int attemptBudget = AdvancedEnergyTransmitterScheduler.attemptBudget(targetCount,
                JDTEConfig.COMMON.advancedEnergyTransmitterMaxTargetsPerTick.get());
        int startIndex = AdvancedEnergyTransmitterScheduler.normalizeCursor(targetCursor, targetCount);
        int attempted = 0;
        int plannedCount = 0;
        long plannedTotal = 0L;
        ensureTransferPlanCapacity(attemptBudget);
        transferReceiverIdentities.clear();

        while (attempted < attemptBudget && plannedTotal < transferBudget) {
            EnergyTarget target = targets.get(AdvancedEnergyTransmitterScheduler.targetIndex(
                    startIndex, attempted, targetCount));
            attempted++;
            IEnergyStorage receiver = target.cache().getCapability();
            if (receiver == null || !receiver.canReceive()
                    || transferReceiverIdentities.put(receiver, Boolean.TRUE) != null) {
                continue;
            }

            int remainingBudget = AdvancedEnergyTransmitterScheduler.clampToInt(transferBudget - plannedTotal);
            int perTargetLimit = JDTEConfig.COMMON.advancedEnergyTransmitterMaxTransferPerTarget.get();
            int offered = perTargetLimit <= 0
                    ? remainingBudget : Math.min(remainingBudget, perTargetLimit);
            int demand = receiver.receiveEnergy(offered, true);
            if (demand <= 0) {
                continue;
            }
            transferPlan.get(plannedCount++).set(target, receiver, demand);
            plannedTotal = AdvancedEnergyTransmitterScheduler.saturatingAdd(plannedTotal, demand);
        }

        targetCursor = AdvancedEnergyTransmitterScheduler.advanceCursor(
                startIndex, attempted, targetCount);
        lastAttemptedTargets = attempted;
        if (plannedTotal <= 0L) {
            returnNetworkEnergyReserve();
            lastTransferred = 0;
            return;
        }

        long reserveBefore = networkEnergyReserve;
        if (!creative) {
            prepareEnergyBatch(plannedTotal);
        }
        long available = creative
                ? plannedTotal
                : AdvancedEnergyTransmitterScheduler.saturatingAdd(
                        energyStorage.getEnergyStored(), networkEnergyReserve);
        long transferred = 0L;
        int particleBudget = showParticles
                ? JDTEConfig.COMMON.advancedEnergyTransmitterMaxParticleTargetsPerTick.get() : 0;
        for (int planIndex = 0; planIndex < plannedCount; planIndex++) {
            PlannedTransfer planned = transferPlan.get(planIndex);
            if (available <= 0L) {
                break;
            }
            int offered = (int) Math.min(available, planned.demand);
            int accepted = Math.min(offered, Math.max(0,
                    planned.receiver.receiveEnergy(offered, false)));
            if (accepted <= 0) {
                continue;
            }
            if (!creative) {
                consumePreparedEnergy(accepted);
            }
            transferred = AdvancedEnergyTransmitterScheduler.saturatingAdd(transferred, accepted);
            available -= accepted;
            if (particleBudget > 0) {
                sendTransferParticle(serverLevel, planned.target.pos());
                particleBudget--;
            }
        }
        returnNetworkEnergyReserve();
        if (!creative && (transferred > 0L || networkEnergyReserve != reserveBefore)) {
            setChanged();
        }
        lastTransferred = AdvancedEnergyTransmitterScheduler.clampToInt(transferred);
    }

    private void ensureTransferPlanCapacity(int required) {
        while (transferPlan.size() < required) {
            transferPlan.add(new PlannedTransfer());
        }
    }

    private int effectiveBudget(int configuredBudget) {
        int multiplier = UpgradeHelper.hasOverclock(this)
                ? JDTEConfig.COMMON.advancedEnergyTransmitterOverclockTransferMultiplier.get()
                : 1;
        return AdvancedEnergyTransmitterScheduler.clampToInt(
                AdvancedEnergyTransmitterScheduler.saturatingMultiply(configuredBudget, multiplier));
    }

    private void prepareEnergyBatch(long plannedDemand) {
        long available = AdvancedEnergyTransmitterScheduler.saturatingAdd(
                energyStorage.getEnergyStored(), networkEnergyReserve);
        long missing = Math.max(0L, plannedDemand - available);
        if (missing <= 0L) {
            return;
        }

        drainEnergyItem(AdvancedEnergyTransmitterScheduler.clampToInt(missing));
        available = AdvancedEnergyTransmitterScheduler.saturatingAdd(
                energyStorage.getEnergyStored(), networkEnergyReserve);
        missing = Math.max(0L, plannedDemand - available);
        int meLimit = effectiveBudget(
                JDTEConfig.COMMON.advancedEnergyTransmitterMeExtractionLimitPerTick.get());
        long request = Math.min(missing, meLimit);
        if (request <= 0L) {
            return;
        }
        long extracted = AdvancedEnergyTransmitterScheduler.clampExternalResult(
                request, energyNetworkSource.extract(request));
        networkEnergyReserve = AdvancedEnergyTransmitterScheduler.saturatingAdd(
                networkEnergyReserve, extracted);
    }

    private void returnNetworkEnergyReserve() {
        if (networkEnergyReserve <= 0L) {
            return;
        }
        long returned = AdvancedEnergyTransmitterScheduler.clampExternalResult(
                networkEnergyReserve, energyNetworkSource.insert(networkEnergyReserve));
        if (returned <= 0L) {
            return;
        }
        networkEnergyReserve = AdvancedEnergyTransmitterScheduler.consumeReserve(
                networkEnergyReserve, returned);
        setChanged();
    }

    private void sendTransferParticle(ServerLevel serverLevel, BlockPos target) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        Vec3 source = Vec3.atCenterOf(getBlockPos()).subtract(
                facing.getStepX() * 0.3D,
                facing.getStepY() * 0.3D,
                facing.getStepZ() * 0.3D);
        Vec3 destination = Vec3.atCenterOf(target);
        ItemFlowParticleData particle = new ItemFlowParticleData(
                new ItemStack(Items.YELLOW_CONCRETE),
                destination.x, destination.y, destination.z, 2);
        serverLevel.sendParticles(particle, source.x, source.y, source.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private int calculateSettingsFingerprint() {
        int hash = 1;
        hash = 31 * hash + Double.hashCode(areaAffectingData.xRadius);
        hash = 31 * hash + Double.hashCode(areaAffectingData.yRadius);
        hash = 31 * hash + Double.hashCode(areaAffectingData.zRadius);
        hash = 31 * hash + areaAffectingData.xOffset;
        hash = 31 * hash + areaAffectingData.yOffset;
        hash = 31 * hash + areaAffectingData.zOffset;
        hash = 31 * hash + (filterData.allowlist ? 1 : 0);
        hash = 31 * hash + (filterData.compareNBT ? 1 : 0);
        FilterBasicHandler handler = getFilterHandler();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            hash = 31 * hash + ItemStack.hashItemAndComponents(handler.getStackInSlot(slot));
        }
        return hash;
    }

    private void invalidateTargets(boolean discardCurrentTargets) {
        settingsFingerprint = Integer.MIN_VALUE;
        nextTargetRefreshTick = 0;
        scanActive = false;
        scanIndex = 0;
        scanVolume = 0;
        pendingTargets.clear();
        pendingReceiverIdentities.clear();
        if (discardCurrentTargets) {
            targets.clear();
            targetCursor = 0;
        }
    }

    @Override
    public void onChunkUnloaded() {
        returnNetworkEnergyReserve();
        energyNetworkSource.destroy();
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        targets.clear();
        pendingTargets.clear();
        pendingReceiverIdentities.clear();
        returnNetworkEnergyReserve();
        energyNetworkSource.destroy();
        super.setRemoved();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putLong("networkEnergyReserve", networkEnergyReserve);
        tag.put("inventory", machineHandler.serializeNBT(provider));
        tag.putBoolean("showParticles", showParticles);
        if (boundPlayerId != null) {
            tag.putUUID("boundPlayer", boundPlayerId);
            tag.putString("boundPlayerName", boundPlayerName);
        }
        energyNetworkSource.save(tag);
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
        networkEnergyReserve = Math.max(0L, tag.getLong("networkEnergyReserve"));
        if (tag.contains("inventory")) {
            machineHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        }
        if (tag.contains("showParticles")) {
            showParticles = tag.getBoolean("showParticles");
        }
        if (tag.hasUUID("boundPlayer")) {
            boundPlayerId = tag.getUUID("boundPlayer");
            boundPlayerName = tag.getString("boundPlayerName");
        } else {
            boundPlayerId = null;
            boundPlayerName = "";
        }
        energyNetworkSource.load(tag);
        loadAreaSettings(tag);
        invalidateTargets(true);
    }
}