package com.jdte.ae;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;
import appeng.hooks.ticking.TickHandler;
import appeng.me.service.helpers.NetworkCraftingProviders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickHandlerMixinIntegrationTest {
    @AfterEach
    void resetClock() {
        AEVirtualTickContext.reset();
    }

    @Test
    void transformedTickHandlerUsesScopedMonotonicValuesWithoutLeakingTheScope() {
        TickHandler handler = TickHandler.instance();
        long nativeTick = handler.getCurrentTick();

        long virtualTick;
        try (AEVirtualTickContext.Scope ignored = AEVirtualTickContext.enter(nativeTick, 7)) {
            virtualTick = handler.getCurrentTick();
            assertTrue(virtualTick > nativeTick);
        }

        assertFalse(AEVirtualTickContext.isActive());
        assertEquals(virtualTick, handler.getCurrentTick());
    }

    @Test
    void realAeCraftingProviderChangesGetUniqueAdjacentFrameStamps() {
        TickHandler handler = TickHandler.instance();
        long firstFrame = handler.getCurrentTick();
        NetworkCraftingProviders providers = new NetworkCraftingProviders();
        ICraftingProvider provider = new EmptyCraftingProvider();

        long mountedAt;
        try (AEVirtualTickContext.Scope ignored =
                     AEVirtualTickContext.enter(firstFrame, 2)) {
            providers.addProvider(provider);
            mountedAt = providers.getLastModifiedOnTick();
        }

        long removedAt;
        try (AEVirtualTickContext.Scope ignored =
                     AEVirtualTickContext.enter(firstFrame + 1L, 1)) {
            providers.removeProvider(provider);
            removedAt = providers.getLastModifiedOnTick();
        }

        assertTrue(removedAt > mountedAt);
    }

    private static final class EmptyCraftingProvider implements ICraftingProvider {
        @Override
        public List<IPatternDetails> getAvailablePatterns() {
            return List.of();
        }

        @Override
        public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
            return false;
        }

        @Override
        public boolean isBusy() {
            return false;
        }
    }
}
