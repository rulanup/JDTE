package com.jdte.client.emi;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.client.MachineScreenAreaProvider;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;

@EmiEntrypoint
public final class JDTEEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addExclusionArea(BaseMachineScreen.class, (screen, consumer) -> {
            if (!(screen instanceof MachineScreenAreaProvider provider)) {
                return;
            }
            provider.jdte$getMachineScreenAreas().forEach(area -> consumer.accept(new Bounds(
                    area.getX(), area.getY(), area.getWidth(), area.getHeight())));
        });
    }
}