package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.setup.Config;
import com.jdte.common.blockentities.ExtendedFluidGeneratorBE;
import com.jdte.common.items.UpgradeCardItem;
import net.minecraft.world.item.ItemStack;

/** Reject generator/Bio card changes that would silently discard stored FE or fluid. */
public final class GeneratorCapacityGuard {
    private GeneratorCapacityGuard() {}
    public static boolean permits(BaseMachineBE machine,UpgradeItemStackHandler handler,int slot,ItemStack replacement) {
        if(!UpgradeHelper.isExtendedGenerator(machine) && !(machine instanceof com.jdte.common.blockentities.BioExtractorBE))return true;
        if(handler!=UpgradeHelper.getUpgradeHandler(machine))return true;
        var layout=new java.util.ArrayList<ItemStack>(handler.getSlots());
        for(int i=0;i<handler.getSlots();i++)layout.add(i==slot?replacement:handler.getStackInSlot(i));
        return permitsLayout(machine,layout);
    }
    public static boolean permitsLayout(BaseMachineBE machine,java.util.List<ItemStack> layout) {
        if(!UpgradeHelper.isExtendedGenerator(machine) && !(machine instanceof com.jdte.common.blockentities.BioExtractorBE))return true;
        int capacity=0,fluid=0;boolean ultimate=false;
        for(ItemStack stack:layout) {
            if(stack.getItem() instanceof UpgradeCardItem card) {
                if(card.getType()==UpgradeType.CAPACITY)capacity++;
                if(card.getType()==UpgradeType.FLUID)fluid++;
                if(card.getType()==UpgradeType.ULTIMATE_CAPACITY)ultimate=true;
            }
        }
        int base=machine instanceof com.jdte.common.blockentities.BioExtractorBE bio?bio.baseEnergyCapacity():machine instanceof ExtendedFluidGeneratorBE?Config.GENERATOR_FLUID_T1_MAX_FE.get():Config.GENERATOR_T1_MAX_FE.get();
        int energyLimit=scaled(base,capacity,ultimate);
        if(((PoweredMachineBE)machine).getEnergyStorage().getEnergyStored()>energyLimit)return false;
        if(machine instanceof com.jdte.common.blockentities.BioExtractorBE bio)return bio.getFluidTank().getFluidAmount()<=scaled(bio.baseFluidCapacity(),capacity+fluid,ultimate);
        return !(machine instanceof ExtendedFluidGeneratorBE liquid)||liquid.getFluidTank().getFluidAmount()<=scaled(4000,capacity+fluid,ultimate);
    }
    static int scaled(int base,int powers,boolean ultimate) {return (int)Math.min(Integer.MAX_VALUE,((long)base*(ultimate?50:1))<<Math.min(6,Math.max(0,powers)));}
}
