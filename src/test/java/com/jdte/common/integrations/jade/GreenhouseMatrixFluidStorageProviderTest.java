package com.jdte.common.integrations.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreenhouseMatrixFluidStorageProviderTest {
    @Test
    void decodesAggregateTankDataOnClient() {
        JDTEJadePlugin.GreenhouseMatrixFluidStorageProvider provider =
                new JDTEJadePlugin.GreenhouseMatrixFluidStorageProvider();
        CompoundTag encoded = FluidView.writeDefault(JadeFluidObject.of(Fluids.WATER, 250), 1_000);

        List<ClientViewGroup<FluidView>> groups = provider.getClientGroups(
                null, List.of(new ViewGroup<>(List.of(encoded))));

        assertEquals(1, groups.size());
        assertEquals(1, groups.getFirst().views.size());
        assertEquals(0.25F, groups.getFirst().views.getFirst().ratio);
    }
}
