package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blocks.resources.CoalBlock_T1;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.resources.Coal_T1;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.setup.Config;
import com.jdte.common.containers.LargePortableContainerMenus;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.entity.FuelValues;
import java.util.function.Consumer;

public class LargeFuelCanisterItem extends FuelCanister {
    public LargeFuelCanisterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        Level level = context.level();
        if (level == null) {
            return;
        }

        if (Minecraft.getInstance().hasShiftDown()) {
            tooltip.accept(Component.translatable("justdirethings.fuelcanisteramt",
                    MagicHelpers.formatted(FuelCanister.getFuelLevel(stack))).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.accept(Component.translatable("justdirethings.fuelcanisteritemsamt",
                    fuelItemEquivalentTooltip(FuelCanister.getFuelLevel(stack))).withStyle(ChatFormatting.AQUA));
        }
    }

    static String fuelItemEquivalentTooltip(int fuelLevel) {
        return fuelItemEquivalentTooltip(fuelLevel, getMinimumFuelConsumed());
    }

    static String fuelItemEquivalentTooltip(int fuelLevel, int minimumFuelConsumed) {
        return MagicHelpers.formatted((float) fuelLevel / minimumFuelConsumed);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        LargePortableContainerMenus.openFromMainHand(
                player, OpenLargePortableContainerPayload.ContainerKind.LARGE_FUEL_CANISTER);
        return InteractionResult.PASS;
    }

    @Override
    public int getBurnTime(ItemStack stack, RecipeType<?> recipeType, FuelValues fuels) {
        return FuelCanister.getFuelLevel(stack) >= getMinimumFuelConsumed() ? getMinimumFuelConsumed() : 0;
    }

    @Override
    public ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        if (!(instance instanceof ItemStack stack)) return null;
        ItemStack remaining = stack.copy();
        decrementFuel(remaining);
        return ItemStackTemplate.fromNonEmptyStack(remaining);
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

    public static int getFullness(ItemStack stack) {
        int fuelLevel = FuelCanister.getFuelLevel(stack);
        if (fuelLevel <= 0) {
            return 0;
        }
        return Math.min(4, (int) Math.ceil((double) fuelLevel * 4 / getMaxFuelLevel()));
    }

    public static void decrementFuel(ItemStack stack) {
        int fuelLevel = FuelCanister.getFuelLevel(stack);
        if (fuelLevel >= getMinimumFuelConsumed()) {
            fuelLevel -= getMinimumFuelConsumed();
        }
        FuelCanister.setFuelLevel(stack, fuelLevel);
    }

    public static int incrementFuel(ItemStack canisterStack, ItemStack fuelStack, FuelValues fuels) {
        int currentFuel = FuelCanister.getFuelLevel(canisterStack);
        int burnTime = fuelStack.getBurnTime(RecipeType.SMELTING, fuels);
        if (burnTime <= 0) return 0;

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
        int consumed = Math.min(fuelStack.getCount(), Math.max(0, getMaxFuelLevel() - currentFuel) / burnTime);
        int addedFuel = consumed * burnTime;

        if (addedFuel > 0) {
            burnSpeed = FuelCanister.calculateBurnSpeed(currentFuel, burnSpeed, addedFuel, burnMultiplier);
        }

        FuelCanister.setFuelLevel(canisterStack, currentFuel + addedFuel);
        FuelCanister.setBurnSpeed(canisterStack, burnSpeed);
        return consumed;
    }
}
