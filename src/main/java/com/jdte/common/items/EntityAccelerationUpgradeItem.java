package com.jdte.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

/** Source machine crafting ingredient and one-time wand attachment. Wand gameplay is a separate port. */
public final class EntityAccelerationUpgradeItem extends Item {
    public static final String WAND_FLAG="jdte:ultimate_time_wand_entity_acceleration";
    public EntityAccelerationUpgradeItem(){super(new Properties().stacksTo(64));}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable("tooltip.jdte.entity_acceleration_upgrade").withStyle(ChatFormatting.GRAY));}
    @Override public boolean overrideStackedOnOther(ItemStack stack,Slot slot,ClickAction action,Player player){
        var target=slot.getItem();
        if(action!=ClickAction.SECONDARY || !slot.allowModification(player)
                || !BuiltInRegistries.ITEM.getKey(target.getItem()).toString().equals("jdte:ultimate_time_wand")
                || target.getOrCreateTag().getBoolean(WAND_FLAG))return false;
        target.getOrCreateTag().putBoolean(WAND_FLAG,true);slot.setChanged();stack.shrink(1);
        player.playSound(SoundEvents.ARMOR_EQUIP_NETHERITE,1,1.2f);return true;
    }
}
