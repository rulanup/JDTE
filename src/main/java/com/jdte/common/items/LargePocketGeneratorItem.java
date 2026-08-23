package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.jdte.common.containers.LargePortableContainerMenus;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import com.direwolf20.justdirethings.setup.Config;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LargePocketGeneratorItem extends PocketGenerator {
    public static int getScaledMaxEnergy(int basePocketCapacity) {
        return LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity);
    }

    @Override
    public int getMaxEnergy() {
        return getScaledMaxEnergy(Config.POCKET_GENERATOR_MAX_FE.get());
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
