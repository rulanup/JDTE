package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PotionCanister;

public class LargePotionCanisterItem extends PotionCanister {
    public static int getMaxMB() {
        return LargePortableContainerLogic.potionCapacity();
    }
}
