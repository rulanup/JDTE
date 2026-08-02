package com.jdte.common.recipes;

/**
 * 全局配方缓存失效信号：数据包重载时递增，机器侧据此丢弃自身配方缓存。
 * 与 {@link GreenhouseCropResolver} 的失效计数分离，避免作物注册与机器配方语义耦合。
 */
public final class RecipeCacheSignal {
    private static long generation;

    private RecipeCacheSignal() {
    }

    public static long generation() {
        return generation;
    }

    public static void invalidate() {
        generation++;
    }
}
