package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.direwolf20.justdirethings.common.capabilities.EnergyStorageItemStackNoReceive;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.common.containers.handlers.BatchComponentItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import com.jdte.common.containers.LargePortableContainerMenus;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import com.direwolf20.justdirethings.setup.Config;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.common.ModConfigSpec;

public class LargePocketGeneratorItem extends PocketGenerator {
    private static final int FALLBACK_BASE_MAX_ENERGY = 1_000_000;
    private static final int FALLBACK_FE_PER_FUEL_TICK = 1;
    private static final int FALLBACK_BURN_SPEED_MULTIPLIER = 1;

    public LargePocketGeneratorItem(Properties properties) {
        super(properties);
    }

    public static int resolveFuelBurnTime(ItemStack fuelStack, FuelValues fuels) {
        return PortableFuelBurnSpeedHelper.resolveBurnTime(fuelStack, fuels);
    }

    public static int getScaledMaxEnergy(int basePocketCapacity) {
        return LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity);
    }

    public static float getEnabledProperty(ItemStack stack) {
        return stack.getOrDefault(JustDireDataComponents.TOOL_ENABLED, true) ? 1.0F : 0.0F;
    }

    public static EnergyStorageItemStackNoReceive createEnergyStorage(ItemAccess access) {
        return new EnergyStorageItemStackNoReceive(access, getConfiguredMaxEnergy());
    }

    @Override
    public int getMaxEnergy() {
        return getConfiguredMaxEnergy();
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
    public void tryBurn(EnergyStorageItemStackNoReceive energyStorage, ItemStack generatorStack, ServerLevel world) {
        burnFuel(energyStorage, generatorStack, world.fuelValues());
    }

    void burnFuel(EnergyStorageItemStackNoReceive energyStorage, ItemStack generatorStack, FuelValues fuels) {
        int energyPerTick = getFePerFuelTick() * getBurnSpeedMultiplier(generatorStack);
        if (energyStorage.forceReceiveEnergy(energyPerTick, true) <= 0) {
            return;
        }

        if (generatorStack.getOrDefault(JustDireDataComponents.POCKETGEN_COUNTER, 0) <= 0
                && !initializeLargeBurn(generatorStack, fuels)) {
            return;
        }

        energyStorage.forceReceiveEnergy(getFePerFuelTick() * getBurnSpeedMultiplier(generatorStack), false);
        consumeBurnTick(generatorStack, fuels);
    }

    private void consumeBurnTick(ItemStack generatorStack, FuelValues fuels) {
        int remaining = Math.max(0, generatorStack.getOrDefault(JustDireDataComponents.POCKETGEN_COUNTER, 0) - 1);
        generatorStack.set(JustDireDataComponents.POCKETGEN_COUNTER, remaining);
        if (remaining == 0) {
            generatorStack.set(JustDireDataComponents.POCKETGEN_MAXBURN, 0);
            initializeLargeBurn(generatorStack, fuels);
        }
    }

    private boolean initializeLargeBurn(ItemStack generatorStack, FuelValues fuels) {
        BatchComponentItemHandler fuelHandler = new BatchComponentItemHandler(ItemAccess.forStack(generatorStack), JustDireDataComponents.ITEMSTACK_HANDLER.get(), 1);
        ItemStack fuelStack = fuelHandler.copyStack(0);
        int burnTime = resolveFuelBurnTime(fuelStack, fuels);
        if (burnTime <= 0) {
            return false;
        }

        setFuelMultiplier(generatorStack, PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuelStack));
        int burnTicks = burnTime / Math.max(1, getBurnSpeedMultiplier(generatorStack));
        if (burnTicks <= 0) {
            return false;
        }
        var remainder = fuelStack.getCraftingRemainder();
        if (remainder != null) {
            var left = remainder.create();
            fuelHandler.set(0, ItemResource.of(left), left.getCount());
        } else {
            fuelStack.shrink(1);
            fuelHandler.set(0, ItemResource.of(fuelStack), fuelStack.getCount());
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

    private static int getConfiguredMaxEnergy() {
        return getScaledMaxEnergy(resolveConfigInt(Config.POCKET_GENERATOR_MAX_FE, FALLBACK_BASE_MAX_ENERGY));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown()) {
            LargePortableContainerMenus.openFromMainHand(
                    player, OpenLargePortableContainerPayload.ContainerKind.LARGE_POCKET_GENERATOR);
        }
        return InteractionResult.SUCCESS;
    }
}
