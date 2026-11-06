package com.jdte.common.integrations.curios;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
/** Loaded only after the optional Curios presence check. */
public final class LargePocketGeneratorCurios {
    private LargePocketGeneratorCurios(){}
    public static ItemStack stack(Player player){return CuriosApi.getCuriosInventory(player).resolve().flatMap(h->h.getStacksHandler("large_pocket_generator")).filter(h->h.getSlots()>0).map(h->h.getStacks().getStackInSlot(0)).orElse(ItemStack.EMPTY);}
    /** Neo parent also charges actual equipped Curios, after vanilla inventory entries. */
    public static void chargeEquipment(Player player, com.jdte.common.items.PocketGeneratorEnergyStorage source, int limit) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (var slots : handler.getCurios().values()) {
                var items = slots.getStacks();
                for (int i = 0; i < items.getSlots(); i++)
                    items.getStackInSlot(i).getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY)
                        .ifPresent(target -> source.transferTo(target, limit));
            }
        });
    }
}
