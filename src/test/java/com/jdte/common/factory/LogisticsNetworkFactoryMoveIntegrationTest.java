package com.jdte.common.factory;

import me.almana.logisticsnetworks.data.NetworkRegistry;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LogisticsNetworkFactoryMoveIntegrationTest {
    @Test
    void preservesNetworkWhileItsOnlyNodeIsPacked() {
        var registry = new NetworkRegistry();
        UUID owner = UUID.randomUUID();
        UUID node = UUID.randomUUID();
        var network = registry.createNetwork("Factory", owner);
        UUID id = network.getId();
        registry.addNodeToNetwork(id, node);
        LogisticsNetworkFactoryMoveIntegration.detachForMove(registry, id, node);
        assertSame(network, registry.getNetwork(id));
        assertTrue(network.getNodeUuids().isEmpty());
        assertTrue(network.isCacheDirty());
        registry.addNodeToNetwork(id, node);
        assertEquals(Set.of(node), network.getNodeUuids());
        assertEquals("Factory", network.getName());
        assertEquals(owner, network.getOwnerUuid());
    }
}
