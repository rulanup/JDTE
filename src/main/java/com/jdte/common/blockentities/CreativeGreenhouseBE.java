package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.greenhouse.CreativeGreenhouseOutputCatalog;
import com.jdte.common.recipes.GreenhouseCropDefinition;
import com.jdte.common.recipes.GreenhouseCropResolver;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Exposes the configured greenhouse products as an inexhaustible catalog. Only
 * the four seed templates are persistent; outputs are rebuilt from recipes.
 */
public class CreativeGreenhouseBE extends BaseMachineBE implements ExtendedUpgradeMachine {
    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_START_SLOT = INPUT_SLOTS;
    public static final int MAX_OUTPUT_TYPES = 64;
    public static final int UPGRADE_SLOTS = 8;
    public static final int TOTAL_SLOTS = INPUT_SLOTS + MAX_OUTPUT_TYPES;
    public static final int BASE_ACTIVE_OUTPUT_TYPE_LIMIT = 16;
    public static final int OUTPUT_TYPES_PER_CAPACITY = 16;

    private final ItemStackHandler seedHandler = new ItemStackHandler(INPUT_SLOTS) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < INPUT_SLOTS && GreenhouseCropResolver.find(level, stack) != null;
        }

        @Override
        protected void onContentsChanged(int slot) {
            catalogDirty = true;
            setChanged();
            markDirtyClient();
        }
    };
    private final CreativeGreenhouseOutputCatalog outputCatalog =
            new CreativeGreenhouseOutputCatalog(MAX_OUTPUT_TYPES);
    /**
     * Menu slot packets write their server-authoritative snapshots through Slot#set on the client.
     * Keep those display-only values separate from the inexhaustible server catalog so output slots
     * remain immutable to every server-side caller.
     */
    private final ItemStackHandler clientOutputHandler = new ItemStackHandler(MAX_OUTPUT_TYPES);
    private final ItemStackHandler machineHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (slot >= 0 && slot < INPUT_SLOTS) return seedHandler.getStackInSlot(slot);
            if (isClientSide() && slot >= OUTPUT_START_SLOT && slot < TOTAL_SLOTS) {
                return clientOutputHandler.getStackInSlot(slot - OUTPUT_START_SLOT).copy();
            }
            return outputView().getStackInSlot(slot - OUTPUT_START_SLOT);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (slot >= 0 && slot < INPUT_SLOTS) {
                seedHandler.setStackInSlot(slot, stack);
            } else if (isClientSide() && slot >= OUTPUT_START_SLOT && slot < TOTAL_SLOTS) {
                clientOutputHandler.setStackInSlot(slot - OUTPUT_START_SLOT, stack);
            }
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot >= 0 && slot < INPUT_SLOTS ? seedHandler.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= 0 && slot < INPUT_SLOTS) return seedHandler.extractItem(slot, amount, simulate);
            if (isClientSide() && slot >= OUTPUT_START_SLOT && slot < TOTAL_SLOTS) {
                ItemStack prototype = clientOutputHandler.getStackInSlot(slot - OUTPUT_START_SLOT);
                return prototype.isEmpty() || amount <= 0 ? ItemStack.EMPTY
                        : prototype.copyWithCount(Math.min(amount, prototype.getMaxStackSize()));
            }
            return outputView().extractItem(slot - OUTPUT_START_SLOT, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot >= 0 && slot < INPUT_SLOTS) return seedHandler.getSlotLimit(slot);
            if (isClientSide() && slot >= OUTPUT_START_SLOT && slot < TOTAL_SLOTS) {
                ItemStack prototype = clientOutputHandler.getStackInSlot(slot - OUTPUT_START_SLOT);
                return prototype.isEmpty() ? 0 : prototype.getMaxStackSize();
            }
            return outputView().getSlotLimit(slot - OUTPUT_START_SLOT);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < INPUT_SLOTS && seedHandler.isItemValid(slot, stack);
        }

        private IItemHandler outputView() {
            return outputCatalog.itemView();
        }
    };
    private final IItemHandler automationItemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return TOTAL_SLOTS;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (slot >= 0 && slot < INPUT_SLOTS) return seedHandler.getStackInSlot(slot);
            return canAutomationExtract(slot, outputCatalog.distinctTypes())
                    ? outputCatalog.itemView().extractItem(slot - OUTPUT_START_SLOT, Integer.MAX_VALUE, true)
                    : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return canAutomationInsert(slot) ? seedHandler.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return canAutomationExtract(slot, outputCatalog.distinctTypes())
                    ? outputCatalog.itemView().extractItem(slot - OUTPUT_START_SLOT, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < INPUT_SLOTS ? seedHandler.getSlotLimit(slot)
                    : canAutomationExtract(slot, outputCatalog.distinctTypes())
                    ? outputCatalog.itemView().getSlotLimit(slot - OUTPUT_START_SLOT) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return canAutomationInsert(slot) && seedHandler.isItemValid(slot, stack);
        }
    };
    private final ContainerData greenhouseData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> isClientSide() ? syncedDistinctTypes : outputCatalog.distinctTypes();
                case 1 -> isClientSide() ? syncedActiveOutputLimit : getActiveOutputTypeLimit();
                case 2 -> isClientSide() ? syncedOverflow : catalogOverflow ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> syncedDistinctTypes = value;
                case 1 -> syncedActiveOutputLimit = value;
                case 2 -> syncedOverflow = value;
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    private long catalogRecipeGeneration = Long.MIN_VALUE;
    private int catalogUpgradeSignature = Integer.MIN_VALUE;
    private boolean catalogDirty = true;
    private boolean catalogOverflow;
    private int syncedDistinctTypes;
    private int syncedActiveOutputLimit = BASE_ACTIVE_OUTPUT_TYPE_LIMIT;
    private int syncedOverflow;

    public CreativeGreenhouseBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
    }

    public CreativeGreenhouseBE(BlockPos pos, BlockState state) {
        this(JDTEBlockEntities.CREATIVE_GREENHOUSE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        rebuildCatalogIfRequired();
    }

    @Override
    public void tickServer() {
        super.tickServer();
        rebuildCatalogIfRequired();
    }

    private void rebuildCatalogIfRequired() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        long recipeGeneration = GreenhouseCropResolver.cacheGeneration();
        int upgradeSignature = catalogUpgradeSignature();
        if (!catalogDirty && catalogRecipeGeneration == recipeGeneration
                && catalogUpgradeSignature == upgradeSignature) return;

        List<ItemStack> products = new ArrayList<>();
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            ItemStack seed = seedHandler.getStackInSlot(slot);
            GreenhouseCropDefinition definition = GreenhouseCropResolver.find(serverLevel, seed);
            if (seed.isEmpty() || definition == null) continue;

            List<ItemStack> outputs = definition.outputs();
            if (UpgradeHelper.hasSeedConversionUpgrade(this) && !outputs.isEmpty()) {
                outputs = GreenhouseEssenceConversionHelper.replaceSeeds(outputs, seed, outputs.getFirst());
            }
            if (UpgradeHelper.hasEssenceConversionUpgrade(this)) {
                outputs = GreenhouseEssenceConversionHelper.convert(serverLevel, outputs);
            }
            addDistinct(products, outputs);
        }

        catalogRecipeGeneration = recipeGeneration;
        catalogUpgradeSignature = upgradeSignature;
        catalogDirty = false;
        int activeLimit = getActiveOutputTypeLimit();
        catalogOverflow = products.size() > activeLimit;
        List<ItemStack> activeProducts = products.size() > activeLimit
                ? products.subList(0, activeLimit) : products;
        if (outputCatalog.replaceCatalog(activeProducts)
                == CreativeGreenhouseOutputCatalog.ReplaceResult.DISTINCT_TYPE_LIMIT_EXCEEDED) {
            catalogOverflow = true;
        }
        setChanged();
        markDirtyClient();
        for (int output = 0; output < outputCatalog.distinctTypes(); output++) {
            MachineOutputManager.submit(this, OUTPUT_START_SLOT + output);
        }
        AEOutputManager.refresh(this);
    }

    private int catalogUpgradeSignature() {
        int signature = UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY);
        if (UpgradeHelper.hasSeedConversionUpgrade(this)) signature |= 1 << 8;
        if (UpgradeHelper.hasEssenceConversionUpgrade(this)) signature |= 1 << 9;
        return signature;
    }

    private static void addDistinct(List<ItemStack> target, List<ItemStack> candidates) {
        for (ItemStack candidate : candidates) {
            if (candidate.isEmpty() || target.stream()
                    .anyMatch(existing -> ItemStack.isSameItemSameComponents(existing, candidate))) continue;
            target.add(candidate.copyWithCount(1));
        }
    }

    public static boolean isSupportedUpgrade(UpgradeType type) {
        return type == UpgradeType.CAPACITY || type == UpgradeType.OVERCLOCK || type == UpgradeType.FORTUNE
                || type == UpgradeType.ESSENCE_CONVERSION || type == UpgradeType.SEED_CONVERSION
                || type == UpgradeType.AE_OUTPUT;
    }

    public static int activeOutputTypeLimitForCapacityUpgrades(int capacityUpgrades) {
        int capped = Math.max(0, Math.min(3, capacityUpgrades));
        return Math.min(MAX_OUTPUT_TYPES, BASE_ACTIVE_OUTPUT_TYPE_LIMIT + capped * OUTPUT_TYPES_PER_CAPACITY);
    }

    public static boolean canAutomationInsert(int slot) {
        return slot >= 0 && slot < INPUT_SLOTS;
    }

    public static boolean canAutomationExtract(int slot, int distinctTypes) {
        return slot >= OUTPUT_START_SLOT
                && slot < OUTPUT_START_SLOT + Math.clamp(distinctTypes, 0, MAX_OUTPUT_TYPES);
    }

    public ItemStackHandler getMachineHandler() {
        return machineHandler;
    }

    public ItemStackHandler getSeedHandler() {
        return seedHandler;
    }

    public IItemHandler getAutomationItemHandler() {
        return automationItemHandler;
    }

    public ContainerData getCreativeGreenhouseData() {
        return greenhouseData;
    }

    public int getDistinctOutputTypes() {
        return outputCatalog.distinctTypes();
    }

    public CreativeGreenhouseOutputCatalog getOutputCatalog() {
        return outputCatalog;
    }

    public int getActiveOutputTypeLimit() {
        return activeOutputTypeLimitForCapacityUpgrades(UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY));
    }

    public boolean hasCatalogOverflow() {
        return catalogOverflow;
    }

    private boolean isClientSide() {
        return level != null && level.isClientSide;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("seeds", seedHandler.serializeNBT(provider));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("seeds")) seedHandler.deserializeNBT(provider, tag.getCompound("seeds"));
        catalogDirty = true;
        catalogOverflow = false;
    }
}
