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
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class ItemSenderBE extends BaseMachineBE implements FilterableBE, RedstoneControlledBE, AreaAffectingBE {
    public static final int STORAGE_SLOTS = 9;

    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    protected final ItemStackHandler itemHandler;

    protected ItemSenderBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = STORAGE_SLOTS;
        areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
        // 设置默认半径为1，这样可以搜索相邻方块
        areaAffectingData.xRadius = 1;
        areaAffectingData.yRadius = 1;
        areaAffectingData.zRadius = 1;
        itemHandler = new ItemStackHandler(STORAGE_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    @Override
    public ItemStackHandler getMachineHandler() {
        return itemHandler;
    }

    @Override
    public AreaAffectingData getAreaAffectingData() {
        return areaAffectingData;
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if (isActiveRedstone() && canRun()) {
            sendItems();
        }
    }

    protected void sendItems() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int itemsToSend = getItemsToSend();
        int sent = 0;

        // 在范围内寻找目标容器
        AABB area = getAABB(getBlockPos());
        for (BlockPos targetPos : BlockPos.betweenClosed(
                (int) area.minX, (int) area.minY, (int) area.minZ,
                (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)) {
            if (sent >= itemsToSend) break;

            // 跳过自身位置
            if (targetPos.equals(getBlockPos())) continue;

            IItemHandler targetHandler = serverLevel.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, null);
            if (targetHandler == null) continue;

            for (int i = 0; i < STORAGE_SLOTS && sent < itemsToSend; i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (stack.isEmpty()) continue;

                int canSend = Math.min(stack.getCount(), itemsToSend - sent);
                ItemStack toSend = stack.copy();
                toSend.setCount(canSend);

                ItemStack remaining = ItemHandlerHelper.insertItemStacked(targetHandler, toSend, false);
                int actuallySent = canSend - remaining.getCount();

                if (actuallySent > 0) {
                    stack.shrink(actuallySent);
                    itemHandler.setStackInSlot(i, stack);
                    sent += actuallySent;
                }
            }
        }

        if (sent > 0) {
            setChanged();
        }
    }

    protected abstract int getBaseItemsToSend();

    protected int getItemsToSend() {
        int base = getBaseItemsToSend();
        if (UpgradeHelper.hasCreativeUpgrade(this)) return base;
        if (UpgradeHelper.countUpgrades(this, UpgradeType.OVERCLOCK) > 0) return base * 2;
        if (UpgradeHelper.countUpgrades(this, UpgradeType.UNDERCLOCK) > 0) return Math.max(1, base / 4);
        return base;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
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
    public RedstoneControlData getRedstoneControlData() {
        return redstoneControlData;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", itemHandler.serializeNBT(provider));
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        }
        loadAreaSettings(tag);
    }
}
