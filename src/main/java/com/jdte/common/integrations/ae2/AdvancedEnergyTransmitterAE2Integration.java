package com.jdte.common.integrations.ae2;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.util.AECableType;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

final class AdvancedEnergyTransmitterAE2Integration
        implements AdvancedEnergyTransmitterEnergySource, IInWorldGridNodeHost, IActionHost {
    private static final String NODE_TAG = "jdteAppliedFluxNode";
    private static final AEKey FE_KEY = FluxKey.of(EnergyType.FE);
    private static final IGridNodeListener<AdvancedEnergyTransmitterAE2Integration> NODE_LISTENER =
            new IGridNodeListener<>() {
                @Override
                public void onSaveChanges(AdvancedEnergyTransmitterAE2Integration source, IGridNode node) {
                    source.owner.setChanged();
                }

                @Override
                public void onGridChanged(AdvancedEnergyTransmitterAE2Integration source, IGridNode node) {
                    source.clearStorageCache();
                }

                @Override
                public void onStateChanged(AdvancedEnergyTransmitterAE2Integration source, IGridNode node,
                                           State state) {
                    if (!node.isActive()) {
                        source.clearStorageCache();
                    }
                }
            };

    private final AdvancedEnergyTransmitterBE owner;
    private final IActionSource actionSource = IActionSource.ofMachine(this);
    private CompoundTag savedNodeData = new CompoundTag();
    @Nullable
    private IManagedGridNode managedNode;
    @Nullable
    private IGrid cachedGrid;
    @Nullable
    private IStorageService cachedStorage;

    AdvancedEnergyTransmitterAE2Integration(AdvancedEnergyTransmitterBE owner) {
        this.owner = owner;
        this.managedNode = createManagedNode();
    }

    static void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                (level, pos, state, blockEntity, context) -> {
                    if (!(blockEntity instanceof AdvancedEnergyTransmitterBE transmitter)) {
                        return null;
                    }
                    Object host = transmitter.getEnergyNetworkSource().getGridNodeHost();
                    return host instanceof IInWorldGridNodeHost gridHost ? gridHost : null;
                },
                JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get());
    }

    @Override
    public void ensureReady(ServerLevel level, BlockPos pos) {
        if (managedNode == null) {
            managedNode = createManagedNode();
            if (!savedNodeData.isEmpty()) {
                managedNode.loadFromNBT(savedNodeData);
            }
        }
        if (!managedNode.isReady()) {
            managedNode.create(level, pos);
        }
    }

    @Override
    public long extract(long maxFe) {
        IStorageService storage = getActiveStorage();
        if (storage == null || maxFe <= 0L) {
            return 0L;
        }
        return storage.getInventory().extract(
                FE_KEY, maxFe, Actionable.MODULATE, actionSource);
    }

    @Override
    public long insert(long maxFe) {
        IStorageService storage = getActiveStorage();
        if (storage == null || maxFe <= 0L) {
            return 0L;
        }
        return storage.getInventory().insert(
                FE_KEY, maxFe, Actionable.MODULATE, actionSource);
    }

    @Override
    public Status getStatus() {
        return getActiveNode() == null ? Status.OFFLINE : Status.ONLINE;
    }

    @Override
    public void save(CompoundTag tag) {
        if (managedNode != null) {
            managedNode.saveToNBT(savedNodeData);
        }
        if (!savedNodeData.isEmpty()) {
            tag.put(NODE_TAG, savedNodeData.copy());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        savedNodeData = tag.contains(NODE_TAG) ? tag.getCompound(NODE_TAG).copy() : new CompoundTag();
        if (managedNode != null && !managedNode.isReady() && !savedNodeData.isEmpty()) {
            managedNode.loadFromNBT(savedNodeData);
        }
    }

    @Override
    public void destroy() {
        clearStorageCache();
        if (managedNode == null) {
            return;
        }
        if (managedNode.isReady()) {
            managedNode.saveToNBT(savedNodeData);
        }
        managedNode.destroy();
        managedNode = null;
    }

    @Override
    public Object getGridNodeHost() {
        return this;
    }

    @Nullable
    @Override
    public IGridNode getGridNode(Direction direction) {
        return managedNode == null ? null : managedNode.getNode();
    }

    @Override
    public AECableType getCableConnectionType(Direction direction) {
        return AECableType.SMART;
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return getGridNode(null);
    }

    private IManagedGridNode createManagedNode() {
        return GridHelper.createManagedNode(this, NODE_LISTENER)
                .setInWorldNode(true)
                .setExposedOnSides(EnumSet.allOf(Direction.class))
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(1.0)
                .setVisualRepresentation(JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get())
                .setTagName("advanced_energy_transmitter");
    }

    @Nullable
    private IGridNode getActiveNode() {
        IGridNode node = managedNode == null ? null : managedNode.getNode();
        return node != null && node.isActive() ? node : null;
    }

    @Nullable
    private IStorageService getActiveStorage() {
        IGridNode node = getActiveNode();
        if (node == null) {
            clearStorageCache();
            return null;
        }
        IGrid grid = node.getGrid();
        if (grid != cachedGrid || cachedStorage == null) {
            cachedGrid = grid;
            cachedStorage = grid.getStorageService();
        }
        return cachedStorage;
    }

    private void clearStorageCache() {
        cachedGrid = null;
        cachedStorage = null;
    }
}