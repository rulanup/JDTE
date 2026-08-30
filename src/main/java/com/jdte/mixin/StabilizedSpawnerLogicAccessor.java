package com.jdte.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Draconic Evolution 稳定刷怪笼逻辑的访问器：私有 tile 字段（brandonscore 类型
 * 不在编译类路径，故以 Object 返回）与私有的 resetTimer（粉碎后重新武装原生延迟）。
 */
@Pseudo
@Mixin(targets = "com.brandon3055.draconicevolution.blocks.tileentity.StabilizedSpawnerLogic", remap = false)
public interface StabilizedSpawnerLogicAccessor {

    @Accessor(value = "tile", remap = false)
    Object jdte$tile();

    @Invoker(value = "resetTimer", remap = false)
    void jdte$resetTimer();
}
