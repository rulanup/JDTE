package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.direwolf20.justdirethings.common.capabilities.EnergyStorageItemStackNoReceive;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import net.neoforged.neoforge.items.ComponentItemHandler;
import com.jdte.common.containers.LargePortableContainerMenus;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import com.direwolf20.justdirethings.setup.Config;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;

public class LargePocketGeneratorItem extends PocketGenerator {
    private static final int FALLBACK_FE_PER_FUEL_TICK = 1;
    private static final int FALLBACK_BURN_SPEED_MULTIPLIER = 1;

    public static int resolveFuelBurnTime(ItemStack fuelStack) {
        return PortableFuelBurnSpeedHelper.resolveBurnTime(fuelStack);
    }

    public static int getScaledMaxEnergy(int basePocketCapacity) {
        return LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity);
    }

    public static float getEnabledProperty(ItemStack stack) {
        return stack.getOrDefault(JustDireDataComponents.TOOL_ENABLED, true) ? 1.0F : 0.0F;
    }

    @Override
    public int getMaxEnergy() {
        return getScaledMaxEnergy(Config.POCKET_GENERATOR_MAX_FE.get());
    }

    @Override
    public int getFePerFuelTick() {
        return resolveConfigInt(Config.POCKET_GENERATOR_FE_PER_FUEL_TICK, FALLBACK_FE_PER_FUEL_TICK);
    }

    @Override
    public int getBurnSpeedMultiplier(ItemStack stack) {
        return resolveConfigInt(Config.POCKET_GENERATOR_BURN_SPEED_MULTIPLIER,
                FALLBACK_BURN_SPEED_MULTIPLIER) * getFuelMultiplier(stack);
    }

    @Override
    public void tryBurn(EnergyStorageItemStackNoReceive energyStorage, ItemStack generatorStack) {
        int energyPerTick = getFePerFuelTick() * getBurnSpeedMultiplier(generatorStack);
        if (energyStorage.forceReceiveEnergy(energyPerTick, true) <= 0) {
            return;
        }

        if (generatorStack.getOrDefault(JustDireDataComponents.POCKETGEN_COUNTER, 0) <= 0
                && !initializeLargeBurn(generatorStack)) {
            return;
        }

        energyStorage.forceReceiveEnergy(getFePerFuelTick() * getBurnSpeedMultiplier(generatorStack), false);
        consumeBurnTick(generatorStack);
    }

    private void consumeBurnTick(ItemStack generatorStack) {
        int remaining = Math.max(0, generatorStack.getOrDefault(JustDireDataComponents.POCKETGEN_COUNTER, 0) - 1);
        generatorStack.set(JustDireDataComponents.POCKETGEN_COUNTER, remaining);
        if (remaining == 0) {
            generatorStack.set(JustDireDataComponents.POCKETGEN_MAXBURN, 0);
            initializeLargeBurn(generatorStack);
        }
    }

    private boolean initializeLargeBurn(ItemStack generatorStack) {
        ComponentItemHandler fuelHandler = new ComponentItemHandler(
                generatorStack, JustDireDataComponents.ITEMSTACK_HANDLER.get(), 1);
        ItemStack fuelStack = fuelHandler.getStackInSlot(0);
        int burnTime = resolveFuelBurnTime(fuelStack);
        if (burnTime <= 0) {
            return false;
        }

        setFuelMultiplier(generatorStack, PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuelStack));
        int burnTicks = burnTime / Math.max(1, getBurnSpeedMultiplier(generatorStack));
        if (burnTicks <= 0) {
            return false;
        }
        if (fuelStack.hasCraftingRemainingItem()) {
            fuelHandler.setStackInSlot(0, fuelStack.getCraftingRemainingItem());
        } else {
            fuelStack.shrink(1);
            fuelHandler.setStackInSlot(0, fuelStack);
        }
        generatorStack.set(JustDireDataComponents.POCKETGEN_COUNTER, burnTicks);
        generatorStack.set(JustDireDataComponents.POCKETGEN_MAXBURN, burnTicks);
        return true;
    }

    private static int resolveConfigInt(ModConfigSpec.IntValue value, int fallback) {
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        if (!player.isShiftKeyDown()) {
            LargePortableContainerMenus.openFromMainHand(
                    player, OpenLargePortableContainerPayload.ContainerKind.LARGE_POCKET_GENERATOR);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
