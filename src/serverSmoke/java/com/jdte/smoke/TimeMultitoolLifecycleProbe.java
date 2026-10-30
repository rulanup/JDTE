package com.jdte.smoke;

import com.jdte.common.items.*;
import com.jdte.setup.JDTEItems;
import com.direwolf20.justdirethings.common.items.interfaces.*;
import com.direwolf20.justdirethings.setup.Registration;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import java.nio.file.*;
import java.util.*;

/** Actual packaged game-mode/native-machine work; FakePlayer calls are explicit test fixture actions. */
public final class TimeMultitoolLifecycleProbe {
    private static final BlockPos CHEST=new BlockPos(1028,110,1028),WORK=CHEST.east(4);
    private String mode="";private MinecraftServer server;private int ticks,stage;private boolean unloaded;private ChestBlockEntity old;private CompoundTag expected;private ItemStack tool;
    private void check(boolean v,String s){if(!v)throw new IllegalStateException("Multitool lifecycle: "+s);}
    private ChestBlockEntity chest(){return (ChestBlockEntity)server.overworld().getBlockEntity(CHEST);}
    private void force(boolean v){server.overworld().setChunkForced(CHEST.getX()>>4,CHEST.getZ()>>4,v);}
    private int energy(){return tool.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).getEnergyStored();}
    private int fluid(){return FluidContainingItem.getAvailableFluid(tool);}
    private net.minecraftforge.common.util.FakePlayer actor(){var p=FakePlayerFactory.get(server.overworld(),new com.mojang.authlib.GameProfile(UUID.fromString("352ba670-3440-45f6-b6e3-1067099b2010"),"TimeToolLifecycle"));p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,tool);p.setPos(Vec3.atCenterOf(WORK).add(0,2,0));p.setXRot(90);return p;}
    private void fillStone(){for(var p:BlockPos.betweenClosed(WORK.offset(-1,0,-1),WORK.offset(1,0,1)))server.overworld().setBlockAndUpdate(p,Blocks.STONE.defaultBlockState());}
    private void collect(int count){var entities=server.overworld().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(CHEST).inflate(8));int actual=entities.stream().filter(e->e.getItem().is(Items.COBBLESTONE)).mapToInt(e->e.getItem().getCount()).sum();check(actual==count,"actual new cobblestone count "+actual+" expected "+count);for(var e:entities)if(e.getItem().is(Items.COBBLESTONE))e.discard();var old=chest().getItem(1);chest().setItem(1,new ItemStack(Items.COBBLESTONE,(old.isEmpty()?0:old.getCount())+count));}
    @SubscribeEvent public void start(ServerStartedEvent e)throws Exception{
        mode=System.getProperty("jdte.multitoolLifecycle","");if(mode.isEmpty())return;server=e.getServer();check(Files.exists(Path.of(".jdte-lifecycle-fixture")),"marked fixture");force(true);var level=server.overworld();level.getChunkAt(CHEST);
        if(mode.equals("verify")){expected=NbtIo.readCompressed(Path.of("multitool-expected.nbt").toFile());check(expected.equals(chest().saveWithoutMetadata()),"fresh JVM restores exact whole chest/item/mode/native abilities/FE/fluid before work");tool=chest().removeItem(5,1);stage=10;return;}
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);for(var p:BlockPos.betweenClosed(CHEST.offset(-2,-1,-2),CHEST.offset(6,4,6))){level.removeBlockEntity(p);level.setBlockAndUpdate(p,p.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}level.setBlockAndUpdate(CHEST,Blocks.CHEST.defaultBlockState());
        tool=new ItemStack(JDTEItems.TIME_MULTITOOL.get());tool.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).receiveEnergy(20000,false);tool.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new).fill(new FluidStack(Registration.TIME_FLUID_SOURCE.get(),10000),IFluidHandler.FluidAction.EXECUTE);TimeMultitoolSpeedStorage.set(tool,TimeMultitoolSpeedMode.SIXTEEN);tool.setHoverName(net.minecraft.network.chat.Component.literal("Keep time mode and native upgrades"));tool.getOrCreateTag().putString("unknown_fixture_tag","retain");
        var r=(net.minecraft.world.item.crafting.SmithingRecipe)level.getRecipeManager().byKey(new net.minecraft.resources.ResourceLocation("jdte","time_multitool_hammer")).orElseThrow();var inv=new SimpleContainer(ItemStack.EMPTY,tool,new ItemStack(Ability.HAMMER.getUpgradeItem()));check(r.matches(inv,level),"actual native smithing recipe matches new tool");tool=r.assemble(inv,level.registryAccess());check(!tool.isEmpty()&&ToggleableTool.hasUpgrade(tool,Ability.HAMMER),"actual smithing preserves source data and installs hammer");
    }
    @SubscribeEvent public void unload(ChunkEvent.Unload e){if(server!=null&&stage==4&&e.getLevel()==server.overworld()&&e.getChunk().getPos().equals(new net.minecraft.world.level.ChunkPos(CHEST)))unloaded=true;}
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e)throws Exception{
        if(mode.isEmpty()||e.phase!=TickEvent.Phase.END||e.getServer()!=server)return;check(++ticks<1800,"timeout "+stage);var level=server.overworld();
        if(stage==0&&ticks>10){fillStone();var p=actor();check(p.gameMode.destroyBlock(WORK),"actual reobfuscated Minecraft game-mode mining");check(energy()==19550&&fluid()==9856,"nine removals cost450FE/144mB");p.getInventory().clearContent();stage=1;}
        else if(stage==1){collect(9);var p=actor();ToggleableTool.setSetting(tool,"hammer",false);var target=CHEST.south(3);var machinePos=CHEST.south(4);level.setBlockAndUpdate(target,Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(machinePos,Registration.BlockBreakerT1.get().defaultBlockState());var machine=(com.direwolf20.justdirethings.common.blockentities.BlockBreakerT1BE)level.getBlockEntity(machinePos);machine.getMachineHandler().setStackInSlot(0,tool);check(machine.tryBreakBlock(tool,p,target,level.getBlockState(target))&&energy()==19500&&fluid()==9840,"actual native breaker/FakePlayer adapter pays one50FE/16mB");machine.getMachineHandler().setStackInSlot(0,ItemStack.EMPTY);level.removeBlock(machinePos,false);p.getInventory().clearContent();ToggleableTool.setSetting(tool,"hammer",true);stage=2;}
        else if(stage==2){collect(1);chest().setItem(0,tool);chest().setChanged();stage=3;}
        else if(stage==3){var before=tool.save(new CompoundTag());var moved=chest().removeItem(0,1);check(moved.getCount()==1&&before.equals(moved.save(new CompoundTag()))&&chest().getItem(0).isEmpty(),"real inventory split/move retains one exact full-NBT item");tool=moved;chest().setItem(5,moved);chest().setChanged();old=chest();expected=old.saveWithoutMetadata();server.saveEverything(false,true,true);force(false);stage=4;}
        else if(stage==4&&unloaded&&!level.getChunkSource().hasChunk(CHEST.getX()>>4,CHEST.getZ()>>4)){level.getChunkSource().save(true);force(true);level.getChunkAt(CHEST);check(chest()!=old&&expected.equals(chest().saveWithoutMetadata()),"actual unload/fresh BE restores exact all item state");NbtIo.writeCompressed(expected,Path.of("multitool-expected.nbt").toFile());finish("multitool-seed-success","PASS: packaged real game-mode nine-block hammer450FE/144mB and native breaker50FE/16mB; ten actual cobblestone,19500FE/9840mB/native upgrade/mode16/name; actual inventory move, ChunkEvent.Unload/fresh BE/exact NBT and saved shutdown. FakePlayer method fixture, not real-client input.");}
        else if(stage==10&&ticks>10){check(energy()==19500&&fluid()==9840&&TimeMultitoolItem.getSelectedSpeedMode(tool)==TimeMultitoolSpeedMode.SIXTEEN,"new JVM source resources and mode restored");fillStone();var p=actor();p.setShiftKeyDown(true);JDTEItems.TIME_MULTITOOL.get().use(level,p,InteractionHand.MAIN_HAND);p.setShiftKeyDown(false);check(TimeMultitoolItem.getSelectedSpeedMode(tool)==TimeMultitoolSpeedMode.TWO_FIFTY_SIX,"real server use cycles restored mode16 to256");check(p.gameMode.destroyBlock(WORK)&&energy()==19050&&fluid()==7536,"resumed nine blocks pay450FE/2304mB exactly");p.getInventory().clearContent();stage=11;}
        else if(stage==11){collect(9);chest().setItem(5,tool);chest().setChanged();check(chest().getItem(1).getCount()==19,"nineteen total actual drops preserved");finish("multitool-verify-success","PASS: fresh packaged JVM exact NBT, retained mode16/native smithing/name; actual server air-use selects256 then nine real game-mode removals pay450FE/2304mB;19050FE/7536mB/nineteen cobblestone. FakePlayer method fixture, not graphical input.");}
    }
    private void finish(String marker,String s)throws Exception{Files.writeString(Path.of(marker),s+"\n");mode="";server.saveEverything(false,true,true);server.halt(false);}
}
