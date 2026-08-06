package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class TimeFreezerBE extends BaseMachineBE implements FluidMachineBE, RedstoneControlledBE {
    public final FluidContainerData fluidContainerData;
    public final JDTEFluidTank fluidTank;
    public final RedstoneControlData redstoneControlData = new RedstoneControlData();

    protected TimeFreezerBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tickSpeed = 1;
        fluidTank = new JDTEFluidTank(getMaxMB(), fluidStack -> fluidStack.getFluid() instanceof TimeFluid);
        fluidContainerData = new FluidContainerData(this);
    }

    public TimeFreezerBE(BlockPos pos, BlockState state) {
        this(JDTEBlockEntities.TIME_FREEZER.get(), pos, state);
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        boolean wantFreeze = isActiveRedstone()
                && (UpgradeHelper.hasCreativeUpgrade(this) || fluidTank.getFluidAmount() >= getFluidCostPerTick());
        if (wantFreeze) {
            if (!UpgradeHelper.hasCreativeUpgrade(this)) {
                fluidTank.drain(getFluidCostPerTick(), IFluidHandler.FluidAction.EXECUTE);
                setChanged();
            }
            TimeFreezerManager.activate(this, serverLevel);
        } else {
            TimeFreezerManager.deactivate(this);
        }
    }

    public int getFluidCostPerTick() {
        return JDTEConfig.COMMON.timeFreezer.timeFreezerFluidPerTick.get();
    }

    @Override
    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.timeFreezer.timeFreezerFluidCapacity.get());
    }

    @Override
    public ContainerData getFluidContainerData() {
        return fluidContainerData;
    }

    @Override
    public JDTEFluidTank getFluidTank() {
        return fluidTank;
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
    public boolean isDefaultSettings() {
        return super.isDefaultSettings() && fluidTank.getFluid().isEmpty();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        TimeFreezerManager.register(this);
    }

    @Override
    public void setRemoved() {
        TimeFreezerManager.unregister(this);
        super.setRemoved();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("fluidTank", fluidTank.serializeNBT(provider));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("fluidTank")) {
            fluidTank.deserializeNBT(provider, tag.getCompound("fluidTank"));
        }
    }
}
