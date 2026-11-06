package com.jdte.common.blockentities;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.core.definitions.*;
import com.jdte.common.items.UltimateTimeWandItem;
import com.jdte.common.entities.UltimateTimeWandEntity;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
@PrefixGameTestTemplate(false)
public final class UltimateTimeWandAEGameTests {
 @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=220) public static void realWandUsesNativeAeServicePriorityAndCompletesInscriber(GameTestHelper h){var target=new BlockPos(2,1,3);h.setBlock(target,AEBlocks.INSCRIBER.block());h.setBlock(target.east(),AEBlocks.CREATIVE_ENERGY_CELL.block());
  h.runAfterDelay(80,()->{var tile=(InscriberBlockEntity)h.getBlockEntity(target);tile.getInternalInventory().setItemDirect(0,AEItems.SILICON_PRESS.stack());tile.getInternalInventory().setItemDirect(2,AEItems.SILICON.stack());var s=new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());s.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).receiveEnergy(Integer.MAX_VALUE,false);s.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new).fill(new FluidStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(),100),FluidAction.EXECUTE);s.getOrCreateTag().putString(UltimateTimeWandItem.MODE,"max");var p=h.makeMockSurvivalPlayer();p.setPos(Vec3.atCenterOf(h.absolutePos(target.south(2))));p.setItemInHand(InteractionHand.MAIN_HAND,s);
   h.assertTrue(UltimateTimeWandTargetRuntime.resolve(h.getLevel(),tile.getBlockPos()).orElseThrow().route()==UltimateTimeWandTargetRuntime.Route.AE2,"native AE service has priority over ordinary ticker");h.assertTrue(s.getItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(tile.getBlockPos()),Direction.UP,tile.getBlockPos(),false)))==InteractionResult.SUCCESS,"actual wand AE activation");var markers=h.getLevel().getEntitiesOfClass(UltimateTimeWandEntity.class,new AABB(tile.getBlockPos()));h.assertTrue(markers.size()==1,"one AE request");markers.get(0).tick();ExtendedTimeAccelerationManager.onServerTickPost(new net.minecraftforge.event.TickEvent.ServerTickEvent(net.minecraftforge.event.TickEvent.Phase.END,()->true,h.getLevel().getServer()));var inv=tile.getInternalInventory();h.assertTrue(inv.getStackInSlot(3).is(AEItems.SILICON_PRINT.asItem())&&inv.getStackInSlot(3).getCount()==1&&inv.getStackInSlot(2).isEmpty()&&inv.getStackInSlot(0).is(AEItems.SILICON_PRESS.asItem()),"real native recipe completes once, reusable press retained");markers.forEach(net.minecraft.world.entity.Entity::discard);h.succeed();});
 }
}
