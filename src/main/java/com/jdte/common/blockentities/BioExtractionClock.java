package com.jdte.common.blockentities;

/** Source scan counter and bounded Time Accelerator batches, without integer overflow. */
public final class BioExtractionClock {
    private BioExtractionClock() {}
    public record Advance(int remaining,int cycles) {}
    public static Advance advance(int progress,int added,int interval) {
        int period=Math.max(1,interval);long total=(long)Math.max(0,progress)+Math.max(0,added);
        return new Advance((int)(total%period),(int)Math.min(64,total/period));
    }
}
