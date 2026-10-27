package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.items.*;
import com.jdte.common.recipes.GreenhouseCropDefinition;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import java.util.*;

/** Deterministic actual harvest-path tests, registered when Mystical Agriculture is present. */
@PrefixGameTestTemplate(false)
public final class SeedHarvestGameTests {
    private static final BlockPos POS=new BlockPos(3,1,2);
    private static Item essence() { return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mysticalagriculture","iron_essence")); }
    private static Item seed() { return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mysticalagriculture","iron_seeds")); }
    private static BaseMachineBE machine(GameTestHelper h,boolean large,boolean conversion) {
        h.setBlock(POS,large?JDTEBlocks.LARGE_GREENHOUSE.get():JDTEBlocks.GREENHOUSE.get());
        var machine=(BaseMachineBE)h.getBlockEntity(POS);
        if(conversion)UpgradeHelper.getUpgradeHandler(machine).insertItem(0,new ItemStack(JDTEItems.SEED_CONVERSION_UPGRADE.get()),false);
        machine.getMachineHandler().setStackInSlot(0,new ItemStack(seed(),16));return machine;
    }
    private static int count(BaseMachineBE be,Item item){int n=0;int start=be instanceof LargeGreenhouseBE?LargeGreenhouseBE.OUTPUT_START_SLOT:GreenhouseBE.OUTPUT_START_SLOT;for(int k=start;k<be.getMachineHandler().getSlots();k++)if(be.getMachineHandler().getStackInSlot(k).is(item))n+=be.getMachineHandler().getStackInSlot(k).getCount();return n;}
    private static int harvest(GameTestHelper h,BaseMachineBE be,int times) {
        // Invoke the real private harvest boundary with a deterministic crop generator; no fake inventory.
        var definition=new GreenhouseCropDefinition(List.of(new ItemStack(essence())),ResourceLocation.withDefaultNamespace("wheat"),null,false,1,1,
                (level,pos,tool)->List.of(new ItemStack(essence()),new ItemStack(seed(),3)));
        try {
            var outputField=be.getClass().getDeclaredField("internalOutputHandler");outputField.setAccessible(true);
            var ledger=GreenhouseCapacityLedger.capture((IItemHandler)outputField.get(be));
            var method=Arrays.stream(be.getClass().getDeclaredMethods()).filter(m->m.getName().equals("generateAndStoreDrops")).findFirst().orElseThrow();method.setAccessible(true);
            Object[] args=be instanceof LargeGreenhouseBE?new Object[]{h.getLevel(),new ItemStack(seed()),definition,times,0,ledger}:new Object[]{h.getLevel(),new ItemStack(seed()),definition,times,ledger};
            return (int)method.invoke(be,args);
        } catch(java.lang.reflect.InvocationTargetException error){throw new IllegalStateException("Actual greenhouse harvest failed",error.getCause());}
        catch(ReflectiveOperationException error){throw new IllegalStateException(error);}
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty") public static void seedCardConvertsNewHarvestSeedsOnly(GameTestHelper h){
        var be=machine(h,false,false);h.assertTrue(h.getLevel().getRecipeManager().byKey(com.jdte.JDTE.id("seed_conversion_upgrade")).isPresent(),"real conditional crafting recipe loaded");h.assertTrue(harvest(h,be,2)==2&&count(be,seed())==6&&count(be,essence())==2,"unupgraded harvest preserves original seed drops");
        UpgradeHelper.getUpgradeHandler(be).insertItem(0,new ItemStack(JDTEItems.SEED_CONVERSION_UPGRADE.get()),false);
        h.assertTrue(harvest(h,be,2)==2&&count(be,seed())==6&&count(be,essence())==10,"new seeds replaced one-for-one; existing stored seeds remain unchanged");
        h.assertTrue(be.getMachineHandler().getStackInSlot(0).getCount()==16,"planted templates are reusable");h.succeed();
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty") public static void largeSeedConversionAndUpgradeSurviveSave(GameTestHelper h){
        var be=machine(h,true,true);h.assertTrue(harvest(h,be,2)==2&&count(be,essence())==8&&count(be,seed())==0,"large greenhouse converts all new seed drops");
        var nbt=be.saveWithFullMetadata();be.load(nbt);h.assertTrue(UpgradeHelper.hasSeedConversionUpgrade(be)&&count(be,essence())==8&&be.getMachineHandler().getStackInSlot(0).getCount()==16,"saved card, output and templates preserved");h.succeed();
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty") public static void seedConversionFitsConvertedCountsBeforeCommitting(GameTestHelper h){
        var be=machine(h,false,true);var normal=(GreenhouseBE)be;
        for(int k=0;k<normal.getActiveOutputSlots();k++)be.getMachineHandler().setStackInSlot(GreenhouseBE.OUTPUT_START_SLOT+k,new ItemStack(Items.COBBLESTONE,normal.getOutputSlotLimit()));
        be.getMachineHandler().setStackInSlot(GreenhouseBE.OUTPUT_START_SLOT,new ItemStack(essence(),normal.getOutputSlotLimit()-1));
        var before=be.saveWithFullMetadata().getCompound("inventory").copy();
        h.assertTrue(harvest(h,be,1)==0&&before.equals(be.saveWithFullMetadata().getCompound("inventory")),"four converted essence cannot consume a one-item gap");h.succeed();
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty") public static void seedConversionPrecedesRecordedEssenceCrafting(GameTestHelper h){
        var be=machine(h,false,true);var grid=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));for(int k=0;k<9;k++)if(k!=4)grid.set(k,new ItemStack(essence()));
        var pattern=CraftingPatternData.capture(h.getLevel(),grid).orElseThrow();var upgrade=new ItemStack(JDTEItems.ESSENCE_CONVERSION_UPGRADE.get());EssenceConversionUpgradeItem.setPatterns(upgrade,List.of(pattern));
        UpgradeHelper.getUpgradeHandler(be).insertItem(1,upgrade,false);
        int completed=harvest(h,be,2);((GreenhouseBE)be).prepareOutputConversion();
        h.assertTrue(completed==2&&count(be,seed())==0&&count(be,essence())==0&&count(be,pattern.result().getItem())==pattern.result().getCount(),"seed-to-essence conversion feeds the recorded recipe before output");h.succeed();
    }
}
