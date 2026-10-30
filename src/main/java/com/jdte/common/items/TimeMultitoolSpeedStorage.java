package com.jdte.common.items;
import net.minecraft.world.item.ItemStack;
/** Forge NBT representation of the pinned integer mode component. */
public final class TimeMultitoolSpeedStorage {
    public static final String KEY="jdte:time_multitool_speed_mode";
    private TimeMultitoolSpeedStorage() {}
    public static TimeMultitoolSpeedMode get(ItemStack stack) {return TimeMultitoolSpeedMode.fromStoredIndex(stack.hasTag()?stack.getTag().getInt(KEY):0);}
    public static void set(ItemStack stack,TimeMultitoolSpeedMode mode) {stack.getOrCreateTag().putInt(KEY,mode.ordinal());}
}
