package com.jdte.client.smoke;
import com.jdte.common.blockentities.*;
import com.jdte.common.containers.*;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class EntityAcceleratorClientSmoke {
 private static final BlockPos A=new BlockPos(32,110,32),B=A.east(8);private static int stage,ticks,at;private static volatile RuntimeException failure;private static volatile boolean done;private static Cow cow;private static volatile int expectedEnergy;
 private static void check(boolean b,String s){if(!b)throw new IllegalStateException("Entity GUI stage "+stage+": "+s);}
 private static void next(){stage++;at=ticks;com.mojang.logging.LogUtils.getLogger().info("Entity accelerator GUI stage {}",stage);}
 private static void server(Minecraft mc,java.util.function.Consumer<ServerPlayer> action){var s=mc.getSingleplayerServer();var id=mc.player.getUUID();s.execute(()->{try{action.accept(s.getPlayerList().getPlayer(id));}catch(RuntimeException e){failure=e;throw e;}});}
 private static AdvancedEntityAcceleratorBE be(ServerPlayer p,BlockPos pos){return (AdvancedEntityAcceleratorBE)p.serverLevel().getBlockEntity(pos);}
 private static void open(Minecraft mc,BlockPos pos){mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));}
 private static void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),msg->{});}
 private static mezz.jei.api.runtime.IJeiRuntime jei(){try{var f=com.jdte.common.jei.JDTEJeiPlugin.class.getDeclaredField("runtime");f.setAccessible(true);return (mezz.jei.api.runtime.IJeiRuntime)f.get(null);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event)throws Exception{if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.entitySmoke"))return;if(failure!=null)throw failure;var mc=Minecraft.getInstance();int elapsed=++ticks-at;check(ticks<4500,"timeout");if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;switch(stage){
 case 0->{if(mc.player.tickCount<90)return;server(mc,p->{var l=p.serverLevel();for(var pos:BlockPos.betweenClosed(A.offset(-2,-1,-2),B.offset(2,3,2)))l.setBlockAndUpdate(pos,pos.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());for(var pos:java.util.List.of(A,B)){l.setBlockAndUpdate(pos,(pos.equals(A)?JDTEBlocks.ADVANCED_ENTITY_ACCELERATOR.get():JDTEBlocks.EXTENDED_ENTITY_ACCELERATOR.get()).defaultBlockState());var b=be(p,pos);UpgradeHelper.getUpgradeHandler(b).insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);b.getEnergyStorage().setEnergy(pos.equals(A)?9000001:24000003);b.getFluidTank().setFluid(new net.minecraftforge.fluids.FluidStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(),123457));b.setMultiplier(pos.equals(A)?17:257);b.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;b.evaluateRedstone();}p.teleportTo(A.getX()+.5,A.getY(),A.getZ()+2.5);p.getInventory().clearContent();p.getInventory().setItem(9,new ItemStack(JDTEItems.FILTER_UPGRADE.get()));p.inventoryMenu.broadcastChanges();done=true;});next();}
 case 1->{if(!done||elapsed<40)return;open(mc,A);next();}
 case 2->{if(!(mc.player.containerMenu instanceof AdvancedEntityAcceleratorContainer m)||elapsed<30)return;check(m.getEnergy()==9000001&&m.getFluidAmount()==123457&&m.getMultiplier()==17,"full-width Advanced buffers/multiplier");check(UpgradeHelper.getUpgradeHandler(m.baseMachineBE).getSlots()==4,"four actual card slots");check(net.minecraft.client.resources.language.I18n.exists("config.jade.plugin_jdte.entity_accelerator_status")&&net.minecraft.client.resources.language.I18n.exists("config.jade.plugin_jdte.bio_extractor_status"),"both Jade config labels present");shot(mc,"jdte-entity-advanced.png");com.jdte.common.network.JDTEPacketHandler.CHANNEL.sendToServer(new com.jdte.common.network.data.TimeAcceleratorPayload(Integer.MAX_VALUE));next();}
 case 3->{if(elapsed<30)return;check(((AdvancedEntityAcceleratorContainer)mc.player.containerMenu).getMultiplier()==64,"server clamps hostile multiplier and synchronizes");mc.player.closeContainer();server(mc,p->p.teleportTo(B.getX()+.5,B.getY(),B.getZ()+2.5));next();}
 case 4->{if(elapsed<30)return;open(mc,B);next();}
 case 5->{if(!(mc.player.containerMenu instanceof ExtendedEntityAcceleratorContainer m)||elapsed<30)return;check(m.getEnergy()==24000003&&m.getFluidAmount()==123457&&m.getMultiplier()==257,"full-width Extended buffers/multiplier");check(UpgradeHelper.getUpgradeHandler(m.baseMachineBE).getSlots()==8,"eight actual card slots");shot(mc,"jdte-entity-extended.png");var runtime=jei();check(runtime!=null,"real JEI runtime");var recipes=runtime.getRecipeManager().createRecipeLookup(mezz.jei.api.constants.RecipeTypes.CRAFTING).get().toList();check(recipes.stream().anyMatch(r->r.getId().equals(com.jdte.JDTE.id("advanced_entity_accelerator")))&&recipes.stream().anyMatch(r->r.getId().equals(com.jdte.JDTE.id("extended_entity_accelerator"))),"both actual crafting recipes visible to JEI");runtime.getRecipesGui().showRecipes(runtime.getRecipeManager().getRecipeCategory(mezz.jei.api.constants.RecipeTypes.CRAFTING),recipes.stream().filter(r->r.getId().equals(com.jdte.JDTE.id("advanced_entity_accelerator"))).toList(),java.util.List.of());next();}
 case 6->{if(elapsed<30)return;shot(mc,"jdte-entity-jei.png");mc.screen.onClose();mc.player.closeContainer();done=false;server(mc,p->{var b=be(p,B);b.setMultiplier(4);b.getAreaAffectingData().xRadius=2;b.getAreaAffectingData().yRadius=2;b.getAreaAffectingData().zRadius=2;cow=new Cow(EntityType.COW,p.serverLevel());cow.setAge(-100000);cow.setNoAi(false);cow.setPersistenceRequired();cow.moveTo(B.getX()+.5,B.getY()+1,B.getZ()+.5);p.serverLevel().addFreshEntity(cow);b.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.IGNORED;b.evaluateRedstone();done=true;});next();}
 case 7->{if(!done||elapsed<60)return;done=false;server(mc,p->{var b=be(p,B);b.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;b.evaluateRedstone();expectedEnergy=b.getEnergyStorage().getEnergyStored();int spent=24000003-expectedEnergy;int batch=TimeAcceleratorCostMath.energyCost(3,com.direwolf20.justdirethings.setup.Config.TIME_WAND_FE_COST.get());check(spent>0&&spent%batch==0&&cow.getAge()>-100000,"real natural paid entity acceleration");done=true;});next();}
 case 8->{if(!done||elapsed<20)return;open(mc,B);next();}
 case 9->{if(!(mc.player.containerMenu instanceof ExtendedEntityAcceleratorContainer m)||elapsed<30)return;check(m.getEnergy()==expectedEnergy&&m.getMultiplier()==4,"real production buffers/config synchronize");var clientCow=mc.level.getEntity(cow.getId());check(clientCow!=null&&com.jdte.common.manager.EntityAccelerationManager.runAcceleratedTicks(clientCow,4)==0,"client-side entity cannot receive server sub-ticks");shot(mc,"jdte-entity-real-production.png");mc.player.closeContainer();mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(B));next();}
 case 10->{mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(B));if(elapsed<60)return;var data=snownee.jade.impl.ObjectDataCenter.getServerData();check(data!=null&&data.getInt("entityMultiplier")==4&&data.getInt("entityEnergy")==TimeAcceleratorCostMath.energyCost(3,com.direwolf20.justdirethings.setup.Config.TIME_WAND_FE_COST.get()),"real Jade server packets carry multiplier/cost");shot(mc,"jdte-entity-jade.png");Files.writeString(mc.gameDirectory.toPath().resolve("entity-smoke-success.txt"),"PASS: actual Advanced/Extended GUIs, high-bit FE/Time Fluid/multiplier, 4/8 slots, hostile multiplier clamp, real JEI crafting layouts, Jade config labels and status packets, natural real-entity paid production, client-side sub-ticks rejected.\n");mc.stop();stage=11;}

 }}
}
