package com.jdte.client.renderers;

import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class AdvancedEnergyTransmitterBER extends RotatingMachineItemRenderer<AdvancedEnergyTransmitterBE> {
    public AdvancedEnergyTransmitterBER(BlockEntityRendererProvider.Context context) {
        super(context, JDTRegistration.TimeCrystal.get());
    }
}
