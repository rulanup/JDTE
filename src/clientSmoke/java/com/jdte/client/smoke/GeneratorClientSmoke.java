package com.jdte.client.smoke;

import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.fluids.basefluids.RefinedFuel;
import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.client.JDTEKeyMappings;
import com.jdte.client.screens.*;
import com.jdte.common.blockentities.*;
import com.jdte.common.containers.*;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.Files;

/** Opt-in graphical and real menu-packet acceptance; excluded from the production mod. */
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class GeneratorClientSmoke {
    private static final BlockPos SOLID=new BlockPos(20,100,20),FLUID=new BlockPos(24,100,20);
    private static int stage,ticks,changedAt;
    private static volatile RuntimeException failure;
    private static volatile boolean done;
    private static void check(boolean ok,String message) {if(!ok)throw new IllegalStateException("Generator client stage "+stage+": "+message);}
    private static void next() {stage++;changedAt=ticks;com.mojang.logging.LogUtils.getLogger().info("Generator client stage {}",stage);}
    private static void server(Minecraft mc,java.util.function.Consumer<ServerPlayer> action) {var s=mc.getSingleplayerServer();var uuid=mc.player.getUUID();s.execute(()->{try{action.accept(s.getPlayerList().getPlayer(uuid));}catch(RuntimeException ex){failure=ex;throw ex;}});}
    private static top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler curios(net.minecraft.world.entity.player.Player p) {return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new).getStacksHandler("large_fuel_canister").orElseThrow().getStacks();}
    private static int coalTime() {return net.minecraftforge.common.ForgeHooks.getBurnTime(new ItemStack(Items.COAL),net.minecraft.world.item.crafting.RecipeType.SMELTING);}
    private static void shift(Minecraft mc,java.util.function.Predicate<net.minecraft.world.inventory.Slot> match) {var m=mc.player.containerMenu;var s=m.slots.stream().filter(match).findFirst().orElseThrow();mc.gameMode.handleInventoryMouseClick(m.containerId,s.index,0,ClickType.QUICK_MOVE,mc.player);}
    private static void open(Minecraft mc,BlockPos pos) {mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));}
    private static void shot(Minecraft mc,String name) {Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),msg->com.mojang.logging.LogUtils.getLogger().info("Generator screenshot: {}",msg.getString()));}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event)throws Exception {
        if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.generatorSmoke"))return;
        if(failure!=null)throw new IllegalStateException("Server assertion failed",failure);
        var mc=Minecraft.getInstance();int elapsed=++ticks-changedAt;check(ticks<4000,"timeout screen="+mc.screen);
        if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
        switch(stage) {
            case 0->{if(mc.player.tickCount<80)return;server(mc,p->{var l=p.serverLevel();for(var pos:BlockPos.betweenClosed(new BlockPos(17,99,17),new BlockPos(27,104,25)))l.setBlockAndUpdate(pos,pos.getY()==99?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());p.getInventory().clearContent();p.getInventory().selected=0;p.getInventory().setItem(0,new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get()));p.getInventory().setItem(9,new ItemStack(Items.COAL,16));p.getInventory().setItem(10,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()));p.connection.teleport(22.5,100,23.5,180,0);p.inventoryMenu.broadcastChanges();});next();}
            case 1->{if(elapsed<30)return;mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);next();}
            case 2->{if(elapsed<30)return;check(mc.screen instanceof LargeFuelCanisterScreen,"registered real handheld canister GUI");shot(mc,"jdte-large-fuel-empty.png");shift(mc,s->s.getItem().is(Items.COAL));next();}
            case 3->{if(elapsed<30)return;var m=(LargeFuelCanisterContainer)mc.player.containerMenu;check(FuelCanister.getFuelLevel(m.getCurrentStack())==coalTime()*16&&mc.player.getInventory().countItem(Items.COAL)==0,"fill/NBT changes crossed actual menu packets");shot(mc,"jdte-large-fuel-filled.png");server(mc,p->{check(FuelCanister.getFuelLevel(p.getMainHandItem())==coalTime()*16&&p.getInventory().countItem(Items.COAL)==0,"server owns exactly filled fuel");});mc.player.closeContainer();server(mc,p->{p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var can=new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());FuelCanister.setFuelLevel(can,12345);curios(p).setStackInSlot(0,can);p.getInventory().setItem(9,new ItemStack(Items.COAL,8));p.inventoryMenu.broadcastChanges();});next();}
            case 4->{if(elapsed<40)return;check(curios(mc.player).getStackInSlot(0).is(JDTEItems.LARGE_FUEL_CANISTER.get()),"real dedicated Curios slot synchronized");KeyMapping.click(JDTEKeyMappings.LARGE_FUEL_CANISTER.getKey());net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.InputEvent.Key(org.lwjgl.glfw.GLFW.GLFW_KEY_F,0,org.lwjgl.glfw.GLFW.GLFW_PRESS,0));next();}
            case 5->{if(elapsed<30)return;check(mc.screen instanceof LargeFuelCanisterScreen&&FuelCanister.getFuelLevel(((LargeFuelCanisterContainer)mc.player.containerMenu).getCurrentStack())==12345,"F input event opens worn canister via real C2S packet");shift(mc,s->s.getItem().is(Items.COAL));next();}
            case 6->{if(elapsed<30)return;check(FuelCanister.getFuelLevel(((LargeFuelCanisterContainer)mc.player.containerMenu).getCurrentStack())==12345+coalTime()*8,"Curios fuel NBT sync after GUI fill");shot(mc,"jdte-large-fuel-curios.png");mc.player.closeContainer();server(mc,p->{check(FuelCanister.getFuelLevel(curios(p).getStackInSlot(0))==12345+coalTime()*8,"worn canister server fuel exact");var l=p.serverLevel();l.setBlockAndUpdate(SOLID,JDTEBlocks.EXTENDED_GENERATOR.get().defaultBlockState());l.setBlockAndUpdate(FLUID,JDTEBlocks.EXTENDED_FLUID_GENERATOR.get().defaultBlockState());for(var pos:java.util.List.of(SOLID,FLUID)){var be=(com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE)l.getBlockEntity(pos);var cards=UpgradeHelper.getUpgradeHandler(be);cards.insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);for(int i=1;i<=3;i++)cards.insertItem(i,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);((com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE)be).getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.HIGH;((com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE)be).getEnergyStorage().setEnergy(((com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE)be).getMaxEnergy());}
                var solid=(ExtendedGeneratorBE)l.getBlockEntity(SOLID);solid.burnRemaining=400001;solid.maxBurn=500000;solid.feRemaining=9876543;var fuel=BuiltInRegistries.FLUID.stream().filter(f->f instanceof RefinedFuel r&&r.fePerMb()>0).findFirst().orElseThrow();((ExtendedFluidGeneratorBE)l.getBlockEntity(FLUID)).getFluidTank().setFluid(new net.minecraftforge.fluids.FluidStack(fuel,123457));});next();}
            case 7->{if(elapsed<35)return;open(mc,SOLID);next();}
            case 8->{if(elapsed<30)return;check(mc.screen instanceof ExtendedGeneratorScreen,"registered solid generator GUI");var m=(ExtendedGeneratorContainer)mc.player.containerMenu;check(m.getBurnRemaining()==400001&&m.getMaxBurn()==500000,"32-bit burn counters survived actual menu wire");check(m.getEnergy()==((ExtendedGeneratorBE)m.baseMachineBE).getMaxEnergy()&&m.getEnergy()>65535,"real large FE menu sync");check(m.slots.stream().filter(s->s instanceof UpgradeSlot).count()==8,"eight physical menu upgrade slots");shot(mc,"jdte-extended-generator.png");shift(mc,s->s instanceof UpgradeSlot&&s.getItem().is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()));shift(mc,s->!(s instanceof UpgradeSlot)&&s.getItem().is(JDTEItems.GENERATOR_UPGRADE.get()));next();}
            case 9->{if(elapsed<25)return;var m=(ExtendedGeneratorContainer)mc.player.containerMenu;check(mc.player.getInventory().countItem(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())==0&&m.slots.stream().anyMatch(s->s instanceof UpgradeSlot&&s.getItem().is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())),"real shift-click cannot remove capacity under full FE");check(m.slots.stream().anyMatch(s->s instanceof UpgradeSlot&&s.getItem().is(JDTEItems.GENERATOR_UPGRADE.get())),"upgrade installation crossed menu packets");server(mc,p->check(UpgradeHelper.hasGeneratorUpgrade((ExtendedGeneratorBE)p.serverLevel().getBlockEntity(SOLID)),"server generator card active"));shift(mc,s->s instanceof UpgradeSlot&&s.getItem().is(JDTEItems.GENERATOR_UPGRADE.get()));next();}
            case 10->{if(elapsed<25)return;check(mc.player.getInventory().countItem(JDTEItems.GENERATOR_UPGRADE.get())==1,"removal returns exactly one card");mc.player.closeContainer();next();}
            case 11->{if(elapsed<20)return;open(mc,FLUID);next();}
            case 12->{if(elapsed<30)return;check(mc.screen instanceof ExtendedFluidGeneratorScreen,"registered fluid generator GUI");var m=(ExtendedFluidGeneratorContainer)mc.player.containerMenu;check(m.getFluidAmount()==123457,"32-bit fluid amount survived actual wire");check(m.getEnergy()==((ExtendedFluidGeneratorBE)m.baseMachineBE).getMaxEnergy(),"fluid-generator FE sync");shot(mc,"jdte-extended-fluid-generator.png");shift(mc,s->!(s instanceof UpgradeSlot)&&s.getItem().is(JDTEItems.GENERATOR_UPGRADE.get()));next();}
            case 13->{if(elapsed<25)return;check(((ExtendedFluidGeneratorContainer)mc.player.containerMenu).slots.stream().anyMatch(s->s instanceof UpgradeSlot&&s.getItem().is(JDTEItems.GENERATOR_UPGRADE.get())),"fluid-generator upgrade installed");server(mc,p->{check(!UpgradeHelper.hasGeneratorUpgrade((ExtendedGeneratorBE)p.serverLevel().getBlockEntity(SOLID))&&UpgradeHelper.hasGeneratorUpgrade((ExtendedFluidGeneratorBE)p.serverLevel().getBlockEntity(FLUID)),"exactly one card moved between machines");check(p.getInventory().countItem(JDTEItems.GENERATOR_UPGRADE.get())==0,"no duplicated card");done=true;});next();}
            case 14->{if(elapsed<25||!done)return;check(failure==null,"all server assertions completed");Files.writeString(mc.gameDirectory.toPath().resolve("generator-smoke-success.txt"),"PASS: actual graphical handheld and Curios F-event canister menus, coal shift-fill and exact NBT/item counts over real packets; Extended solid/fluid screens, >16-bit FE/burn/fluid synchronization, eight upgrade slots, card install/remove and transfer without duplication.\n");mc.stop();stage=15;}
        }
    }
}
