package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.utils.ContainerDataEncoding;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import com.jdte.mixin.AbstractContainerMenuInvoker;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

public class MineralExtractorContainer extends BaseMachineContainer implements FilterPageHolder {
    protected static final int OUTPUT_COLUMNS = 4;
    protected static final int OUTPUT_ROWS = 4;
    protected static final int OUTPUT_SLOTS_PER_PAGE = OUTPUT_COLUMNS * OUTPUT_ROWS;
    private int outputPage;
    private int filterPage;
    private net.minecraft.server.level.ServerPlayer viewer;
    private int sentPage = -1;
    private java.util.List<ItemStack> sentOutputs = java.util.List.of();

    public MineralExtractorContainer(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos());
    }

    public MineralExtractorContainer(int id, Inventory inventory, BlockPos pos) {
        this(JDTEMenus.MINERAL_EXTRACTOR.get(), id, inventory, pos);
    }

    protected MineralExtractorContainer(net.minecraft.world.inventory.MenuType<?> menuType,
                                        int id, Inventory inventory, BlockPos pos) {
        super(menuType, id, inventory, pos);
        if (baseMachineBE instanceof MineralExtractorBE extractor) addDataSlots(extractor.getMachineData());
        addPlayerSlots(inventory);
        if (inventory.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) viewer = serverPlayer;
    }

    @Override public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int surveySlots = getSurveySlotCount();
        int surveyX = surveySlots == 1
                ? layout.getMineralExtractorSurveyX() : layout.getLargeMineralExtractorSurveyX();
        int surveyY = surveySlots == 1
                ? layout.getMineralExtractorSurveyY() : layout.getLargeMineralExtractorSurveyY();
        int surveySpacing = surveySlots == 1 ? 18 : layout.getLargeMineralExtractorSurveySpacing();
        for (int slot = 0; slot < surveySlots; slot++) {
            addSlot(new SurveySlot(machineHandler, slot,
                    surveyX,
                    surveyY + slot * surveySpacing));
        }
        for (int pageSlot = 0; pageSlot < OUTPUT_SLOTS_PER_PAGE; pageSlot++) {
            addSlot(new PagedOutputSlot(machineHandler, pageSlot,
                    layout.getMineralExtractorOutputStartX()
                            + pageSlot % OUTPUT_COLUMNS * layout.getMineralExtractorOutputSpacing(),
                    layout.getMineralExtractorOutputStartY()
                            + pageSlot / OUTPUT_COLUMNS * layout.getMineralExtractorOutputSpacing(), this));
        }
    }

    public int getProgress() { return data(0, 0); }
    public int getProgressMax() { return data(1, 20); }
    public int getActiveOutputSlots() { return data(2, MineralExtractorBE.BASE_OUTPUT_SLOTS); }
    public int getExperienceFluid() { return ContainerDataEncoding.combine16(data(3, 0), data(12, 0)); }
    public int getTimeFluid() { return ContainerDataEncoding.combine16(data(4, 0), data(13, 0)); }
    public int getFluidCapacity() { return ContainerDataEncoding.combine16(data(5, 1), data(14, 0)); }
    public Fluid getExperienceFluidType() { return decodeFluidType(data(15, 0)); }
    public Fluid getTimeFluidType() { return decodeFluidType(data(16, 0)); }
    public int getMultiplier() { return data(6, 1); }
    public int getMaxMultiplier() { return data(7, 32); }
    public int getStateId() { return data(8, 0); }
    public int getFortunePercent() { return data(9, 0); }
    public boolean usesSurvey() { return data(10, 0) != 0; }
    public int getMineralCount() { return data(11, 0); }
    public int getOutputPage() { return outputPage; }
    public int getMaxOutputPage() { return Math.max(0, (getActiveOutputSlots() - 1) / OUTPUT_SLOTS_PER_PAGE); }
    public void setOutputPage(int page) {
        int next = Math.max(0, Math.min(page, getMaxOutputPage()));
        if (next != outputPage) {
            outputPage = next;
            broadcastChanges();
        }
    }
    @Override public void broadcastFullState() {
        super.broadcastFullState();
        sentPage = -1;
        broadcastChanges();
    }
    @Override public void broadcastChanges() {
        outputPage = Math.max(0, Math.min(outputPage, getMaxOutputPage()));
        int maxFilterPage = Math.max(0, (UpgradeHelper.getActiveFilterSlots(baseMachineBE,
                UpgradeHelper.getBaseFilterSlots(filterHandler)) - 1) / UpgradeHelper.getFilterSlotsPerUpgrade());
        filterPage = Math.max(0, Math.min(filterPage, maxFilterPage));
        super.broadcastChanges();
        if (viewer == null) return;
        java.util.List<ItemStack> snapshot = new java.util.ArrayList<>(OUTPUT_SLOTS_PER_PAGE);
        boolean changed = sentPage != outputPage || sentOutputs.size() != OUTPUT_SLOTS_PER_PAGE;
        for (int i = 0; i < OUTPUT_SLOTS_PER_PAGE; i++) {
            ItemStack stack = slots.get(getSurveySlotCount() + i).getItem().copy();
            snapshot.add(stack);
            if (!changed && !ItemStack.matches(stack, sentOutputs.get(i))) changed = true;
        }
        if (changed) {
            com.jdte.common.network.MineralPackets.sendSlots(viewer,
                    new com.jdte.common.network.MineralPackets.Slots(containerId, outputPage, snapshot));
            sentPage = outputPage; sentOutputs = snapshot;
        }
    }
    public void applyOutputSnapshot(int page, java.util.List<ItemStack> stacks) {
        if (!baseMachineBE.getLevel().isClientSide || page < 0 || page > 3 || stacks.size() != OUTPUT_SLOTS_PER_PAGE) return;
        outputPage = page;
        for (int i = 0; i < OUTPUT_SLOTS_PER_PAGE; i++) slots.get(getSurveySlotCount() + i).set(stacks.get(i));
    }
    @Override public int jdte$getFilterPage() { return filterPage; }
    @Override public void jdte$setFilterPage(int page) {
        int max = Math.max(0, (UpgradeHelper.getActiveFilterSlots(baseMachineBE,
                UpgradeHelper.getBaseFilterSlots(filterHandler)) - 1) / UpgradeHelper.getFilterSlotsPerUpgrade());
        int next = Math.max(0, Math.min(page, max));
        if (next != filterPage) {
            filterPage = next;
            broadcastChanges();
        }
    }

    private int data(int index, int fallback) {
        return baseMachineBE instanceof MineralExtractorBE extractor ? extractor.getMachineData().get(index) : fallback;
    }

    private static Fluid decodeFluidType(int encoded) {
        int registryId = encoded - 1;
        if (registryId < 0 || registryId >= BuiltInRegistries.FLUID.size()) return Fluids.EMPTY;
        Fluid fluid = BuiltInRegistries.FLUID.byId(registryId);
        return fluid == null ? Fluids.EMPTY : fluid;
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.MINERAL_EXTRACTOR.get());
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int surveySlots = getSurveySlotCount();
        int machineEnd = surveySlots + OUTPUT_SLOTS_PER_PAGE;
        int filterEnd = machineEnd + FILTER_SLOTS;
        int upgradeEnd = filterEnd + MineralExtractorBE.UPGRADE_SLOTS;
        if (index < machineEnd) {
            if (!moveStack(stack, upgradeEnd, slots.size(), true)) return ItemStack.EMPTY;
        } else if (index < filterEnd) {
            return ItemStack.EMPTY;
        } else if (index < upgradeEnd) {
            if (!moveStack(stack, upgradeEnd, slots.size(), true)) return ItemStack.EMPTY;
        } else if (stack.is(JDTEItems.MINERAL_SURVEY.get())) {
            if (!moveStack(stack, 0, surveySlots, false)) return ItemStack.EMPTY;
        } else if (UpgradeHelper.isUpgrade(stack) || UpgradeHelper.isSmelterUpgrade(stack)) {
            if (!moveStack(stack, filterEnd, upgradeEnd, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }

    public int getSurveySlotCount() {
        return baseMachineBE instanceof MineralExtractorBE extractor ? extractor.surveySlotCount() : 1;
    }

    public int getMachineSlotCount() {
        return getSurveySlotCount() + OUTPUT_SLOTS_PER_PAGE;
    }

    protected int outputStartSlot() {
        return getSurveySlotCount();
    }

    private boolean moveStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return ((AbstractContainerMenuInvoker) (Object) this)
                .jdte$invokeMoveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }

    private static final class SurveySlot extends SlotItemHandler {
        private SurveySlot(IItemHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public int getMaxStackSize() { return 1; }
    }

    private static final class PagedOutputSlot extends SlotItemHandler {
        private final int pageSlot;
        private final MineralExtractorContainer container;
        private PagedOutputSlot(IItemHandler handler, int pageSlot, int x, int y,
                                MineralExtractorContainer container) {
            super(handler, container.outputStartSlot() + pageSlot, x, y);
            this.pageSlot = pageSlot;
            this.container = container;
        }
        @Override public int getSlotIndex() {
            return container.outputStartSlot()
                    + container.outputPage * OUTPUT_SLOTS_PER_PAGE + pageSlot;
        }
        @Override public ItemStack getItem() {
            return active() ? getItemHandler().getStackInSlot(getSlotIndex()) : ItemStack.EMPTY;
        }
        @Override public boolean hasItem() { return !getItem().isEmpty(); }
        @Override public void set(ItemStack stack) {
            if (active()) ((IItemHandlerModifiable) getItemHandler()).setStackInSlot(getSlotIndex(), stack);
        }
        @Override public void initialize(ItemStack stack) { set(stack); }
        @Override public void setChanged() { container.baseMachineBE.setChanged(); }
        @Override public ItemStack remove(int amount) {
            return active() ? getItemHandler().extractItem(getSlotIndex(), Math.min(amount, getItem().getMaxStackSize()), false) : ItemStack.EMPTY;
        }
        @Override public int getMaxStackSize() {
            return active() ? getItemHandler().getSlotLimit(getSlotIndex()) : 0;
        }
        @Override public int getMaxStackSize(ItemStack stack) {
            return active() ? getItemHandler().getSlotLimit(getSlotIndex()) : 0;
        }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return active() && !getItem().isEmpty(); }
        private boolean active() {
            return getSlotIndex() < container.outputStartSlot() + container.getActiveOutputSlots()
                    && getSlotIndex() < getItemHandler().getSlots();
        }
    }
}
