package com.jdte.common.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UltimateOverclockUpgradeItem extends UpgradeCardItem {
    public UltimateOverclockUpgradeItem(Properties properties) {
        super(properties, UpgradeType.ULTIMATE_OVERCLOCK);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }
        ItemStack target = slot.getItem();
        if (target.is(JDTEItems.ULTIMATE_TIME_WAND.get()) && !UltimateTimeWandItem.hasUltimateOverclock(target)) {
            UltimateTimeWandItem.setUltimateOverclock(target, true);
            stack.shrink(1);
            if (player != null) {
                player.playSound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 1.0F, 1.2F);
            }
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(net.minecraft.network.chat.Component.translatable("tooltip.jdte.ultimate_overclock_upgrade.wand")
                .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
    }
}
