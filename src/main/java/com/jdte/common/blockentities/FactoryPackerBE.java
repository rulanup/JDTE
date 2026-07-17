package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.jdte.JDTE;
import com.jdte.common.factory.FactoryPackageStorage;
import com.jdte.common.factory.FactoryPackageStorage.BlockRecord;
import com.jdte.common.factory.FactoryPackageStorage.EntityRecord;
import com.jdte.common.factory.FactoryPackageStorage.PackageData;
import com.jdte.common.factory.FactoryPackageStorage.TickRecord;
import com.jdte.common.factory.FactoryBlockEntityMoveSupport;
import com.jdte.common.factory.FactoryEntityMoveSupport;
import com.jdte.common.factory.FactoryScheduledTickSupport;
import com.jdte.common.factory.FactoryTransform;
import com.jdte.common.factory.MekanismFactoryMoveIntegration;
import com.jdte.common.entities.TimeAcceleratorEffectEntity;
import com.jdte.common.items.FactoryPackageItem;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.Comparator;

public class FactoryPackerBE extends BaseMachineBE implements AreaAffectingBE, PoweredMachineBE,
        ExtendedUpgradeMachine {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadLocal<Boolean> PERMISSION_PROBE = ThreadLocal.withInitial(() -> false);
    private static final int MAX_BLACKLIST_REPORT_ENTRIES = 16;
    private static final int MAX_MEKANISM_REMOVAL_DEBUG_ENTRIES = 256;
    private static final boolean DEBUG_LOGGING = Boolean.getBoolean("jdte.factoryPackerDebug");
    public enum Phase {
        IDLE,
        CAPTURING,
        WRITING,
        WAITING_ENERGY,
        CUTTING,
        FINALIZING_CUT,
        CLAIMING,
        PRECHECK,
        PLACING,
        FINALIZING_PLACE,
        ROLLBACK_CUT,
        ROLLBACK_PLACE,
        CUTTING_ENTITIES,
        PLACING_ENTITIES,
        ROLLBACK_CUT_ENTITIES,
        ROLLBACK_PLACE_ENTITIES,
        PREPARING_MOVE,
        WRITING_PREPARED,
        ROLLBACK_PREPARE,
        PREPARING_ENTITIES,
        COMMITTING_PLACE,
        CHECKING_PERMISSIONS,
        ROLLBACK_PERMISSION,
        QUIESCING_SOURCE,
        RECAPTURING_SOURCE,
        VERIFYING_SOURCE,
        VERIFYING_PREPARED_SOURCE,
        VERIFYING_CUT_SOURCE
    }

    public static final TagKey<Block> BLACKLIST = TagKey.create(Registries.BLOCK,
            JDTE.id("factory_packer_blacklist"));
    private final AreaAffectingData areaData;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(getMaxEnergy());
    private final PoweredMachineContainerData poweredData = new PoweredMachineContainerData(this);
    private final ItemStackHandler packageHandler = new ItemStackHandler(1) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return !isBusy() && FactoryPackageItem.isFactoryPackage(stack);
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isBusy() ? stack : super.insertItem(slot, stack, simulate);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return isBusy() ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
        }
        @Override protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) markDirtyClient();
        }
    };
    private final ContainerData operationData = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> phase.ordinal();
                case 1 -> cursor;
                case 2 -> totalWork;
                case 3 -> errorCode;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> phase = Phase.values()[Math.floorMod(value, Phase.values().length)];
                case 1 -> cursor = value;
                case 2 -> totalWork = value;
                case 3 -> errorCode = value;
                default -> { }
            }
        }
        @Override public int getCount() { return 4; }
    };

    private Phase phase = Phase.IDLE;
    private int cursor;
    private int totalWork;
    private int rollbackLimit;
    private int entityRollbackLimit;
    private int errorCode;
    private int sourceRetryCount;
    private int quiesceWaitTicks;
    private int energyPaid;
    private boolean sourceQuiesced;
    private boolean quiesceValidated;
    private boolean retryAfterRollback;
    private UUID packageId;
    private UUID claimToken;
    private UUID operationOwner;
    private BlockPos operationOrigin = BlockPos.ZERO;
    private Vec3i operationSize = Vec3i.ZERO;
    private int operationRotation;
    private boolean scheduledTicksRemoved;
    private transient List<BlockRecord> records;
    private transient List<EntityRecord> entityRecords;
    private transient List<TickRecord> tickRecords;
    private transient ResourceLocation sourceDimension;
    private transient BlockPos sourceOrigin = BlockPos.ZERO;
    private transient int packageFormatVersion;
    private transient boolean ioPending;
    private transient List<BlacklistedBlock> blacklistedBlocks = new ArrayList<>();
    private transient int blacklistedBlockCount;
    private transient Long2ObjectOpenHashMap<BlockRecord> sourceRecordIndex;
    private transient Phase lastNotifiedPhase;
    private transient int mekanismRemovalDebugEntries;

    public FactoryPackerBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.FACTORY_PACKER.get(), pos, state);
        MACHINE_SLOTS = 1;
        tickSpeed = 1;
        areaData = new AreaAffectingData(state.getValue(BlockStateProperties.FACING));
    }

    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public AreaAffectingData getAreaAffectingData() { return areaData; }
    @Override public ItemStackHandler getMachineHandler() { return packageHandler; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energy; }
    @Override public ContainerData getContainerData() { return poweredData; }
    @Override public int getStandardEnergyCost() { return 0; }
    @Override public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.factoryPackerEnergyCapacity.get());
    }

    public ContainerData getOperationData() { return operationData; }
    public Phase getPhase() { return phase; }
    public boolean isBusy() { return phase != Phase.IDLE; }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().getBlockEntity(event.getPos()) instanceof FactoryPackerBE packer
                && packer.isBusy()) {
            event.setCanceled(true);
            event.getPlayer().displayClientMessage(message("cannot_break_busy"), true);
        }
    }

    public static boolean isPermissionProbe() { return PERMISSION_PROBE.get(); }

    public Component startOperation(ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel)) return message("invalid_level");
        if (isBusy()) return message("busy");
        ItemStack packageStack = packageHandler.getStackInSlot(0);
        if (!FactoryPackageItem.isFactoryPackage(packageStack)) return message("missing_package");

        var storedId = FactoryPackageItem.getPackageId(packageStack);
        var placementTarget = FactoryPackageItem.getPlacementTarget(packageStack);
        if (storedId.isPresent() && placementTarget.isPresent()
                && !placementTarget.get().dimension().equals(serverLevel.dimension().location())) {
            return message("target_dimension");
        }

        Bounds bounds = currentBounds(packageStack);
        Component validation = validateBounds(serverLevel, bounds);
        if (validation != null) return validation;

        errorCode = 0;
        operationOwner = player.getUUID();
        operationOrigin = bounds.min();
        operationSize = storedId.isPresent()
                ? FactoryPackageItem.getSize(packageStack).orElse(bounds.size()) : bounds.size();
        operationRotation = storedId.isPresent() ? FactoryPackageItem.getRotation(packageStack) : 0;
        cursor = 0;
        rollbackLimit = 0;
        entityRollbackLimit = 0;
        scheduledTicksRemoved = false;
        sourceRetryCount = 0;
        quiesceWaitTicks = 0;
        energyPaid = 0;
        sourceQuiesced = false;
        quiesceValidated = false;
        retryAfterRollback = false;
        sourceRecordIndex = null;
        records = null;
        entityRecords = null;
        tickRecords = null;
        sourceDimension = null;
        sourceOrigin = BlockPos.ZERO;
        packageFormatVersion = 0;
        blacklistedBlocks = new ArrayList<>();
        blacklistedBlockCount = 0;
        mekanismRemovalDebugEntries = 0;
        lastNotifiedPhase = null;

        if (storedId.isPresent()) {
            packageId = storedId.get();
            claimToken = UUID.randomUUID();
            phase = Phase.CLAIMING;
            totalWork = 1;
            setChanged();
            claimAndRead(serverLevel);
            return message("unpack_started");
        }

        packageId = UUID.randomUUID();
        claimToken = null;
        records = new ArrayList<>();
        totalWork = bounds.volume();
        phase = Phase.CAPTURING;
        setChanged();
        return message("pack_started");
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if (!(level instanceof ServerLevel serverLevel)) return;
        notifyPhaseChange(serverLevel);
        if (ioPending) return;
        int budget = operationBudget();
        switch (phase) {
            case IDLE, WRITING, CLAIMING -> { }
            case CAPTURING -> capture(serverLevel, budget);
            case RECAPTURING_SOURCE -> capture(serverLevel, budget);
            case QUIESCING_SOURCE -> quiesceSource(serverLevel, budget);
            case VERIFYING_SOURCE -> verifySource(serverLevel, budget, false);
            case VERIFYING_PREPARED_SOURCE -> verifySource(serverLevel, budget, true);
            case VERIFYING_CUT_SOURCE -> verifyCutSource(serverLevel, budget);
            case WAITING_ENERGY -> waitForEnergy();
            case CUTTING -> cut(serverLevel, budget);
            case CUTTING_ENTITIES -> cutEntities(serverLevel, budget);
            case CHECKING_PERMISSIONS -> checkPermissions(serverLevel, budget);
            case FINALIZING_CUT -> finalizeCut(serverLevel, budget);
            case PRECHECK -> precheck(serverLevel, budget);
            case PLACING -> place(serverLevel, budget);
            case PREPARING_ENTITIES -> prepareEntityPlacement(serverLevel, budget);
            case PLACING_ENTITIES -> placeEntities(serverLevel, budget);
            case FINALIZING_PLACE -> finalizePlacement(serverLevel, budget);
            case COMMITTING_PLACE -> completePlacement(serverLevel);
            case ROLLBACK_CUT -> rollbackCut(serverLevel, budget);
            case ROLLBACK_CUT_ENTITIES -> rollbackCutEntities(serverLevel, budget);
            case ROLLBACK_PLACE -> rollbackPlacement(serverLevel, budget);
            case ROLLBACK_PLACE_ENTITIES -> rollbackPlacementEntities(serverLevel, budget);
            case PREPARING_MOVE -> prepareBlockEntityMoves(serverLevel, budget);
            case WRITING_PREPARED -> { }
            case ROLLBACK_PREPARE -> rollbackPreparedMoves(serverLevel, budget);
            case ROLLBACK_PERMISSION -> rollbackPermissionPreparation(serverLevel, budget);
        }
        notifyPhaseChange(serverLevel);
    }

    private void capture(ServerLevel level, int budget) {
        int volume = volume(operationSize);
        for (int processed = 0; processed < budget && cursor < volume; processed++, cursor++) {
            BlockPos pos = positionAt(operationOrigin, operationSize, cursor);
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            if (state.is(BLACKLIST) || pos.equals(getBlockPos())) {
                rememberBlacklistedBlock(state, pos);
                continue;
            }
            CompoundTag blockEntityData = null;
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                try {
                    blockEntityData = blockEntity.saveWithId(level.registryAccess());
                } catch (Throwable throwable) {
                    LOGGER.error("Failed to capture factory block entity at {}", pos, throwable);
                    fail(4);
                    return;
                }
            }
            records.add(new BlockRecord(pos.subtract(operationOrigin), state, blockEntityData));
        }
        setChanged();
        if (cursor >= volume) {
            if (blacklistedBlockCount > 0) {
                reportBlacklistedBlocks(level);
                fail(2);
            } else {
                captureSupplemental(level);
            }
        }
    }

    private void captureSupplemental(ServerLevel level) {
        entityRecords = new ArrayList<>();
        if (JDTEConfig.COMMON.factoryPackerMoveEntities.get()) {
            BlockPos max = operationOrigin.offset(operationSize).offset(-1, -1, -1);
            AABB area = AABB.encapsulatingFullBlocks(operationOrigin, max);
            List<Entity> roots = level.getEntities((Entity) null, area, entity ->
                    !(entity instanceof Player) && !(entity instanceof TimeAcceleratorEffectEntity)
                            && entity.getVehicle() == null && entity.shouldBeSaved()
                            && entity.getSelfAndPassengers().noneMatch(Player.class::isInstance));
            if (roots.size() > JDTEConfig.COMMON.factoryPackerMaxEntities.get()) {
                fail(15);
                return;
            }
            for (Entity root : roots) {
                CompoundTag data = new CompoundTag();
                if (!root.save(data)) {
                    fail(16);
                    return;
                }
                entityRecords.add(new EntityRecord(root.getUUID(), data));
            }
        }
        BlockPos max = operationOrigin.offset(operationSize).offset(-1, -1, -1);
        tickRecords = JDTEConfig.COMMON.factoryPackerMoveScheduledTicks.get()
                ? FactoryScheduledTickSupport.capture(level, operationOrigin, max) : List.of();
        sourceDimension = level.dimension().location();
        sourceOrigin = operationOrigin;
        if (!sourceQuiesced) {
            phase = Phase.QUIESCING_SOURCE;
            cursor = 0;
            totalWork = records.size() * 2;
            quiesceValidated = false;
            quiesceWaitTicks = 0;
            setChanged();
        } else {
            beginWrite(level);
        }
    }

    private void quiesceSource(ServerLevel level, int budget) {
        BlockPos selectionMax = operationOrigin.offset(operationSize).offset(-1, -1, -1);
        if (!quiesceValidated) {
            for (int processed = 0; processed < budget && cursor < records.size(); processed++, cursor++) {
                BlockRecord record = records.get(cursor);
                if (record.blockEntityData() == null) continue;
                BlockEntity blockEntity = level.getBlockEntity(operationOrigin.offset(record.relativePos()));
                var check = FactoryBlockEntityMoveSupport.validateMekanismMove(blockEntity, operationOrigin,
                        selectionMax);
                if (check.reactor() && !check.complete()) {
                    reportIncompleteReactor(level, check.min(), check.max());
                    fail(19);
                    return;
                }
            }
            if (cursor < records.size()) {
                setChanged();
                return;
            }
            quiesceValidated = true;
            cursor = 0;
        }

        if (quiesceWaitTicks > 0) {
            for (int processed = 0; processed < budget && cursor < records.size(); processed++, cursor++) {
                BlockRecord record = records.get(cursor);
                if (record.blockEntityData() == null) continue;
                BlockEntity blockEntity = level.getBlockEntity(operationOrigin.offset(record.relativePos()));
                if (!FactoryBlockEntityMoveSupport.isQuiescedForMove(blockEntity)) {
                    cursor = 0;
                    if (++quiesceWaitTicks > 100) fail(20);
                    else setChanged();
                    return;
                }
            }
            if (cursor < records.size()) {
                setChanged();
                return;
            }
            sourceQuiesced = true;
            beginAuthoritativeRecapture();
            return;
        }

        for (int processed = 0; processed < budget && cursor < records.size(); processed++, cursor++) {
            BlockRecord record = records.get(cursor);
            if (record.blockEntityData() != null) {
                BlockEntity blockEntity = level.getBlockEntity(operationOrigin.offset(record.relativePos()));
                FactoryBlockEntityMoveSupport.quiesceForMove(blockEntity);
            }
        }
        if (cursor < records.size()) {
            setChanged();
            return;
        }
        quiesceWaitTicks = 1;
        cursor = 0;
        setChanged();
    }

    private void beginAuthoritativeRecapture() {
        records = new ArrayList<>();
        entityRecords = null;
        tickRecords = null;
        sourceDimension = null;
        sourceOrigin = BlockPos.ZERO;
        sourceRecordIndex = null;
        blacklistedBlocks = new ArrayList<>();
        blacklistedBlockCount = 0;
        cursor = 0;
        totalWork = volume(operationSize);
        phase = Phase.RECAPTURING_SOURCE;
        setChanged();
    }

    private void verifySource(ServerLevel level, int budget, boolean prepared) {
        if (!ensureRecords(level, false)) return;
        if (sourceRecordIndex == null) {
            sourceRecordIndex = new Long2ObjectOpenHashMap<>(Math.max(16, records.size()));
            for (BlockRecord record : records) sourceRecordIndex.put(record.relativePos().asLong(), record);
        }
        int sourceVolume = volume(operationSize);
        for (int processed = 0; processed < budget && cursor < sourceVolume; processed++, cursor++) {
            BlockPos pos = positionAt(operationOrigin, operationSize, cursor);
            BlockRecord expected = sourceRecordIndex.get(pos.subtract(operationOrigin).asLong());
            BlockState current = level.getBlockState(pos);
            if (expected == null ? !current.isAir() : current.getBlock() != expected.state().getBlock()) {
                BlockState expectedState = expected == null ? Blocks.AIR.defaultBlockState() : expected.state();
                reportSourceChange(level, prepared ? "verify_prepared" : "verify_snapshot", pos,
                        expectedState, current, sourceChangeReason(expectedState, current));
                handleSourceChange(level, prepared);
                return;
            }
        }
        setChanged();
        if (cursor < sourceVolume) return;
        sourceRecordIndex = null;
        cursor = 0;
        if (prepared) {
            phase = Phase.CUTTING_ENTITIES;
            totalWork = entityRecords.size();
        } else {
            phase = Phase.PREPARING_MOVE;
            totalWork = records.size();
            rollbackLimit = -1;
        }
    }

    private void handleSourceChange(ServerLevel level, boolean prepared) {
        sourceRecordIndex = null;
        if (sourceRetryCount < JDTEConfig.COMMON.factoryPackerSourceChangeRetries.get()) {
            retryAfterRollback = true;
            errorCode = 0;
            if (prepared) {
                cursor = records.size() - 1;
                rollbackLimit = cursor;
                phase = Phase.ROLLBACK_PREPARE;
            } else {
                restartSourceCapture(level);
            }
        } else if (prepared) {
            retryAfterRollback = false;
            errorCode = 7;
            cursor = records.size() - 1;
            rollbackLimit = cursor;
            phase = Phase.ROLLBACK_PREPARE;
        } else {
            errorCode = 7;
            deleteRegularAsync(level);
            completeKeepingError();
        }
        setChanged();
    }

    private void verifyCutSource(ServerLevel level, int budget) {
        if (sourceRecordIndex == null) {
            sourceRecordIndex = new Long2ObjectOpenHashMap<>(Math.max(16, records.size()));
            for (BlockRecord record : records) sourceRecordIndex.put(record.relativePos().asLong(), record);
        }
        int sourceVolume = volume(operationSize);
        for (int processed = 0; processed < budget && cursor < sourceVolume; processed++, cursor++) {
            BlockPos pos = positionAt(operationOrigin, operationSize, cursor);
            BlockState remaining = level.getBlockState(pos);
            if (!remaining.isAir()) {
                BlockRecord original = sourceRecordIndex.get(pos.subtract(operationOrigin).asLong());
                if (original != null) {
                    debugLog("dependent-cut-residue", "pos=" + pos + " captured="
                            + blockStateDescription(original.state()) + " residue="
                            + blockStateDescription(remaining));
                    setAirForMove(level, pos);
                    continue;
                }
                reportSourceChange(level, "verify_cut", pos, Blocks.AIR.defaultBlockState(), remaining,
                        "cut_residue");
                retryAfterRollback = sourceRetryCount < JDTEConfig.COMMON.factoryPackerSourceChangeRetries.get();
                errorCode = retryAfterRollback ? 0 : 7;
                rollbackLimit = records.size();
                cursor = records.size() - 1;
                phase = Phase.ROLLBACK_CUT;
                setChanged();
                return;
            }
        }
        setChanged();
        if (cursor >= sourceVolume) {
            sourceRecordIndex = null;
            phase = Phase.FINALIZING_CUT;
            cursor = 0;
            totalWork = records.size();
        }
    }

    private void restartSourceCapture(ServerLevel level) {
        UUID stalePackage = packageId;
        if (stalePackage != null) deleteRegularAsync(level);
        packageId = UUID.randomUUID();
        sourceRetryCount++;
        debugLog("source-recapture", "stalePackage=" + stalePackage + " newPackage=" + packageId
                + " retry=" + sourceRetryCount + "/"
                + JDTEConfig.COMMON.factoryPackerSourceChangeRetries.get());
        retryAfterRollback = false;
        scheduledTicksRemoved = false;
        beginAuthoritativeRecapture();
    }

    private void reportIncompleteReactor(ServerLevel level, BlockPos min, BlockPos max) {
        if (operationOwner == null) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(operationOwner);
        if (player != null) {
            player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.incomplete_reactor",
                    min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ())
                    .withStyle(ChatFormatting.RED));
        }
    }

    private void beginWrite(ServerLevel level) {
        if (records == null || records.isEmpty()) {
            fail(5);
            return;
        }
        records.sort(Comparator.comparing(record -> record.blockEntityData() != null));
        PackageData data = new PackageData(2, packageId, sourceDimension, sourceOrigin, operationSize,
                List.copyOf(records), List.copyOf(entityRecords), List.copyOf(tickRecords));
        phase = Phase.WRITING;
        cursor = 0;
        totalWork = records.size();
        ioPending = true;
        setChanged();
        MinecraftServer server = level.getServer();
        CompletableFuture.runAsync(() -> {
            try {
                FactoryPackageStorage.write(server, data, JDTEConfig.COMMON.factoryPackerMaxCompressedBytes.get());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }, Util.ioPool()).whenCompleteAsync((ignored, error) -> {
            ioPending = false;
            if (!isCurrent()) return;
            if (error != null) {
                LOGGER.error("Failed to write factory package {}", packageId, error);
                fail(6);
            } else {
                phase = Phase.WAITING_ENERGY;
                cursor = 0;
                setChanged();
            }
        }, server);
    }

    private void waitForEnergy() {
        int required = requiredEnergy();
        if (required <= 0 || UpgradeHelper.hasCreativeUpgrade(this)) {
            energyPaid = Math.max(energyPaid, required);
            phase = Phase.VERIFYING_SOURCE;
            cursor = 0;
            sourceRecordIndex = null;
            setChanged();
            return;
        }
        int additional = Math.max(0, required - energyPaid);
        if (energy.extractEnergy(additional, true) == additional) {
            energy.extractEnergy(additional, false);
            energyPaid += additional;
            phase = Phase.VERIFYING_SOURCE;
            cursor = 0;
            sourceRecordIndex = null;
            setChanged();
        } else if (level instanceof ServerLevel serverLevel) {
            errorCode = 14;
            deleteRegularAsync(serverLevel);
            completeKeepingError();
        }
    }

    private void prepareBlockEntityMoves(ServerLevel level, int budget) {
        if (!ensurePackageData(level, false)) return;
        for (int processed = 0; processed < budget && cursor < records.size(); processed++, cursor++) {
            BlockRecord record = records.get(cursor);
            if (record.blockEntityData() == null) continue;
            BlockPos pos = operationOrigin.offset(record.relativePos());
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                reportSourceChange(level, "prepare_move", pos, record.state(), level.getBlockState(pos),
                        "missing_block_entity");
                retryAfterRollback = sourceRetryCount < JDTEConfig.COMMON.factoryPackerSourceChangeRetries.get();
                errorCode = retryAfterRollback ? 0 : 7;
                cursor = rollbackLimit;
                phase = Phase.ROLLBACK_PREPARE;
                setChanged();
                return;
            }
            try {
                CompoundTag moveData = FactoryBlockEntityMoveSupport.beginMove(blockEntity, level.registryAccess());
                if (moveData == null) throw new IllegalStateException("Block entity move strategy rejected the move");
                records.set(cursor, new BlockRecord(record.relativePos(), record.state(), moveData));
                rollbackLimit = cursor;
                FactoryBlockEntityMoveSupport.prepareNetworkDetach(blockEntity);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to prepare factory block entity move at {}", pos, throwable);
                cursor = rollbackLimit;
                phase = Phase.ROLLBACK_PREPARE;
                errorCode = 4;
                setChanged();
                return;
            }
        }
        setChanged();
        if (cursor >= records.size()) rewritePreparedPackage(level);
    }

    private void rewritePreparedPackage(ServerLevel level) {
        PackageData data = currentPackageData();
        phase = Phase.WRITING_PREPARED;
        cursor = 0;
        ioPending = true;
        setChanged();
        MinecraftServer server = level.getServer();
        CompletableFuture.runAsync(() -> {
            try {
                FactoryPackageStorage.write(server, data, JDTEConfig.COMMON.factoryPackerMaxCompressedBytes.get());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }, Util.ioPool()).whenCompleteAsync((ignored, error) -> {
            ioPending = false;
            if (!isCurrent()) return;
            if (error != null) {
                LOGGER.error("Failed to persist prepared factory package {}", packageId, error);
                cursor = records.size() - 1;
                rollbackLimit = cursor;
                phase = Phase.ROLLBACK_PREPARE;
                errorCode = 6;
            } else {
                phase = Phase.VERIFYING_PREPARED_SOURCE;
                cursor = 0;
                totalWork = volume(operationSize);
                sourceRecordIndex = null;
            }
            setChanged();
        }, server);
    }

    private void rollbackPreparedMoves(ServerLevel level, int budget) {
        for (int processed = 0; processed < budget && cursor >= 0; processed++, cursor--) {
            restorePreparedBlockEntity(level, records.get(cursor));
        }
        setChanged();
        if (cursor < 0) {
            if (retryAfterRollback) restartSourceCapture(level);
            else {
                deleteRegularAsync(level);
                completeKeepingError();
            }
        }
    }

    private void rollbackPermissionPreparation(ServerLevel level, int budget) {
        for (int processed = 0; processed < budget && cursor >= 0; processed++, cursor--) {
            restorePreparedBlockEntity(level, records.get(cursor));
        }
        setChanged();
        if (cursor < 0) {
            cursor = 0;
            phase = Phase.ROLLBACK_CUT_ENTITIES;
            totalWork = entityRollbackLimit;
        }
    }

    private void restorePreparedBlockEntity(ServerLevel level, BlockRecord record) {
        if (record.blockEntityData() == null) return;
        BlockPos pos = operationOrigin.offset(record.relativePos());
        removeBlockEntityForMove(level, pos);
        CompoundTag data = record.blockEntityData().copy();
        data.putInt("x", pos.getX());
        data.putInt("y", pos.getY());
        data.putInt("z", pos.getZ());
        FactoryBlockEntityMoveSupport.completeMove(record.state(), data, level, pos);
    }

    private void cut(ServerLevel level, int budget) {
        if (!ensureRecords(level, false)) return;
        for (int processed = 0; processed < budget && cursor < records.size(); processed++, cursor++) {
            BlockRecord record = records.get(cursor);
            BlockPos pos = operationOrigin.offset(record.relativePos());
            BlockState current = level.getBlockState(pos);
            if (current.getBlock() != record.state().getBlock()) {
                if (FactoryBlockEntityMoveSupport.isExpectedMultiblockTeardown(record.state(), current)) {
                    if (!current.isAir()) {
                        setAirForMove(level, pos);
                    }
                    continue;
                }
                reportSourceChange(level, "cut", pos, record.state(), current,
                        sourceChangeReason(record.state(), current));
                retryAfterRollback = sourceRetryCount < JDTEConfig.COMMON.factoryPackerSourceChangeRetries.get();
                rollbackLimit = cursor;
                cursor = rollbackLimit - 1;
                phase = Phase.ROLLBACK_CUT;
                errorCode = retryAfterRollback ? 0 : 7;
                setChanged();
                return;
            }
            setAirForMove(level, pos);
        }
        setChanged();
        if (cursor >= records.size()) {
            phase = Phase.VERIFYING_CUT_SOURCE;
            cursor = 0;
            totalWork = volume(operationSize);
        }
    }

    private void cutEntities(ServerLevel level, int budget) {
        if (!ensurePackageData(level, false)) return;
        for (int processed = 0; processed < budget && cursor < entityRecords.size(); processed++, cursor++) {
            Entity entity = level.getEntity(entityRecords.get(cursor).uuid());
            if (entity != null) {
                FactoryEntityMoveSupport.prepareRemoval(level, entity);
                discardTree(entity);
            }
            entityRollbackLimit = cursor + 1;
        }
        setChanged();
        if (cursor >= entityRecords.size()) {
            phase = Phase.CHECKING_PERMISSIONS;
            cursor = 0;
            totalWork = records.size();
        }
    }

    private void checkPermissions(ServerLevel level, int budget) {
        if (!ensurePackageData(level, false)) return;
        ServerPlayer owner = operationOwner == null ? null : level.getServer().getPlayerList().getPlayer(operationOwner);
        if (owner == null) {
            beginPermissionRollback(1);
            return;
        }
        for (int processed = 0; processed < budget && cursor < records.size(); processed++, cursor++) {
            BlockRecord record = records.get(cursor);
            BlockPos pos = operationOrigin.offset(record.relativePos());
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() != record.state().getBlock()) {
                reportSourceChange(level, "permission_check", pos, record.state(), state,
                        sourceChangeReason(record.state(), state));
                retryAfterRollback = sourceRetryCount < JDTEConfig.COMMON.factoryPackerSourceChangeRetries.get();
                beginPermissionRollback(retryAfterRollback ? 0 : 7);
                return;
            }
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, pos, state, owner);
            PERMISSION_PROBE.set(true);
            boolean wasShiftKeyDown = owner.isShiftKeyDown();
            owner.setShiftKeyDown(true);
            try {
                if (NeoForge.EVENT_BUS.post(breakEvent).isCanceled()) {
                    beginPermissionRollback(3);
                    return;
                }
            } finally {
                owner.setShiftKeyDown(wasShiftKeyDown);
                PERMISSION_PROBE.remove();
            }
        }
        setChanged();
        if (cursor >= records.size()) {
            if (!tickRecords.isEmpty()) {
                BlockPos max = operationOrigin.offset(operationSize).offset(-1, -1, -1);
                FactoryScheduledTickSupport.clear(level, operationOrigin, max);
                scheduledTicksRemoved = true;
            }
            phase = Phase.CUTTING;
            cursor = 0;
            totalWork = records.size();
        }
    }

    private void beginPermissionRollback(int code) {
        errorCode = code;
        cursor = records.size() - 1;
        phase = Phase.ROLLBACK_PERMISSION;
        totalWork = records.size();
        setChanged();
    }

    private void finalizeCut(ServerLevel level, int budget) {
        for (int processed = 0; processed < budget && cursor < records.size(); processed++, cursor++) {
            BlockRecord record = records.get(cursor);
            level.updateNeighborsAt(operationOrigin.offset(record.relativePos()), record.state().getBlock());
        }
        setChanged();
        if (cursor >= records.size()) {
            packageHandler.setStackInSlot(0,
                    FactoryPackageItem.createFilled(packageId, operationSize, records.size(), entityRecords.size()));
            complete();
        }
    }

    private void claimAndRead(ServerLevel level) {
        ioPending = true;
        MinecraftServer server = level.getServer();
        UUID id = packageId;
        UUID token = claimToken;
        CompletableFuture.supplyAsync(() -> {
            try {
                FactoryPackageStorage.claim(server, id, token);
                return FactoryPackageStorage.read(server, id, token,
                        JDTEConfig.COMMON.factoryPackerMaxUncompressedBytes.get(), server.registryAccess());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }, Util.ioPool()).whenCompleteAsync((data, error) -> {
            ioPending = false;
            if (!isCurrent()) return;
            if (error != null || data == null) {
                LOGGER.error("Failed to claim factory package {}", id, error);
                releaseAfterFailure(8);
                return;
            }
            if (!data.size().equals(operationSize)) {
                applyPackageData(data);
                releaseAfterFailure(9);
                return;
            }
            applyPackageData(data);
            FactoryTransform transform = transform(level);
            for (BlockRecord record : records) {
                if (record.state().is(BLACKLIST)) {
                    rememberBlacklistedBlock(record.state(),
                            operationOrigin.offset(transform.position(record.relativePos())));
                }
            }
            if (blacklistedBlockCount > 0) {
                reportBlacklistedBlocks(level);
                releaseAfterFailure(2);
                return;
            }
            totalWork = volume(rotatedSize());
            cursor = 0;
            phase = Phase.PRECHECK;
            setChanged();
        }, server);
    }

    private void precheck(ServerLevel level, int budget) {
        if (!ensureRecords(level, true)) return;
        Vec3i targetSize = rotatedSize();
        int targetVolume = volume(targetSize);
        for (int processed = 0; processed < budget && cursor < targetVolume; processed++, cursor++) {
            BlockPos pos = positionAt(operationOrigin, targetSize, cursor);
            BlockState current = level.getBlockState(pos);
            if (pos.equals(getBlockPos()) || !current.isAir()) {
                releaseAfterFailure(10);
                return;
            }
        }
        setChanged();
        if (cursor >= targetVolume) {
            cursor = 0;
            totalWork = records.size();
            phase = Phase.PLACING;
        }
    }

    private void place(ServerLevel level, int budget) {
        FactoryTransform transform = transform(level);
        for (int processed = 0; processed < budget && cursor < records.size(); processed++, cursor++) {
            BlockRecord record = records.get(cursor);
            BlockPos pos = operationOrigin.offset(transform.position(record.relativePos()));
            BlockState targetState = transform.state(record.state());
            try {
                if (!level.getBlockState(pos).isAir()) {
                    rollbackLimit = cursor;
                    cursor = rollbackLimit - 1;
                    phase = Phase.ROLLBACK_PLACE;
                    errorCode = 10;
                    setChanged();
                    return;
                }
                if (!level.setBlock(pos, targetState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE)) {
                    throw new IllegalStateException("Block placement was rejected");
                }
                if (record.blockEntityData() != null) {
                    CompoundTag data = transform.blockEntityData(record.blockEntityData(), pos,
                            shouldRemapLinks());
                    if (!FactoryBlockEntityMoveSupport.completeMove(targetState, data, level, pos)) {
                        throw new IllegalStateException("Block entity restoration failed");
                    }
                }
                FactoryBlockEntityMoveSupport.applyRotation(level.getBlockEntity(pos), transform.rotation());
                rollbackLimit = cursor;
            } catch (Throwable throwable) {
                LOGGER.error("Failed to restore factory block at {}", pos, throwable);
                rollbackLimit = cursor;
                cursor = rollbackLimit;
                phase = Phase.ROLLBACK_PLACE;
                errorCode = 11;
                setChanged();
                return;
            }
        }
        setChanged();
        if (cursor >= records.size()) {
            phase = Phase.PREPARING_ENTITIES;
            cursor = 0;
            totalWork = records.size();
        }
    }

    private void placeEntities(ServerLevel level, int budget) {
        FactoryTransform transform = transform(level);
        for (int processed = 0; processed < budget && cursor < entityRecords.size(); processed++, cursor++) {
            EntityRecord record = entityRecords.get(cursor);
            if (level.getEntity(record.uuid()) != null) {
                entityRollbackLimit = cursor;
                cursor = entityRollbackLimit - 1;
                phase = Phase.ROLLBACK_PLACE_ENTITIES;
                errorCode = 18;
                setChanged();
                return;
            }
            CompoundTag data = transform.entityData(record.data(),
                    shouldRemapLinks());
            Entity entity = EntityType.loadEntityRecursive(data, level, loaded -> loaded);
            if (entity == null || !FactoryEntityMoveSupport.preparePlacement(level, entity)
                    || !level.tryAddFreshEntityWithPassengers(entity)) {
                entityRollbackLimit = cursor;
                cursor = entityRollbackLimit - 1;
                phase = Phase.ROLLBACK_PLACE_ENTITIES;
                errorCode = 18;
                setChanged();
                return;
            }
            FactoryEntityMoveSupport.completePlacement(level, entity);
            entityRollbackLimit = cursor + 1;
        }
        setChanged();
        if (cursor >= entityRecords.size()) {
            phase = Phase.COMMITTING_PLACE;
            cursor = 0;
            totalWork = 1;
        }
    }

    private void prepareEntityPlacement(ServerLevel level, int budget) {
        if (finalizePlacedBlocks(level, budget)) {
            phase = Phase.PLACING_ENTITIES;
            cursor = 0;
            totalWork = entityRecords.size();
        }
    }

    private void finalizePlacement(ServerLevel level, int budget) {
        if (finalizePlacedBlocks(level, budget)) completePlacement(level);
    }

    private boolean finalizePlacedBlocks(ServerLevel level, int budget) {
        FactoryTransform transform = transform(level);
        for (int processed = 0; processed < budget && cursor < records.size(); processed++, cursor++) {
            BlockRecord record = records.get(cursor);
            BlockPos pos = operationOrigin.offset(transform.position(record.relativePos()));
            if (record.blockEntityData() != null) {
                FactoryBlockEntityMoveSupport.finalizeExternalMove(level.getBlockEntity(pos), sourceDimension,
                        sourceOrigin.offset(record.relativePos()));
            }
            level.updateNeighborsAt(pos, transform.state(record.state()).getBlock());
            level.getLightEngine().checkBlock(pos);
            BlockState finalState = level.getBlockState(pos);
            level.sendBlockUpdated(pos, finalState, finalState, Block.UPDATE_CLIENTS);
        }
        setChanged();
        return cursor >= records.size();
    }

    private void completePlacement(ServerLevel level) {
        if (!tickRecords.isEmpty()) {
            FactoryScheduledTickSupport.restore(level, operationOrigin, tickRecords, transform(level));
        }
        packageHandler.setStackInSlot(0, ItemStack.EMPTY);
        deleteClaimedAsync(level);
        complete();
    }

    private void rollbackCut(ServerLevel level, int budget) {
        for (int processed = 0; processed < budget && cursor >= 0; processed++, cursor--) {
            restoreRecord(level, operationOrigin, records.get(cursor));
        }
        setChanged();
        if (cursor < 0) {
            if (entityRollbackLimit > 0) {
                cursor = 0;
                phase = Phase.ROLLBACK_CUT_ENTITIES;
                totalWork = entityRollbackLimit;
            } else {
                restoreSourceTicks(level);
                if (retryAfterRollback) restartSourceCapture(level);
                else {
                    deleteRegularAsync(level);
                    completeKeepingError();
                }
            }
        }
    }

    private void rollbackCutEntities(ServerLevel level, int budget) {
        for (int processed = 0; processed < budget && cursor < entityRollbackLimit; processed++, cursor++) {
            restoreEntity(level, entityRecords.get(cursor).data());
        }
        setChanged();
        if (cursor >= entityRollbackLimit) {
            entityRollbackLimit = 0;
            restoreSourceTicks(level);
            if (retryAfterRollback) restartSourceCapture(level);
            else {
                deleteRegularAsync(level);
                completeKeepingError();
            }
        }
    }

    private void rollbackPlacementEntities(ServerLevel level, int budget) {
        for (int processed = 0; processed < budget && cursor >= 0; processed++, cursor--) {
            Entity entity = level.getEntity(entityRecords.get(cursor).uuid());
            if (entity != null) {
                FactoryEntityMoveSupport.prepareRemoval(level, entity);
                discardTree(entity);
            }
        }
        setChanged();
        if (cursor < 0) {
            cursor = rollbackLimit;
            phase = Phase.ROLLBACK_PLACE;
            totalWork = rollbackLimit + 1;
        }
    }

    private void rollbackPlacement(ServerLevel level, int budget) {
        FactoryTransform transform = transform(level);
        for (int processed = 0; processed < budget && cursor >= 0; processed++, cursor--) {
            BlockPos pos = operationOrigin.offset(transform.position(records.get(cursor).relativePos()));
            setAirForMove(level, pos);
        }
        setChanged();
        if (cursor < 0) releaseAfterFailure(errorCode == 0 ? 11 : errorCode);
    }

    private static void restoreRecord(ServerLevel level, BlockPos origin, BlockRecord record) {
        BlockPos pos = origin.offset(record.relativePos());
        level.setBlock(pos, record.state(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        if (record.blockEntityData() != null) {
            CompoundTag data = record.blockEntityData().copy();
            data.putInt("x", pos.getX());
            data.putInt("y", pos.getY());
            data.putInt("z", pos.getZ());
            FactoryBlockEntityMoveSupport.completeMove(record.state(), data, level, pos);
        }
    }

    private void removeBlockEntityForMove(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        prepareBlockEntityForMove(blockEntity);
        level.removeBlockEntity(pos);
    }

    private void prepareBlockEntityForMove(BlockEntity blockEntity) {
        FactoryBlockEntityMoveSupport.preserveMekanismTransmitterContents(blockEntity);
        FactoryBlockEntityMoveSupport.prepareNetworkDetach(blockEntity);
        FactoryBlockEntityMoveSupport.prepareRemoval(blockEntity);
    }

    private void setAirForMove(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        MekanismFactoryMoveIntegration.RemovalDiagnostics before = DEBUG_LOGGING
                ? FactoryBlockEntityMoveSupport.captureMekanismRemovalDiagnostics(level, pos, blockEntity)
                : null;
        if (blockEntity != null) prepareBlockEntityForMove(blockEntity);
        MekanismFactoryMoveIntegration.RemovalDiagnostics prepared = DEBUG_LOGGING
                ? FactoryBlockEntityMoveSupport.captureMekanismRemovalDiagnostics(level, pos, blockEntity)
                : null;
        boolean logDiagnostics = before != null && prepared != null
                && (before.relevant() || prepared.relevant())
                && mekanismRemovalDebugEntries < MAX_MEKANISM_REMOVAL_DEBUG_ENTRIES;
        if (logDiagnostics) {
            mekanismRemovalDebugEntries++;
            debugLog("mekanism-removal", before.describe("before-network-detach", before.radiationLevel()));
            debugLog("mekanism-removal", prepared.describe("after-network-detach", prepared.radiationLevel()));
        }
        if (blockEntity != null) level.removeBlockEntity(pos);
        if (logDiagnostics) {
            debugLog("mekanism-removal", prepared.describe("after-remove-block-entity",
                    mekanism.api.radiation.IRadiationManager.INSTANCE.getRadiationLevel(level, pos)));
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        if (logDiagnostics) {
            debugLog("mekanism-removal", prepared.describe("after-set-air",
                    mekanism.api.radiation.IRadiationManager.INSTANCE.getRadiationLevel(level, pos)));
        }
    }

    private boolean ensureRecords(ServerLevel level, boolean claimed) {
        if (records != null) return true;
        if (!ioPending) readForResume(level, claimed);
        return false;
    }

    private boolean ensurePackageData(ServerLevel level, boolean claimed) {
        if (records != null && entityRecords != null && tickRecords != null) return true;
        if (!ioPending) readForResume(level, claimed);
        return false;
    }

    private void readForResume(ServerLevel level, boolean claimed) {
        ioPending = true;
        MinecraftServer server = level.getServer();
        UUID id = packageId;
        UUID token = claimToken;
        CompletableFuture.supplyAsync(() -> {
            try {
                return FactoryPackageStorage.read(server, id, claimed ? token : null,
                        JDTEConfig.COMMON.factoryPackerMaxUncompressedBytes.get(), server.registryAccess());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }, Util.ioPool()).whenCompleteAsync((data, error) -> {
            ioPending = false;
            if (!isCurrent()) return;
            if (error != null || data == null) {
                LOGGER.error("Failed to resume factory package {}", id, error);
                if (claimed) releaseAfterFailure(12);
                else fail(12);
            } else {
                applyPackageData(data);
                totalWork = records.size();
                if (phase == Phase.WRITING) phase = Phase.WAITING_ENERGY;
                else if (phase == Phase.WRITING_PREPARED) {
                    phase = Phase.VERIFYING_PREPARED_SOURCE;
                    cursor = 0;
                    totalWork = volume(operationSize);
                    sourceRecordIndex = null;
                }
            }
        }, server);
    }

    private void releaseAfterFailure(int code) {
        if (!(level instanceof ServerLevel serverLevel) || packageId == null) {
            fail(code);
            return;
        }
        ioPending = true;
        MinecraftServer server = serverLevel.getServer();
        UUID id = packageId;
        UUID token = claimToken;
        CompletableFuture.runAsync(() -> {
            try {
                FactoryPackageStorage.release(server, id, token);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }, Util.ioPool()).whenCompleteAsync((ignored, error) -> {
            ioPending = false;
            if (error != null) LOGGER.error("Failed to release factory package {}", id, error);
            errorCode = code;
            completeKeepingError();
        }, server);
    }

    private void deleteClaimedAsync(ServerLevel level) {
        deleteAsync(level, true);
    }

    private void deleteRegularAsync(ServerLevel level) {
        deleteAsync(level, false);
    }

    private void deleteAsync(ServerLevel level, boolean claimed) {
        MinecraftServer server = level.getServer();
        UUID id = packageId;
        UUID token = claimToken;
        CompletableFuture.runAsync(() -> {
            try {
                if (claimed) FactoryPackageStorage.deleteClaimed(server, id, token);
                else FactoryPackageStorage.deleteRegular(server, id);
            } catch (IOException exception) {
                LOGGER.warn("Failed to delete {} factory package {}", claimed ? "claimed" : "regular", id,
                        exception);
            }
        }, Util.ioPool());
    }

    private void fail(int code) {
        errorCode = code;
        completeKeepingError();
    }

    private void complete() {
        errorCode = 0;
        completeKeepingError();
    }

    private void completeKeepingError() {
        if (phase != Phase.IDLE && level instanceof ServerLevel serverLevel) {
            notifyOperationResult(serverLevel);
        }
        phase = Phase.IDLE;
        cursor = 0;
        totalWork = 0;
        rollbackLimit = 0;
        packageId = null;
        claimToken = null;
        operationOwner = null;
        operationOrigin = BlockPos.ZERO;
        operationSize = Vec3i.ZERO;
        operationRotation = 0;
        scheduledTicksRemoved = false;
        sourceRetryCount = 0;
        quiesceWaitTicks = 0;
        energyPaid = 0;
        sourceQuiesced = false;
        quiesceValidated = false;
        retryAfterRollback = false;
        records = null;
        entityRecords = null;
        tickRecords = null;
        sourceDimension = null;
        sourceOrigin = BlockPos.ZERO;
        packageFormatVersion = 0;
        ioPending = false;
        blacklistedBlocks = new ArrayList<>();
        blacklistedBlockCount = 0;
        sourceRecordIndex = null;
        lastNotifiedPhase = null;
        markDirtyClient();
    }

    private void notifyPhaseChange(ServerLevel level) {
        if (phase == Phase.IDLE || phase == lastNotifiedPhase) return;
        lastNotifiedPhase = phase;
        debugLog("phase", "cursor=" + cursor + "/" + totalWork);
        if (!JDTEConfig.COMMON.factoryPackerChatNotifications.get()) return;
        sendOwnerMessage(level, Component.translatable("message.jdte.factory_packer.operation_phase",
                Component.translatable("screen.jdte.factory_packer.phase." + phase.ordinal()),
                Math.max(0, cursor), Math.max(0, totalWork)).withStyle(ChatFormatting.GRAY));
    }

    private void notifyOperationResult(ServerLevel level) {
        debugLog(errorCode == 0 ? "operation-complete" : "operation-failed",
                "errorCode=" + errorCode + " cursor=" + cursor + "/" + totalWork);
        if (!JDTEConfig.COMMON.factoryPackerChatNotifications.get()) return;
        Component result = errorCode == 0
                ? Component.translatable("message.jdte.factory_packer.operation_complete")
                        .withStyle(ChatFormatting.GREEN)
                : Component.translatable("message.jdte.factory_packer.operation_failed", errorCode,
                        Component.translatable("screen.jdte.factory_packer.error." + errorCode))
                        .withStyle(ChatFormatting.RED);
        sendOwnerMessage(level, result);
    }

    private void reportSourceChange(ServerLevel level, String stage, BlockPos pos, BlockState expected,
                                    BlockState current, String reason) {
        int maxRetries = JDTEConfig.COMMON.factoryPackerSourceChangeRetries.get();
        boolean willRetry = sourceRetryCount < maxRetries;
        debugLog("source-change", "stage=" + stage + " reason=" + reason + " pos=" + pos
                + " expected=" + blockStateDescription(expected) + " current=" + blockStateDescription(current)
                + " willRetry=" + willRetry + " nextRetry=" + (sourceRetryCount + 1) + "/" + maxRetries);
        if (!JDTEConfig.COMMON.factoryPackerChatNotifications.get()) return;
        ServerPlayer player = owner(level);
        if (player == null) return;
        Component coordinates = interactiveCoordinates(pos, player.hasPermissions(2),
                "message.jdte.factory_packer.source_change_teleport");
        Component retry = willRetry
                ? Component.translatable("message.jdte.factory_packer.source_change_retry",
                        sourceRetryCount + 1, maxRetries)
                : Component.translatable("message.jdte.factory_packer.source_change_final");
        player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.source_change",
                Component.translatable("message.jdte.factory_packer.source_stage." + stage),
                Component.translatable("message.jdte.factory_packer.source_reason." + reason), coordinates, retry)
                .withStyle(ChatFormatting.RED));
        player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.source_change_blocks",
                blockStateComponent(expected), blockStateComponent(current)).withStyle(ChatFormatting.YELLOW));
    }

    private static String sourceChangeReason(BlockState expected, BlockState current) {
        if (expected.isAir()) return "unexpected_block";
        if (current.isAir()) return "missing_block";
        return "replaced_block";
    }

    private static Component blockStateComponent(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return Component.translatable("message.jdte.factory_packer.source_block",
                state.getBlock().getName(), id.toString());
    }

    private static String blockStateDescription(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()) + " " + state;
    }

    private ServerPlayer owner(ServerLevel level) {
        return operationOwner == null ? null : level.getServer().getPlayerList().getPlayer(operationOwner);
    }

    private void sendOwnerMessage(ServerLevel level, Component component) {
        ServerPlayer player = owner(level);
        if (player != null) player.sendSystemMessage(component);
    }

    private void debugLog(String event, String details) {
        if (DEBUG_LOGGING) {
            LOGGER.warn("[FactoryPacker/Debug] package={} phase={} retry={} event={} {}", packageId, phase,
                    sourceRetryCount, event, details);
        }
    }

    private void rememberBlacklistedBlock(BlockState state, BlockPos pos) {
        blacklistedBlockCount++;
        if (blacklistedBlocks.size() < MAX_BLACKLIST_REPORT_ENTRIES) {
            blacklistedBlocks.add(new BlacklistedBlock(state, pos.immutable()));
        }
    }

    private void reportBlacklistedBlocks(ServerLevel level) {
        if (operationOwner == null || blacklistedBlockCount == 0) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(operationOwner);
        if (player == null) return;

        player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.blacklist_summary",
                blacklistedBlockCount, BLACKLIST.location().toString()).withStyle(ChatFormatting.RED));
        boolean canTeleport = player.hasPermissions(2);
        for (BlacklistedBlock entry : blacklistedBlocks) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(entry.state().getBlock());
            Component coordinates = interactiveCoordinates(entry.pos(), canTeleport,
                    "message.jdte.factory_packer.blacklist_teleport");
            player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.blacklist_entry",
                    entry.state().getBlock().getName(), id.toString(), coordinates)
                    .withStyle(ChatFormatting.YELLOW));
        }
        int hidden = blacklistedBlockCount - blacklistedBlocks.size();
        if (hidden > 0) {
            player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.blacklist_more", hidden)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static Component interactiveCoordinates(BlockPos pos, boolean canTeleport, String hoverKey) {
        MutableComponent coordinates = Component.literal("[" + pos.getX() + ", " + pos.getY() + ", "
                + pos.getZ() + "]").withStyle(ChatFormatting.AQUA);
        if (!canTeleport) return coordinates;
        String command = "/tp @s " + (pos.getX() + 0.5D) + " " + (pos.getY() + 1) + " "
                + (pos.getZ() + 0.5D);
        return coordinates.withStyle(style -> style.withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable(hoverKey))));
    }

    private record BlacklistedBlock(BlockState state, BlockPos pos) {}

    private int requiredEnergy() {
        if (records == null) return Integer.MAX_VALUE;
        long cost = (long) records.size() * JDTEConfig.COMMON.factoryPackerEnergyPerBlock.get()
                + (long) (entityRecords == null ? 0 : entityRecords.size())
                * JDTEConfig.COMMON.factoryPackerEnergyPerEntity.get();
        int baseCost = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, cost));
        return UpgradeHelper.adjustEnergyCost(this, baseCost);
    }

    private int operationBudget() {
        int base = JDTEConfig.COMMON.factoryPackerBlocksPerTick.get();
        return UpgradeHelper.hasCreativeUpgrade(this)
                || UpgradeHelper.countUpgrades(this, UpgradeType.OVERCLOCK) > 0 ? base * 4 : base;
    }

    private Bounds currentBounds() {
        AABB area = getAABB(getBlockPos());
        BlockPos min = BlockPos.containing(Math.floor(area.minX), Math.floor(area.minY), Math.floor(area.minZ));
        BlockPos max = BlockPos.containing(Math.ceil(area.maxX) - 1, Math.ceil(area.maxY) - 1,
                Math.ceil(area.maxZ) - 1);
        return new Bounds(min, new Vec3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1,
                max.getZ() - min.getZ() + 1));
    }

    private Bounds currentBounds(ItemStack packageStack) {
        if (FactoryPackageItem.isFilled(packageStack)) {
            var target = FactoryPackageItem.getPlacementTarget(packageStack);
            var size = FactoryPackageItem.getSize(packageStack);
            if (target.isPresent() && size.isPresent()) {
                return new Bounds(target.get().origin(), FactoryPackageItem.getRotatedSize(packageStack));
            }
            if (size.isPresent()) {
                return new Bounds(currentBounds().min(), FactoryPackageItem.getRotatedSize(packageStack));
            }
        }
        return currentBounds();
    }

    @Override
    public AABB getAABB(BlockPos pos) {
        ItemStack stack = packageHandler.getStackInSlot(0);
        var target = FactoryPackageItem.getPlacementTarget(stack);
        var size = FactoryPackageItem.getSize(stack);
        if (level != null && target.isPresent() && size.isPresent()
                && target.get().dimension().equals(level.dimension().location())) {
            BlockPos origin = target.get().origin();
            Vec3i dimensions = FactoryPackageItem.getRotatedSize(stack);
            BlockPos max = origin.offset(dimensions).offset(-1, -1, -1);
            AABB absolute = AABB.encapsulatingFullBlocks(origin, max);
            BlockPos delta = pos.subtract(getBlockPos());
            return absolute.move(delta.getX(), delta.getY(), delta.getZ());
        }
        return AreaAffectingBE.super.getAABB(pos);
    }

    private Component validateBounds(ServerLevel level, Bounds bounds) {
        int maxAxis = JDTEConfig.COMMON.factoryPackerMaxAxis.get();
        if (bounds.size().getX() > maxAxis) return Component.translatable(
                "message.jdte.factory_packer.area_axis_too_large", "X", bounds.size().getX(), maxAxis);
        if (bounds.size().getY() > maxAxis) return Component.translatable(
                "message.jdte.factory_packer.area_axis_too_large", "Y", bounds.size().getY(), maxAxis);
        if (bounds.size().getZ() > maxAxis) return Component.translatable(
                "message.jdte.factory_packer.area_axis_too_large", "Z", bounds.size().getZ(), maxAxis);
        int maxVolume = JDTEConfig.COMMON.factoryPackerMaxVolume.get();
        if (bounds.volume() > maxVolume) return Component.translatable(
                "message.jdte.factory_packer.area_volume_too_large", bounds.volume(), maxVolume);
        BlockPos max = bounds.min().offset(bounds.size()).offset(-1, -1, -1);
        if (bounds.min().getY() < level.getMinBuildHeight() || max.getY() >= level.getMaxBuildHeight()) {
            return message("outside_build_height");
        }
        int minChunkX = SectionPos.blockToSectionCoord(bounds.min().getX());
        int maxChunkX = SectionPos.blockToSectionCoord(max.getX());
        int minChunkZ = SectionPos.blockToSectionCoord(bounds.min().getZ());
        int maxChunkZ = SectionPos.blockToSectionCoord(max.getZ());
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                if (!level.getChunkSource().hasChunk(x, z)) return message("chunks_not_loaded");
            }
        }
        if (AABB.encapsulatingFullBlocks(bounds.min(), max).contains(getBlockPos().getCenter())) {
            return message("contains_packer");
        }
        return null;
    }

    private static int volume(Vec3i size) {
        return Math.multiplyExact(Math.multiplyExact(size.getX(), size.getY()), size.getZ());
    }

    private static BlockPos positionAt(BlockPos origin, Vec3i size, int index) {
        int layer = size.getX() * size.getZ();
        int y = index / layer;
        int remainder = index % layer;
        int z = remainder / size.getX();
        int x = remainder % size.getX();
        return origin.offset(x, y, z);
    }

    private boolean isCurrent() {
        return level != null && !isRemoved() && level.getBlockEntity(getBlockPos()) == this;
    }

    private static Component message(String key) {
        return Component.translatable("message.jdte.factory_packer." + key);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel) || phase == Phase.IDLE) return;
        if (packageId == null) {
            fail(12);
            return;
        }
        if ((phase == Phase.CLAIMING || phase == Phase.PRECHECK || phase == Phase.PLACING
                || phase == Phase.PREPARING_ENTITIES || phase == Phase.PLACING_ENTITIES
                || phase == Phase.FINALIZING_PLACE || phase == Phase.COMMITTING_PLACE
                || phase == Phase.ROLLBACK_PLACE || phase == Phase.ROLLBACK_PLACE_ENTITIES)
                && claimToken == null) {
            fail(12);
            return;
        }
        switch (phase) {
            case CAPTURING, RECAPTURING_SOURCE, QUIESCING_SOURCE -> fail(13);
            case PREPARING_MOVE -> {
                cursor = 0;
                rollbackLimit = -1;
                readForResume(serverLevel, false);
            }
            case WRITING, WRITING_PREPARED, WAITING_ENERGY, CUTTING, CUTTING_ENTITIES, CHECKING_PERMISSIONS,
                    VERIFYING_SOURCE, VERIFYING_PREPARED_SOURCE, VERIFYING_CUT_SOURCE,
                    FINALIZING_CUT, ROLLBACK_CUT, ROLLBACK_CUT_ENTITIES,
                    ROLLBACK_PREPARE, ROLLBACK_PERMISSION -> readForResume(serverLevel, false);
            case CLAIMING -> claimAndRead(serverLevel);
            case PRECHECK, PLACING, PREPARING_ENTITIES, PLACING_ENTITIES, FINALIZING_PLACE, COMMITTING_PLACE,
                    ROLLBACK_PLACE, ROLLBACK_PLACE_ENTITIES -> readForResume(serverLevel, true);
            default -> { }
        }
    }

    @Override
    public void setAreaSettings(double xRadius, double yRadius, double zRadius,
                                int xOffset, int yOffset, int zOffset, boolean renderArea) {
        if (isBusy()) return;
        AreaAffectingBE.super.setAreaSettings(xRadius, yRadius, zRadius, xOffset, yOffset, zOffset, renderArea);
    }

    @Override
    public void handleRotate(Direction oldDirection, Direction newDirection) {
        if (!isBusy()) AreaAffectingBE.super.handleRotate(oldDirection, newDirection);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("factoryPackageSlot", packageHandler.serializeNBT(provider));
        tag.putInt("factoryPackerEnergy", energy.getEnergyStored());
        tag.putInt("factoryPackerPhase", phase.ordinal());
        tag.putInt("factoryPackerCursor", cursor);
        tag.putInt("factoryPackerTotal", totalWork);
        tag.putInt("factoryPackerRollbackLimit", rollbackLimit);
        tag.putInt("factoryPackerEntityRollbackLimit", entityRollbackLimit);
        tag.putInt("factoryPackerError", errorCode);
        tag.putInt("factoryPackerSourceRetries", sourceRetryCount);
        tag.putInt("factoryPackerQuiesceWait", quiesceWaitTicks);
        tag.putInt("factoryPackerEnergyPaid", energyPaid);
        tag.putBoolean("factoryPackerSourceQuiesced", sourceQuiesced);
        tag.putBoolean("factoryPackerQuiesceValidated", quiesceValidated);
        tag.putBoolean("factoryPackerRetryAfterRollback", retryAfterRollback);
        if (packageId != null) tag.putUUID("factoryPackerPackageId", packageId);
        if (claimToken != null) tag.putUUID("factoryPackerClaimToken", claimToken);
        if (operationOwner != null) tag.putUUID("factoryPackerOwner", operationOwner);
        tag.putLong("factoryPackerOrigin", operationOrigin.asLong());
        tag.putIntArray("factoryPackerSize", List.of(operationSize.getX(), operationSize.getY(), operationSize.getZ()));
        tag.putInt("factoryPackerRotation", operationRotation);
        tag.putBoolean("factoryPackerTicksRemoved", scheduledTicksRemoved);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("factoryPackageSlot")) packageHandler.deserializeNBT(provider, tag.getCompound("factoryPackageSlot"));
        if (tag.contains("factoryPackerEnergy")) energy.setEnergy(tag.getInt("factoryPackerEnergy"));
        phase = Phase.values()[Math.floorMod(tag.getInt("factoryPackerPhase"), Phase.values().length)];
        cursor = tag.getInt("factoryPackerCursor");
        totalWork = tag.getInt("factoryPackerTotal");
        rollbackLimit = tag.getInt("factoryPackerRollbackLimit");
        entityRollbackLimit = tag.getInt("factoryPackerEntityRollbackLimit");
        errorCode = tag.getInt("factoryPackerError");
        sourceRetryCount = tag.getInt("factoryPackerSourceRetries");
        quiesceWaitTicks = tag.getInt("factoryPackerQuiesceWait");
        energyPaid = tag.getInt("factoryPackerEnergyPaid");
        sourceQuiesced = tag.getBoolean("factoryPackerSourceQuiesced");
        quiesceValidated = tag.getBoolean("factoryPackerQuiesceValidated");
        retryAfterRollback = tag.getBoolean("factoryPackerRetryAfterRollback");
        packageId = tag.hasUUID("factoryPackerPackageId") ? tag.getUUID("factoryPackerPackageId") : null;
        claimToken = tag.hasUUID("factoryPackerClaimToken") ? tag.getUUID("factoryPackerClaimToken") : null;
        operationOwner = tag.hasUUID("factoryPackerOwner") ? tag.getUUID("factoryPackerOwner") : null;
        operationOrigin = tag.contains("factoryPackerOrigin") ? BlockPos.of(tag.getLong("factoryPackerOrigin")) : BlockPos.ZERO;
        int[] size = tag.getIntArray("factoryPackerSize");
        operationSize = size.length == 3 ? new Vec3i(size[0], size[1], size[2]) : Vec3i.ZERO;
        operationRotation = Math.floorMod(tag.getInt("factoryPackerRotation"), 4);
        scheduledTicksRemoved = tag.getBoolean("factoryPackerTicksRemoved");
        records = null;
        entityRecords = null;
        tickRecords = null;
        sourceDimension = null;
        sourceOrigin = BlockPos.ZERO;
        packageFormatVersion = 0;
        ioPending = false;
        blacklistedBlocks = new ArrayList<>();
        blacklistedBlockCount = 0;
        sourceRecordIndex = null;
    }

    private void applyPackageData(PackageData data) {
        records = new ArrayList<>(data.blocks());
        entityRecords = data.entities();
        tickRecords = data.ticks();
        sourceDimension = data.sourceDimension();
        sourceOrigin = data.sourceOrigin();
        packageFormatVersion = data.formatVersion();
        sourceRecordIndex = null;
    }

    private PackageData currentPackageData() {
        return new PackageData(2, packageId, sourceDimension, sourceOrigin, operationSize,
                List.copyOf(records), List.copyOf(entityRecords), List.copyOf(tickRecords));
    }

    private Vec3i rotatedSize() {
        return operationRotation % 2 == 0 ? operationSize
                : new Vec3i(operationSize.getZ(), operationSize.getY(), operationSize.getX());
    }

    private FactoryTransform transform(ServerLevel level) {
        return new FactoryTransform(operationSize, operationRotation, sourceOrigin, operationOrigin,
                sourceDimension, level.dimension().location());
    }

    private boolean shouldRemapLinks() {
        return packageFormatVersion >= 2 && sourceDimension != null
                && JDTEConfig.COMMON.factoryPackerRemapInternalLinks.get();
    }

    private void restoreSourceTicks(ServerLevel level) {
        if (!scheduledTicksRemoved || tickRecords == null || tickRecords.isEmpty()) return;
        FactoryTransform identity = new FactoryTransform(operationSize, 0, sourceOrigin, sourceOrigin,
                sourceDimension, sourceDimension);
        FactoryScheduledTickSupport.restore(level, sourceOrigin, tickRecords, identity);
        scheduledTicksRemoved = false;
    }

    private static boolean restoreEntity(ServerLevel level, CompoundTag data) {
        if (data.hasUUID("UUID") && level.getEntity(data.getUUID("UUID")) != null) return true;
        Entity entity = EntityType.loadEntityRecursive(data.copy(), level, loaded -> loaded);
        if (entity == null || !FactoryEntityMoveSupport.preparePlacement(level, entity)
                || !level.tryAddFreshEntityWithPassengers(entity)) return false;
        FactoryEntityMoveSupport.completePlacement(level, entity);
        return true;
    }

    private static void discardTree(Entity root) {
        List<Entity> tree = root.getSelfAndPassengers().toList();
        for (int index = tree.size() - 1; index >= 0; index--) tree.get(index).discard();
    }

    private record Bounds(BlockPos min, Vec3i size) {
        int volume() { return FactoryPackerBE.volume(size); }
    }
}
