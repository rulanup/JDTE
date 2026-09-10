package com.jdte.ae;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.me.Grid;
import appeng.me.GridNode;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.jdte.common.acceleration.ExternalTimeAccelerationBackend.TargetState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeGridResolverTest {
    private static final BlockPos FIRST_POS = new BlockPos(1, 2, 3);
    private static final BlockPos SECOND_POS = new BlockPos(4, 5, 6);

    @Test
    void plainPositionIsNotExternal() throws Exception {
        AeGridResolver resolver = new AeGridResolver((level, pos) -> null);
        resolver.beginFrame();

        assertEquals(TargetState.NOT_EXTERNAL,
                resolver.resolve(AeGridTestFixture.level("overworld"), FIRST_POS).state());
    }

    @Test
    void recognizedHostWithoutAnOnlineBootedConcreteGridIsInactive() throws Exception {
        Grid grid = AeGridTestFixture.grid(10);
        IGridNode offline = AeGridTestFixture.node(grid,
                AeGridTestFixture.level("overworld"), false, true);
        IGridNode booting = AeGridTestFixture.node(grid,
                AeGridTestFixture.level("overworld"), true, false);
        Map<BlockPos, IInWorldGridNodeHost> hosts = Map.of(
                FIRST_POS, side -> offline,
                SECOND_POS, side -> booting);
        AeGridResolver resolver = new AeGridResolver((level, pos) -> hosts.get(pos));
        resolver.beginFrame();
        ServerLevel level = AeGridTestFixture.level("overworld");

        assertEquals(TargetState.INACTIVE_EXTERNAL, resolver.resolve(level, FIRST_POS).state());
        assertEquals(TargetState.INACTIVE_EXTERNAL, resolver.resolve(level, SECOND_POS).state());
    }

    @Test
    void positionsOnTheSameGridShareTheCanonicalHandle() throws Exception {
        Grid grid = AeGridTestFixture.grid(11);
        ServerLevel level = AeGridTestFixture.level("overworld");
        IGridNode firstNode = AeGridTestFixture.node(grid, level, true, true);
        IGridNode secondNode = AeGridTestFixture.node(grid, level, true, true);
        Map<BlockPos, IInWorldGridNodeHost> hosts = Map.of(
                FIRST_POS, side -> firstNode,
                SECOND_POS, side -> secondNode);
        AeGridResolver resolver = new AeGridResolver((ignoredLevel, pos) -> hosts.get(pos));
        resolver.beginFrame();

        var first = resolver.resolve(level, FIRST_POS);
        var second = resolver.resolve(level, SECOND_POS);

        assertEquals(TargetState.ACTIVE_EXTERNAL, first.state());
        assertEquals(TargetState.ACTIVE_EXTERNAL, second.state());
        assertSame(first.handle(), second.handle());
    }

    @Test
    void oneNodeExposedOnEverySideCreatesOneAnchor() throws Exception {
        Grid grid = AeGridTestFixture.grid(12);
        ServerLevel level = AeGridTestFixture.level("overworld");
        IGridNode node = AeGridTestFixture.node(grid, level, true, true);
        AeGridResolver resolver = new AeGridResolver((ignoredLevel, pos) -> side -> node);
        resolver.beginFrame();

        AeGridHandle handle = (AeGridHandle) resolver.resolve(level, FIRST_POS).handle();

        assertEquals(1, handle.anchors().size());
    }

    @Test
    void loadedLevelsAreIdentityDistinctAndDimensionSorted() throws Exception {
        Grid grid = AeGridTestFixture.grid(13);
        ServerLevel overworld = AeGridTestFixture.level("overworld");
        ServerLevel nether = AeGridTestFixture.level("the_nether");
        ServerLevel offline = AeGridTestFixture.level("offline");
        IGridNode overworldOne = AeGridTestFixture.node(grid, overworld, true, true);
        IGridNode overworldTwo = AeGridTestFixture.node(grid, overworld, true, true);
        IGridNode netherNode = AeGridTestFixture.node(grid, nether, true, true);
        IGridNode offlineNode = AeGridTestFixture.node(grid, offline, false, true);
        AeGridTestFixture.setNodes(grid, overworldOne, overworldTwo, netherNode, offlineNode);
        AeGridHandle handle = new AeGridHandle(grid);

        assertEquals(List.of(overworld, nether), handle.loadedLevels());
    }

    @Test
    void validityRequiresAnOnlineBootedAnchorStillOnTheSameGrid() throws Exception {
        Grid grid = AeGridTestFixture.grid(14);
        ServerLevel level = AeGridTestFixture.level("overworld");
        AtomicBoolean online = new AtomicBoolean(true);
        IGridNode node = AeGridTestFixture.node(grid, level, online, new AtomicBoolean(true));
        AeGridHandle handle = new AeGridHandle(grid);
        handle.addAnchor(node, level, FIRST_POS);

        assertTrue(handle.isValid());
        online.set(false);
        assertFalse(handle.isValid());
    }
}

