package com.jdte.common.containers;

import com.jdte.client.LargePortableContainerClientEvents;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import com.jdte.setup.JDTEItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LargePortableContainerFlowTest {
    @Test
    void handheldResolutionAlwaysUsesMainHandEvenWhenOffhandTriggered() {
        ItemStack mainHand = new ItemStack(JDTEItems.LARGE_POCKET_GENERATOR.get());
        ItemStack offHand = new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());

        Optional<ItemStack> resolved = LargePortableContainerMenus.resolveHandheldStack(
                OpenLargePortableContainerPayload.ContainerKind.LARGE_POCKET_GENERATOR,
                InteractionHand.OFF_HAND,
                mainHand,
                offHand
        );

        assertTrue(resolved.isPresent());
        assertSame(mainHand, resolved.orElseThrow());
    }

    @Test
    void handheldResolutionRejectsMatchingOffhandWhenMainHandDoesNotMatchRequestedKind() {
        ItemStack mainHand = new ItemStack(JDTEItems.LARGE_POCKET_GENERATOR.get());
        ItemStack offHand = new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());

        Optional<ItemStack> resolved = LargePortableContainerMenus.resolveHandheldStack(
                OpenLargePortableContainerPayload.ContainerKind.LARGE_FUEL_CANISTER,
                InteractionHand.OFF_HAND,
                mainHand,
                offHand
        );

        assertTrue(resolved.isEmpty());
    }

    @Test
    void curiosResolutionRereadsTheExactRequestedSlotForTheContainerKind() {
        ItemStack largePotion = new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());
        ItemStack wrongLargeFuel = new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());

        Optional<ItemStack> resolved = LargePortableContainerMenus.resolveCuriosStack(
                OpenLargePortableContainerPayload.ContainerKind.LARGE_POTION_CANISTER,
                true,
                slotId -> switch (slotId) {
                    case LargePortableContainerMenus.LARGE_POTION_CANISTER_SLOT -> Optional.of(largePotion);
                    case LargePortableContainerMenus.LARGE_FUEL_CANISTER_SLOT -> Optional.of(wrongLargeFuel);
                    default -> Optional.empty();
                }
        );

        assertTrue(resolved.isPresent());
        assertSame(largePotion, resolved.orElseThrow());
    }

    @Test
    void curiosResolutionIsSafeWhenCuriosIsUnavailable() {
        AtomicBoolean queried = new AtomicBoolean(false);

        Optional<ItemStack> resolved = LargePortableContainerMenus.resolveCuriosStack(
                OpenLargePortableContainerPayload.ContainerKind.LARGE_FUEL_CANISTER,
                false,
                slotId -> {
                    queried.set(true);
                    return Optional.empty();
                }
        );

        assertTrue(resolved.isEmpty());
        assertFalse(queried.get());
    }

    @Test
    void clientSourceResolverPrefersTheLiveMainHandStackOverTheDecodedCopy() {
        ItemStack liveMainHand = new ItemStack(JDTEItems.LARGE_POCKET_GENERATOR.get());

        Optional<ItemStack> resolved = LargePortableContainerMenus.resolveClientSourceStack(
                LargePortableContainerSource.mainHand(),
                liveMainHand,
                false,
                slotId -> Optional.empty()
        );

        assertTrue(resolved.isPresent());
        assertSame(liveMainHand, resolved.orElseThrow());
    }

    @Test
    void clientSourceResolverUsesTheExactLiveCuriosSlotWhenPresent() {
        ItemStack liveCurios = new ItemStack(JDTEItems.LARGE_POCKET_GENERATOR.get());

        Optional<ItemStack> resolved = LargePortableContainerMenus.resolveClientSourceStack(
                LargePortableContainerSource.curios(LargePortableContainerMenus.LARGE_POCKET_GENERATOR_SLOT),
                ItemStack.EMPTY,
                true,
                slotId -> LargePortableContainerMenus.LARGE_POCKET_GENERATOR_SLOT.equals(slotId)
                        ? Optional.of(liveCurios)
                        : Optional.empty()
        );

        assertTrue(resolved.isPresent());
        assertSame(liveCurios, resolved.orElseThrow());
    }

    @Test
    void clientHotkeyCollectorEmitsOnlyThePressedKindsWhenPlayerCanOpen() {
        List<OpenLargePortableContainerPayload> payloads = LargePortableContainerClientEvents.collectOpenPayloads(
                true,
                false,
                () -> true,
                () -> false,
                () -> true
        );

        assertEquals(List.of(
                new OpenLargePortableContainerPayload(OpenLargePortableContainerPayload.ContainerKind.LARGE_POCKET_GENERATOR),
                new OpenLargePortableContainerPayload(OpenLargePortableContainerPayload.ContainerKind.LARGE_FUEL_CANISTER)
        ), payloads);
    }

    @Test
    void clientHotkeyCollectorSuppressesRequestsWithoutPlayerOrWhenAnotherScreenIsOpen() {
        assertTrue(LargePortableContainerClientEvents.collectOpenPayloads(
                false, false, () -> true, () -> true, () -> true).isEmpty());
        assertTrue(LargePortableContainerClientEvents.collectOpenPayloads(
                true, true, () -> true, () -> true, () -> true).isEmpty());
    }
}
