package com.jdte.common.blockentities;

import appeng.api.AECapabilities;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import com.jdte.common.acceleration.ExternalTimeAccelerationBackend;
import com.jdte.common.acceleration.ExternalTimeAccelerationBackends;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AE2TimeAccelerationTargetTest {

    @Test
    void gridTickableWithoutVanillaBlockEntityTickerIsAnAE2Target() throws Exception {
        BlockPos pos = new BlockPos(4, 5, 6);
        BlockState state = AEBlocks.MOLECULAR_ASSEMBLER.block().defaultBlockState();
        MolecularAssemblerBlockEntity blockEntity = new MolecularAssemblerBlockEntity(
                AEBlockEntities.MOLECULAR_ASSEMBLER.get(), pos, state);
        TargetLevel level = targetLevel(pos, state, blockEntity, gridNodeHost());

        assertNull(state.getTicker(level, blockEntity.getType()));

        Optional<ExtendedTimeAccelerationManager.TargetKey> target =
                ExtendedTimeAccelerationManager.resolveTargetKey(level, pos, true);

        assertEquals(ExtendedTimeAccelerationManager.TargetKind.AE2_GRID,
                target.orElseThrow().kind());
    }

    @Test
    void inactiveExternalAssemblerDoesNotUseTheLegacyAe2Fallback() throws Exception {
        BlockPos pos = new BlockPos(4, 5, 6);
        BlockState state = AEBlocks.MOLECULAR_ASSEMBLER.block().defaultBlockState();
        MolecularAssemblerBlockEntity blockEntity = new MolecularAssemblerBlockEntity(
                AEBlockEntities.MOLECULAR_ASSEMBLER.get(), pos, state);
        TargetLevel level = targetLevel(pos, state, blockEntity, gridNodeHost());
        InactiveExternalBackend backend = new InactiveExternalBackend();

        try (ExternalTimeAccelerationBackends.Registration ignored =
                     ExternalTimeAccelerationBackends.register(backend)) {
            var target = ExtendedTimeAccelerationManager.classifyMachineTarget(
                    new ExtendedTimeAccelerationManager.TargetLocation(level, pos),
                    true,
                    ExternalTimeAccelerationBackends.current().orElseThrow(),
                    (location, ae2FallbackEnabled) ->
                            ExtendedTimeAccelerationManager.resolveTargetKey(
                                    location.level(), location.pos(), ae2FallbackEnabled));

            assertTrue(target.isEmpty());
            assertEquals(1, backend.resolveCalls);
        }
    }

    private static TargetLevel targetLevel(BlockPos pos, BlockState state, BlockEntity blockEntity,
                                           IInWorldGridNodeHost nodeHost) throws Exception {
        TargetChunk chunk = allocate(TargetChunk.class);
        chunk.pos = pos;
        chunk.state = state;
        chunk.blockEntity = blockEntity;

        TargetChunkSource chunkSource = allocate(TargetChunkSource.class);
        chunkSource.chunk = chunk;

        TargetLevel level = allocate(TargetLevel.class);
        level.chunkSource = chunkSource;
        level.nodeHost = nodeHost;
        return level;
    }

    private static IInWorldGridNodeHost gridNodeHost() {
        IGridTickable tickable = new IGridTickable() {
            @Override
            public TickingRequest getTickingRequest(IGridNode node) {
                return new TickingRequest(1, 1, false);
            }

            @Override
            public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
                return TickRateModulation.URGENT;
            }
        };
        IGridNode node = (IGridNode) Proxy.newProxyInstance(
                IGridNode.class.getClassLoader(), new Class<?>[]{IGridNode.class},
                (proxy, method, args) -> method.getName().equals("getService")
                        && args != null && args.length == 1 && args[0] == IGridTickable.class
                        ? tickable : defaultValue(method.getReturnType()));
        return side -> node;
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == double.class) {
            return 0.0D;
        }
        return null;
    }

    private static <T> T allocate(Class<T> type) throws Exception {
        return type.cast(unsafe().allocateInstance(type));
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static final class TargetLevel extends ServerLevel {
        private TargetChunkSource chunkSource;
        private IInWorldGridNodeHost nodeHost;

        private TargetLevel() {
            super(null, null, null, null, null, null, null, false, 0, java.util.List.of(), false, null);
        }

        @Override
        public ServerChunkCache getChunkSource() {
            return chunkSource;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T, C> T getCapability(BlockCapability<T, C> capability, BlockPos pos, C context) {
            return capability == AECapabilities.IN_WORLD_GRID_NODE_HOST ? (T) nodeHost : null;
        }
    }

    private static final class TargetChunkSource extends ServerChunkCache {
        private LevelChunk chunk;

        private TargetChunkSource() {
            super(null, null, null, null, null, null, 0, 0, false, null, null, null);
        }

        @Override
        public LevelChunk getChunkNow(int chunkX, int chunkZ) {
            return chunk;
        }
    }

    private static final class TargetChunk extends LevelChunk {
        private BlockPos pos;
        private BlockState state;
        private BlockEntity blockEntity;

        private TargetChunk() {
            super(null, ChunkPos.ZERO);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return this.pos.equals(pos) ? state : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return this.pos.equals(pos) ? blockEntity : null;
        }
    }

    private static final class InactiveExternalBackend implements ExternalTimeAccelerationBackend {
        private int resolveCalls;

        @Override
        public void beginFrame(MinecraftServer server) {
        }

        @Override
        public TargetResolution resolve(ServerLevel level, BlockPos pos) {
            resolveCalls++;
            return TargetResolution.inactiveExternal();
        }

        @Override
        public void submit(TargetHandle target, Object contributor, int additionalCycles) {
        }

        @Override
        public void executeFrame(MinecraftServer server) {
        }

        @Override
        public void clearFrame(MinecraftServer server) {
        }
    }

}
