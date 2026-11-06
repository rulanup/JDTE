package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.direwolf20.justdirethings.setup.Config;
import com.jdte.common.containers.LargePocketGeneratorMenuAccess;
import com.jdte.common.containers.handlers.LargePocketGeneratorHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.server.ServerLifecycleHooks;
import java.lang.ref.WeakReference;
import java.util.*;

public class LargePocketGeneratorItem extends PocketGenerator {
    private final Map<ItemStack, Stamp> inventoryTicks = new WeakHashMap<>();
    private record Stamp(WeakReference<MinecraftServer> server, int tick) {}
    private static int config(net.minecraftforge.common.ForgeConfigSpec.IntValue value, int fallback) {
        // Item capabilities are also constructed before common config loading, as in the pinned source.
        try { return value.get(); } catch (IllegalStateException notLoaded) { return fallback; }
    }
    public static int capacity() { return LargePortableContainerLogic.pocketGeneratorCapacity(config(Config.POCKET_GENERATOR_MAX_FE, 1_000_000)); }
    @Override public int getMaxEnergy() { return capacity(); }
    @Override public int getFePerFuelTick() { return config(Config.POCKET_GENERATOR_FE_PER_FUEL_TICK, 1); }
    @Override public int getFuelMultiplier(ItemStack stack) { return stack.hasTag() && stack.getTag().contains(FUELMULTIPLIER) ? Math.max(1, stack.getTag().getInt(FUELMULTIPLIER)) : 1; }
    @Override public int getBurnSpeedMultiplier(ItemStack stack) { return PocketGeneratorMath.multiply(config(Config.POCKET_GENERATOR_BURN_SPEED_MULTIPLIER, 1), getFuelMultiplier(stack)); }
    public int productionRate(ItemStack stack) { return PocketGeneratorMath.multiply(getFePerFuelTick(), getBurnSpeedMultiplier(stack)); }
    public static boolean canMutate(ItemStack stack) {
        var server = ServerLifecycleHooks.getCurrentServer();
        return stack.getItem() instanceof LargePocketGeneratorItem && stack.getCount() == 1 && server != null && server.isSameThread() && !server.isStopped();
    }
    public static LargePocketGeneratorHandler fuel(ItemStack stack) { return (LargePocketGeneratorHandler)stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new); }
    public static PocketGeneratorEnergyStorage energy(ItemStack stack) { return (PocketGeneratorEnergyStorage)stack.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new); }
    @Override public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> new PocketGeneratorEnergyStorage(stack));
            private final LazyOptional<net.minecraftforge.items.IItemHandler> fuel = LazyOptional.of(() -> new LargePocketGeneratorHandler(stack, true));
            public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                if (cap == ForgeCapabilities.ENERGY) return energy.cast();
                if (cap == ForgeCapabilities.ITEM_HANDLER) return fuel.cast();
                return LazyOptional.empty();
            }
        };
    }
    @Override public void tryBurn(IEnergyStorage storage, ItemStack stack) {
        if (!canMutate(stack) || storage != energy(stack)) return;
        var owned = energy(stack);
        if (owned.forceReceiveEnergy(productionRate(stack), true) <= 0) return;
        int remaining = stack.hasTag() ? Math.max(0, stack.getTag().getInt(COUNTER)) : 0;
        if (remaining == 0 && !initializeBurn(stack)) return;
        if (owned.forceReceiveEnergy(productionRate(stack), false) <= 0) return;
        remaining = Math.max(0, stack.getTag().getInt(COUNTER) - 1);
        stack.getOrCreateTag().putInt(COUNTER, remaining);
        if (remaining == 0) { stack.getOrCreateTag().putInt(MAXBURN, 0); initializeBurn(stack); }
    }
    private boolean initializeBurn(ItemStack stack) {
        var fuel = fuel(stack).getStackInSlot(0).copy();
        int time = PortableFuelBurnSpeedHelper.resolveBurnTime(fuel);
        int multiplier = Math.max(1, PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuel));
        int speed = PocketGeneratorMath.multiply(config(Config.POCKET_GENERATOR_BURN_SPEED_MULTIPLIER, 1), multiplier);
        int ticks = PocketGeneratorMath.burnTicks(time, speed);
        if (fuel.isEmpty() || ticks <= 0 || fuel.hasCraftingRemainingItem() && fuel.getCount() != 1) return false;
        var remainder = fuel.hasCraftingRemainingItem() ? fuel.getCraftingRemainingItem() : fuel.copyWithCount(fuel.getCount() - 1);
        var inventory = new net.minecraftforge.items.ItemStackHandler(1); inventory.setStackInSlot(0, remainder);
        var next = stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
        next.put(LargePocketGeneratorHandler.KEY, inventory.serializeNBT()); next.putInt(FUELMULTIPLIER, multiplier);
        next.putInt(COUNTER, ticks); next.putInt(MAXBURN, ticks); stack.setTag(next);
        return true;
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player) || !canMutate(stack) || entity.isRemoved() || !player.isAlive() || !getEnabled(stack)) return;
        var server = level.getServer(); var last = inventoryTicks.get(stack);
        if (last != null && last.server().get() == server && last.tick() == server.getTickCount()) return;
        inventoryTicks.put(stack, new Stamp(new WeakReference<>(server), server.getTickCount()));
        var storage = energy(stack); tryBurn(storage, stack);
        if (storage.getEnergyStored() < getFEPerTick() / 10) return;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var target = player.getInventory().getItem(i);
            if (target == stack) continue;
            target.getCapability(ForgeCapabilities.ENERGY).ifPresent(receiver -> storage.transferTo(receiver, getFEPerTick()));
        }
        if (net.minecraftforge.fml.ModList.get().isLoaded("curios"))
            com.jdte.common.integrations.curios.LargePocketGeneratorCurios.chargeEquipment(player, storage, getFEPerTick());
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && !player.isShiftKeyDown()) LargePocketGeneratorMenuAccess.open(player, false);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("justdirethings.screen.energy", com.direwolf20.justdirethings.util.MagicHelpers.formatted(energy(stack).getEnergyStored()), com.direwolf20.justdirethings.util.MagicHelpers.formatted(capacity())));
        tooltip.add(Component.translatable(getEnabled(stack) ? "tooltip.jdte.large_pocket_generator.enabled" : "tooltip.jdte.large_pocket_generator.disabled"));
        if (stack.hasTag() && stack.getTag().getInt(COUNTER) > 0) {
            tooltip.add(Component.translatable("justdirethings.screen.burn_time", com.direwolf20.justdirethings.util.MagicHelpers.ticksInSeconds(stack.getTag().getInt(COUNTER))));
            tooltip.add(Component.translatable("justdirethings.screen.fepertick", productionRate(stack)));
        }
    }
}
