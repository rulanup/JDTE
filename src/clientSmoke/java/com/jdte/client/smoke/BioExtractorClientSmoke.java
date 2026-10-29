package com.jdte.client.smoke;
import com.jdte.client.screens.*;
import com.jdte.common.blockentities.*;
import com.jdte.common.containers.*;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.Files;
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class BioExtractorClientSmoke {
 private static final BlockPos A=new BlockPos(20,100,20),B=A.east(4);
 private static int stage,ticks,at;private static volatile RuntimeException failure;private static volatile boolean done;private static Sheep sheep;private static Cow cow;
 private static void check(boolean b,String s){if(!b)throw new IllegalStateException("Bio client stage "+stage+": "+s);}
 private static void next(){stage++;at=ticks;com.mojang.logging.LogUtils.getLogger().info("Bio extractor client stage {}",stage);}
 private static void server(Minecraft mc,java.util.function.Consumer<ServerPlayer> action){var s=mc.getSingleplayerServer();var id=mc.player.getUUID();s.execute(()->{try{action.accept(s.getPlayerList().getPlayer(id));}catch(RuntimeException e){failure=e;throw e;}});}
 private static BioExtractorBE be(ServerPlayer p,BlockPos pos){return (BioExtractorBE)p.serverLevel().getBlockEntity(pos);}
 private static void open(Minecraft mc,BlockPos pos){mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));}
 private static void shift(Minecraft mc,java.util.function.Predicate<net.minecraft.world.inventory.Slot> predicate){var m=mc.player.containerMenu;var slot=m.slots.stream().filter(predicate).findFirst().orElseThrow();mc.gameMode.handleInventoryMouseClick(m.containerId,slot.index,0,ClickType.QUICK_MOVE,mc.player);}
 private static void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),msg->{});}
 private static mezz.jei.api.runtime.IJeiRuntime jei(){try{var f=com.jdte.common.jei.JDTEJeiPlugin.class.getDeclaredField("runtime");f.setAccessible(true);return (mezz.jei.api.runtime.IJeiRuntime)f.get(null);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e)throws Exception{
  if(e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.bioSmoke"))return;if(failure!=null)throw new IllegalStateException("Server assertion",failure);var mc=Minecraft.getInstance();int elapsed=++ticks-at;check(ticks<4500,"timeout "+mc.screen);if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
  switch(stage){
   case 0->{if(mc.player.tickCount<90)return;server(mc,p->{var l=p.serverLevel();for(var pos:BlockPos.betweenClosed(new BlockPos(17,99,17),new BlockPos(27,104,25)))l.setBlockAndUpdate(pos,pos.getY()==99?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(A,JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get().defaultBlockState());l.setBlockAndUpdate(B,JDTEBlocks.EXTENDED_BIO_EXTRACTOR.get().defaultBlockState());for(var pos:java.util.List.of(A,B)){var b=be(p,pos);UpgradeHelper.getUpgradeHandler(b).insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);b.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;b.getEnergyStorage().setEnergy(b.getMaxEnergy());b.getFluidTank().setFluid(new net.minecraftforge.fluids.FluidStack(net.minecraftforge.common.ForgeMod.MILK.get(),pos.equals(A)?80001:200003));var tag=b.saveWithFullMetadata();tag.putInt("tickCounter",7);b.load(tag);}be(p,A).getMachineHandler().setStackInSlot(8,new ItemStack(Items.EGG,31));be(p,B).getMachineHandler().setStackInSlot(17,new ItemStack(Items.BLUE_WOOL,16));p.getInventory().clearContent();p.getInventory().selected=0;p.getInventory().setItem(9,new ItemStack(Items.STONE,64));p.getInventory().setItem(10,new ItemStack(JDTEItems.FILTER_UPGRADE.get()));p.connection.teleport(22.5,100,23.5,180,0);p.inventoryMenu.broadcastChanges();});next();}
   case 1->{if(elapsed<30)return;open(mc,A);next();}
   case 2->{if(elapsed<30)return;check(mc.screen instanceof AdvancedBioExtractorScreen,"real Advanced GUI registered");var m=(AdvancedBioExtractorContainer)mc.player.containerMenu;check(m.getFluidAmount()==80001&&m.getEnergy()==5000000&&m.getScanProgress()==7,"full-width milk/FE/progress sync");check(m.slots.stream().filter(s->s instanceof UpgradeSlot).count()==4,"four real slots");shot(mc,"jdte-bio-advanced.png");shift(mc,s->s.getItem().is(Items.EGG));shift(mc,s->s.getItem().is(Items.STONE));shift(mc,s->s.getItem().is(JDTEItems.FILTER_UPGRADE.get()));next();}
   case 3->{if(elapsed<30)return;check(mc.player.getInventory().countItem(Items.EGG)==31&&mc.player.getInventory().countItem(Items.STONE)==64,"output extraction and rejected manual input conserve items");check(UpgradeHelper.countUpgrades(((AdvancedBioExtractorContainer)mc.player.containerMenu).baseMachineBE,UpgradeType.FILTER)==1,"actual packet installs filter");shot(mc,"jdte-bio-filter-layout.png");mc.player.closeContainer();next();}
   case 4->{if(elapsed<20)return;open(mc,B);next();}
   case 5->{if(elapsed<30)return;check(mc.screen instanceof ExtendedBioExtractorScreen,"real Extended GUI registered");var m=(ExtendedBioExtractorContainer)mc.player.containerMenu;check(m.getFluidAmount()==200003&&m.getEnergy()==10000000&&m.slots.stream().filter(s->s instanceof UpgradeSlot).count()==8,"Extended full-width milk/FE/eight cards");shot(mc,"jdte-bio-extended.png");shift(mc,s->s.getItem().is(Items.BLUE_WOOL));next();}
   case 6->{if(elapsed<25)return;check(mc.player.getInventory().countItem(Items.BLUE_WOOL)==16,"eighteenth output works through actual menu");mc.player.closeContainer();var runtime=jei();check(runtime!=null,"real JEI runtime available");check(mc.getResourceManager().getResource(com.jdte.client.jei.bioextractor.BioExtractorRecipeCategory.FLUID_BAR_TEXTURE).isPresent(),"JEI fluid frame texture exists in actual resource manager");var type=com.jdte.client.jei.bioextractor.BioExtractorRecipeCategory.RECIPE_TYPE;check(runtime.getRecipeManager().createRecipeLookup(type).get().count()==9,"JEI has all nine live source recipes");runtime.getRecipesGui().showTypes(java.util.List.of(type));next();}
   case 7->{if(elapsed<25)return;shot(mc,"jdte-bio-jei.png");mc.screen.onClose();if(mc.player.containerMenu!=mc.player.inventoryMenu)mc.player.closeContainer();mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(A));next();}
   case 8->{mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(A));if(elapsed<50)return;var data=snownee.jade.impl.ObjectDataCenter.getServerData();check(data!=null&&data.getInt("bioSlots")==9&&data.getInt("bioInterval")==20,"real Jade packet contains Bio status");shot(mc,"jdte-bio-jade.png");server(mc,p->{var b=be(p,A);for(int i=0;i<9;i++)b.getMachineHandler().setStackInSlot(i,ItemStack.EMPTY);b.getFluidTank().setFluid(net.minecraftforge.fluids.FluidStack.EMPTY);b.getEnergyStorage().setEnergy(100000);var u=UpgradeHelper.getUpgradeHandler(b);for(int i=1;i<u.getSlots();i++)u.extractItem(i,1,false);b.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.IGNORED;sheep=EntityType.SHEEP.create(p.serverLevel());sheep.setPos(21.5,100,20.5);sheep.setNoAi(true);sheep.setColor(DyeColor.BLUE);p.serverLevel().addFreshEntity(sheep);});next();}
   case 9->{if(elapsed<65)return;server(mc,p->{var a=be(p,A);int total=0;for(int i=0;i<9;i++)if(a.getMachineHandler().getStackInSlot(i).is(Items.BLUE_WOOL))total+=a.getMachineHandler().getStackInSlot(i).getCount();check(sheep.isAlive()&&sheep.isSheared()&&total==2&&a.getEnergyStorage().getEnergyStored()==97000,"real world ticks harvest one colored sheep for exact cost");a.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;var b=be(p,B);b.getFluidTank().setFluid(net.minecraftforge.fluids.FluidStack.EMPTY);b.getEnergyStorage().setEnergy(200000);b.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.IGNORED;cow=EntityType.COW.create(p.serverLevel());cow.setPos(24.5,100,21.5);cow.setNoAi(true);p.serverLevel().addFreshEntity(cow);});next();}
   case 10->{if(elapsed<65)return;server(mc,p->{var b=be(p,B);check(cow.isAlive()&&b.getFluidTank().getFluidAmount()==1000&&b.getEnergyStorage().getEnergyStored()==195000,"real world ticks milk cow once for exact cost/cooldown");b.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;done=true;});next();}
   case 11->{if(elapsed<30||!done)return;open(mc,B);next();}
   case 12->{if(elapsed<30)return;check(((ExtendedBioExtractorContainer)mc.player.containerMenu).getFluidAmount()==1000&&((ExtendedBioExtractorContainer)mc.player.containerMenu).getEnergy()==195000,"real produced milk and FE synchronize");shot(mc,"jdte-bio-real-production.png");Files.writeString(mc.gameDirectory.toPath().resolve("bio-smoke-success.txt"),"PASS: actual Advanced/Extended GUIs, full-width FE/milk/progress, 4/8 upgrades, real item extraction/rejected insertion/filter installation, live JEI nine recipes, real Jade status packets; natural world ticks produce colored sheep wool and cow milk with exact FE and cooldown.\n");mc.stop();stage=13;}
  }
 }
}
