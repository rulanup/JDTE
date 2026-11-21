package com.jdte.mixin;

import com.brandon3055.draconicevolution.blocks.tileentity.TileStabilizedSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Draconic Evolution 稳定刷怪笼逻辑的访问器：私有 tile 字段与私有的 resetTimer
 * （粉碎后重新武装原生延迟）。@Accessor 按方法描述符精确匹配目标成员，getter 的
 * 返回类型必须与字段类型完全一致——返回 Object 会按 tile:Ljava/lang/Object; 查找，
 * 找不到候选导致应用失败、放置刷怪笼即崩溃，因此必须返回字段的确切类型
 * TileStabilizedSpawner（DE 为 compileOnly 依赖）。tile 上的 spawnerTier 属
 * brandonscore ManagedEnum 类型，不在编译类路径，仍需反射解包。
 */
@Pseudo
@Mixin(targets = "com.brandon3055.draconicevolution.blocks.tileentity.StabilizedSpawnerLogic", remap = false)
public interface StabilizedSpawnerLogicAccessor {

    @Accessor(value = "tile", remap = false)
    TileStabilizedSpawner jdte$tile();

    @Invoker(value = "resetTimer", remap = false)
    void jdte$resetTimer();
}
