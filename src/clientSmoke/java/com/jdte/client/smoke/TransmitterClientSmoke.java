package com.jdte.client.smoke;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.client.screens.*;
import com.jdte.common.blockentities.*;
import com.jdte.common.containers.*;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;

/** Opt-in real graphical/network/online-player test. Never included in the production artifact. */
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class TransmitterClientSmoke {
    private static final BlockPos ADV=new BlockPos(20,100,20),EXT=new BlockPos(24,100,20);
    private static int stage,ticks,changedAt,templateSource;
    private static volatile RuntimeException serverFailure;
    private static boolean disconnecting;
    private static volatile AdvancedEnergyTransmitterBE detached;
    private static volatile net.minecraftforge.energy.IEnergyStorage retained;
    private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException("Transmitter client stage "+stage+": "+message);}
    private static void next(){stage++;changedAt=ticks;com.mojang.logging.LogUtils.getLogger().info("Transmitter client stage {}",stage);}
    private static void server(Minecraft mc,java.util.function.Consumer<ServerPlayer> action){var srv=mc.getSingleplayerServer();var id=mc.player.getUUID();srv.execute(()->{try{action.accept(srv.getPlayerList().getPlayer(id));}catch(RuntimeException e){serverFailure=e;throw e;}});}
    private static AdvancedEnergyTransmitterBE advanced(ServerPlayer p){return (AdvancedEnergyTransmitterBE)p.serverLevel().getBlockEntity(ADV);}
    private static BaseMachineBE current(Minecraft mc){return ((com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer)mc.player.containerMenu).baseMachineBE;}
    private static void open(Minecraft mc,BlockPos pos){mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));}
    private static void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),msg->com.mojang.logging.LogUtils.getLogger().info("Transmitter screenshot: {}",msg.getString()));}
    private static int top(BaseMachineScreen<?> screen){try{var f=BaseMachineScreen.class.getDeclaredField("topSectionTop");f.setAccessible(true);return f.getInt(screen);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
    private static void button(Minecraft mc,int x){
        var s=(BaseMachineScreen<?>)mc.screen;int bx=s.getGuiLeft()+x,by=top(s)+62;
        // Deliver to the rendered button itself. Reusing the prior ghost-slot hover at unrelated coordinates
        // would incorrectly exercise JDT's ghost click branch before its button dispatch.
        var widget=s.children().stream().filter(c->c instanceof net.minecraft.client.gui.components.AbstractWidget)
                .map(c->(net.minecraft.client.gui.components.AbstractWidget)c).filter(w->w.getX()==bx&&w.getY()==by).findFirst().orElseThrow();
        check(widget.mouseClicked(bx+5,by+5,0),"actual rendered button handled click: "+x);widget.mouseReleased(bx+5,by+5,0);
    }

    private static void shift(Minecraft mc,java.util.function.Predicate<net.minecraft.world.inventory.Slot> predicate){var m=mc.player.containerMenu;var slot=m.slots.stream().filter(predicate).findFirst().orElseThrow();mc.gameMode.handleInventoryMouseClick(m.containerId,slot.index,0,ClickType.QUICK_MOVE,mc.player);}
    private static top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler curios(ServerPlayer p){return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new).getStacksHandler("transmitter_test").orElseThrow().getStacks();}
    private static net.minecraftforge.energy.IEnergyStorage fe(ItemStack stack){return stack.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);}
    private static ItemStack almostFull(){var stack=new ItemStack(Registration.EclipseAlloyPickaxe.get());var cap=fe(stack);cap.receiveEnergy(cap.getMaxEnergyStored()-1000,false);return stack;}
    private static void charged(ServerPlayer p){for(var stack:java.util.List.of(p.getInventory().getItem(0),p.getInventory().armor.get(0),p.getOffhandItem(),curios(p).getStackInSlot(0)))check(fe(stack).getEnergyStored()==fe(stack).getMaxEnergyStored(),"each actual hotbar/armor/offhand/Curios item reached capacity");check(advanced(p).getEnergyStorage().getEnergyStored()==0,"source paid exactly all equipment gaps");}
    private static void aim(Minecraft mc){var origin=mc.player.getEyePosition();for(var box:mc.level.getBlockState(ADV).getShape(mc.level,ADV).toAabbs()){var point=box.getCenter().add(ADV.getX(),ADV.getY(),ADV.getZ());var hit=mc.level.clip(new net.minecraft.world.level.ClipContext(origin,point,net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,mc.player));if(hit.getBlockPos().equals(ADV)){mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,point);return;}}throw new IllegalStateException("Cannot aim at transmitter");}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event)throws Exception{
        if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.transmitterSmoke"))return;
        if(disconnecting)return;
        if(serverFailure!=null)throw new IllegalStateException("Asynchronous server assertion failed",serverFailure);
        var mc=Minecraft.getInstance();int elapsed=++ticks-changedAt;check(ticks<6500,"timeout, screen="+mc.screen);
        if(stage==20){
            if(mc.level!=null||mc.getSingleplayerServer()!=null||elapsed<80)return;
            check(!detached.isBoundPlayerOnline(),"disconnected real operator resolves offline");
            check(retained.receiveEnergy(100,false)==0&&retained.extractEnergy(100,false)==0,"held endpoint inactive after real disconnect/world unload");
            mc.createWorldOpenFlows().loadLevel(new TitleScreen(),"jdte-mineral-client-smoke");next();return;
        }
        if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
        switch(stage){
            case 0->{if(mc.player.tickCount<80)return;server(mc,p->{var l=p.serverLevel();for(int x=17;x<=27;x++)for(int z=17;z<=25;z++){l.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());for(int y=100;y<=104;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}
                l.setBlockAndUpdate(ADV,JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get().defaultBlockState());l.setBlockAndUpdate(EXT,JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get().defaultBlockState());
                var a=advanced(p);a.getEnergyStorage().setEnergy(1900000000);a.getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.HIGH;UpgradeHelper.getUpgradeHandler(a).insertItem(0,new ItemStack(JDTEItems.FILTER_UPGRADE.get()),false);
                var e=(ExtendedEnergyTransmitterBE)l.getBlockEntity(EXT);e.getEnergyStorage().setEnergy(700000);e.getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.HIGH;
                p.getInventory().clearContent();p.getInventory().setItem(9,new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get()));p.getInventory().setItem(10,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));p.getInventory().setItem(11,new ItemStack(Items.STONE,64));p.connection.teleport(22.5,100,23.5,180,0);p.inventoryMenu.broadcastChanges();});next();}
            case 1->{if(elapsed<30)return;open(mc,ADV);next();}
            case 2->{if(elapsed<30)return;check(mc.screen instanceof AdvancedEnergyTransmitterScreen,"registered advanced GUI");var m=(AdvancedEnergyTransmitterContainer)mc.player.containerMenu;check(m.getEnergy()==1900000000,"1.9B FE crossed actual menu packets");check(m.slots.stream().filter(s->s instanceof UpgradeSlot).count()==8,"eight real upgrade slots");shot(mc,"jdte-transmitter-advanced.png");button(mc,8);button(mc,26);
                var from=m.slots.stream().filter(s->!(s instanceof com.direwolf20.justdirethings.common.containers.slots.FilterBasicSlot)&&s.getItem().is(Items.STONE)).findFirst().orElseThrow();var filter=m.slots.stream().filter(s->s instanceof com.jdte.common.containers.DynamicFilterSlot).findFirst().orElseThrow();
                templateSource=from.index;mc.gameMode.handleInventoryMouseClick(m.containerId,from.index,0,ClickType.PICKUP,mc.player);
                var screen=(BaseMachineScreen<?>)mc.screen;
                double px=(screen.getGuiLeft()+filter.x+8)*(double)mc.getWindow().getScreenWidth()/mc.getWindow().getGuiScaledWidth();
                double py=(screen.getGuiTop()+filter.y+8)*(double)mc.getWindow().getScreenHeight()/mc.getWindow().getGuiScaledHeight();
                org.lwjgl.glfw.GLFW.glfwFocusWindow(mc.getWindow().getWindow());
                org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(),px,py);
                // Xorg without a window manager can suppress cursor callbacks; inject the same native mouse event.
                var move=MouseHandler.class.getDeclaredMethod("onMove",long.class,double.class,double.class);move.setAccessible(true);
                move.invoke(mc.mouseHandler,mc.getWindow().getWindow(),px,py);move.invoke(mc.mouseHandler,mc.getWindow().getWindow(),px,py);
                stage=201;changedAt=ticks;}
            case 201->{if(elapsed<10)return;var m=mc.player.containerMenu;var screen=(BaseMachineScreen<?>)mc.screen;var filter=m.slots.stream().filter(s->s instanceof com.jdte.common.containers.DynamicFilterSlot).findFirst().orElseThrow();
                check(m.getCarried().is(Items.STONE)&&m.getCarried().getCount()==64,"real cursor holds all 64 filter templates before ghost click");
                var hoverField=net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class.getDeclaredField("hoveredSlot");hoverField.setAccessible(true);
                var hovered=(net.minecraft.world.inventory.Slot)hoverField.get(screen);
                check(hovered==filter,"actual rendered hover selects filter slot: expected="+filter.index+", actual="+(hovered==null?"null":hovered.index));
                check(screen.mouseClicked(screen.getGuiLeft()+filter.x+8,screen.getGuiTop()+filter.y+8,0),"real hovered ghost-slot click handled");
                check(filter.getItem().is(Items.STONE),"native screen predicted the ghost template locally");
                mc.gameMode.handleInventoryMouseClick(m.containerId,templateSource,0,ClickType.PICKUP,mc.player);shift(mc,s->!(s instanceof UpgradeSlot)&&s.getItem().is(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get()));stage=3;changedAt=ticks;}
            case 3->{if(elapsed<25)return;check(UpgradeHelper.hasEnergyOverloadUpgrade(current(mc)),"installed overload synced to client");server(mc,p->{var a=advanced(p);check(a.getFilterData().allowlist&&a.getFilterData().compareNBT&&a.getFilterHandler().getStackInSlot(0).is(Items.STONE),"GUI filter packets and ghost template reach server: allow="+a.getFilterData().allowlist+", NBT="+a.getFilterData().compareNBT+", template="+a.getFilterHandler().getStackInSlot(0));check(p.getInventory().countItem(Items.STONE)==64,"filter consumed no template");});shot(mc,"jdte-transmitter-upgrade-filter.png");shift(mc,s->s instanceof UpgradeSlot&&s.getItem().is(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get()));button(mc,44);button(mc,116);next();}
            case 4->{if(elapsed<30)return;var m=(AdvancedEnergyTransmitterContainer)mc.player.containerMenu;check(!UpgradeHelper.hasEnergyOverloadUpgrade(current(mc))&&m.hasBoundPlayer()&&m.isBoundPlayerOnline()&&m.isShowingParticles(),"removal, binding and particles synchronized: overload="+UpgradeHelper.hasEnergyOverloadUpgrade(current(mc))+", bound="+m.hasBoundPlayer()+", online="+m.isBoundPlayerOnline()+", particles="+m.isShowingParticles());server(mc,p->{check(p.getInventory().countItem(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get())==1,"removed upgrade returned exactly once");check(advanced(p).getBoundPlayerIdForCopy().orElseThrow().equals(p.getUUID()),"GUI bound actual online operator");});shot(mc,"jdte-transmitter-bound.png");mc.player.closeContainer();next();}
            case 5->{if(elapsed<20)return;open(mc,EXT);next();}
            case 6->{if(elapsed<30)return;check(mc.screen instanceof ExtendedEnergyTransmitterScreen,"registered extended GUI");var m=(ExtendedEnergyTransmitterContainer)mc.player.containerMenu;check(m.getEnergy()==700000,"extended owned FE crossed menu");button(mc,8);button(mc,26);button(mc,116);shift(mc,s->!(s instanceof UpgradeSlot)&&s.getItem().is(JDTEItems.CAPACITY_UPGRADE.get()));next();}
            case 7->{if(elapsed<30)return;check(UpgradeHelper.countUpgrades(current(mc),UpgradeType.CAPACITY)==1,"extended card installed through actual menu");server(mc,p->{var e=(ExtendedEnergyTransmitterBE)p.serverLevel().getBlockEntity(EXT);check(e.getFilterData().allowlist&&e.getFilterData().compareNBT&&!e.showParticles,"extended settings packets applied");});shot(mc,"jdte-transmitter-extended.png");shift(mc,s->s instanceof UpgradeSlot&&s.getItem().is(JDTEItems.CAPACITY_UPGRADE.get()));next();}
            case 8->{if(elapsed<25)return;check(UpgradeHelper.countUpgrades(current(mc),UpgradeType.CAPACITY)==0,"extended removal sync");mc.player.closeContainer();server(mc,p->{check(p.getInventory().countItem(JDTEItems.CAPACITY_UPGRADE.get())==1,"extended removed card returned once");p.getInventory().clearContent();p.getInventory().setItem(0,almostFull());
                var boots=new ItemStack(Registration.EclipseAlloyBoots.get());fe(boots).receiveEnergy(fe(boots).getMaxEnergyStored()-1000,false);p.getInventory().armor.set(0,boots);p.setItemInHand(InteractionHand.OFF_HAND,almostFull());
                var curio=almostFull();check(curios(p).isItemValid(0,curio)&&curios(p).insertItem(0,curio,false).isEmpty(),"test datapack enables a legitimate FE item in a real validated Curios slot");
                advanced(p).getEnergyStorage().setEnergy(4000);advanced(p).getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.IGNORED;
            });next();}
            case 9->{if(elapsed<35)return;server(mc,TransmitterClientSmoke::charged);aim(mc);next();}
            case 10->{aim(mc);if(elapsed<50)return;var data=snownee.jade.impl.ObjectDataCenter.getServerData();check(data!=null&&data.getBoolean("jdte_player_bound")&&data.getBoolean("jdte_player_online"),"real Jade received online binding status");shot(mc,"jdte-transmitter-jade.png");server(mc,p->{detached=advanced(p);retained=detached.getCapability(ForgeCapabilities.ENERGY,detached.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)).orElseThrow(IllegalStateException::new);});next();}
            case 11->{if(elapsed<15)return;check(detached!=null&&retained!=null,"server retained-reference capture completed");stage=20;changedAt=ticks;disconnecting=true;try{mc.level.disconnect();mc.clearLevel();mc.setScreen(new TitleScreen());}finally{disconnecting=false;}}
            case 21->{if(mc.player.tickCount<70)return;server(mc,p->{check(advanced(p).getBoundPlayerIdForCopy().orElseThrow().equals(p.getUUID())&&advanced(p).isBoundPlayerOnline(),"same real operator UUID reconnected and saved binding restored");charged(p);var stack=curios(p).getStackInSlot(0);check(fe(stack).extractEnergy(1000,false)==1000,"actual persisted Curios item accepts new charging work");advanced(p).getEnergyStorage().setEnergy(1000);});next();}
            case 22->{if(elapsed<35)return;server(mc,TransmitterClientSmoke::charged);open(mc,ADV);next();}
            case 23->{if(elapsed<35)return;check(mc.screen instanceof AdvancedEnergyTransmitterScreen&&((AdvancedEnergyTransmitterContainer)mc.player.containerMenu).isBoundPlayerOnline(),"GUI reopened after actual reconnect");shot(mc,"jdte-transmitter-reconnected.png");Files.writeString(mc.gameDirectory.toPath().resolve("transmitter-smoke-success.txt"),"PASS: real graphical Advanced/Extended menus, 32-bit FE, filter packets/ghost conservation, eight upgrade slots/install/remove, binding/particles, Jade online data; online hotbar/actual armor/offhand/validated Curios FE charging; real disconnect/world unload/rejoin and saved UUID/Curios recharge, exact source FE costs. Curios test slot exists only in clientSmoke datapack.\n");mc.stop();stage=24;}
        }
    }
}
