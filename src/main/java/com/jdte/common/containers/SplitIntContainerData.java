package com.jdte.common.containers;

import net.minecraft.world.inventory.ContainerData;

/** Vanilla menu properties are signed shorts on the wire; expose each logical int as two words. */
public final class SplitIntContainerData implements ContainerData {
    private final ContainerData logical;
    private final int[] received;
    public SplitIntContainerData(ContainerData logical) { this.logical=logical;received=new int[logical.getCount()]; }
    @Override public int getCount() { return logical.getCount()*2; }
    @Override public int get(int index) { int n=logical.get(index/2);return (index&1)==0?n&0xffff:n>>>16; }
    @Override public void set(int index,int value) {
        int key=index/2;
        received[key]=(index&1)==0?(received[key]&0xffff0000)|(value&0xffff):(received[key]&0xffff)|((value&0xffff)<<16);
        logical.set(key,received[key]);
    }
}
