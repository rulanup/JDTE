package com.jdte.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TimeAccelerationTargetTest {

    @Test
    void targetCopiesPositionButPreservesServerLevelIdentity() throws Exception {
        ServerLevel first = serverLevelFixture();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(3, 4, 5);
        TimeAccelerationTarget target = new TimeAccelerationTarget(first, mutable);
        mutable.set(6, 7, 8);

        assertSame(first, target.level());
        assertEquals(new BlockPos(3, 4, 5), target.pos());
    }

    private static ServerLevel serverLevelFixture() throws Exception {
        return (ServerLevel) unsafe().allocateInstance(ServerLevel.class);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }
}
