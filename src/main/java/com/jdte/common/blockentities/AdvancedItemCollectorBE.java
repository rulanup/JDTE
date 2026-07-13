package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class AdvancedItemCollectorBE extends BaseMachineBE
        implements FilterableBE, AreaAffectingBE, RedstoneControlledBE, ExtendedUpgradeMachine {
    private BlockCapabilityCache<IItemHandler, Direction> attachedInventory;
    private final FilterData filterData = new FilterData();
    private final RedstoneControlData redstoneControlData = new RedstoneControlData();
    private final AreaAffectingData areaAffectingData;

    public AdvancedItemCollectorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_ITEM_COLLECTOR.get(), pos, state);
        areaAffectingData = new AreaAffectingData(state.getValue(BlockStateProperties.FACING).getOpposite());
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public FilterData getFilterData() {
        return filterData;
    }

    @Override
    public FilterBasicHandler getFilterHandler() {
        return getData(Registration.HANDLER_BASIC_FILTER);
    }

    @Override
    public RedstoneControlData getRedstoneControlData() {
        return redstoneControlData;
    }

    @Override
    public AreaAffectingData getAreaAffectingData() {
        return areaAffectingData;
    }

    @Override
    public void tickServer() {
        super.tickServer();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) AdvancedItemCollectorManager.register(this);
    }

    @Override
    public void setRemoved() {
        AdvancedItemCollectorManager.unregister(this);
        super.setRemoved();
    }

    @Override
    public void setAreaSettings(double xRadius, double yRadius, double zRadius,
                                int xOffset, int yOffset, int zOffset, boolean renderArea) {
        AreaAffectingBE.super.setAreaSettings(
                xRadius, yRadius, zRadius, xOffset, yOffset, zOffset, renderArea);
        AdvancedItemCollectorManager.refresh(this);
    }

    @Override
    public void handleRotate(Direction oldDirection, Direction newDirection) {
        AreaAffectingBE.super.handleRotate(oldDirection, newDirection);
        attachedInventory = null;
        AdvancedItemCollectorManager.refresh(this);
    }

    public ItemStack collect(ItemStack stack, Vec3 position) {
        if (!canCollect(stack) || !getAABB(getBlockPos()).contains(position)) {
            return stack;
        }
        return insertCollectedStack(stack);
    }

    boolean canCollect(ItemStack stack) {
        return !stack.isEmpty() && !isRemoved() && level instanceof ServerLevel
                && isActiveRedstone() && isStackValidFilter(stack);
    }

    ItemStack insertCollectedStack(ItemStack stack) {
        return insertCollectedStack(stack, false);
    }

    ItemStack insertCollectedStack(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || isRemoved() || !(level instanceof ServerLevel) || !isActiveRedstone()) {
            return stack;
        }
        IItemHandler inventory = getAttachedInventory();
        return inventory == null ? stack : ItemHandlerHelper.insertItemStacked(inventory, stack, simulate);
    }

    boolean isAttachedInventoryAt(BlockPos pos) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        return getBlockPos().relative(facing).equals(pos);
    }

    private IItemHandler getAttachedInventory() {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        if (attachedInventory == null) {
            Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
            attachedInventory = BlockCapabilityCache.create(
                    Capabilities.ItemHandler.BLOCK,
                    serverLevel,
                    getBlockPos().relative(facing),
                    facing.getOpposite());
        }
        return attachedInventory.getCapability();
    }
}
