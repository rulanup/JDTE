package com.jdte.common.blockentities;

import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedBioExtractorBE extends BioExtractorBE {
    public static final int BASE_ENERGY_CAPACITY = 100_000;
    public static final int BASE_FLUID_CAPACITY = 16_000;
    public static final float BASE_RADIUS = 5.0f;
    public static final int OUTPUT_SLOTS = 9;

    public AdvancedBioExtractorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_BIO_EXTRACTOR.get(), pos, state,
                OUTPUT_SLOTS, BASE_ENERGY_CAPACITY, BASE_FLUID_CAPACITY, BASE_RADIUS);
    }

    @Override
    protected int getMaxEntitiesPerTick() {
        if (UpgradeHelper.hasCreativeUpgrade(this) || UpgradeHelper.countUpgrades(this, UpgradeType.OVERCLOCK) > 0) {
            return 2;
        }
        return 1;
    }
}
