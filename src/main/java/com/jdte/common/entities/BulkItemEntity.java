package com.jdte.common.entities;

import com.jdte.setup.JDTEEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** A normal dropped item with an integer count, avoiding 100,000 entities for one full output slot. */
public final class BulkItemEntity extends ItemEntity {
    private static final EntityDataAccessor<Integer> COUNT = SynchedEntityData.defineId(BulkItemEntity.class, EntityDataSerializers.INT);
    private ItemStack fullStack;
    public BulkItemEntity(EntityType<? extends BulkItemEntity> type, Level level) { super(type, level); }
    public BulkItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(JDTEEntities.BULK_ITEM.get(), level);
        setPos(x, y, z); setItem(stack.copy()); setDefaultPickUpDelay();
        setDeltaMovement((random.nextDouble() - 0.5) * 0.2, 0.2, (random.nextDouble() - 0.5) * 0.2);
    }
    @Override protected void defineSynchedData() { super.defineSynchedData(); entityData.define(COUNT, 0); }
    @Override public ItemStack getItem() { return fullStack == null ? ItemStack.EMPTY : fullStack; }
    @Override public void setItem(ItemStack stack) {
        super.setItem(stack.copyWithCount(1)); // The vanilla tracked stack count is only one byte.
        entityData.set(COUNT, stack.getCount());
        fullStack = stack.copy();
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        fullStack = super.getItem().copyWithCount(entityData.get(COUNT));
    }
    @Override public void playerTouch(Player player) {
        super.playerTouch(player); // Keep Forge pickup events, ownership, stats and legal inventory stack sizes.
        if (!isRemoved()) setItem(getItem());
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Item", getItem().copyWithCount(1).save(new CompoundTag()));
        tag.putInt("JDTECount", getItem().getCount());
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        int count = tag.contains("JDTECount") ? tag.getInt("JDTECount") : getItem().getCount();
        if (count <= 0) { discard(); return; }
        setItem(getItem().copyWithCount(count));
    }
    @Override public ItemStack getPickResult() { return getItem().copyWithCount(1); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
