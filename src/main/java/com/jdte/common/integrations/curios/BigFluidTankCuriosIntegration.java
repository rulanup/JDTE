package com.jdte.common.integrations.curios;

import com.jdte.setup.JDTEItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;
public class BigFluidTankCuriosIntegration {
    public static final String TANK_SLOT_ID = "big_fluid_tank";

    public static void registerCurioBehaviors() {
        if (!ModList.get().isLoaded("curios")) {
            return;
        }
        top.theillusivec4.curios.api.CuriosApi.registerCurio(
                JDTEItems.LARGE_POCKET_GENERATOR.get(),
                new top.theillusivec4.curios.api.type.capability.ICurioItem() {
                    @Override
                    public void curioTick(top.theillusivec4.curios.api.SlotContext slotContext, ItemStack stack) {
                        LivingEntity entity = slotContext.entity();
                        if (!(entity.level() instanceof ServerLevel level)) return;
                        JDTEItems.LARGE_POCKET_GENERATOR.get()
                                .inventoryTick(stack, level, entity, null);
                    }
                });
        top.theillusivec4.curios.api.CuriosApi.registerCurio(
                JDTEItems.BIG_FLUID_TANK.get(),
                new top.theillusivec4.curios.api.type.capability.ICurioItem() {
                    @Override
                    public void curioTick(top.theillusivec4.curios.api.SlotContext slotContext, ItemStack stack) {
                        LivingEntity entity = slotContext.entity();
                        if (!(entity.level() instanceof ServerLevel level)) return;
                        JDTEItems.BIG_FLUID_TANK.get()
                                .inventoryTick(stack, level, entity, null);
                    }
                });
    }

}
