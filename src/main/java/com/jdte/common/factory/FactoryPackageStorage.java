package com.jdte.common.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public final class FactoryPackageStorage {
    private static final int FORMAT_VERSION = 2;
    private static final String DIRECTORY = "jdte_factory_packages";

    private FactoryPackageStorage() {}

    public static void write(MinecraftServer server, PackageData data, long maxCompressedBytes) throws IOException {
        Path path = packagePath(server, data.id(), null);
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        Files.createDirectories(path.getParent());
        NbtIo.writeCompressed(encode(data), temp);
        if (Files.size(temp) > maxCompressedBytes) {
            Files.deleteIfExists(temp);
            throw new IOException("Factory package exceeds compressed size limit");
        }
        moveReplace(temp, path);
    }

    public static PackageData read(MinecraftServer server, UUID id, UUID claimToken,
                                   long maxUncompressedBytes, HolderLookup.Provider registries) throws IOException {
        CompoundTag root = NbtIo.readCompressed(packagePath(server, id, claimToken),
                NbtAccounter.create(maxUncompressedBytes));
        return decode(id, root, registries);
    }

    public static void claim(MinecraftServer server, UUID id, UUID claimToken) throws IOException {
        if (claimToken == null) throw new IOException("Factory package claim token is missing");
        Path source = packagePath(server, id, null);
        Path claimed = packagePath(server, id, claimToken);
        if (Files.exists(claimed)) {
            if (Files.exists(source)) throw new IOException("Factory package has conflicting storage files");
            return;
        }
        if (!Files.exists(source)) throw new IOException("Factory package data is missing or already in use");
        moveReplace(source, claimed);
    }

    public static void release(MinecraftServer server, UUID id, UUID claimToken) throws IOException {
        if (claimToken == null) throw new IOException("Factory package claim token is missing");
        Path claimed = packagePath(server, id, claimToken);
        Path regular = packagePath(server, id, null);
        if (Files.exists(claimed) && Files.exists(regular)) {
            throw new IOException("Factory package has conflicting storage files");
        }
        if (Files.exists(claimed)) moveReplace(claimed, regular);
    }

    public static void deleteRegular(MinecraftServer server, UUID id) throws IOException {
        Files.deleteIfExists(packagePath(server, id, null));
    }

    public static void deleteClaimed(MinecraftServer server, UUID id, UUID claimToken) throws IOException {
        if (claimToken == null) throw new IOException("Factory package claim token is missing");
        Files.deleteIfExists(packagePath(server, id, claimToken));
    }

    private static Path packagePath(MinecraftServer server, UUID id, UUID claimToken) {
        String suffix = claimToken == null ? ".nbt" : "." + claimToken + ".placing.nbt";
        return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(DIRECTORY)
                .resolve(id + suffix);
    }

    private static void moveReplace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static CompoundTag encode(PackageData data) {
        CompoundTag root = new CompoundTag();
        root.putInt("version", FORMAT_VERSION);
        root.putUUID("id", data.id());
        if (data.sourceDimension() != null) root.putString("sourceDimension", data.sourceDimension().toString());
        root.putLong("sourceOrigin", data.sourceOrigin().asLong());
        root.putIntArray("size", List.of(data.size().getX(), data.size().getY(), data.size().getZ()));
        ListTag blocks = new ListTag();
        for (BlockRecord record : data.blocks()) {
            CompoundTag entry = new CompoundTag();
            entry.putIntArray("pos", List.of(record.relativePos().getX(), record.relativePos().getY(),
                    record.relativePos().getZ()));
            entry.put("state", NbtUtils.writeBlockState(record.state()));
            if (record.blockEntityData() != null) entry.put("blockEntity", record.blockEntityData().copy());
            blocks.add(entry);
        }
        root.put("blocks", blocks);
        ListTag entities = new ListTag();
        for (EntityRecord record : data.entities()) entities.add(record.data().copy());
        root.put("entities", entities);
        ListTag ticks = new ListTag();
        for (TickRecord record : data.ticks()) {
            CompoundTag entry = new CompoundTag();
            entry.putBoolean("fluid", record.fluid());
            entry.putString("type", record.type().toString());
            entry.putIntArray("pos", List.of(record.relativePos().getX(), record.relativePos().getY(),
                    record.relativePos().getZ()));
            entry.putInt("delay", record.delay());
            entry.putInt("priority", record.priority());
            ticks.add(entry);
        }
        root.put("ticks", ticks);
        return root;
    }

    private static PackageData decode(UUID expectedId, CompoundTag root, HolderLookup.Provider registries)
            throws IOException {
        int version = root.getInt("version");
        if (version < 1 || version > FORMAT_VERSION || !root.hasUUID("id")
                || !expectedId.equals(root.getUUID("id"))) {
            throw new IOException("Invalid factory package data");
        }
        int[] size = root.getIntArray("size");
        if (size.length != 3 || size[0] <= 0 || size[1] <= 0 || size[2] <= 0) {
            throw new IOException("Invalid factory package dimensions");
        }
        List<BlockRecord> blocks = new ArrayList<>();
        Set<Long> occupied = new HashSet<>();
        ListTag list = root.getList("blocks", Tag.TAG_COMPOUND);
        long volume = (long) size[0] * size[1] * size[2];
        if (list.size() > volume) throw new IOException("Factory package contains too many blocks");
        var blockLookup = registries.lookupOrThrow(Registries.BLOCK);
        for (int index = 0; index < list.size(); index++) {
            CompoundTag entry = list.getCompound(index);
            int[] pos = entry.getIntArray("pos");
            if (pos.length != 3 || !entry.contains("state", Tag.TAG_COMPOUND)) {
                throw new IOException("Invalid factory package block record");
            }
            if (pos[0] < 0 || pos[0] >= size[0] || pos[1] < 0 || pos[1] >= size[1]
                    || pos[2] < 0 || pos[2] >= size[2]) {
                throw new IOException("Factory package block lies outside its dimensions");
            }
            long packedPos = BlockPos.asLong(pos[0], pos[1], pos[2]);
            if (!occupied.add(packedPos)) throw new IOException("Factory package contains duplicate blocks");
            BlockState state = NbtUtils.readBlockState(blockLookup, entry.getCompound("state"));
            CompoundTag blockEntityData = entry.contains("blockEntity", Tag.TAG_COMPOUND)
                    ? entry.getCompound("blockEntity").copy() : null;
            blocks.add(new BlockRecord(new BlockPos(pos[0], pos[1], pos[2]), state, blockEntityData));
        }
        ResourceLocation sourceDimension = version >= 2
                ? ResourceLocation.tryParse(root.getString("sourceDimension")) : null;
        BlockPos sourceOrigin = version >= 2 && root.contains("sourceOrigin")
                ? BlockPos.of(root.getLong("sourceOrigin")) : BlockPos.ZERO;
        List<EntityRecord> entities = new ArrayList<>();
        if (version >= 2) {
            ListTag entityList = root.getList("entities", Tag.TAG_COMPOUND);
            for (int index = 0; index < entityList.size(); index++) {
                CompoundTag entityData = entityList.getCompound(index).copy();
                if (!entityData.hasUUID("UUID")) throw new IOException("Factory package entity has no UUID");
                entities.add(new EntityRecord(entityData.getUUID("UUID"), entityData));
            }
        }
        List<TickRecord> ticks = new ArrayList<>();
        if (version >= 2) {
            ListTag tickList = root.getList("ticks", Tag.TAG_COMPOUND);
            for (int index = 0; index < tickList.size(); index++) {
                CompoundTag entry = tickList.getCompound(index);
                int[] pos = entry.getIntArray("pos");
                ResourceLocation type = ResourceLocation.tryParse(entry.getString("type"));
                if (pos.length != 3 || type == null || pos[0] < 0 || pos[0] >= size[0]
                        || pos[1] < 0 || pos[1] >= size[1] || pos[2] < 0 || pos[2] >= size[2]) {
                    throw new IOException("Invalid factory package scheduled tick");
                }
                ticks.add(new TickRecord(entry.getBoolean("fluid"), type,
                        new BlockPos(pos[0], pos[1], pos[2]), Math.max(0, entry.getInt("delay")),
                        entry.getInt("priority")));
            }
        }
        return new PackageData(version, expectedId, sourceDimension, sourceOrigin,
                new Vec3i(size[0], size[1], size[2]), List.copyOf(blocks),
                List.copyOf(entities), List.copyOf(ticks));
    }

    public record PackageData(int formatVersion, UUID id, ResourceLocation sourceDimension, BlockPos sourceOrigin,
                              Vec3i size, List<BlockRecord> blocks, List<EntityRecord> entities,
                              List<TickRecord> ticks) {}

    public record BlockRecord(BlockPos relativePos, BlockState state, CompoundTag blockEntityData) {}

    public record EntityRecord(UUID uuid, CompoundTag data) {}

    public record TickRecord(boolean fluid, ResourceLocation type, BlockPos relativePos, int delay, int priority) {}
}