final class AeGridTestFixture {
    private static final Unsafe UNSAFE = unsafe();

    private AeGridTestFixture() {
    }

    static Grid grid(int serialNumber) throws Exception {
        Grid grid = allocate(Grid.class);
        setObject(grid, "machines", HashMultimap.<Class<?>, IGridNode>create());
        setObject(grid, "pivot", allocate(GridNode.class));
        setInt(grid, "serialNumber", serialNumber);
        return grid;
    }

    static void setNodes(Grid grid, IGridNode... nodes) throws Exception {
        SetMultimap<Class<?>, IGridNode> machines = HashMultimap.create();
        Arrays.stream(nodes).forEach(node -> machines.put(Object.class, node));
        setObject(grid, "machines", machines);
    }

    static IGridNode node(Grid grid, ServerLevel level, boolean online, boolean booted) {
        return node(grid, level, new AtomicBoolean(online), new AtomicBoolean(booted));
    }

    static IGridNode node(Grid grid, ServerLevel level,
                          AtomicBoolean online, AtomicBoolean booted) {
        return (IGridNode) Proxy.newProxyInstance(
                IGridNode.class.getClassLoader(), new Class<?>[]{IGridNode.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isOnline" -> online.get();
                    case "hasGridBooted" -> booted.get();
                    case "getGrid" -> grid;
                    case "getLevel" -> level;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "TestGridNode@" + Integer.toHexString(
                            System.identityHashCode(proxy));
                    default -> defaultValue(method.getReturnType());
                });
    }

    static IInWorldGridNodeHost host(Map<Direction, IGridNode> nodes) {
        EnumMap<Direction, IGridNode> copy = new EnumMap<>(Direction.class);
        copy.putAll(nodes);
        return copy::get;
    }

    static ServerLevel level(String path) throws Exception {
        NamedServerLevel level = allocate(NamedServerLevel.class);
        level.dimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath("jdte_ae_test", path));
        return level;
    }

    static MinecraftServer server() throws Exception {
        return allocate(DedicatedServer.class);
    }

    private static void setObject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        UNSAFE.putObject(target, UNSAFE.objectFieldOffset(field), value);
    }

    private static void setInt(Object target, String fieldName, int value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        UNSAFE.putInt(target, UNSAFE.objectFieldOffset(field), value);
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == double.class) {
            return 0.0D;
        }
        return null;
    }

    private static <T> T allocate(Class<T> type) throws InstantiationException {
        return type.cast(UNSAFE.allocateInstance(type));
    }

    private static Unsafe unsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static final class NamedServerLevel extends ServerLevel {
        private ResourceKey<Level> dimension;

        private NamedServerLevel() {
            super(null, null, null, null, null, null, null,
                    false, 0L, List.of(), false, null);
        }

        @Override
        public ResourceKey<Level> dimension() {
            return dimension;
        }
    }
}
