package com.jdte.common.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.jdte.common.player.LifeAppleProgression;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public class LifeAppleItem extends Item {
    public LifeAppleItem() {
        super(new Properties()
                .stacksTo(64)
                .food(new FoodProperties.Builder()
                        .nutrition(0)
                        .saturationModifier(0)
                        .alwaysEdible()
                        .build())
        );
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 20;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            LifeAppleProgression.consume(entity);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.life_apple").withStyle(ChatFormatting.RED));
    }
}
