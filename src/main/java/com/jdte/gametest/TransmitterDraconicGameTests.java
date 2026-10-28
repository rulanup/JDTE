package com.jdte.gametest;

import com.brandon3055.draconicevolution.init.DEContent;
import com.brandon3055.draconicevolution.blocks.tileentity.TileFusionCraftingInjector;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Actual registered Draconic injector, public demand API, real transmitter debit. Not a complete fusion recipe. */
@PrefixGameTestTemplate(false)
public final class TransmitterDraconicGameTests {
    @GameTest(templateNamespace="jdte",template="mineral_empty") public static void actualDraconicInjectorReceivesBoundedRealFeAndFullTargetStops(GameTestHelper h){
        var a=new BlockPos(2,1,2);var b=new BlockPos(4,1,2);
        h.setBlock(a,JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get());h.setBlock(b,DEContent.BASIC_CRAFTING_INJECTOR.get());
        var old=h.getBlockEntity(a);var source=new AdvancedEnergyTransmitterBE(old.getBlockPos(),old.getBlockState()){@Override public AABB getAABB(BlockPos ignored){return new AABB(h.absolutePos(b));}};h.getLevel().setBlockEntity(source);
        var injector=(TileFusionCraftingInjector)h.getBlockEntity(b);injector.setEnergyRequirement(10000,1000);injector.setInjectorEnergy(0);
        source.getEnergyStorage().setEnergy(6500);source.tickServer();
        h.assertTrue(injector.getInjectorEnergy()==6500&&source.getEnergyStorage().getEnergyStored()==0,"real Draconic energy field received exactly the owned FE");
        source.getEnergyStorage().setEnergy(6000);source.tickServer();
        h.assertTrue(injector.getInjectorEnergy()==10000&&source.getEnergyStorage().getEnergyStored()==2500,"actual injector demand clips transfer at the remaining 3500 FE");
        source.tickServer();h.assertTrue(injector.getInjectorEnergy()==10000&&source.getEnergyStorage().getEnergyStored()==2500,"full injector cannot consume more FE");h.succeed();
    }
}
