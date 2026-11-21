package com.jdte.common.blockentities;
import com.jdte.common.manager.ExtendedTimeAccelerationManager;

import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTETags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrystalIncubatorBE extends TimeAcceleratorBE implements ExtendedUpgradeMachine, PoweredMachineBE, CoalescedAcceleratedMachine {
    private static final double AE2_GROWTH_ACCELERATOR_INTERVAL_TICKS = 10.0D;
    private static final double REGULAR_GROWTH_REFERENCE_MULTIPLIER = 8.0D;
    public static final int OUTPUT_SLOTS = 9;
    private final MachineInventoryHandler outputHandler = new MachineInventoryHandler(OUTPUT_SLOTS) {
        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return resource.isEmpty();
        }

        @Override
        protected void onContentsChanged(int slot, ItemStack previous) {
            setChanged();
        }
    };
    private final List<BlockPos> buddingPositions = new ArrayList<>();
    private int scanIndex;
    private int scanVolume = -1;
    private int cacheAge;
    private int regularGrowthCursor;
    private int harvestCursor;
    private final Map<Block, Boolean> matureCrystalCache = new HashMap<>();
    private double pendingRandomTicks;
    private int multiplier;
    private final MachineEnergyStorage energyStorage;
    private final PoweredMachineContainerData poweredMachineData;

    public CrystalIncubatorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.CRYSTAL_INCUBATOR.get(), pos, state);
        MACHINE_SLOTS = OUTPUT_SLOTS;
        multiplier = JDTEConfig.COMMON.crystalIncubatorMaxMultiplier.get();
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
    }

    @Override
    public ItemStacksResourceHandler getMachineHandler() {
        return outputHandler;
    }

    @Override
    public int getEffectiveMultiplier() {
        if (UpgradeHelper.hasUltimateOverclock(this)) {
            return JDTEConfig.COMMON.crystalIncubatorOverclockMultiplier.get() * 10;
        }
        return UpgradeHelper.hasOverclock(this)
                ? JDTEConfig.COMMON.crystalIncubatorOverclockMultiplier.get()
                : multiplier;
    }

    @Override
    public int getAccelerationWorkTicks(int effectiveMultiplier) {
        return TimeAcceleratorTiming.batchWorkTicks(
                effectiveMultiplier,
                JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.get());
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = Math.max(1, Math.min(multiplier, JDTEConfig.COMMON.crystalIncubatorMaxMultiplier.get()));
        markDirtyClient();
    }

    @Override
    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.crystalIncubatorFluidCapacity.get());
    }

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.crystalIncubatorEnergyCapacity.get());
    }

    @Override
    public MachineEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public ContainerData getContainerData() {
        return poweredMachineData;
    }

    @Override
    public int getStandardEnergyCost() {
        return getEnergyCost(getAccelerationWorkTicks(getEffectiveMultiplier()));
    }

    @Override
    protected double getFluidCostPerTick(int workTicks) {
        return Math.max(0.0D, workTicks * Config.TIMEWAND_FLUID_COST.get()
                * JDTEConfig.COMMON.crystalIncubatorFluidCostMultiplier.get() / 600.0D);
    }

    @Override
    protected void handleAccelerationTick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        updateBuddingCache(serverLevel);
        if (buddingPositions.isEmpty()) {
            return;
        }

        harvestMatureCrystals(serverLevel);

        ExtendedTimeAccelerationManager.PreparedAcceleration prepared =
                ExtendedTimeAccelerationManager.prepareAcceleration(this);
        if (!hasResources(prepared.fluidCost(), prepared.energyCost())) {
            return;
        }

        boolean processed = growCachedBudding(serverLevel, prepared.workTicks());
        if (processed) {
            ExtendedTimeAccelerationManager.consumePreparedResources(this, prepared);
            harvestMatureCrystals(serverLevel);
        }
    }

    protected int accumulatedAcceleratedTicks = 0;

    @Override
    public void accumulateAcceleratedTicks(int ticks) {
        if (ticks > 0) {
            accumulatedAcceleratedTicks = (int) Math.min(Integer.MAX_VALUE, (long) accumulatedAcceleratedTicks + ticks);
        }
    }

    @Override
    public void flushAcceleratedTicks() {
        int ticks = accumulatedAcceleratedTicks;
        accumulatedAcceleratedTicks = 0;
        if (ticks <= 0 || !isActiveRedstone() || !canRun() || !UpgradeHelper.mayRunWithUpgrades(this)) {
            return;
        }
        processAcceleratedTicks(ticks);
    }

    protected void processAcceleratedTicks(int ticks) {
        if (!(level instanceof ServerLevel serverLevel) || !hasOutputSpace()) {
            return;
        }
        updateBuddingCache(serverLevel);
        if (buddingPositions.isEmpty()) {
            return;
        }
        int maxCycles = Math.min(ticks, 64);
        for (int i = 0; i < maxCycles; i++) {
            harvestMatureCrystals(serverLevel);
            if (!hasOutputSpace()) {
                if (UpgradeHelper.hasAEOutputUpgrade(this)) {
                    com.jdte.common.manager.AEOutputManager.flush(serverLevel, this);
                }
                if (!hasOutputSpace()) {
                    break;
                }
            }
            ExtendedTimeAccelerationManager.PreparedAcceleration prepared =
                    ExtendedTimeAccelerationManager.prepareAcceleration(this);
            if (!hasResources(prepared.fluidCost(), prepared.energyCost())) {
                break;
            }
            boolean processed = growCachedBudding(serverLevel, prepared.workTicks());
            if (processed) {
                ExtendedTimeAccelerationManager.consumePreparedResources(this, prepared);
                harvestMatureCrystals(serverLevel);
                if (UpgradeHelper.hasAdvancedAEOutputUpgrade(this)) {
                    com.jdte.common.manager.AEOutputManager.flush(serverLevel, this);
                }
            } else {
                break;
            }
        }
    }

    private boolean hasOutputSpace() {
        for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
            ItemResource resource = outputHandler.getResource(slot);
            if (resource.isEmpty() || outputHandler.getAmountAsInt(slot) < outputHandler.getCapacityAsInt(slot, resource)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getEnergyCost(int workTicks) {
        double cost = workTicks * (double) Config.TIMEWAND_RF_COST.get()
                * JDTEConfig.COMMON.crystalIncubatorEnergyCostMultiplier.get();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0.0D, Math.ceil(cost)));
    }

    @Override
    public boolean hasResources(int fluidCost, int energyCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            return true;
        }
        return super.hasResources(fluidCost, energyCost)
                && energyStorage.extractEnergy(energyCost, true) == energyCost;
    }

    @Override
    public void consumeResources(int workTicks, int energyCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            return;
        }
        super.consumeResources(workTicks, energyCost);
        energyStorage.extractEnergy(energyCost, false);
    }

    private void updateBuddingCache(ServerLevel serverLevel) {
        cacheAge++;
        if (scanVolume >= 0 && scanIndex >= scanVolume
                && cacheAge < JDTEConfig.COMMON.crystalIncubatorCacheRefreshInterval.get()) {
            return;
        }
        AABB area = getAABB(getBlockPos());
        int minX = (int) Math.floor(area.minX);
        int minY = (int) Math.floor(area.minY);
        int minZ = (int) Math.floor(area.minZ);
        int sizeX = Math.max(1, (int) Math.ceil(area.maxX) - minX);
        int sizeY = Math.max(1, (int) Math.ceil(area.maxY) - minY);
        int sizeZ = Math.max(1, (int) Math.ceil(area.maxZ) - minZ);
        int volume = sizeX * sizeY * sizeZ;

        if (scanIndex >= scanVolume || scanVolume != volume) {
            buddingPositions.clear();
            scanIndex = 0;
            scanVolume = volume;
            cacheAge = 0;
        }

        int end = Math.min(scanVolume, scanIndex + JDTEConfig.COMMON.crystalIncubatorScanBatchSize.get());
        for (; scanIndex < end; scanIndex++) {
            int x = scanIndex % sizeX;
            int yz = scanIndex / sizeX;
            int z = yz % sizeZ;
            int y = yz / sizeZ;
            BlockPos pos = new BlockPos(minX + x, minY + y, minZ + z);
            if (!serverLevel.hasChunkAt(pos)) {
                continue;
            }
            BlockState state = serverLevel.getBlockState(pos);
            if (state.is(JDTETags.CRYSTAL_INCUBATOR_BUDDING_BLOCKS)) {
                buddingPositions.add(pos.immutable());
            }
        }
        regularGrowthCursor = normalizeCursor(regularGrowthCursor);
        harvestCursor = normalizeCursor(harvestCursor);
    }

    private boolean growCachedBudding(ServerLevel serverLevel, int workTicks) {
        if (buddingPositions.isEmpty()) {
            return false;
        }
        boolean processed = false;
        int motherBatch = JDTEConfig.COMMON.crystalIncubatorMotherBatchSize.get();
        int regularMothers = Math.min(buddingPositions.size(), motherBatch);
        int activeRegularMothers = 0;
        for (int i = 0; i < regularMothers; i++) {
            BlockPos pos = buddingPositions.get(regularGrowthCursor++ % buddingPositions.size());
            if (!serverLevel.hasChunkAt(pos)) {
                continue;
            }
            BlockState state = serverLevel.getBlockState(pos);
            if (state.is(JDTETags.CRYSTAL_INCUBATOR_BUDDING_BLOCKS) && state.isRandomlyTicking()) {
                activeRegularMothers++;
            }
        }
        double equivalentAccelerators = JDTEConfig.COMMON.crystalIncubatorRegularGrowthAcceleratorsAt8x.get();
        pendingRandomTicks += (double) activeRegularMothers * workTicks * equivalentAccelerators
                / (REGULAR_GROWTH_REFERENCE_MULTIPLIER * AE2_GROWTH_ACCELERATOR_INTERVAL_TICKS);
        int operationBudget = JDTEConfig.COMMON.crystalIncubatorGrowthOperationsPerTick.get();
        int operations = Math.min((int) Math.floor(pendingRandomTicks), operationBudget);
        for (int i = 0; i < operations; i++) {
            for (int checked = 0; checked < buddingPositions.size(); checked++) {
                BlockPos pos = buddingPositions.get(regularGrowthCursor++ % buddingPositions.size());
                if (!serverLevel.hasChunkAt(pos)) {
                    continue;
                }
                BlockState state = serverLevel.getBlockState(pos);
                if (state.is(JDTETags.CRYSTAL_INCUBATOR_BUDDING_BLOCKS) && state.isRandomlyTicking()) {
                    state.randomTick(serverLevel, pos, serverLevel.getRandom());
                    pendingRandomTicks -= 1.0D;
                    processed = true;
                    break;
                }
            }
        }
        pendingRandomTicks = Math.min(pendingRandomTicks, operationBudget);
        if (activeRegularMothers > 0) {
            processed = true;
        }
        return processed;
    }

    private void harvestMatureCrystals(ServerLevel serverLevel) {
        if (buddingPositions.isEmpty()) {
            return;
        }
        int budget = JDTEConfig.COMMON.crystalIncubatorHarvestOperationsPerTick.get();
        int mothers = Math.min(buddingPositions.size(), JDTEConfig.COMMON.crystalIncubatorMotherBatchSize.get());
        for (int i = 0; i < mothers && budget > 0; i++) {
            BlockPos motherPos = buddingPositions.get(harvestCursor++ % buddingPositions.size());
            if (!serverLevel.hasChunkAt(motherPos)) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos crystalPos = motherPos.relative(direction);
                BlockState crystalState = serverLevel.getBlockState(crystalPos);
                if (crystalState.is(JDTETags.CRYSTAL_INCUBATOR_HARVESTABLE_CRYSTALS)
                        && isMatureHarvestableCrystal(serverLevel, crystalState, crystalPos)) {
                    if (harvestCrystal(serverLevel, crystalPos, crystalState)) {
                        budget--;
                    }
                }
            }
        }
    }

    private boolean harvestCrystal(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        ItemStack tool = createFortuneTool(serverLevel);
        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, serverLevel.getBlockEntity(pos), getFakePlayer(serverLevel), tool);
        MachineInventoryHandler simulated = new MachineInventoryHandler(OUTPUT_SLOTS);
        for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
            simulated.setStack(slot, outputHandler.copyStack(slot));
        }
        for (ItemStack drop : drops) {
            if (!drop.isEmpty() && ResourceHandlerUtil.insertStacking(simulated,
                    ItemResource.of(drop), drop.getCount(), null) < drop.getCount()) {
                return false;
            }
        }

        BlockState replacement = state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED)
                ? Blocks.WATER.defaultBlockState()
                : Blocks.AIR.defaultBlockState();
        if (!serverLevel.setBlock(pos, replacement, Block.UPDATE_ALL)) {
            return false;
        }
        for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
            outputHandler.setStack(slot, simulated.copyStack(slot));
        }
        setChanged();
        return true;
    }

    private ItemStack createFortuneTool(ServerLevel serverLevel) {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        boolean precision = UpgradeHelper.countUpgrades(this, UpgradeType.PRECISION) > 0;
        int fortune = precision ? 0 : UpgradeHelper.countUpgrades(this, UpgradeType.FORTUNE);
        if (precision || fortune > 0) {
            ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            if (precision) {
                enchantments.set(serverLevel.registryAccess().holderOrThrow(Enchantments.SILK_TOUCH), 1);
            } else {
                enchantments.set(serverLevel.registryAccess().holderOrThrow(Enchantments.FORTUNE), fortune);
            }
            EnchantmentHelper.setEnchantments(tool, enchantments.toImmutable());
        }
        return tool;
    }

    private boolean isMatureHarvestableCrystal(ServerLevel serverLevel, BlockState state, BlockPos pos) {
        IntegerProperty ageProperty = findAgeProperty(state);
        if (ageProperty != null) {
            return state.getValue(ageProperty) >= ageProperty.getPossibleValues().size() - 1;
        }
        return matureCrystalCache.computeIfAbsent(state.getBlock(),
                block -> hasMatureCrystalDrops(serverLevel, state, pos));
    }

    @Nullable
    private static IntegerProperty findAgeProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty && property.getName().equals("age")) {
                return integerProperty;
            }
        }
        return null;
    }

    private boolean hasMatureCrystalDrops(ServerLevel serverLevel, BlockState state, BlockPos pos) {
        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos,
                serverLevel.getBlockEntity(pos), getFakePlayer(serverLevel),
                new ItemStack(Items.DIAMOND_PICKAXE));
        if (drops.isEmpty()) {
            return false;
        }
        for (ItemStack drop : drops) {
            if (!drop.is(Tags.Items.DUSTS)) {
                return true;
            }
        }
        return false;
    }

    private int normalizeCursor(int cursor) {
        return buddingPositions.isEmpty() ? 0 : cursor % buddingPositions.size();
    }

    @Override
    public boolean isDefaultSettings() {
        if (!super.isDefaultSettings()
                || energyStorage.getEnergyStored() != 0
                || multiplier != JDTEConfig.COMMON.crystalIncubatorMaxMultiplier.get()) {
            return false;
        }
        for (int slot = 0; slot < outputHandler.size(); slot++) {
            if (!outputHandler.getResource(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setAreaSettings(double xRadius, double yRadius, double zRadius,
                                int xOffset, int yOffset, int zOffset, boolean renderArea) {
        super.setAreaSettings(xRadius, yRadius, zRadius, xOffset, yOffset, zOffset, renderArea);
        scanIndex = scanVolume;
        cacheAge = JDTEConfig.COMMON.crystalIncubatorCacheRefreshInterval.get();
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putChild("inventory", outputHandler);
        tag.putDouble("pendingRandomTicks", pendingRandomTicks);
        tag.putInt("multiplier", multiplier);
        tag.putInt("energy", energyStorage.getEnergyStored());
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        tag.child("inventory").ifPresent(outputHandler::deserialize);
        pendingRandomTicks = tag.getDoubleOr("pendingRandomTicks", 0D);
        multiplier = tag.keySet().contains("multiplier")
                ? Math.max(1, Math.min(tag.getIntOr("multiplier", 0), JDTEConfig.COMMON.crystalIncubatorMaxMultiplier.get()))
                : JDTEConfig.COMMON.crystalIncubatorMaxMultiplier.get();
        tag.getInt("energy").ifPresent(energyStorage::setEnergy);
        loadAreaSettings(tag);
        scanIndex = scanVolume;
    }
}
