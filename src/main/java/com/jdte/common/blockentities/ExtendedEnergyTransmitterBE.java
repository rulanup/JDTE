package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedEnergyTransmitterBE extends EnergyTransmitterBE implements ExtendedUpgradeMachine {
    public ExtendedEnergyTransmitterBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_ENERGY_TRANSMITTER.get(), pos, state);
    }

    private final com.direwolf20.justdirethings.common.capabilities.TransmitterEnergyStorage ownedEnergy =
            new ExtendedTransmitterEnergyStorage(getMaxEnergy(), this);

    @Override public com.direwolf20.justdirethings.common.capabilities.TransmitterEnergyStorage getEnergyStorage() {
        // Native constructors may query this virtual method before the subclass field exists.
        return ownedEnergy == null ? super.getEnergyStorage() : ownedEnergy;
    }

    @Override public java.util.Map<BlockPos, com.direwolf20.justdirethings.common.capabilities.TransmitterEnergyStorage> getTransmitterEnergyStorages() {
        if (!TransmitterPorts.live(this)) return java.util.Map.of();
        var result = new java.util.LinkedHashMap<BlockPos, com.direwolf20.justdirethings.common.capabilities.TransmitterEnergyStorage>();
        result.put(worldPosition, getEnergyStorage());
        var seen = new java.util.IdentityHashMap<com.direwolf20.justdirethings.common.capabilities.TransmitterEnergyStorage, Boolean>();
        seen.put(getEnergyStorage(), Boolean.TRUE);
        super.getTransmitterEnergyStorages().forEach((pos, storage) -> {
            if (seen.put(storage, Boolean.TRUE) == null) result.put(pos, storage);
        });
        return result;
    }

    @Override public int getTotalEnergyStored() { return TransmitterNetworkEnergy.total(this, false); }
    @Override public int getTotalMaxEnergyStored() { return TransmitterNetworkEnergy.total(this, true); }
    @Override public int distributeEnergy(int amount) { return getEnergyStorage().receiveEnergy(amount, false); }
    @Override public int extractEnergy(int amount) { return getEnergyStorage().extractEnergy(amount, false); }
    @Override public void balanceEnergy() { TransmitterNetworkEnergy.balance(this); }

    @Override
    public IEnergyStorage getTransmitterHandler(BlockPos blockPos) {
        if (chunkUnloaded || isRemoved() || level!=null && (!level.isLoaded(blockPos) || level.getBlockEntity(worldPosition)!=this)) return null;
        if (blockPos.equals(worldPosition)) {
            return getEnergyStorage();
        }
        if (level == null || !level.getBlockState(blockPos).is(Registration.EnergyTransmitter.get())) {
            return null;
        }
        var peer = level.getBlockEntity(blockPos);
        if (peer == null || peer.isRemoved()) return null;
        // Resolve the current block entity and facing; a native cache may still refer to a replaced peer.
        return peer.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY,
                level.getBlockState(blockPos).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)).orElse(null);
    }

    @Override
    public int fePerTick() {
        if (com.jdte.common.upgrades.UpgradeHelper.hasEnergyOverloadUpgrade(this)) {
            return Integer.MAX_VALUE;
        }
        return super.fePerTick();
    }
    @Override public void onChunkUnloaded() {
        chunkUnloaded = true;
        super.onChunkUnloaded();
    }
    @Override public void tickServer() {
        if (TransmitterPorts.live(this)) super.tickServer();
    }
    private boolean chunkUnloaded;
    boolean isChunkUnloaded() { return chunkUnloaded; }
    @Override public void onLoad() {
        chunkUnloaded = false;
        super.onLoad();
    }
    @Override public void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
        super.saveAdditional(tag);
        // Forge JDT's inherited BaseMachineBE save does not serialize this native owned FE field.
        tag.putInt("energy", getEnergyStorage().getRealEnergyStored());
    }
    @Override public void load(net.minecraft.nbt.CompoundTag tag) {
        super.load(tag);
        if (tag.contains("energy", net.minecraft.nbt.Tag.TAG_INT))
            getEnergyStorage().setEnergy(Math.max(0, Math.min(getMaxEnergy(), tag.getInt("energy"))));
    }
    private java.util.EnumMap<Direction, net.minecraftforge.common.util.LazyOptional<net.minecraftforge.energy.IEnergyStorage>> sidedEnergy;
    private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler> exposedItems;
    private void createPorts() {
        sidedEnergy=new java.util.EnumMap<>(Direction.class);
        for(Direction side:Direction.values())sidedEnergy.put(side,net.minecraftforge.common.util.LazyOptional.of(()->new TransmitterPorts.NativeEnergyPort(this,side)));
        exposedItems=net.minecraftforge.common.util.LazyOptional.of(()->TransmitterPorts.items(this));
    }
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap,Direction side) {
        if(cap==net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY) {
            if(side==null||!TransmitterPorts.facing(this,side))return net.minecraftforge.common.util.LazyOptional.empty();
            if(sidedEnergy==null)createPorts();return sidedEnergy.get(side).cast();
        }
        if(cap==net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) {
            if(exposedItems==null)createPorts();return exposedItems.cast();
        }
        
        return super.getCapability(cap,side);
    }
    @Override public void invalidateCaps() {
        super.invalidateCaps();
        if(sidedEnergy!=null)sidedEnergy.values().forEach(net.minecraftforge.common.util.LazyOptional::invalidate);
        if(exposedItems!=null)exposedItems.invalidate();
    }
    @Override public void reviveCaps() { super.reviveCaps();createPorts(); }
}
