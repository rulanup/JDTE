package com.jdte.common.items;
import com.jdte.common.blockentities.*;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.*;
import java.util.*;
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class BioExtractorTierGameTests {
 private static final BlockPos POS=new BlockPos(2,1,2);
 private static UseOnContext ctx(GameTestHelper h,ItemStack item){var p=h.makeMockSurvivalPlayer();p.setItemInHand(InteractionHand.MAIN_HAND,item);return new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(h.absolutePos(POS)),Direction.UP,h.absolutePos(POS),false));}
 private static AdvancedBioExtractorBE source(GameTestHelper h){h.setBlock(POS,JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get());var b=(AdvancedBioExtractorBE)h.getBlockEntity(POS);var u=UpgradeHelper.getUpgradeHandler(b);for(int i=0;i<4;i++)u.insertItem(i,new ItemStack(i==0?JDTEItems.CAPACITY_UPGRADE.get():i==3?JDTEItems.OVERCLOCK_UPGRADE.get():JDTEItems.FILTER_UPGRADE.get()),false);b.getEnergyStorage().setEnergy(140000);b.getFluidTank().setFluid(new FluidStack(net.minecraftforge.common.ForgeMod.MILK.get(),30000));b.getMachineHandler().setStackInSlot(8,new ItemStack(Items.EGG,31));b.getFilterHandler().setStackInSlot(26,new ItemStack(Items.COW_SPAWN_EGG));var data=b.saveWithFullMetadata();data.putInt("tickCounter",17);data.putInt("acceleratedTicks",11);b.load(data);return b;}
 @GameTest(template="mineral_empty") public static void actualUpgradeMovesNineOutputsToEighteenAndAllActiveStorage(GameTestHelper h){
  var old=source(h);var oldItems=old.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());h.assertTrue(card.useOn(ctx(h,card))==InteractionResult.SUCCESS&&card.isEmpty(),"real new nineteenth Extended conversion succeeds");var b=(ExtendedBioExtractorBE)h.getBlockEntity(POS);
  h.assertTrue(b.getMachineHandler().getSlots()==18&&b.getMachineHandler().getStackInSlot(8).getCount()==31&&b.getMachineHandler().getStackInSlot(17).isEmpty()&&b.getEnergyStorage().getEnergyStored()==140000&&b.getFluidTank().getFluidAmount()==30000,"all physical output/FE/fluid storage migrates");
  h.assertTrue(UpgradeHelper.getUpgradeHandler(b).getSlots()==8&&UpgradeHelper.getUpgradeHandler(b).getStackInSlot(3).is(JDTEItems.OVERCLOCK_UPGRADE.get())&&b.getFilterHandler().getStackInSlot(26).is(Items.COW_SPAWN_EGG)&&b.getScanProgress()==17&&b.saveWithFullMetadata().getInt("acceleratedTicks")==11,"cards, last filter page and both progress counters preserved");h.assertTrue(oldItems.extractItem(8,31,false).isEmpty()&&old.getEnergyStorage().getEnergyStored()==0,"detached public item port and raw old FE have no second resources");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void protectionDenialRestoresEntireBioStateAndCard(GameTestHelper h){
  var old=source(h);var before=old.saveWithFullMetadata();var called=new int[1];java.util.function.Consumer<net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent> deny=e->{if(e.getLevel()==h.getLevel()&&e.getPos().equals(old.getBlockPos())){called[0]++;e.setCanceled(true);}};
  net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL,false,net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent.class,deny);
  try{var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());h.assertTrue(card.useOn(ctx(h,card))==InteractionResult.FAIL&&card.getCount()==1&&called[0]==1&&((AdvancedBioExtractorBE)h.getBlockEntity(POS)).saveWithFullMetadata().equals(before),"actual Forge protection rollback preserves exact unique NBT/resources and conversion card");}finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(deny);}h.succeed();
 }
 @GameTest(template="mineral_empty") public static void packedCraftingAndActualWrenchPlacementRetainBioResources(GameTestHelper h){
  var b=source(h);var old=new ItemStack(JDTEItems.ADVANCED_BIO_EXTRACTOR.get());old.getOrCreateTag().put("JustDiresBEData",b.saveWithoutMetadata());var input=new net.minecraft.world.inventory.TransientCraftingContainer(new net.minecraft.world.inventory.AbstractContainerMenu(null,0){public boolean stillValid(net.minecraft.world.entity.player.Player p){return true;}public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player p,int i){return ItemStack.EMPTY;}},3,3);input.setItem(1,new ItemStack(JDTEItems.EXTENDED_UPGRADE.get()));input.setItem(4,old);
  var recipe=(net.minecraft.world.item.crafting.CraftingRecipe)h.getLevel().getRecipeManager().byKey(com.jdte.JDTE.id("extended_bio_extractor")).orElseThrow();h.assertTrue(recipe.matches(input,h.getLevel()),"registered packed crafting recipe matches");var output=recipe.assemble(input,h.getLevel().registryAccess());h.setBlock(POS,JDTEBlocks.EXTENDED_BIO_EXTRACTOR.get());var ext=(ExtendedBioExtractorBE)h.getBlockEntity(POS);ext.load(output.getTag().getCompound("JustDiresBEData"));h.assertTrue(ext.getMachineHandler().getSlots()==18&&ext.getEnergyStorage().getEnergyStored()==140000&&ext.getFluidTank().getFluidAmount()==30000&&UpgradeHelper.getUpgradeHandler(ext).getStackInSlot(3).is(JDTEItems.OVERCLOCK_UPGRADE.get()),"packed recipe keeps source state and expands real inventory");
  var p=h.makeMockSurvivalPlayer();p.setPos(ext.getBlockPos().getX()+.5,ext.getBlockPos().getY()+1,ext.getBlockPos().getZ()+.5);h.assertTrue(EclipseAlloyWrenchItem.pickupMachine(h.getLevel(),ext.getBlockPos(),p),"actual wrench packs extractor");var packed=p.getInventory().items.stream().filter(s->s.is(JDTEItems.EXTENDED_BIO_EXTRACTOR.get())).findFirst().orElseThrow();p.setItemInHand(InteractionHand.MAIN_HAND,packed);h.assertTrue(packed.useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(h.absolutePos(POS)),Direction.UP,h.absolutePos(POS),false))).consumesAction(),"actual packed BlockItem placement");var fresh=(ExtendedBioExtractorBE)h.getBlockEntity(POS);h.assertTrue(fresh.getMachineHandler().getStackInSlot(8).getCount()==31&&fresh.getEnergyStorage().getEnergyStored()==140000&&fresh.getFluidTank().getFluidAmount()==30000&&fresh.getScanProgress()==17,"placed packed extractor retains outputs, FE, fluid and progress");h.succeed();
 }
}
