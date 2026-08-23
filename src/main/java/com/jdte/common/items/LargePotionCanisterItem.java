package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.direwolf20.justdirethings.util.MagicHelpers;
import net.minecraft.core.component.DataComponents;
import com.jdte.common.containers.LargePortableContainerMenus;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

import java.util.List;

public class LargePotionCanisterItem extends PotionCanister {
    public static int getPotionCapacityMb() {
        return LargePortableContainerLogic.potionCapacity();
    }

    public static int getFullness(ItemStack stack) {
        int amount = getPotionAmount(stack);
        if (amount <= 0) {
            return 0;
        }
        return Math.min(4, (int) Math.ceil((double) amount / LargePortableContainerLogic.POTION_BATCH_MB));
    }

    public int getCapacityMb() {
        return getPotionCapacityMb();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Level level = context.level();
        if (level == null) {
            return;
        }

        PotionContents contents = getPotionContents(stack);
        int amount = getPotionAmount(stack);
        if (amount == 0 || contents.equals(PotionContents.EMPTY)) {
            return;
        }

        tooltip.add(Component.literal(potionAmountTooltip(amount)));
        contents.addPotionTooltip(tooltip::add, 1.0F, 20.0F);
    }

    static String potionAmountTooltip(int amount) {
        return MagicHelpers.formatted(amount) + "/" + MagicHelpers.formatted(getPotionCapacityMb());
    }

    public static boolean tryFillBatch(ItemStack canister, ItemStack potionInput) {
        if (!(potionInput.getItem() instanceof PotionItem) || potionInput.getCount() < 4) {
            return false;
        }

        PotionContents inputContents = potionInput.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        PotionContents currentContents = getPotionContents(canister);
        int currentAmount = getPotionAmount(canister);
        if ((!currentContents.equals(PotionContents.EMPTY) && !currentContents.equals(inputContents))
                || !LargePortableContainerLogic.canFillPotionBatch(potionInput.getCount(), currentAmount, getPotionCapacityMb())) {
            return false;
        }

        if (currentContents.equals(PotionContents.EMPTY)) {
            canister.set(JustDireDataComponents.POTION_CONTENTS, inputContents);
        }
        canister.set(JustDireDataComponents.POTION_AMOUNT, currentAmount + 1_000);
        potionInput.shrink(4);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        LargePortableContainerMenus.openFromMainHand(
                player, OpenLargePortableContainerPayload.ContainerKind.LARGE_POTION_CANISTER);
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }
}
