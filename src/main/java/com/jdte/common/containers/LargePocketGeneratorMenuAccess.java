package com.jdte.common.containers;

import com.jdte.common.items.LargePocketGeneratorItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkHooks;

public final class LargePocketGeneratorMenuAccess {
    private LargePocketGeneratorMenuAccess(){}
    public static ItemStack resolve(Player player,boolean curios){return curios?(ModList.get().isLoaded("curios")?com.jdte.common.integrations.curios.LargePocketGeneratorCurios.stack(player):ItemStack.EMPTY):player.getMainHandItem();}
    public static void open(Player player,boolean curios){
        if(!(player instanceof ServerPlayer server)||!server.isAlive()||server.hasDisconnected())return;
        var stack=resolve(player,curios);if(!(stack.getItem() instanceof LargePocketGeneratorItem)||stack.getCount()!=1)return;
        NetworkHooks.openScreen(server,new SimpleMenuProvider((id,inv,p)->new LargePocketGeneratorContainer(id,inv,stack,curios),Component.translatable("item.jdte.large_pocket_generator")),buf->{buf.writeBoolean(curios);buf.writeItem(stack);});
    }
}
