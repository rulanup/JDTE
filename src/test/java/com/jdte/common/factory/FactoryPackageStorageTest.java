package com.jdte.common.factory;

import com.jdte.test.TestWorldRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FactoryPackageStorageTest {
    private static HolderLookup.Provider registries;

    @BeforeAll
    static void loadRegistries() {
        registries = TestWorldRegistries.load();
    }

    @Test
    void restoresCurrentAndLegacyPackages() throws IOException {
        UUID packageId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        var entity = new CompoundTag();
        entity.store("UUID", UUIDUtil.CODEC, entityId);
        var machine = new CompoundTag();
        machine.putLong("energy", 9_000_000_000L);
        var record = new FactoryPackageStorage.BlockRecord(BlockPos.ZERO, Blocks.CHEST.defaultBlockState(), machine);
        var data = new FactoryPackageStorage.PackageData(2, packageId, Identifier.parse("minecraft:overworld"),
                new BlockPos(19, 252, -90), new Vec3i(2, 1, 1), List.of(record),
                List.of(new FactoryPackageStorage.EntityRecord(entityId, entity)),
                List.of(new FactoryPackageStorage.TickRecord(true, Identifier.parse("minecraft:water"),
                        new BlockPos(1, 0, 0), 27, -2)));
        var tag = FactoryPackageStorage.encode(data);
        assertEquals(data, FactoryPackageStorage.decode(packageId, tag, registries));
        var unavailable = tag.copy();
        unavailable.getListOrEmpty("blocks").getCompoundOrEmpty(0).getCompoundOrEmpty("state")
                .putString("Name", "missing:barrel");
        assertTrue(assertThrows(IOException.class, () -> FactoryPackageStorage.decode(packageId, unavailable, registries))
                .getMessage().contains("missing:barrel"));
        tag.putInt("version", 1);
        for (String key : List.of("sourceDimension", "sourceOrigin", "entities", "ticks")) tag.remove(key);
        var legacy = FactoryPackageStorage.decode(packageId, tag, registries);
        assertEquals(List.of(record), legacy.blocks());
        assertEquals(BlockPos.ZERO, legacy.sourceOrigin());
        assertTrue(legacy.entities().isEmpty());
        assertTrue(legacy.ticks().isEmpty());
        var blocks = tag.getListOrEmpty("blocks");
        blocks.add(blocks.getFirst().copy());
        assertThrows(IOException.class, () -> FactoryPackageStorage.decode(packageId, tag, registries));
    }
}
