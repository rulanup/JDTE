package com.jdte.client.smoke;

import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.direwolf20.justdirethings.common.items.interfaces.*;
import com.direwolf20.justdirethings.common.items.tools.basetools.BaseBow;
import com.direwolf20.justdirethings.common.entities.JustDireArrow;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.client.JDTEKeyMappings;
import com.jdte.client.screens.LargePotionCanisterScreen;
import com.jdte.common.containers.LargePotionCanisterContainer;
import com.jdte.common.items.*;
import com.jdte.setup.*;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;
import java.util.List;

@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class PotionCanisterClientSmoke {
 private static int stage,ticks,at,batch;
 private static volatile int shots;
 private static volatile boolean done;
 private static volatile RuntimeException failure;
 private static void check(boolean v,String text){if(!v)throw new IllegalStateException("Potion client stage "+stage+": "+text);}
 private static void next(){stage++;at=ticks;com.mojang.logging.LogUtils.getLogger().info("Potion client stage {}",stage);}
 private static void server(Minecraft mc,java.util.function.Consumer<ServerPlayer> action){var s=mc.getSingleplayerServer();var id=mc.player.getUUID();s.execute(()->{try{action.accept(s.getPlayerList().getPlayer(id));}catch(RuntimeException x){failure=x;throw x;}});}
 private static ItemStack potion(){var s=PotionUtils.setPotion(new ItemStack(Items.POTION),Potions.SWIFTNESS);PotionUtils.setCustomEffects(s,List.of(new MobEffectInstance(MobEffects.REGENERATION,321,2,true,false,false)));s.getOrCreateTag().putInt("CustomPotionColor",0x12ab34);return s;}
 private static void seedFour(ServerPlayer p){for(int i=9;i<=12;i++)p.getInventory().setItem(i,potion());p.containerMenu.broadcastChanges();}
 private static top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler curios(net.minecraft.world.entity.player.Player p){return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new).getStacksHandler("large_potion_canister").orElseThrow().getStacks();}
 private static void shift(Minecraft mc,int slot){var m=mc.player.containerMenu;mc.gameMode.handleInventoryMouseClick(m.containerId,slot,0,ClickType.QUICK_MOVE,mc.player);}
 private static void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),msg->{});}
 private static mezz.jei.api.runtime.IJeiRuntime jei(){try{var f=com.jdte.common.jei.JDTEJeiPlugin.class.getDeclaredField("runtime");f.setAccessible(true);return (mezz.jei.api.runtime.IJeiRuntime)f.get(null);}catch(ReflectiveOperationException x){throw new IllegalStateException(x);}}
 @SubscribeEvent public static void arrow(EntityJoinLevelEvent e){if(Boolean.getBoolean("jdte.potionSmoke")&&!e.getLevel().isClientSide&&e.getEntity() instanceof JustDireArrow a){check(a.getEffects().stream().anyMatch(x->x.getEffect()==MobEffects.REGENERATION&&x.getDuration()==321&&x.getAmplifier()==2),"real server arrow carries full custom effect");shots++;}}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event)throws Exception{
  if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.potionSmoke"))return;if(failure!=null)throw failure;var mc=Minecraft.getInstance();int elapsed=++ticks-at;check(ticks<5500,"timeout");if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  switch(stage){
   case 0->{if(mc.player.tickCount<70)return;server(mc,p->{var l=p.serverLevel();var pos=new BlockPos(32,110,32);for(var b:BlockPos.betweenClosed(pos.offset(-4,-1,-4),pos.offset(4,4,4))){l.removeBlockEntity(b);l.setBlockAndUpdate(b,b.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}p.getInventory().clearContent();p.getInventory().selected=0;p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get()));seedFour(p);p.teleportTo(32.5,110,32.5);done=true;});next();}
   case 1->{if(!done||elapsed<30)return;mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);next();}
   case 2->{if(elapsed<25||!(mc.screen instanceof LargePotionCanisterScreen))return;var m=(LargePotionCanisterContainer)mc.player.containerMenu;check(m.slots.size()==37&&PotionCanister.getPotionAmount(m.getCurrentStack())==0,"real registered empty handheld GUI");shot(mc,"jdte-potion-empty.png");for(int i=1;i<=4;i++)shift(mc,i);next();}
   case 3->{if(elapsed<25)return;var m=(LargePotionCanisterContainer)mc.player.containerMenu;check(PotionCanister.getPotionAmount(m.getCurrentStack())==1000&&m.slots.get(0).getItem().is(Items.GLASS_BOTTLE)&&m.slots.get(0).getItem().getCount()==4&&mc.player.getInventory().countItem(Items.POTION)==0,"real four unstackable inputs/one batch/four bottles sync");shot(mc,"jdte-potion-filled.png");shift(mc,0);batch=1;done=false;server(mc,p->{seedFour(p);done=true;});next();}
   case 4->{if(!done||elapsed<25)return;for(int i=1;i<=4;i++)shift(mc,i);next();}
   case 5->{if(elapsed<25)return;var m=(LargePotionCanisterContainer)mc.player.containerMenu;batch++;check(PotionCanister.getPotionAmount(m.getCurrentStack())==batch*1000&&m.slots.get(0).getItem().getCount()==4,"each real batch adds exactly1000");shift(mc,0);if(batch<4){done=false;server(mc,p->{seedFour(p);done=true;});stage=4;at=ticks;}else{done=false;server(mc,p->{seedFour(p);done=true;});next();}}
   case 6->{if(!done||elapsed<25)return;for(int i=1;i<=4;i++)shift(mc,i);next();}
   case 7->{if(elapsed<25)return;var m=(LargePotionCanisterContainer)mc.player.containerMenu;check(PotionCanister.getPotionAmount(m.getCurrentStack())==4000&&m.slots.get(0).getItem().is(Items.POTION)&&m.slots.get(0).getItem().getCount()==4&&mc.player.getInventory().countItem(Items.GLASS_BOTTLE)==16,"full capacity retains all four excess potions; sixteen bottles conserved");shot(mc,"jdte-potion-full-pending.png");shift(mc,0);next();}
   case 8->{if(elapsed<25)return;var m=(LargePotionCanisterContainer)mc.player.containerMenu;check(m.slots.get(0).getItem().isEmpty()&&mc.player.getInventory().countItem(Items.POTION)==4&&PotionCanister.getPotionAmount(m.getCurrentStack())==4000,"blocked batch fully recoverable");mc.player.closeContainer();done=false;server(mc,p->{var c=p.getMainHandItem();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);curios(p).setStackInSlot(0,c);p.inventoryMenu.broadcastChanges();done=true;});next();}
   case 9->{if(!done||elapsed<35)return;check(PotionCanister.getPotionAmount(curios(mc.player).getStackInSlot(0))==4000,"actual dedicated Curios slot NBT sync");KeyMapping.click(JDTEKeyMappings.LARGE_POTION_CANISTER.getKey());net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.InputEvent.Key(org.lwjgl.glfw.GLFW.GLFW_KEY_P,0,org.lwjgl.glfw.GLFW.GLFW_PRESS,0));next();}
   case 10->{if(elapsed<25||!(mc.screen instanceof LargePotionCanisterScreen))return;var m=(LargePotionCanisterContainer)mc.player.containerMenu;check(PotionCanister.getPotionAmount(m.getCurrentStack())==4000&&LargePotionCanisterData.effects(m.getCurrentStack()).stream().anyMatch(x->x.getDuration()==321&&x.getAmplifier()==2),"P event opens worn canister with full effects");shot(mc,"jdte-potion-curios.png");var runtime=jei();check(runtime!=null,"real JEI runtime");var recipes=runtime.getRecipeManager().createRecipeLookup(mezz.jei.api.constants.RecipeTypes.CRAFTING).get().filter(r->r.getId().equals(com.jdte.JDTE.id("large_potion_canister"))||r.getId().equals(com.jdte.JDTE.id("large_fuel_canister"))).toList();check(recipes.size()==2,"both portable source/result wire recipes visible");runtime.getRecipesGui().showRecipes(runtime.getRecipeManager().getRecipeCategory(mezz.jei.api.constants.RecipeTypes.CRAFTING),recipes,java.util.List.of());next();}
   case 11->{if(elapsed<25)return;shot(mc,"jdte-potion-jei.png");mc.screen.onClose();mc.player.closeContainer();done=false;server(mc,p->{var can=curios(p).getStackInSlot(0);curios(p).setStackInSlot(0,ItemStack.EMPTY);var bow=new ItemStack(Registration.EclipseAlloyBow.get());ToggleableTool.setSetting(bow,"enabled",true);for(var a:new Ability[]{Ability.POTIONARROW,Ability.SPLASH,Ability.LINGERING}){bow.getOrCreateTag().putBoolean("upgrade_"+a.getName(),true);ToggleableTool.setSetting(bow,a.getName(),true);}bow.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).receiveEnergy(Integer.MAX_VALUE,false);BaseBow.setPotionCanister(bow,can,0);p.setItemInHand(InteractionHand.MAIN_HAND,bow);p.getInventory().setItem(15,new ItemStack(Items.ARROW,4));p.inventoryMenu.broadcastChanges();done=true;});next();}
   case 12->{if(!done||elapsed<30)return;check(mc.player.getMainHandItem().is(Registration.EclipseAlloyBow.get()),"real bow synchronized");mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);next();}
   case 13->{if(elapsed<30)return;check(mc.player.isUsingItem(),"real client/server bow draw active");mc.options.keyUse.setDown(false);mc.gameMode.releaseUsingItem(mc.player);next();}
   case 14->{if(elapsed<30)return;check(shots==1&&PotionCanister.getPotionAmount(BaseBow.getPotionCanister(mc.player.getMainHandItem(),0))==3900&&mc.player.getInventory().countItem(Items.ARROW)==3,"actual C2S charged/released shot spends100mB and one ammo without capacity clamp");shot(mc,"jdte-potion-native-bow.png");Files.writeString(mc.gameDirectory.toPath().resolve("potion-smoke-success.txt"),"PASS: real graphical Forge client native four-single-potion fill/bottle/full pending recovery, full NBT/Curios/P event/menus/JEI both portable recipes; actual C2S native bow draw/release spawns custom-effect arrow, exact100mB/one ammo, preserves3900mB. Controlled server inventory/Curios material setup, not independent multiplayer.\n");mc.stop();stage=15;}
  }
 }
}
