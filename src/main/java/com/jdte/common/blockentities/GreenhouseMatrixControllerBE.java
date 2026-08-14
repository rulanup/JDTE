package com.jdte.common.blockentities;

import com.jdte.common.blocks.GreenhouseMatrixStructure;
import com.jdte.common.blocks.GreenhouseMatrixCasingBlock;
import com.jdte.common.containers.GreenhouseMatrixContainer;
import com.jdte.common.greenhouse.GreenhouseMatrixEnhancement;
import com.jdte.common.greenhouse.GreenhouseMatrixMember;
import com.jdte.common.greenhouse.GreenhouseMatrixMemberLookup;
import com.jdte.common.greenhouse.GreenhouseMatrixOutputBuffer;
import com.jdte.common.greenhouse.GreenhouseMatrixSimulation;
import com.jdte.common.greenhouse.GreenhouseMatrixDropGenerator;
import com.jdte.common.greenhouse.GreenhouseMatrixAccelerationClock;
import com.jdte.common.greenhouse.GreenhouseMatrixAutoCraftingCatalog;
import com.jdte.common.greenhouse.GreenhouseMatrixAutoCraftingProcessor;
import com.jdte.common.greenhouse.GreenhouseMatrixProductionGroup;
import com.jdte.common.greenhouse.GreenhouseMatrixProductionProfile;
import com.jdte.common.greenhouse.GreenhouseMatrixRenderRegistry;
import com.jdte.common.greenhouse.GreenhouseMatrixRuntime;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEItems;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GreenhouseMatrixControllerBE extends BlockEntity implements MenuProvider, CoalescedAcceleratedMachine {
    public static final int CONTROLLER_UPGRADE_SLOTS = 8;
    public static final int GLOBAL_UPGRADE_SLOTS = 8;
    private static final int UPGRADE_INSTALL_BUDGET = 16;
    private boolean formed;
    private boolean enabled = true;
    private boolean renderEnabled = true;
    private boolean autoIoEnabled = true;
    private String error = "unvalidated";
    private BlockPos min = BlockPos.ZERO;
    private BlockPos max = BlockPos.ZERO;
    private List<BlockPos> greenhouses = List.of();
    private List<BlockPos> ports = List.of();
    private List<BlockPos> autoCraftingPages = List.of();
    private final EnumMap<GreenhouseMatrixEnhancement, Integer> enhancements = new EnumMap<>(GreenhouseMatrixEnhancement.class);
    private int validationTicker;
    private int autoIoPortCursor;
    private boolean casingConnectionsRefreshed;
    private int upgradeSourceCursor;
    private int upgradeTargetCursor;
    private boolean loadingUpgradeInventory;
    private final AEOutputManager.MatrixState aeOutputState = new AEOutputManager.MatrixState();
    private GreenhouseMatrixCapabilitySnapshot capabilitySnapshot = GreenhouseMatrixCapabilitySnapshot.EMPTY;
    private final GreenhouseMatrixSimulation simulation = new GreenhouseMatrixSimulation();
    private final GreenhouseMatrixOutputBuffer outputBuffer = new GreenhouseMatrixOutputBuffer(4_096, Long.MAX_VALUE);
    private final GreenhouseMatrixAccelerationClock accelerationClock = new GreenhouseMatrixAccelerationClock();
    private long lastSimulationGameTime = Long.MIN_VALUE;
    private boolean autoCraftingCatalogDirty = true;
    private GreenhouseMatrixAutoCraftingCatalog.Snapshot autoCraftingCatalog =
            GreenhouseMatrixAutoCraftingCatalog.Snapshot.empty(Long.MIN_VALUE);
    private int autoCraftingRoundRobinCursor;

    private final ItemStackHandler controllerUpgradeHandler = new ItemStackHandler(CONTROLLER_UPGRADE_SLOTS) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            if (!stack.is(JDTEItems.GREENHOUSE_MATRIX_QUICK_INSTALL_UPGRADE.get())
                    && !UpgradeHelper.isUpgrade(stack, UpgradeType.AE_OUTPUT)) return false;
            for (int other = 0; other < getSlots(); other++) {
                if (other != slot && getStackInSlot(other).is(stack.getItem())) return false;
            }
            return true;
        }
        @Override protected void onContentsChanged(int slot) { onControllerUpgradeInventoryChanged(); }
    };
    private final ItemStackHandler globalUpgradeHandler = new ItemStackHandler(GLOBAL_UPGRADE_SLOTS) {
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof UpgradeCardItem card && isGreenhouseUpgrade(card.getType());
        }
        @Override protected void onContentsChanged(int slot) { onUpgradeInventoryChanged(); }
    };

    private final IItemHandler inputHandler = new MatrixItemHandler(true);
    private final IItemHandler outputHandler = new MatrixItemHandler(false);
    private final IFluidHandler fluidHandler = new MatrixFluidHandler();
    private final IEnergyStorage energyHandler = new MatrixEnergyHandler();

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> formed ? 1 : 0;
                case 1 -> enabled ? 1 : 0;
                case 2 -> renderEnabled ? 1 : 0;
                case 3 -> greenhouses.size();
                case 4 -> count(GreenhouseMatrixEnhancement.SPEED);
                case 5 -> count(GreenhouseMatrixEnhancement.EFFICIENCY);
                case 6 -> count(GreenhouseMatrixEnhancement.SEED_CONVERSION);
                case 7 -> count(GreenhouseMatrixEnhancement.ESSENCE_CONVERSION);
                case 8 -> max.getX() - min.getX() + 1;
                case 9 -> max.getY() - min.getY() + 1;
                case 10 -> max.getZ() - min.getZ() + 1;
                case 11 -> errorCode(error);
                case 12 -> autoIoEnabled ? 1 : 0;
                case 13 -> simulation.groupCount();
                case 14 -> simulation.rebuilding() ? 1 : 0;
                case 15 -> (int) outputBuffer.totalCount();
                case 16 -> (int) (outputBuffer.totalCount() >>> 32);
                case 17 -> outputBuffer.distinctTypes();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 18; }
    };

    public GreenhouseMatrixControllerBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.GREENHOUSE_MATRIX_CONTROLLER.get(), pos, state);
        for (GreenhouseMatrixEnhancement type : GreenhouseMatrixEnhancement.values()) enhancements.put(type, 0);
    }

    public void invalidateAutoCraftingCatalog() {
        autoCraftingCatalogDirty = true;
    }

    private GreenhouseMatrixAutoCraftingCatalog.Snapshot autoCraftingCatalog(ServerLevel serverLevel) {
        long generation = com.jdte.common.greenhouse.GreenhouseMatrixPatternSupport.recipeGeneration();
        if (autoCraftingCatalogDirty || autoCraftingCatalog.recipeGeneration() != generation) {
            autoCraftingCatalog = GreenhouseMatrixAutoCraftingCatalog.capture(serverLevel, autoCraftingPages);
            autoCraftingCatalogDirty = false;
        }
        return autoCraftingCatalog;
    }

    public void serverTick() {
        if (++validationTicker >= 100) {
            validationTicker = 0;
            validateNow();
        }
        if (formed && autoIoEnabled && level instanceof ServerLevel serverLevel) {
            autoIoPortCursor = GreenhouseMatrixAutoIo.tick(serverLevel, this, ports, min, max, autoIoPortCursor);
        }
        if (formed && level instanceof ServerLevel serverLevel) {
            simulation.rebuildStep(JDTEConfig.COMMON.greenhouseMatrixProfileScanBudget.get());
            tickSimulation(serverLevel);
        }
        if (formed && hasQuickInstallUpgrade()) distributeQueuedUpgrades();
        if (formed && hasAEOutputUpgrade()) AEOutputManager.tickMatrix(this);
    }

    public void validateNow() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        boolean oldFormed = formed;
        List<BlockPos> oldMembers = greenhouses;
        List<BlockPos> oldPorts = ports;
        List<BlockPos> oldAutoCraftingPages = autoCraftingPages;
        GreenhouseMatrixStructure.ScanResult result = GreenhouseMatrixStructure.scan(serverLevel, worldPosition);
        for (BlockPos pos : oldMembers) {
            GreenhouseMatrixRuntime.remove(serverLevel, pos, worldPosition);
            releaseMember(serverLevel, pos);
        }
        formed = result.formed();
        error = result.error();
        min = result.min();
        max = result.max();
        greenhouses = result.greenhouses();
        ports = result.ports();
        autoCraftingPages = result.autoCraftingPages();
        invalidateAutoCraftingCatalog();
        invalidateCapabilitySnapshot();
        enhancements.clear();
        enhancements.putAll(result.enhancements());
        if (!formed) {
            accelerationClock.clear();
            casingConnectionsRefreshed = false;
        } else if (!casingConnectionsRefreshed) {
            refreshCasingConnections(serverLevel);
            casingConnectionsRefreshed = true;
        }
        Set<BlockPos> affectedPorts = new LinkedHashSet<>(oldPorts);
        affectedPorts.addAll(ports);
        for (BlockPos pos : oldPorts) {
            if ((!formed || !ports.contains(pos))
                    && serverLevel.getBlockEntity(pos) instanceof GreenhouseMatrixPortBE port) {
                port.unlink(worldPosition);
            }
        }
        for (BlockPos pos : oldAutoCraftingPages) {
            if ((!formed || !autoCraftingPages.contains(pos))
                    && serverLevel.getBlockEntity(pos) instanceof GreenhouseMatrixAutoCraftingBE page) {
                page.unlink(worldPosition);
            }
        }
        if (formed) {
            GreenhouseMatrixRuntime.Effects effects = new GreenhouseMatrixRuntime.Effects(worldPosition, enabled,
                    count(GreenhouseMatrixEnhancement.SPEED), count(GreenhouseMatrixEnhancement.EFFICIENCY),
                    count(GreenhouseMatrixEnhancement.SEED_CONVERSION), count(GreenhouseMatrixEnhancement.ESSENCE_CONVERSION),
                    hasAEOutputUpgrade());
            for (BlockPos pos : greenhouses) {
                claimMember(serverLevel, pos);
                GreenhouseMatrixRuntime.put(serverLevel, pos, effects);
            }
            for (BlockPos pos : ports) {
                if (serverLevel.getBlockEntity(pos) instanceof GreenhouseMatrixPortBE port) port.link(worldPosition);
            }
            for (BlockPos pos : autoCraftingPages) {
                if (serverLevel.getBlockEntity(pos) instanceof GreenhouseMatrixAutoCraftingBE page) {
                    page.link(worldPosition);
                }
            }
            simulation.beginRebuild(greenhouses, pos -> {
                BlockEntity member = serverLevel.getBlockEntity(pos);
                return member instanceof GreenhouseMatrixMember matrixMember
                        ? matrixMember.captureMatrixProfiles(serverLevel, effects) : List.of();
            });
        }
        if (oldFormed != formed || !oldMembers.equals(greenhouses) || !oldPorts.equals(ports)) {
            for (BlockPos pos : affectedPorts) serverLevel.invalidateCapabilities(pos);
        }
        sync();
    }

    private void refreshCasingConnections(ServerLevel level) {
        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            boolean boundary = cursor.getX() == min.getX() || cursor.getX() == max.getX()
                    || cursor.getY() == min.getY() || cursor.getY() == max.getY()
                    || cursor.getZ() == min.getZ() || cursor.getZ() == max.getZ();
            if (!boundary) continue;
            BlockPos pos = cursor.immutable();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof GreenhouseMatrixCasingBlock)) continue;
            BlockState connected = GreenhouseMatrixCasingBlock.connectedState(state, level, pos);
            if (connected != state) level.setBlock(pos, connected, Block.UPDATE_CLIENTS);
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (!enabled) accelerationClock.clear();
        validateNow();
    }

    public void setRenderEnabled(boolean renderEnabled) {
        if (this.renderEnabled == renderEnabled) return;
        this.renderEnabled = renderEnabled;
        sync();
    }

    public void setAutoIoEnabled(boolean autoIoEnabled) {
        if (this.autoIoEnabled == autoIoEnabled) return;
        this.autoIoEnabled = autoIoEnabled;
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean isFormed() { return formed; }
    public boolean isEnabled() { return enabled; }
    public boolean isRenderEnabled() { return renderEnabled; }
    public boolean isAutoIoEnabled() { return autoIoEnabled; }
    public ItemStackHandler getControllerUpgradeHandler() { return controllerUpgradeHandler; }
    public ItemStackHandler getGlobalUpgradeHandler() { return globalUpgradeHandler; }
    public boolean hasQuickInstallUpgrade() {
        for (int slot = 0; slot < controllerUpgradeHandler.getSlots(); slot++) {
            if (controllerUpgradeHandler.getStackInSlot(slot)
                    .is(JDTEItems.GREENHOUSE_MATRIX_QUICK_INSTALL_UPGRADE.get())) return true;
        }
        return false;
    }
    public boolean hasAEOutputUpgrade() { return !getAEOutputUpgrade().isEmpty(); }
    public ItemStack getAEOutputUpgrade() {
        for (int slot = 0; slot < controllerUpgradeHandler.getSlots(); slot++) {
            ItemStack stack = controllerUpgradeHandler.getStackInSlot(slot);
            if (UpgradeHelper.isUpgrade(stack, UpgradeType.AE_OUTPUT)) return stack;
        }
        return ItemStack.EMPTY;
    }
    public AEOutputManager.MatrixState getAEOutputState() { return aeOutputState; }
    public boolean isGlobalUpgradeBufferEmpty() {
        for (int slot = 0; slot < globalUpgradeHandler.getSlots(); slot++) {
            if (!globalUpgradeHandler.getStackInSlot(slot).isEmpty()) return false;
        }
        return true;
    }
    public int getQueuedUpgradeCount() {
        int count = 0;
        for (int slot = 0; slot < globalUpgradeHandler.getSlots(); slot++) {
            count += globalUpgradeHandler.getStackInSlot(slot).getCount();
        }
        return count;
    }
    public String getError() { return error; }
    public int getGreenhouseCount() { return greenhouses.size(); }
    public List<BlockPos> getAutoCraftingPages() { return autoCraftingPages; }
    public int getAutoCraftingPageCount() { return autoCraftingPages.size(); }
    public int getAutoCraftingInvalidMask(int page) {
        if (!(level instanceof ServerLevel serverLevel)) return 0;
        return autoCraftingCatalog(serverLevel).invalidMask(page);
    }
    @Nullable public GreenhouseMatrixPatternItemHandler getAutoCraftingPatternHandler(int page) {
        if (level == null || page < 0 || page >= autoCraftingPages.size()) return null;
        return level.getBlockEntity(autoCraftingPages.get(page)) instanceof GreenhouseMatrixAutoCraftingBE blockEntity
                ? blockEntity.patterns() : null;
    }
    public ContainerData getMatrixData() { return data; }
    public GreenhouseMatrixSimulation getSimulation() { return simulation; }
    public GreenhouseMatrixOutputBuffer getOutputBuffer() { return outputBuffer; }
    public IItemHandler getInputHandler() { return formed ? inputHandler : null; }
    public IItemHandler getOutputHandler() { return formed ? outputBuffer.itemView() : null; }
    public IFluidHandler getFluidHandler() { return formed ? fluidHandler : null; }
    public IEnergyStorage getEnergyHandler() { return formed ? energyHandler : null; }
    private int count(GreenhouseMatrixEnhancement type) { return enhancements.getOrDefault(type, 0); }
    private static int errorCode(String error) {
        return switch (error) {
            case "controller_count" -> 1;
            case "size" -> 2;
            case "unloaded" -> 3;
            case "open_shell", "disconnected" -> 4;
            case "invalid_interior" -> 5;
            case "no_greenhouse" -> 6;
            case "missing_port" -> 7;
            case "conflicting_crafting_enhancements" -> 8;
            default -> 0;
        };
    }

    @Override public Component getDisplayName() { return Component.translatable("block.jdte.greenhouse_matrix_controller"); }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GreenhouseMatrixContainer(id, inventory, worldPosition, this);
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("enabled", enabled);
        tag.putBoolean("renderEnabled", renderEnabled);
        tag.putBoolean("autoIoEnabled", autoIoEnabled);
        tag.putBoolean("formed", formed);
        tag.putString("error", error);
        tag.putLong("min", min.asLong());
        tag.putLong("max", max.asLong());
        writePositions(tag, "greenhouses", greenhouses);
        writePositions(tag, "ports", ports);
        writePositions(tag, "autoCraftingPages", autoCraftingPages);
        tag.put("controllerUpgrades", controllerUpgradeHandler.serializeNBT(provider));
        tag.put("globalUpgradeBuffer", globalUpgradeHandler.serializeNBT(provider));
        tag.put("matrixOutputBuffer", outputBuffer.save(provider));
        tag.put("matrixSimulation", simulation.save(provider));
        tag.putInt("autoCraftingRoundRobinCursor", autoCraftingRoundRobinCursor);
        if (accelerationClock.pendingTicks() > 0L) {
            tag.putLong("acceleratedSimulationTicks", accelerationClock.pendingTicks());
        }
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        List<BlockPos> oldMembers = greenhouses;
        super.loadAdditional(tag, provider);
        loadingUpgradeInventory = true;
        enabled = !tag.contains("enabled") || tag.getBoolean("enabled");
        renderEnabled = !tag.contains("renderEnabled") || tag.getBoolean("renderEnabled");
        autoIoEnabled = !tag.contains("autoIoEnabled") || tag.getBoolean("autoIoEnabled");
        formed = tag.getBoolean("formed");
        error = tag.getString("error");
        min = tag.contains("min") ? BlockPos.of(tag.getLong("min")) : BlockPos.ZERO;
        max = tag.contains("max") ? BlockPos.of(tag.getLong("max")) : BlockPos.ZERO;
        greenhouses = readPositions(tag, "greenhouses");
        ports = readPositions(tag, "ports");
        autoCraftingPages = readPositions(tag, "autoCraftingPages");
        invalidateAutoCraftingCatalog();
        invalidateCapabilitySnapshot();
        if (tag.contains("controllerUpgrades")) {
            FixedSizeItemStackHandlerSerialization.deserialize(controllerUpgradeHandler, provider,
                    tag.getCompound("controllerUpgrades"), CONTROLLER_UPGRADE_SLOTS);
        }
        if (tag.contains("globalUpgradeBuffer")) {
            FixedSizeItemStackHandlerSerialization.deserialize(globalUpgradeHandler, provider,
                    tag.getCompound("globalUpgradeBuffer"), GLOBAL_UPGRADE_SLOTS);
        }
        if (tag.contains("matrixOutputBuffer")) {
            outputBuffer.load(tag.getCompound("matrixOutputBuffer"), provider,
                    warning -> org.slf4j.LoggerFactory.getLogger(GreenhouseMatrixControllerBE.class)
                            .warn("{} at matrix controller {}", warning, worldPosition));
        }
        if (tag.contains("matrixSimulation")) {
            simulation.load(tag.getCompound("matrixSimulation"), provider,
                    warning -> org.slf4j.LoggerFactory.getLogger(GreenhouseMatrixControllerBE.class)
                            .warn("{} at matrix controller {}", warning, worldPosition));
        }
        accelerationClock.restore(tag.getLong("acceleratedSimulationTicks"));
        autoCraftingRoundRobinCursor = Math.max(0, tag.getInt("autoCraftingRoundRobinCursor"));
        loadingUpgradeInventory = false;
        if (level != null && level.isClientSide()) GreenhouseMatrixRenderRegistry.replace(level, oldMembers, greenhouses, renderEnabled);
    }

    @Override public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide()) {
            GreenhouseMatrixRenderRegistry.replace(level, List.of(), greenhouses, renderEnabled);
        } else if (formed && level instanceof ServerLevel serverLevel) {
            for (BlockPos pos : greenhouses) claimMember(serverLevel, pos);
            validationTicker = 99;
        }
    }

    @Override public void setRemoved() {
        if (level != null && level.isClientSide()) GreenhouseMatrixRenderRegistry.replace(level, greenhouses, List.of(), true);
        formed = false;
        accelerationClock.clear();
        invalidateCapabilitySnapshot();
        if (level instanceof ServerLevel serverLevel) {
            for (BlockPos pos : greenhouses) {
                GreenhouseMatrixRuntime.remove(serverLevel, pos, worldPosition);
                releaseMember(serverLevel, pos);
            }
        }
        super.setRemoved();
    }

    private void claimMember(ServerLevel serverLevel, BlockPos pos) {
        if (getLoadedMember(serverLevel, pos) instanceof GreenhouseMatrixMember member) {
            member.claimMatrix(worldPosition);
        }
    }

    private void releaseMember(ServerLevel serverLevel, BlockPos pos) {
        if (getLoadedMember(serverLevel, pos) instanceof GreenhouseMatrixMember member) {
            member.releaseMatrix(worldPosition);
        }
    }

    private static BlockEntity getLoadedMember(ServerLevel serverLevel, BlockPos pos) {
        return GreenhouseMatrixMemberLookup.unlessServerStopped(serverLevel.getServer().isStopped(), () -> {
            LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            return chunk == null ? null : chunk.getBlockEntity(pos);
        });
    }

    private void tickSimulation(ServerLevel serverLevel) {
        long gameTime = serverLevel.getGameTime();
        if (!enabled) {
            lastSimulationGameTime = gameTime;
            return;
        }
        if (lastSimulationGameTime == Long.MIN_VALUE || gameTime < lastSimulationGameTime) {
            lastSimulationGameTime = gameTime;
            return;
        }
        int interval = JDTEConfig.COMMON.greenhouseSettlementInterval.get();
        long elapsed = gameTime - lastSimulationGameTime;
        long settlements = elapsed / interval;
        if (settlements <= 0L) return;
        long elapsedTicks = saturatingMultiply(settlements, interval);
        lastSimulationGameTime += elapsedTicks;
        advanceSimulation(serverLevel, elapsedTicks, gameTime);
    }

    @Override
    public void accumulateAcceleratedTicks(int ticks) {
        if (formed && enabled && accelerationClock.add(ticks)) setChanged();
    }

    @Override
    public void flushAcceleratedTicks() {
        if (!formed || !enabled || !(level instanceof ServerLevel serverLevel)) {
            accelerationClock.clear();
            return;
        }
        long elapsedTicks = accelerationClock.takeCompleteTicks(
                JDTEConfig.COMMON.greenhouseSettlementInterval.get());
        if (elapsedTicks > 0L) {
            advanceSimulation(serverLevel, elapsedTicks, serverLevel.getGameTime());
        }
    }

    private void advanceSimulation(ServerLevel serverLevel, long elapsedTicks, long gameTime) {
        int interval = JDTEConfig.COMMON.greenhouseSettlementInterval.get();
        long settlements = elapsedTicks / interval;
        if (settlements <= 0L) return;
        simulation.advanceWork(elapsedTicks,
                saturatingMultiply(JDTEConfig.COMMON.greenhouseMaxHarvestsPerSettlementV2.get(), settlements),
                JDTEConfig.COMMON.greenhouseMaxPendingWork.get());
        settleSimulation(serverLevel, gameTime);
    }

    private void settleSimulation(ServerLevel serverLevel, long gameTime) {
        GreenhouseMatrixCapabilitySnapshot resources = capabilitySnapshot();
        long availableEnergy = resources.energyStoredLong();
        boolean changed = false;
        for (GreenhouseMatrixProductionGroup group : simulation.groups()) {
            GreenhouseMatrixProductionProfile profile = group.profile();
            long candidate = group.pendingHarvests();
            if (candidate <= 0L) continue;
            if (!profile.creative()) {
                if (profile.fluidPerHarvest() > 0) candidate = Math.min(candidate,
                        resources.fluidStoredLong(profile.fluid()) / profile.fluidPerHarvest());
                if (profile.energyPerHarvest() > 0) candidate = Math.min(candidate,
                        availableEnergy / profile.energyPerHarvest());
            }
            if (candidate <= 0L) continue;

            RandomSource random = RandomSource.create(serverLevel.getSeed() ^ worldPosition.asLong()
                    ^ gameTime ^ profile.hashCode());
            GreenhouseMatrixDropGenerator.Result generated = GreenhouseMatrixDropGenerator.generate(
                    serverLevel, profile, candidate,
                    JDTEConfig.COMMON.greenhouseMatrixDynamicSamplesPerGroup.get(), random);
            if (generated.drops().isEmpty() || outputBuffer.insertBatch(generated.drops(), true) != 0L) continue;

            long fluidCost = profile.creative() ? 0L : saturatingMultiply(candidate, profile.fluidPerHarvest());
            long energyCost = profile.creative() ? 0L : saturatingMultiply(candidate, profile.energyPerHarvest());
            if (resources.drainFluid(profile.fluid(), fluidCost) != fluidCost
                    || resources.extractEnergy(energyCost) != energyCost) {
                invalidateCapabilitySnapshot();
                break;
            }
            if (outputBuffer.insertBatch(generated.drops(), false) != 0L) {
                throw new IllegalStateException("Simulated matrix output changed after successful preflight");
            }
            availableEnergy -= energyCost;
            group.consumeHarvests(candidate);
            changed = true;
        }
        if (!autoCraftingPages.isEmpty()) {
            GreenhouseMatrixAutoCraftingProcessor.Result crafting = GreenhouseMatrixAutoCraftingProcessor.process(
                    outputBuffer, autoCraftingCatalog(serverLevel).recipes(), autoCraftingRoundRobinCursor);
            if (crafting.changed()) {
                autoCraftingRoundRobinCursor = crafting.nextCursor();
                changed = true;
            }
        }
        if (changed) setChanged();
    }

    private static long saturatingMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) return 0L;
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        tag.remove("matrixOutputBuffer");
        tag.remove("matrixSimulation");
        tag.remove("acceleratedSimulationTicks");
        return tag;
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet,
                                       HolderLookup.Provider provider) {
        CompoundTag tag = packet.getTag();
        if (tag != null) loadAdditional(tag, provider);
    }

    private static void writePositions(CompoundTag tag, String key, List<BlockPos> positions) {
        ListTag list = new ListTag();
        for (BlockPos pos : positions) list.add(LongTag.valueOf(pos.asLong()));
        tag.put(key, list);
    }

    private static List<BlockPos> readPositions(CompoundTag tag, String key) {
        ListTag list = tag.getList(key, 4);
        List<BlockPos> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) result.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
        return List.copyOf(result);
    }

    private void onUpgradeInventoryChanged() {
        if (!loadingUpgradeInventory) setChanged();
    }

    private void onControllerUpgradeInventoryChanged() {
        if (loadingUpgradeInventory) return;
        setChanged();
        if (formed && level instanceof ServerLevel serverLevel) {
            GreenhouseMatrixRuntime.Effects effects = new GreenhouseMatrixRuntime.Effects(worldPosition, enabled,
                    count(GreenhouseMatrixEnhancement.SPEED), count(GreenhouseMatrixEnhancement.EFFICIENCY),
                    count(GreenhouseMatrixEnhancement.SEED_CONVERSION), count(GreenhouseMatrixEnhancement.ESSENCE_CONVERSION),
                    hasAEOutputUpgrade());
            for (BlockPos pos : greenhouses) GreenhouseMatrixRuntime.put(serverLevel, pos, effects);
        }
    }

    private static boolean isGreenhouseUpgrade(UpgradeType type) {
        return switch (type) {
            case CAPACITY, FLUID, OVERCLOCK, CREATIVE, FORTUNE, AE_OUTPUT,
                    ESSENCE_CONVERSION, SEED_CONVERSION -> true;
            default -> false;
        };
    }

    private void distributeQueuedUpgrades() {
        if (!(level instanceof ServerLevel) || greenhouses.isEmpty() || isGlobalUpgradeBufferEmpty()) return;
        List<BaseMachineBE> targets = new ArrayList<>();
        for (BlockPos pos : greenhouses) {
            if (level.getBlockEntity(pos) instanceof GreenhouseBE greenhouse) targets.add(greenhouse);
            else if (level.getBlockEntity(pos) instanceof LargeGreenhouseBE greenhouse) targets.add(greenhouse);
        }
        if (targets.isEmpty()) return;

        int budget = UPGRADE_INSTALL_BUDGET;
        while (budget-- > 0) {
            boolean moved = false;
            for (int offset = 0; offset < GLOBAL_UPGRADE_SLOTS; offset++) {
                int sourceSlot = (upgradeSourceCursor + offset) % GLOBAL_UPGRADE_SLOTS;
                if (tryInstallOne(sourceSlot, targets)) {
                    upgradeSourceCursor = (sourceSlot + 1) % GLOBAL_UPGRADE_SLOTS;
                    moved = true;
                    break;
                }
            }
            if (!moved) break;
        }
    }

    private boolean tryInstallOne(int sourceSlot, List<BaseMachineBE> targets) {
        ItemStack queued = globalUpgradeHandler.getStackInSlot(sourceSlot);
        if (queued.isEmpty()) return false;
        ItemStack one = queued.copyWithCount(1);
        int start = Math.floorMod(upgradeTargetCursor, targets.size());
        for (int offset = 0; offset < targets.size(); offset++) {
            BaseMachineBE target = targets.get((start + offset) % targets.size());
            UpgradeItemStackHandler upgrades = UpgradeHelper.getUpgradeHandler(target);
            for (int targetSlot = 0; targetSlot < upgrades.getSlots(); targetSlot++) {
                if (!upgrades.insertItem(targetSlot, one, true).isEmpty()) continue;
                ItemStack extracted = globalUpgradeHandler.extractItem(sourceSlot, 1, false);
                if (extracted.isEmpty()) return false;
                ItemStack remainder = upgrades.insertItem(targetSlot, extracted, false);
                if (!remainder.isEmpty()) {
                    globalUpgradeHandler.insertItem(sourceSlot, remainder, false);
                    return false;
                }
                upgradeTargetCursor = (start + offset + 1) % targets.size();
                return true;
            }
        }
        return false;
    }

    public void dropUpgradeContents() {
        if (level == null || level.isClientSide()) return;
        dropHandler(controllerUpgradeHandler);
        dropHandler(globalUpgradeHandler);
        for (GreenhouseMatrixDropGenerator.Drop drop : outputBuffer.drainAll()) {
            long remaining = drop.amount();
            while (remaining > 0L) {
                int amount = (int) Math.min(Integer.MAX_VALUE, remaining);
                Block.popResource(level, worldPosition, drop.stack().copyWithCount(amount));
                remaining -= amount;
            }
        }
    }

    private void dropHandler(ItemStackHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) Block.popResource(level, worldPosition, stack.copy());
            handler.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    private GreenhouseMatrixCapabilitySnapshot capabilitySnapshot() {
        if (level == null || !formed) return GreenhouseMatrixCapabilitySnapshot.EMPTY;
        if (capabilitySnapshot != GreenhouseMatrixCapabilitySnapshot.EMPTY) return capabilitySnapshot;
        capabilitySnapshot = GreenhouseMatrixCapabilitySnapshot.create(greenhouses, pos -> {
            BlockEntity member = level.getBlockEntity(pos);
            if (member instanceof GreenhouseBE greenhouse) {
                IItemHandler items = greenhouse.getAutomationItemHandler();
                return new GreenhouseMatrixCapabilitySnapshot.MachineTarget(items,
                        0, GreenhouseBE.INPUT_SLOTS,
                        GreenhouseBE.OUTPUT_START_SLOT, items.getSlots(),
                        greenhouse.getFluidTank(), greenhouse.getEnergyStorage());
            }
            if (member instanceof LargeGreenhouseBE greenhouse) {
                IItemHandler items = greenhouse.getAutomationItemHandler();
                return new GreenhouseMatrixCapabilitySnapshot.MachineTarget(items,
                        0, LargeGreenhouseBE.INPUT_SLOTS,
                        LargeGreenhouseBE.OUTPUT_START_SLOT, items.getSlots(),
                        greenhouse.getFluidTank(), greenhouse.getEnergyStorage());
            }
            return null;
        });
        return capabilitySnapshot;
    }

    private void invalidateCapabilitySnapshot() {
        capabilitySnapshot = GreenhouseMatrixCapabilitySnapshot.EMPTY;
    }

    private final class MatrixItemHandler implements IItemHandler {
        private final boolean input;
        private MatrixItemHandler(boolean input) { this.input = input; }
        @Override public int getSlots() { return capabilitySnapshot().itemSlots(input); }
        private GreenhouseMatrixCapabilitySnapshot.ItemTarget target(int slot) {
            return capabilitySnapshot().itemTarget(input, slot);
        }
        @Override public ItemStack getStackInSlot(int slot) {
            GreenhouseMatrixCapabilitySnapshot.ItemTarget target = target(slot);
            return target == null ? ItemStack.EMPTY : target.handler().getStackInSlot(target.slot());
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!input) return stack;
            GreenhouseMatrixCapabilitySnapshot.ItemTarget target = target(slot);
            return target == null ? stack : target.handler().insertItem(target.slot(), stack, simulate);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (input) return ItemStack.EMPTY;
            GreenhouseMatrixCapabilitySnapshot.ItemTarget target = target(slot);
            return target == null ? ItemStack.EMPTY : target.handler().extractItem(target.slot(), amount, simulate);
        }
        @Override public int getSlotLimit(int slot) {
            GreenhouseMatrixCapabilitySnapshot.ItemTarget target = target(slot);
            return target == null ? 0 : target.handler().getSlotLimit(target.slot());
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            if (!input) return false;
            GreenhouseMatrixCapabilitySnapshot.ItemTarget target = target(slot);
            return target != null && target.handler().isItemValid(target.slot(), stack);
        }
    }

    private final class MatrixFluidHandler implements IFluidHandler {
        @Override public int getTanks() { return capabilitySnapshot().fluidTanks(); }
        @Override public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? capabilitySnapshot().fluidInTank() : FluidStack.EMPTY;
        }
        @Override public int getTankCapacity(int tank) {
            return tank == 0 ? capabilitySnapshot().fluidCapacity() : 0;
        }
        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && capabilitySnapshot().isFluidValid(stack);
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            return capabilitySnapshot().fill(resource, action);
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    }

    private final class MatrixEnergyHandler implements IEnergyStorage {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            return capabilitySnapshot().receiveEnergy(maxReceive, simulate);
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return capabilitySnapshot().energyStored(); }
        @Override public int getMaxEnergyStored() { return capabilitySnapshot().maxEnergyStored(); }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }
}
