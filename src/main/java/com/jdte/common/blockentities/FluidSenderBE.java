package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public abstract class FluidSenderBE extends BaseMachineBE implements FilterableBE, RedstoneControlledBE, FluidMachineBE, AreaAffectingBE {
    public static final int BASE_FLUID_CAPACITY = 8000; // 8 buckets

    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    public final JDTEFluidTank fluidTank;

    public final FluidContainerData fluidContainerData;

    protected FluidSenderBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
        // 设置默认半径为1，这样可以搜索相邻方块
        areaAffectingData.xRadius = 1;
        areaAffectingData.yRadius = 1;
        areaAffectingData.zRadius = 1;
        fluidTank = new JDTEFluidTank(getMaxMB(), f -> true);
        fluidContainerData = new FluidContainerData(this);
    }

    @Override
    public AreaAffectingData getAreaAffectingData() {
        return areaAffectingData;
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        if (isActiveRedstone() && canRun()) {
            sendFluid();
        }
    }

    protected void sendFluid() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int fluidToSend = getFluidToSend();

        // 在范围内寻找目标容器
        AABB area = getAABB(getBlockPos());
        for (BlockPos targetPos : BlockPos.betweenClosed(
                (int) area.minX, (int) area.minY, (int) area.minZ,
                (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)) {
            // 跳过自身位置
            if (targetPos.equals(getBlockPos())) continue;

            IFluidHandler targetHandler = serverLevel.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, null);
            if (targetHandler == null) continue;

            FluidStack toSend = fluidTank.drain(fluidToSend, IFluidHandler.FluidAction.SIMULATE);
            if (toSend.isEmpty()) return;

            int filled = targetHandler.fill(toSend, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                fluidTank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                setChanged();
                return; // 每tick只发送到一个目标
            }
        }
    }

    protected abstract int getBaseFluidToSend();

    protected int getFluidToSend() {
        int base = getBaseFluidToSend();
        if (UpgradeHelper.hasCreativeUpgrade(this)) return base;
        if (UpgradeHelper.countUpgrades(this, UpgradeType.OVERCLOCK) > 0) return base * 2;
        if (UpgradeHelper.countUpgrades(this, UpgradeType.UNDERCLOCK) > 0) return Math.max(1, base / 4);
        return base;
    }

    @Override
    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, BASE_FLUID_CAPACITY);
    }

    @Override
    public JDTEFluidTank getFluidTank() {
        return fluidTank;
    }

    @Override
    public FluidContainerData getFluidContainerData() {
        return fluidContainerData;
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
        tag.put("fluidTank", fluidTank.serializeNBT(provider));
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("fluidTank")) {
            fluidTank.deserializeNBT(provider, tag.getCompound("fluidTank"));
        }
        loadAreaSettings(tag);
    }
}
