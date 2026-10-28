package com.jdte.common.containers;

import com.jdte.common.items.LargeFuelCanisterItem;
import com.jdte.common.integrations.curios.LargeFuelCanisterCurios;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkHooks;

/** Fuel-only slice of the source portable-menu dispatch; no registrations for unported siblings. */
public final class LargeFuelCanisterMenuAccess {
    private LargeFuelCanisterMenuAccess() {}
    public static ItemStack resolve(Player player,boolean curios){
        return curios?(ModList.get().isLoaded("curios")?LargeFuelCanisterCurios.stack(player):ItemStack.EMPTY):player.getMainHandItem();
    }
    public static void open(Player player,boolean curios){
        if(!(player instanceof ServerPlayer server))return;
        var stack=resolve(player,curios);if(!(stack.getItem() instanceof LargeFuelCanisterItem))return;
        NetworkHooks.openScreen(server,new SimpleMenuProvider((id,inv,p)->new LargeFuelCanisterContainer(id,inv,stack,curios),Component.translatable("item.jdte.large_fuel_canister")),buf->{buf.writeBoolean(curios);buf.writeItem(stack);});
    }
}
