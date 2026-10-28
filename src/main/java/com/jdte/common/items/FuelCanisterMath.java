package com.jdte.common.items;

/** Checked whole-fuel accounting, independent of Minecraft for regression tests. */
public final class FuelCanisterMath {
    private FuelCanisterMath() {}
    public static int acceptedItems(int stored,int capacity,int burnTime,int offered){
        if(stored<0||capacity<stored||burnTime<=0||offered<=0)return 0;
        return (int)Math.min(offered,((long)capacity-stored)/burnTime);
    }
    public static int energyBudget(int burnTicks,int fePerTick,int multiplier){
        if(burnTicks<=0||fePerTick<=0||multiplier<=0)return 0;
        long base=(long)burnTicks*fePerTick;
        return base>Integer.MAX_VALUE/multiplier?0:(int)(base*multiplier);
    }
    public static int rate(int base,int multiplier){return (int)Math.min(Integer.MAX_VALUE,(long)Math.max(0,base)*Math.max(0,multiplier));}
    public static int safeBurnCounter(int burnTicks,int speed){return Math.min(Integer.MAX_VALUE-1,Math.max(0,burnTicks)/Math.max(1,speed));}
}
