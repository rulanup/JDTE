package com.jdte.common.integrations;

import com.brandon3055.draconicevolution.blocks.tileentity.StabilizedSpawnerLogic;
import com.brandon3055.draconicevolution.blocks.tileentity.TileStabilizedSpawner;
import com.jdte.common.utils.SpawnerCrushHelper;
import com.jdte.mixin.StabilizedSpawnerLogicAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import org.jetbrains.annotations.Nullable;

/**
 * 龙之研究稳定刷怪笼兼容：稳定刷怪笼的逻辑类继承原版 BaseSpawner，但覆写了
 * serverTick——延迟走自有 ManagedShort、实体由怪物灵魂（MobSoul）创建，
 * 因此原版拦截点对它无效。此集成在 DE 原生生成时刻（MobSoul.createEntity 调用点，
 * 由 SpawnerMixin 的嵌套拦截触发）消费本次生成周期：
 * 实体类型从显示实体（内部读取怪物灵魂）取 id 并构造只含 id 的 SpawnData，
 * 与原版刷怪笼一样走 finalizeSpawn；数量按稳定刷怪笼档位换算。
 * 粉碎成功后调用私有的 resetTimer 重新武装原生延迟并取消实体生成。
 */
public final class DraconicStabilizedSpawnerIntegration {
    /** 原版刷怪笼默认每次生成数量，反射失败时的兜底值。 */
    private static final int FALLBACK_SPAWN_COUNT = 4;

    private DraconicStabilizedSpawnerIntegration() {
    }

    public static boolean tryCrush(ServerLevel level, BlockPos pos, BaseSpawner spawner, @Nullable Object logicObj) {
        if (!(logicObj instanceof StabilizedSpawnerLogic logic)) {
            return false;
        }
        // 显示实体内部读取怪物灵魂，无需触及 brandonscore 字段即可确定实体类型。
        // 该方法主要面向客户端渲染，服务端调用异常时安全回退（不粉碎，DE 正常生成）。
        Entity display;
        try {
            display = logic.getOrCreateDisplayEntity(level, pos);
        } catch (RuntimeException ignored) {
            return false;
        }
        if (display == null) {
            return false;
        }
        SpawnData spawnData = new SpawnData();
        spawnData.getEntityToSpawn().putString("id", EntityType.getKey(display.getType()).toString());

        int spawnCount = resolveSpawnCount(logic);
        if (!SpawnerCrushHelper.tryCrush(level, pos, spawner, spawnData, spawnCount)) {
            return false;
        }
        ((StabilizedSpawnerLogicAccessor) logic).jdte$resetTimer();
        return true;
    }

    /**
     * 稳定刷怪笼档位的单次生成数量：tile.spawnerTier（ManagedEnum，brandonscore
     * 类型不在编译类路径）经反射解包后调用档位的 getSpawnCount。
     */
    private static int resolveSpawnCount(StabilizedSpawnerLogic logic) {
        try {
            TileStabilizedSpawner tile = ((StabilizedSpawnerLogicAccessor) logic).jdte$tile();
            Object managedEnum = TileStabilizedSpawner.class.getField("spawnerTier").get(tile);
            Object tier = managedEnum.getClass().getMethod("get").invoke(managedEnum);
            if (tier instanceof TileStabilizedSpawner.SpawnerTier spawnerTier) {
                return spawnerTier.getSpawnCount();
            }
        } catch (ReflectiveOperationException | ClassCastException | NullPointerException ignored) {
            // 版本差异导致反射失败时退回原版默认数量
        }
        return FALLBACK_SPAWN_COUNT;
    }
}
