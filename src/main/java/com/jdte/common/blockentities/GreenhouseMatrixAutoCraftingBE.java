package com.jdte.common.blockentities;

import com.jdte.common.greenhouse.GreenhouseMatrixPatternSupport;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Objects;

public final class GreenhouseMatrixAutoCraftingBE extends BlockEntity {
    private final GreenhouseMatrixPatternItemHandler patterns = new GreenhouseMatrixPatternItemHandler(
            GreenhouseMatrixPatternSupport::isEncodedPattern, this::onPatternsChanged);
    private BlockPos controllerPos;

    public GreenhouseMatrixAutoCraftingBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.GREENHOUSE_MATRIX_AUTO_CRAFTING.get(), pos, state);
    }

    public GreenhouseMatrixPatternItemHandler patterns() {
        return patterns;
    }

    public void link(BlockPos controller) {
        updateController(controller);
    }

    public void unlink(BlockPos expectedController) {
        if (Objects.equals(controllerPos, expectedController)) updateController(null);
    }

    @Nullable
    public BlockPos controllerPos() {
        return controllerPos;
    }

    public void dropPatterns() {
        if (level == null || level.isClientSide()) return;
        for (int slot = 0; slot < patterns.getSlots(); slot++) {
            ItemStack stack = patterns.extractItem(slot, patterns.getSlotLimit(slot), false);
            if (!stack.isEmpty()) Block.popResource(level, worldPosition, stack);
        }
    }

    private void updateController(@Nullable BlockPos next) {
        BlockPos immutable = next == null ? null : next.immutable();
        if (Objects.equals(controllerPos, immutable)) return;
        GreenhouseMatrixControllerBE previous = controller();
        controllerPos = immutable;
        if (previous != null) previous.invalidateAutoCraftingCatalog();
        GreenhouseMatrixControllerBE current = controller();
        if (current != null) current.invalidateAutoCraftingCatalog();
        setChangedAndSync();
    }

    private void onPatternsChanged() {
        GreenhouseMatrixControllerBE controller = controller();
        if (controller != null) controller.invalidateAutoCraftingCatalog();
        setChangedAndSync();
    }

    @Nullable
    private GreenhouseMatrixControllerBE controller() {
        return level != null && controllerPos != null
                && level.getBlockEntity(controllerPos) instanceof GreenhouseMatrixControllerBE controller
                ? controller : null;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("patterns", patterns.serializeNBT(provider));
        if (controllerPos != null) tag.putLong("controller", controllerPos.asLong());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("patterns")) patterns.load(tag.getCompound("patterns"), provider);
        controllerPos = tag.contains("controller") ? BlockPos.of(tag.getLong("controller")) : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider provider) {
        CompoundTag tag = packet.getTag();
        if (tag != null) loadAdditional(tag, provider);
    }
}
