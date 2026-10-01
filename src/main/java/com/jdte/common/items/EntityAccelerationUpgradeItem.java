package com.jdte.common.items;

import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class EntityAccelerationUpgradeItem extends Item {
    public EntityAccelerationUpgradeItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.entity_acceleration_upgrade")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }
        ItemStack target = slot.getItem();
        if (target.is(JDTEItems.ULTIMATE_TIME_WAND.get()) && !UltimateTimeWandItem.hasEntityAcceleration(target)) {
            UltimateTimeWandItem.setEntityAcceleration(target, true);
            stack.shrink(1);
            if (player != null) {
                player.playSound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 1.0F, 1.2F);
            }
            return true;
        }
        return false;
    }
}
