package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PotionCanister;

public class LargePotionCanisterItem extends PotionCanister {
    public static int getPotionCapacityMb() {
        return LargePortableContainerLogic.potionCapacity();
    }

    public int getCapacityMb() {
        return getPotionCapacityMb();
    }
}
