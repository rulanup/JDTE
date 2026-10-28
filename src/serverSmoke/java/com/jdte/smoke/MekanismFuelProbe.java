package com.jdte.smoke;

import com.jdte.common.blockentities.FluidMixerBE;
import com.jdte.common.integrations.MekanismFluidMixerIntegration;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import java.nio.file.*;

/** Optional production-runtime probe of the actual installed Extras datapack recipe. */
public final class MekanismFuelProbe {
    private boolean finished;private int ticks;
    private static void check(boolean v,String s){if(!v)throw new IllegalStateException("Extras fuel: "+s);}
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){
        if(finished||e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.mekeFuel"))return;if(++ticks<100)return;
        try{
            check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked disposable server required");var level=e.getServer().overworld();
            var nativeId=ResourceLocation.fromNamespaceAndPath("mekanism_extras","chemical_infusing/fluorinated_naquadah_uranium_fuel");check(level.getRecipeManager().byKey(nativeId).isPresent(),"real Extras recipe loaded");
            var candidates=MekanismFluidMixerIntegration.getCachedRecipes(level);var recipe=candidates.stream().filter(r->r.getId().getPath().endsWith(nativeId.getPath())).findFirst().orElseThrow(()->new IllegalStateException("No mapped fuel recipe; registered fluids="+BuiltInRegistries.FLUID.keySet().stream().filter(id->id.getPath().contains("uranium")||id.getPath().contains("naquadah")).toList()));
            check(BuiltInRegistries.FLUID.getKey(recipe.output().getFluid()).equals(ResourceLocation.fromNamespaceAndPath("mekanism_extras","fluorinated_naquadah_uranium_fuel")),"mapped product is real registered liquid fuel");
            for(int i=0;i<2;i++){
                var p=new BlockPos(1200+i*2,90,1200);level.setChunkForced(p.getX()>>4,p.getZ()>>4,true);level.getChunkAt(p);level.removeBlockEntity(p);level.setBlock(p,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2|64);level.setBlockAndUpdate(p,(i==0?JDTEBlocks.ADVANCED_FLUID_MIXER.get():JDTEBlocks.EXTENDED_FLUID_MIXER.get()).defaultBlockState());var be=(FluidMixerBE)level.getBlockEntity(p);
                UpgradeHelper.getUpgradeHandler(be).insertItem(0,new ItemStack(i==0?JDTEItems.MIXING_UPGRADE.get():JDTEItems.ULTIMATE_MIXING_PARALLEL_UPGRADE.get()),false);
                // Reverse the source recipe's two input fluids deliberately.
                var a=recipe.fluidInputB().copy();a.setAmount(a.getAmount()*25);var b=recipe.fluidInputA().copy();b.setAmount(b.getAmount()*25);be.inputTankA.setFluid(a);be.inputTankB.setFluid(b);be.getEnergyStorage().receiveEnergy(i==0?100000:200000,false);
                for(int tick=0;tick<100;tick++)be.tickServer();int batches=i==0?8:25;
                check(be.outputTank.getFluid().getFluid()==recipe.output().getFluid()&&be.outputTank.getFluidAmount()==recipe.output().getAmount()*batches,"actual tier "+i+" produced liquid, not air");check(be.inputTankA.getFluidAmount()==recipe.fluidInputB().getAmount()*(25-batches)&&be.inputTankB.getFluidAmount()==recipe.fluidInputA().getAmount()*(25-batches),"exact swapped-input consumption");check(be.getEnergyStorage().getEnergyStored()==(i==0?100000:200000)-5000*batches,"exact native recipe energy per batch");
            }
            String observations="Installed Forge Extras fuel recipe="+nativeId+"; mapped recipes="+candidates.size()+"; uranium/silicon registry IDs="+BuiltInRegistries.FLUID.keySet().stream().filter(id->id.getNamespace().equals("mekanism_extras")&&(id.getPath().contains("uranium")||id.getPath().contains("silicon"))).toList();
            Files.writeString(Path.of("meke-fuel-observations.txt"),observations+"\n");Files.writeString(Path.of("meke-fuel-success.txt"),"PASS: actual Mekanism Extras fuel recipe mapped to real fluids and completed 100 native work calls in both production mixers; swapped inputs, 8/25 parallel batches and exact FE/product accounting\n");finished=true;e.getServer().halt(false);
        }catch(Exception x){try{Files.writeString(Path.of("meke-fuel-failure.txt"),x.toString());}catch(java.io.IOException io){x.addSuppressed(io);}throw new IllegalStateException(x);}
    }
}
