package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blocks.resources.CoalBlock_T1;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.resources.Coal_T1;
import com.direwolf20.justdirethings.setup.Config;
import com.jdte.common.containers.LargePortableContainerMenus;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class LargeFuelCanisterItem extends FuelCanister {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        LargePortableContainerMenus.openFromMainHand(
                player, hand, OpenLargePortableContainerPayload.ContainerKind.LARGE_FUEL_CANISTER);
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    @Override
    public int getBurnTime(ItemStack stack, RecipeType<?> recipeType) {
        return FuelCanister.getFuelLevel(stack) >= getMinimumFuelConsumed() ? getMinimumFuelConsumed() : 0;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack remaining = stack.copy();
        decrementFuel(remaining);
        return remaining;
    }

    public static int getMaxFuelLevel(int baseFuelCapacity) {
        return LargePortableContainerLogic.fuelCapacity(baseFuelCapacity);
    }

    public static int getMaxFuelLevel() {
        return getMaxFuelLevel(Config.FUEL_CANISTER_MAXIMUM_FUEL.get());
    }

    public static int getMinimumFuelConsumed(int baseMinimumFuelConsumed) {
        return LargePortableContainerLogic.fuelMinimumConsumption(baseMinimumFuelConsumed);
    }

    public static int getMinimumFuelConsumed() {
        return getMinimumFuelConsumed(Config.FUEL_CANISTER_MINIMUM_TICKS_CONSUMED.get());
    }

    public static int getBurnSpeedMultiplier(ItemStack stack) {
        return getBurnSpeedMultiplier(FuelCanister.getBurnSpeedMultiplier(stack));
    }

    public static int getBurnSpeedMultiplier(int baseBurnSpeedMultiplier) {
        return LargePortableContainerLogic.fuelBurnMultiplier(baseBurnSpeedMultiplier);
    }

    public static void decrementFuel(ItemStack stack) {
        int fuelLevel = FuelCanister.getFuelLevel(stack);
        if (fuelLevel >= getMinimumFuelConsumed()) {
            fuelLevel -= getMinimumFuelConsumed();
        }
        FuelCanister.setFuelLevel(stack, fuelLevel);
    }

    public static void incrementFuel(ItemStack canisterStack, ItemStack fuelStack) {
        int currentFuel = FuelCanister.getFuelLevel(canisterStack);
        int burnTime = fuelStack.getBurnTime(RecipeType.SMELTING);
        if (burnTime == 0) {
            return;
        }

        double burnSpeed = FuelCanister.getBurnSpeed(canisterStack);
        int baseBurnMultiplier = 1;
        Item item = fuelStack.getItem();
        if (item instanceof Coal_T1 coal) {
            baseBurnMultiplier = coal.getBurnSpeedMultiplier();
        } else if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof CoalBlock_T1 coalBlock) {
                baseBurnMultiplier = coalBlock.getBurnSpeedMultiplier();
            }
        }

        int burnMultiplier = LargePortableContainerLogic.fuelBurnMultiplier(baseBurnMultiplier);
        int addedFuel = 0;
        while (currentFuel + addedFuel + burnTime <= getMaxFuelLevel() && !fuelStack.isEmpty()) {
            addedFuel += burnTime;
            fuelStack.shrink(1);
        }

        if (addedFuel > 0) {
            burnSpeed = FuelCanister.calculateBurnSpeed(currentFuel, burnSpeed, addedFuel, burnMultiplier);
        }

        FuelCanister.setFuelLevel(canisterStack, currentFuel + addedFuel);
        FuelCanister.setBurnSpeed(canisterStack, burnSpeed);
    }
}
