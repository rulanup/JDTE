package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.direwolf20.justdirethings.setup.Config;

public class LargePocketGeneratorItem extends PocketGenerator {
    public static int getScaledMaxEnergy(int basePocketCapacity) {
        return LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity);
    }

    @Override
    public int getMaxEnergy() {
        return getScaledMaxEnergy(Config.POCKET_GENERATOR_MAX_FE.get());
    }
}
