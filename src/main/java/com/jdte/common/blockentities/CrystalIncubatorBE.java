package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.jdte.common.integrations.JustDynaThingsCrystalIntegration;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTETags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class CrystalIncubatorBE extends TimeAcceleratorBE implements ExtendedUpgradeMachine, PoweredMachineBE {
    private static final double AE2_GROWTH_ACCELERATOR_INTERVAL_TICKS = 10.0D;
    private static final double REGULAR_GROWTH_REFERENCE_MULTIPLIER = 8.0D;
    public static final int OUTPUT_SLOTS = 9;
    private final ItemStackHandler outputHandler = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final List<BlockPos> buddingPositions = new ArrayList<>();
    private final List<BlockPos> regularBuddingPositions = new ArrayList<>();
    private final List<BlockPos> dynaBuddingPositions = new ArrayList<>();
    private int scanIndex;
    private int scanVolume = -1;
    private int cacheAge;
    private int regularGrowthCursor;
    private int dynaGrowthCursor;
    private int harvestCursor;
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
    public ItemStackHandler getMachineHandler() {
        return outputHandler;
    }

    @Override
    public int getEffectiveMultiplier() {
        return UpgradeHelper.hasOverclock(this)
                ? JDTEConfig.COMMON.crystalIncubatorOverclockMultiplier.get()
                : multiplier;
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
        return getEnergyCost(getEffectiveMultiplier());
    }

    @Override
    protected double getFluidCostPerTick(int multiplier) {
        return Math.max(0.0D, multiplier * Config.TIMEWAND_FLUID_COST.get()
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

        int multiplier = getEffectiveMultiplier();
        int fluidCost = getFluidDrainAmount(multiplier);
        int energyCost = getEnergyCost(multiplier);
        if (!hasResources(fluidCost, energyCost)) {
            return;
        }

        boolean processed = growCachedBudding(serverLevel, multiplier, energyCost, fluidCost);
        if (processed) {
            consumeResources(fluidCost, energyCost);
            harvestMatureCrystals(serverLevel);
        }
    }

    @Override
    protected int getEnergyCost(int multiplier) {
        double cost = multiplier * (double) Config.TIMEWAND_RF_COST.get()
                * JDTEConfig.COMMON.crystalIncubatorEnergyCostMultiplier.get();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0.0D, Math.ceil(cost)));
    }

    @Override
    protected boolean hasResources(int fluidCost, int energyCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            return true;
        }
        return super.hasResources(fluidCost, energyCost)
                && energyStorage.extractEnergy(energyCost, true) == energyCost;
    }

    @Override
    protected void consumeResources(int fluidCost, int energyCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            return;
        }
        super.consumeResources(fluidCost, energyCost);
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
            regularBuddingPositions.clear();
            dynaBuddingPositions.clear();
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
            BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
            if (isDynaBudding(blockEntity)) {
                buddingPositions.add(pos.immutable());
                dynaBuddingPositions.add(pos.immutable());
            } else if (state.is(JDTETags.CRYSTAL_INCUBATOR_BUDDING_BLOCKS)) {
                buddingPositions.add(pos.immutable());
                regularBuddingPositions.add(pos.immutable());
            }
        }
        regularGrowthCursor = normalizeCursor(regularGrowthCursor, regularBuddingPositions);
        dynaGrowthCursor = normalizeCursor(dynaGrowthCursor, dynaBuddingPositions);
        harvestCursor = normalizeCursor(harvestCursor);
    }

    private boolean growCachedBudding(ServerLevel serverLevel, int multiplier,
                                      int reservedEnergy, int reservedFluid) {
        if (buddingPositions.isEmpty()) {
            return false;
        }
        boolean processed = false;
        int motherBatch = JDTEConfig.COMMON.crystalIncubatorMotherBatchSize.get();
        int dynaMothers = Math.min(dynaBuddingPositions.size(), motherBatch);
        for (int i = 0; i < dynaMothers; i++) {
            BlockPos pos = dynaBuddingPositions.get(dynaGrowthCursor++ % dynaBuddingPositions.size());
            if (!serverLevel.hasChunkAt(pos)) {
                continue;
            }
            BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
            if (isDynaBudding(blockEntity)) {
                int attempts = Math.min(JDTEConfig.COMMON.crystalIncubatorDynaGrowthAttempts.get(), Math.max(1, multiplier / 4));
                processed |= JustDynaThingsCrystalIntegration.grow(
                        blockEntity, serverLevel.random, attempts,
                        energyStorage, fluidTank, reservedEnergy, reservedFluid,
                        UpgradeHelper.hasCreativeUpgrade(this)) > 0;
            }
        }

        int regularMothers = Math.min(regularBuddingPositions.size(), motherBatch);
        int activeRegularMothers = 0;
        for (int i = 0; i < regularMothers; i++) {
            BlockPos pos = regularBuddingPositions.get(
                    regularGrowthCursor++ % regularBuddingPositions.size());
            if (!serverLevel.hasChunkAt(pos)) {
                continue;
            }
            BlockState state = serverLevel.getBlockState(pos);
            if (state.is(JDTETags.CRYSTAL_INCUBATOR_BUDDING_BLOCKS) && state.isRandomlyTicking()) {
                activeRegularMothers++;
            }
        }
        double equivalentAccelerators = JDTEConfig.COMMON.crystalIncubatorRegularGrowthAcceleratorsAt8x.get();
        pendingRandomTicks += activeRegularMothers * multiplier * equivalentAccelerators
                / (REGULAR_GROWTH_REFERENCE_MULTIPLIER * AE2_GROWTH_ACCELERATOR_INTERVAL_TICKS);
        int operationBudget = JDTEConfig.COMMON.crystalIncubatorGrowthOperationsPerTick.get();
        int operations = Math.min((int) Math.floor(pendingRandomTicks), operationBudget);
        for (int i = 0; i < operations; i++) {
            for (int checked = 0; checked < regularBuddingPositions.size(); checked++) {
                BlockPos pos = regularBuddingPositions.get(
                        regularGrowthCursor++ % regularBuddingPositions.size());
                if (!serverLevel.hasChunkAt(pos)) {
                    continue;
                }
                BlockState state = serverLevel.getBlockState(pos);
                if (state.is(JDTETags.CRYSTAL_INCUBATOR_BUDDING_BLOCKS) && state.isRandomlyTicking()) {
                    state.randomTick(serverLevel, pos, serverLevel.random);
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
            BlockEntity mother = serverLevel.getBlockEntity(motherPos);
            for (Direction direction : Direction.values()) {
                BlockPos crystalPos = motherPos.relative(direction);
                BlockState crystalState = serverLevel.getBlockState(crystalPos);
                if (crystalState.is(JDTETags.CRYSTAL_INCUBATOR_HARVESTABLE_CRYSTALS)
                        || isDynaMatureCrystal(mother, crystalState)) {
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
        ItemStackHandler simulated = new ItemStackHandler(OUTPUT_SLOTS);
        for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
            simulated.setStackInSlot(slot, outputHandler.getStackInSlot(slot).copy());
        }
        for (ItemStack drop : drops) {
            if (!ItemHandlerHelper.insertItemStacked(simulated, drop.copy(), false).isEmpty()) {
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
            outputHandler.setStackInSlot(slot, simulated.getStackInSlot(slot));
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

    private boolean isDynaBudding(BlockEntity blockEntity) {
        return ModList.get().isLoaded("justdynathings")
                && JustDynaThingsCrystalIntegration.isBudding(blockEntity);
    }

    private boolean isDynaMatureCrystal(BlockEntity budding, BlockState state) {
        return ModList.get().isLoaded("justdynathings")
                && JustDynaThingsCrystalIntegration.isMatureCrystal(budding, state);
    }

    private int normalizeCursor(int cursor) {
        return buddingPositions.isEmpty() ? 0 : cursor % buddingPositions.size();
    }

    private static int normalizeCursor(int cursor, List<BlockPos> positions) {
        return positions.isEmpty() ? 0 : cursor % positions.size();
    }

    @Override
    public boolean isDefaultSettings() {
        if (!super.isDefaultSettings()
                || energyStorage.getEnergyStored() != 0
                || multiplier != JDTEConfig.COMMON.crystalIncubatorMaxMultiplier.get()) {
            return false;
        }
        for (int slot = 0; slot < outputHandler.getSlots(); slot++) {
            if (!outputHandler.getStackInSlot(slot).isEmpty()) {
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
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", outputHandler.serializeNBT(provider));
        tag.putDouble("pendingRandomTicks", pendingRandomTicks);
        tag.putInt("multiplier", multiplier);
        tag.putInt("energy", energyStorage.getEnergyStored());
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) {
            outputHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        }
        pendingRandomTicks = tag.getDouble("pendingRandomTicks");
        multiplier = tag.contains("multiplier")
                ? Math.max(1, Math.min(tag.getInt("multiplier"), JDTEConfig.COMMON.crystalIncubatorMaxMultiplier.get()))
                : JDTEConfig.COMMON.crystalIncubatorMaxMultiplier.get();
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
        loadAreaSettings(tag);
        scanIndex = scanVolume;
    }
}
