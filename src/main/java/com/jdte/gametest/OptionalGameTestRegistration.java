package com.jdte.gametest;

import com.jdte.JDTE;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/** Do not let Forge's development-client discovery load absent optional API classes. */
@Mod.EventBusSubscriber(modid=JDTE.MODID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class OptionalGameTestRegistration {
    private OptionalGameTestRegistration() {}
    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        if (ModList.get().isLoaded("ae2")) {
            event.register(AEUpgradeGameTests.class);
            event.register(com.jdte.common.blockentities.AEAccelerationGameTests.class);
            event.register(AECraftingJobGameTests.class);
            event.register(AEFluidMixerJobGameTests.class);
            if (ModList.get().isLoaded("appflux")) {
                event.register(AppFluxUpgradeGameTests.class);
                event.register(TransmitterAppliedFluxGameTests.class);
            }
        }
        if (ModList.get().isLoaded("mysticalagriculture")) event.register(com.jdte.common.blockentities.SeedHarvestGameTests.class);
        if (ModList.get().isLoaded("draconicevolution")) event.register(TransmitterDraconicGameTests.class);
        if (ModList.get().isLoaded("curios")) event.register(CuriosRegistrationGameTests.class);
    }
}
