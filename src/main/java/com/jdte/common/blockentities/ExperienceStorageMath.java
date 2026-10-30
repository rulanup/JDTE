package com.jdte.common.blockentities;

/** Integer XP is authoritative; Forge's int-sized fluid view is rounded to whole XP. */
public final class ExperienceStorageMath {
    public static final int MB_PER_XP = 20;
    private ExperienceStorageMath() {}

    public static int acceptedXp(int stored, int offered) {
        return (int) Math.max(0L, Math.min((long) offered, Integer.MAX_VALUE - (long) Math.max(0, stored)));
    }

    public static int fluidAmount(int xp) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, (long) xp * MB_PER_XP));
    }

    public static int fillAmount(int xp, int offered) {
        long available = Math.max(0L, Integer.MAX_VALUE - (long) Math.max(0, xp) * MB_PER_XP);
        return (int) (Math.min(Math.max(0, offered) / MB_PER_XP, available / MB_PER_XP) * MB_PER_XP);
    }

    public static int drainAmount(int xp, int requested) {
        return Math.min(Math.max(0, xp), Math.max(0, requested) / MB_PER_XP) * MB_PER_XP;
    }

    public static int acceptedOrbCount(int stored, int value, int count) {
        return value <= 0 ? 0 : Math.min(Math.max(0, count), (Integer.MAX_VALUE - Math.max(0, stored)) / value);
    }
}
