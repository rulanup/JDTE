package com.jdte.client.smoke;

import com.jdte.common.items.*;
import com.jdte.common.containers.LargePocketGeneratorContainer;
import com.jdte.client.screens.LargePocketGeneratorScreen;
import com.jdte.client.JDTEKeyMappings;
import com.jdte.setup.JDTEItems;
import com.direwolf20.justdirethings.common.items.PocketGenerator;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;

/** Real client packets and natural inventory/Curios ticks; materials/equipment setup is controlled. */
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class PocketGeneratorClientSmoke {
    private static int stage,ticks,at;private static volatile boolean done,ledger;private static volatile RuntimeException failure;private static java.util.UUID actor;private static volatile int observations,curiosObservations;
    private static LargePocketGeneratorItem item(){return JDTEItems.LARGE_POCKET_GENERATOR.get();}
    private static void check(boolean v,String text){if(!v)throw new IllegalStateException("Pocket client stage "+stage+": "+text);}
    private static void next(){stage++;at=ticks;com.mojang.logging.LogUtils.getLogger().info("Pocket graphical stage {}",stage);}
    private static void server(Minecraft mc,java.util.function.Consumer<ServerPlayer> action){var server=mc.getSingleplayerServer();var id=mc.player.getUUID();server.execute(()->{try{action.accept(server.getPlayerList().getPlayer(id));}catch(RuntimeException x){failure=x;throw x;}});}
    private static top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler worn(net.minecraft.world.entity.player.Player p){return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new).getStacksHandler("large_pocket_generator").orElseThrow(IllegalStateException::new).getStacks();}
    private static ItemStack generator(net.minecraft.world.entity.player.Player p){var held=p.getMainHandItem();return held.is(JDTEItems.LARGE_POCKET_GENERATOR.get())?held:worn(p).getStackInSlot(0);}
    private static void shift(Minecraft mc,int slot){mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,slot,0,ClickType.QUICK_MOVE,mc.player);}
    private static void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),x->{});}
    private static void toggle(){com.direwolf20.justdirethings.common.network.PacketHandler.CHANNEL.sendToServer(new com.direwolf20.justdirethings.common.network.data.ToggleToolPayload("enabled"));}
    @SubscribeEvent public static void playerTick(TickEvent.PlayerTickEvent event){
        if(!Boolean.getBoolean("jdte.pocketSmoke")||event.phase!=TickEvent.Phase.END||event.player.level().isClientSide||!ledger||!event.player.getUUID().equals(actor))return;
        try{var p=event.player;var s=generator(p);check(s.is(JDTEItems.LARGE_POCKET_GENERATOR.get()),"controlled physical item remains held or equipped");int outside=p.getInventory().countItem(Items.COAL);var pending=LargePocketGeneratorItem.fuel(s).getStackInSlot(0);int fuel=pending.is(Items.COAL)?pending.getCount():0;var cursor=p.containerMenu.getCarried();if(cursor.is(Items.COAL))outside+=cursor.getCount();int remaining=s.hasTag()?s.getTag().getInt(PocketGenerator.COUNTER):0;int credits=(outside+fuel)*400+remaining;int stored=LargePocketGeneratorItem.energy(s).getEnergyStored();int charged=p.getInventory().getItem(1).getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).getEnergyStored();check((3200L-credits)*60==stored+charged,"natural-work conservation: credits="+credits+", FE="+stored+"+"+charged);observations++;if(p.getMainHandItem().isEmpty())curiosObservations++;}catch(RuntimeException x){failure=x;throw x;}
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e)throws Exception{
        if(e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.pocketSmoke"))return;if(failure!=null)throw failure;var mc=Minecraft.getInstance();int elapsed=++ticks-at;check(ticks<5000,"timeout");if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        switch(stage){
            case 0->{if(mc.player.tickCount<70)return;server(mc,p->{var l=p.serverLevel();var pos=new BlockPos(32,110,32);for(var b:BlockPos.betweenClosed(pos.offset(-4,-1,-4),pos.offset(4,4,4))){l.removeBlockEntity(b);l.setBlockAndUpdate(b,b.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}p.getInventory().clearContent();p.getInventory().selected=0;var s=new ItemStack(JDTEItems.LARGE_POCKET_GENERATOR.get());s.setHoverName(net.minecraft.network.chat.Component.literal("Large Pocket Generator"));item().setEnabled(s,false);p.setItemInHand(InteractionHand.MAIN_HAND,s);p.getInventory().setItem(1,new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get()));p.getInventory().setItem(9,new ItemStack(Items.COAL,8));p.teleportTo(32.5,110,32.5);actor=p.getUUID();ledger=true;p.inventoryMenu.broadcastChanges();done=true;});next();}
            case 1->{if(!done||elapsed<30)return;mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);next();}
            case 2->{if(elapsed<25||!(mc.screen instanceof LargePocketGeneratorScreen))return;var m=(LargePocketGeneratorContainer)mc.player.containerMenu;check(m.slots.size()==37&&LargePocketGeneratorItem.energy(m.getCurrentStack()).getMaxEnergyStored()==4_000_000,"registered actual GUI/capacity");mc.gameMode.handleInventoryMouseClick(m.containerId,28,0,ClickType.PICKUP,mc.player);check(m.getCarried().isEmpty()&&mc.player.getMainHandItem().is(JDTEItems.LARGE_POCKET_GENERATOR.get()),"bound selected item cannot be predicted out of itself");shift(mc,1);next();}
            case 3->{if(elapsed<25)return;var m=(LargePocketGeneratorContainer)mc.player.containerMenu;check(m.slots.get(0).getItem().getCount()==8&&LargePocketGeneratorItem.energy(m.getCurrentStack()).getEnergyStored()==0,"actual C2S shift insert persists all eight fuels while disabled");shot(mc,"jdte-pocket-disabled.png");toggle();next();}
            case 4->{if(elapsed<45)return;var m=(LargePocketGeneratorContainer)mc.player.containerMenu;check(item().getEnabled(m.getCurrentStack())&&m.slots.get(0).getItem().getCount()==7&&m.getCurrentStack().getTag().getInt(PocketGenerator.COUNTER)>0&&mc.player.getInventory().getItem(1).getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).getEnergyStored()>0,"actual native toggle packet enables natural production and real equipment charging");shot(mc,"jdte-pocket-running.png");shift(mc,0);shift(mc,0);next();}
            case 5->{if(elapsed<25)return;var m=(LargePocketGeneratorContainer)mc.player.containerMenu;check(m.slots.get(0).getItem().isEmpty()&&mc.player.getInventory().countItem(Items.COAL)==7,"repeated extraction during natural production returns exactly seven remaining coal");for(int i=0;i<36;i++)if(mc.player.getInventory().getItem(i).is(Items.COAL)){shift(mc,i<9?28+i:i-8);break;}next();}
            case 6->{if(elapsed<25)return;var m=(LargePocketGeneratorContainer)mc.player.containerMenu;check(m.slots.get(0).getItem().getCount()==7&&mc.player.getInventory().countItem(Items.COAL)==0,"live reinsertion cannot restore the already consumed coal");mc.player.closeContainer();done=false;server(mc,p->{var s=p.getMainHandItem();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);worn(p).setStackInSlot(0,s);p.inventoryMenu.broadcastChanges();done=true;});next();}
            case 7->{if(!done||elapsed<50)return;check(curiosObservations>=20&&worn(mc.player).getStackInSlot(0).is(JDTEItems.LARGE_POCKET_GENERATOR.get()),"actual Curios slot ticks naturally with exact resource ledger");KeyMapping.click(JDTEKeyMappings.LARGE_POCKET_GENERATOR.getKey());net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.InputEvent.Key(org.lwjgl.glfw.GLFW.GLFW_KEY_G,0,org.lwjgl.glfw.GLFW.GLFW_PRESS,0));next();}
            case 8->{if(elapsed<25||!(mc.screen instanceof LargePocketGeneratorScreen))return;var m=(LargePocketGeneratorContainer)mc.player.containerMenu;check(m.getCurrentStack().is(JDTEItems.LARGE_POCKET_GENERATOR.get())&&m.slots.get(0).getItem().is(Items.COAL),"actual G key opens same live equipped generator");shot(mc,"jdte-pocket-curios.png");done=false;server(mc,p->{ledger=false;var s=worn(p).getStackInSlot(0);item().setEnabled(s,false);LargePocketGeneratorItem.energy(s).forceReceiveEnergy(3_456_789,false); // Fixture bar/sync boundary, not claimed generated FE.
                s.getOrCreateTag().putInt("Energy",3_456_789);p.containerMenu.broadcastChanges();done=true;});next();}
            case 9->{if(!done||elapsed<30)return;var m=(LargePocketGeneratorContainer)mc.player.containerMenu;check(LargePocketGeneratorItem.energy(m.getCurrentStack()).getEnergyStored()==3_456_789&&!item().getEnabled(m.getCurrentStack()),"full-width real Curios item NBT synchronizes FE above short range");shot(mc,"jdte-pocket-full-width.png");var f=com.jdte.common.jei.JDTEJeiPlugin.class.getDeclaredField("runtime");f.setAccessible(true);var jei=(mezz.jei.api.runtime.IJeiRuntime)f.get(null);check(jei!=null,"actual JEI runtime");var recipes=jei.getRecipeManager().createRecipeLookup(mezz.jei.api.constants.RecipeTypes.CRAFTING).get().filter(r->java.util.Set.of("large_pocket_generator","large_fuel_canister","large_potion_canister").contains(r.getId().getPath())&&r.getId().getNamespace().equals("jdte")).toList();check(recipes.size()==3,"all three preserving portable recipe IDs survive actual wire/JEI");jei.getRecipesGui().showRecipes(jei.getRecipeManager().getRecipeCategory(mezz.jei.api.constants.RecipeTypes.CRAFTING),recipes,java.util.List.of());next();}
            case 10->{if(elapsed<25)return;shot(mc,"jdte-pocket-jei.png");check(observations>100&&curiosObservations>20,"sufficient actual natural inventory and Curios ledger observations");Files.writeString(mc.gameDirectory.toPath().resolve("pocket-smoke-success.txt"),"PASS: real graphical handheld/Curios/G menus, native toggle C2S, natural production+charging and concurrent/repeated menu moves with exact FE/fuel/burn-credit ledger ("+observations+" samples, "+curiosObservations+" worn), no bound-item prediction,3456789FE full-width sync and actual JEI all three preserving portable recipes. Controlled material/equipment setup; no independent multiplayer claim.\n");mc.stop();stage=11;}
        }
    }
}
