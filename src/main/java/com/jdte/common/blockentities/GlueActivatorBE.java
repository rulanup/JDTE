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
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class GlueActivatorBE extends BaseMachineBE implements FilterableBE, RedstoneControlledBE, AreaAffectingBE, BaseFilterMachine {
    public static final int REVIVE_SLOT = 0;
    public static final int BASE_DELAY_TICKS = 120; // 6 seconds

    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    protected final ItemStackHandler itemHandler;

    protected GlueActivatorBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = 1;
        areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
        itemHandler = new ItemStackHandler(1) {
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
            activateGoo();
        }
    }

    protected void activateGoo() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        AABB area = getAABB(getBlockPos());
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = (int) area.minX; x <= (int) area.maxX; x++) {
            for (int y = (int) area.minY; y <= (int) area.maxY; y++) {
                for (int z = (int) area.minZ; z <= (int) area.maxZ; z++) {
                    mutablePos.set(x, y, z);
                    BlockState blockState = serverLevel.getBlockState(mutablePos);
                    BlockEntity blockEntity = serverLevel.getBlockEntity(mutablePos);

                    if (blockEntity instanceof com.direwolf20.justdirethings.common.blockentities.basebe.GooBlockBE_Base) {
                        if (!blockState.getValue(com.direwolf20.justdirethings.common.blocks.gooblocks.GooBlock_Base.ALIVE)) {
                            if (tryReviveGoo(serverLevel, mutablePos.immutable(), blockState)) {
                                return; // One activation per tick
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean tryReviveGoo(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        ItemStack reviveItem = itemHandler.getStackInSlot(REVIVE_SLOT);
        if (reviveItem.isEmpty()) return false;

        // Check if the item is a valid revival item for this goo tier
        if (!isValidRevivalItem(reviveItem)) return false;

        // Revive the goo
        serverLevel.setBlock(pos, state.setValue(com.direwolf20.justdirethings.common.blocks.gooblocks.GooBlock_Base.ALIVE, true), 3);

        // Consume item
        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            reviveItem.shrink(1);
            itemHandler.setStackInSlot(REVIVE_SLOT, reviveItem);
        }

        setChanged();
        return true;
    }

    protected boolean isValidRevivalItem(ItemStack itemStack) {
        return itemStack.is(com.direwolf20.justdirethings.datagen.JustDireItemTags.GOO_REVIVE_TIER_1)
                || itemStack.is(com.direwolf20.justdirethings.datagen.JustDireItemTags.GOO_REVIVE_TIER_2)
                || itemStack.is(com.direwolf20.justdirethings.datagen.JustDireItemTags.GOO_REVIVE_TIER_3)
                || itemStack.is(com.direwolf20.justdirethings.datagen.JustDireItemTags.GOO_REVIVE_TIER_4);
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
