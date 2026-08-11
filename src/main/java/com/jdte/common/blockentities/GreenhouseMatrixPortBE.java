package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class GreenhouseMatrixPortBE extends BlockEntity {
    private BlockPos controllerPos;

    public GreenhouseMatrixPortBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.GREENHOUSE_MATRIX_PORT.get(), pos, state);
    }

    public void link(BlockPos controllerPos) {
        updateController(controllerPos);
    }

    public void unlink(BlockPos expectedController) {
        if (Objects.equals(controllerPos, expectedController)) updateController(null);
    }

    private void updateController(@Nullable BlockPos nextController) {
        BlockPos immutable = nextController == null ? null : nextController.immutable();
        if (Objects.equals(controllerPos, immutable)) return;
        updateControllerLink(controllerPos, immutable, value -> controllerPos = value, this::invalidateCapabilities);
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    static void updateControllerLink(@Nullable BlockPos current, @Nullable BlockPos next,
                                     Consumer<BlockPos> store, Runnable invalidate) {
        if (Objects.equals(current, next)) return;
        store.accept(next);
        invalidate.run();
    }

    public GreenhouseMatrixControllerBE controller() {
        return level != null && controllerPos != null
                && level.getBlockEntity(controllerPos) instanceof GreenhouseMatrixControllerBE controller
                ? controller : null;
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (controllerPos != null) tag.putLong("controller", controllerPos.asLong());
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        BlockPos loadedController = tag.contains("controller") ? BlockPos.of(tag.getLong("controller")) : null;
        if (!Objects.equals(controllerPos, loadedController)) {
            controllerPos = loadedController;
            if (level != null) invalidateCapabilities();
        }
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet,
                                       HolderLookup.Provider provider) {
        CompoundTag tag = packet.getTag();
        if (tag != null) loadAdditional(tag, provider);
    }
}
