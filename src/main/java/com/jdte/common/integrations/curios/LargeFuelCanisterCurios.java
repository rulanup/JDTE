package com.jdte.common.integrations.curios;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
public final class LargeFuelCanisterCurios {
    private LargeFuelCanisterCurios() {}
    public static ItemStack stack(Player player){return CuriosApi.getCuriosInventory(player).resolve().flatMap(h->h.getStacksHandler("large_fuel_canister")).filter(h->h.getSlots()>0).map(h->h.getStacks().getStackInSlot(0)).orElse(ItemStack.EMPTY);}
}
