package com.jdte.common.items;

/** Source4000mB container; one committed batch always consumes four potions. */
public final class PotionCanisterMath {
    public static final int CAPACITY=4000, BATCH=1000, POTIONS_PER_BATCH=4;
    private PotionCanisterMath(){}
    public static boolean canFill(int stored,int offered){return stored>=0&&stored<=CAPACITY-BATCH&&offered>=POTIONS_PER_BATCH;}
    public static int boundedAmount(long amount){return (int)Math.max(0,Math.min(CAPACITY,amount));}
    public static int fullness(int amount){return amount<=0?0:(int)Math.min(4,(amount+999L)/1000L);}
}
