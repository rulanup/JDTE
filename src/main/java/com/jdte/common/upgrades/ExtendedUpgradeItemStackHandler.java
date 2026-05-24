package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;

public class ExtendedUpgradeItemStackHandler extends UpgradeItemStackHandler {
    public static final int EXTENDED_SLOT_COUNT = 8;

    public ExtendedUpgradeItemStackHandler(BaseMachineBE machine) {
        super(machine, EXTENDED_SLOT_COUNT);
    }
}
