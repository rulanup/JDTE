package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;

public class LargePotionCanisterItem extends PotionCanister {
    public static int getPotionCapacityMb() {
        return LargePortableContainerLogic.potionCapacity();
    }

    public int getCapacityMb() {
        return getPotionCapacityMb();
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
}
