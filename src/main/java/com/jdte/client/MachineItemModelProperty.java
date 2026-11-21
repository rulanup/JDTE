package com.jdte.client;

import com.jdte.common.items.FactoryPackageItem;
import com.jdte.common.items.LargeFuelCanisterItem;
import com.jdte.common.items.LargePocketGeneratorItem;
import com.jdte.common.items.LargePotionCanisterItem;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.common.items.UltimateTimeWandData;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;

import java.util.function.ToDoubleFunction;

enum MachineItemModelProperty implements RangeSelectItemModelProperty {
    FILLED("filled", stack -> FactoryPackageItem.isFilled(stack) ? 1 : 0),
    FULLNESS("fullness", MachineItemModelProperty::fullness),
    MODE("mode", stack -> UltimateTimeWandData.Mode.fromName(stack.getOrDefault(
            JDTEDataComponents.ULTIMATE_TIME_WAND_MODE.get(), UltimateTimeWandData.Mode.NORMAL.serializedName())).ordinal()),
    GENERATOR_ENABLED("generator_enabled", LargePocketGeneratorItem::getEnabledProperty),
    POTION_FULLNESS("potion_fullness", LargePotionCanisterItem::getFullness),
    FUEL_FULLNESS("fuel_fullness", LargeFuelCanisterItem::getFullness);

    final String id;
    private final MapCodec<MachineItemModelProperty> codec = MapCodec.unit(this);
    private final ToDoubleFunction<ItemStack> property;

    MachineItemModelProperty(String id, ToDoubleFunction<ItemStack> property) {
        this.id = id;
        this.property = property;
    }

    @Override public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
        return (float) property.applyAsDouble(stack);
    }

    @Override public MapCodec<MachineItemModelProperty> type() { return codec; }

    private static float fullness(ItemStack stack) {
        if (stack.is(JDTEItems.ULTIMATE_PORTAL_GUN.get())) return UltimatePortalGunItem.getFullness(stack);
        if (!stack.is(JDTEItems.ULTIMATE_TIME_WAND.get())) return 0;
        var storage = ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM);
        return storage == null || storage.size() == 0 ? 0 : (float) storage.getAmountAsInt(0)
                / Math.max(1, JDTEConfig.COMMON.ultimateTimeWandFluidCapacity.get());
    }
}
