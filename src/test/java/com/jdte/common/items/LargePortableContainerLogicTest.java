package com.jdte.common.items;

import com.jdte.setup.JDTEItems;
import com.jdte.common.containers.LargePortableContainerBinding;
import com.jdte.common.containers.handlers.LargeFuelCanisterHandler;
import com.jdte.common.containers.handlers.LargePotionCanisterHandler;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LargePortableContainerLogicTest {
    @Test
    void scalesPocketGeneratorCapacityByFourAtTheLastSafeValue() {
        int basePocketCapacity = 536_870_911;

        assertEquals(2_147_483_644,
                LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity));
    }

    @Test
    void saturatesPocketGeneratorCapacityWhenMultiplicationWouldOverflow() {
        int basePocketCapacity = 536_870_912;

        assertEquals(Integer.MAX_VALUE,
                LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity));
    }

    @Test
    void exposesTheConfiguredPotionBatchCapacityAndBatchSize() {
        assertEquals(4_000, LargePortableContainerLogic.potionCapacity());
        assertEquals(1_000, LargePortableContainerLogic.POTION_BATCH_MB);
    }

    @Test
    void onlyFourPotionsCanFillAOneThousandMillibucketBatchWithinCapacity() {
        assertTrue(LargePortableContainerLogic.canFillPotionBatch(4, 3_000, 4_000));
        assertFalse(LargePortableContainerLogic.canFillPotionBatch(4, 3_001, 4_000));
        assertFalse(LargePortableContainerLogic.canFillPotionBatch(3, 3_000, 4_000));
    }

    @Test
    void scalesFuelCapacityByFourAtTheLastSafeValue() {
        int baseFuelCapacity = 536_870_911;

        assertEquals(2_147_483_644, LargePortableContainerLogic.fuelCapacity(baseFuelCapacity));
    }

    @Test
    void saturatesFuelCapacityWhenMultiplicationWouldOverflow() {
        int baseFuelCapacity = 536_870_912;

        assertEquals(Integer.MAX_VALUE, LargePortableContainerLogic.fuelCapacity(baseFuelCapacity));
    }

    @Test
    void scalesFuelMinimumConsumptionByTenAtTheLastSafeValue() {
        int baseMinimum = 214_748_364;

        assertEquals(2_147_483_640, LargePortableContainerLogic.fuelMinimumConsumption(baseMinimum));
    }

    @Test
    void saturatesFuelMinimumConsumptionWhenMultiplicationWouldOverflow() {
        int baseMinimum = 214_748_365;

        assertEquals(Integer.MAX_VALUE, LargePortableContainerLogic.fuelMinimumConsumption(baseMinimum));
    }

    @Test
    void scalesFuelBurnMultiplierByTenAtTheLastSafeValue() {
        int baseBurnMultiplier = 214_748_364;

        assertEquals(2_147_483_640, LargePortableContainerLogic.fuelBurnMultiplier(baseBurnMultiplier));
    }

    @Test
    void saturatesFuelBurnMultiplierWhenMultiplicationWouldOverflow() {
        int baseBurnMultiplier = 214_748_365;

        assertEquals(Integer.MAX_VALUE, LargePortableContainerLogic.fuelBurnMultiplier(baseBurnMultiplier));
    }

    @Test
    void registersLargePortableContainerItemHolders() {
        assertEquals("large_pocket_generator", JDTEItems.LARGE_POCKET_GENERATOR.getId().getPath());
        assertEquals("large_potion_canister", JDTEItems.LARGE_POTION_CANISTER.getId().getPath());
        assertEquals("large_fuel_canister", JDTEItems.LARGE_FUEL_CANISTER.getId().getPath());
    }

    @Test
    void exposesLargePortableContainerItemTypesAndSingleStackDefaults() {
        LargePocketGeneratorItem largePocketGenerator = JDTEItems.LARGE_POCKET_GENERATOR.get();
        LargePotionCanisterItem largePotionCanister = JDTEItems.LARGE_POTION_CANISTER.get();
        LargeFuelCanisterItem largeFuelCanister = JDTEItems.LARGE_FUEL_CANISTER.get();

        assertNotNull(largePocketGenerator);
        assertNotNull(largePotionCanister);
        assertNotNull(largeFuelCanister);
        assertEquals(1, new ItemStack(largePocketGenerator).getMaxStackSize());
        assertEquals(1, new ItemStack(largePotionCanister).getMaxStackSize());
        assertEquals(1, new ItemStack(largeFuelCanister).getMaxStackSize());
    }

    @Test
    void largePocketGeneratorUsesFourTimesTheConfiguredEnergyCapacity() {
        int basePocketCapacity = 536_870_911;

        assertEquals(
                LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity),
                LargePocketGeneratorItem.getScaledMaxEnergy(basePocketCapacity));
    }

    @Test
    void largePotionCanisterExposesFourThousandMillibucketCapacityEntry() {
        assertEquals(4_000, LargePotionCanisterItem.getPotionCapacityMb());
        assertEquals(4_000, JDTEItems.LARGE_POTION_CANISTER.get().getCapacityMb());
    }

    @Test
    void largePotionCanisterFillsOneBatchFromFourMatchingPotions() {
        ItemStack canister = new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());
        ItemStack potionInput = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        potionInput.setCount(4);

        assertTrue(LargePotionCanisterItem.tryFillBatch(canister, potionInput));
        assertEquals(0, potionInput.getCount());
        assertEquals(1_000, LargePotionCanisterItem.getPotionAmount(canister));
        assertEquals(PotionContents.createItemStack(Items.POTION, Potions.WATER)
                .getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY), LargePotionCanisterItem.getPotionContents(canister));
    }

    @Test
    void largePotionCanisterRejectsMixedPotionBatchWithoutChangingData() {
        ItemStack canister = new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());
        ItemStack potionInput = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
        potionInput.setCount(4);
        PotionContents originalContents = PotionContents.createItemStack(Items.POTION, Potions.WATER)
                .getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        canister.set(JustDireDataComponents.POTION_CONTENTS, originalContents);
        canister.set(JustDireDataComponents.POTION_AMOUNT, 1_000);

        assertFalse(LargePotionCanisterItem.tryFillBatch(canister, potionInput));
        assertEquals(4, potionInput.getCount());
        assertEquals(1_000, LargePotionCanisterItem.getPotionAmount(canister));
        assertEquals(originalContents, LargePotionCanisterItem.getPotionContents(canister));
    }

    @Test
    void largePotionCanisterRejectsShortOrOverflowingBatchWithoutChangingData() {
        ItemStack shortCanister = new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());
        ItemStack shortInput = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        shortInput.setCount(3);

        assertFalse(LargePotionCanisterItem.tryFillBatch(shortCanister, shortInput));
        assertEquals(3, shortInput.getCount());
        assertEquals(0, LargePotionCanisterItem.getPotionAmount(shortCanister));

        ItemStack fullCanister = new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());
        ItemStack fullInput = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        fullInput.setCount(4);
        PotionContents waterContents = fullInput.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        fullCanister.set(JustDireDataComponents.POTION_CONTENTS, waterContents);
        fullCanister.set(JustDireDataComponents.POTION_AMOUNT, 3_500);

        assertFalse(LargePotionCanisterItem.tryFillBatch(fullCanister, fullInput));
        assertEquals(4, fullInput.getCount());
        assertEquals(3_500, LargePotionCanisterItem.getPotionAmount(fullCanister));
        assertEquals(waterContents, LargePotionCanisterItem.getPotionContents(fullCanister));
    }

    @Test
    void largeFuelCanisterExposesScaledCapacityAndConsumptionEntries() {
        assertEquals(2_147_483_644, LargeFuelCanisterItem.getMaxFuelLevel(536_870_911));
        assertEquals(Integer.MAX_VALUE, LargeFuelCanisterItem.getMaxFuelLevel(536_870_912));
        assertEquals(2_147_483_640, LargeFuelCanisterItem.getMinimumFuelConsumed(214_748_364));
        assertEquals(Integer.MAX_VALUE, LargeFuelCanisterItem.getMinimumFuelConsumed(214_748_365));
    }

    @Test
    void largeFuelCanisterReturnsTenTimesTheBaseBurnSpeedMultiplier() {
        ItemStack stack = new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());
        FuelCanister.setBurnSpeed(stack, 2.0D);

        assertEquals(20, LargeFuelCanisterItem.getBurnSpeedMultiplier(stack));
    }

    @Test
    void largePotionMenuHandlerFillsExactlyOneBatchAndReturnsFourGlassBottles() {
        ItemStack canister = new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());
        LargePotionCanisterHandler handler =
                new LargePotionCanisterHandler(canister, JustDireDataComponents.TOOL_CONTENTS.get(), 1);
        ItemStack input = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        input.setCount(4);

        handler.setStackInSlot(0, input);

        assertEquals(1_000, LargePotionCanisterItem.getPotionAmount(canister));
        assertEquals(Items.GLASS_BOTTLE, handler.getStackInSlot(0).getItem());
        assertEquals(4, handler.getStackInSlot(0).getCount());
    }

    @Test
    void largePotionMenuHandlerLeavesInputUntouchedWhenBatchCannotFill() {
        ItemStack canister = new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());
        LargePotionCanisterHandler handler =
                new LargePotionCanisterHandler(canister, JustDireDataComponents.TOOL_CONTENTS.get(), 1);
        ItemStack shortInput = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        shortInput.setCount(3);

        handler.setStackInSlot(0, shortInput);

        assertEquals(0, LargePotionCanisterItem.getPotionAmount(canister));
        assertEquals(Items.POTION, handler.getStackInSlot(0).getItem());
        assertEquals(3, handler.getStackInSlot(0).getCount());
    }

    @Test
    void largeFuelMenuHandlerRejectsFuelCanistersAsInput() {
        ItemStack canister = new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());
        LargeFuelCanisterHandler handler = new LargeFuelCanisterHandler(1, canister);

        assertFalse(handler.isItemValid(0, new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get())));
    }

    @Test
    void largePortableContainerBindingRequiresTheSameResolvedItemStackInstance() {
        ItemStack trackedStack = new ItemStack(JDTEItems.LARGE_POCKET_GENERATOR.get());
        ItemStack[] liveSlot = {trackedStack};
        Predicate<ItemStack> validator = stack -> stack.getItem() instanceof LargePocketGeneratorItem;
        LargePortableContainerBinding binding =
                new LargePortableContainerBinding(trackedStack, () -> liveSlot[0], validator);

        assertTrue(binding.isStillValid());

        liveSlot[0] = trackedStack.copy();

        assertFalse(binding.isStillValid());
    }

    @Test
    void largePortableContainerPayloadRoundTripsEachAllowedContainerKind() {
        for (OpenLargePortableContainerPayload.ContainerKind kind : OpenLargePortableContainerPayload.ContainerKind.values()) {
            RegistryFriendlyByteBuf buffer = buffer();
            try {
                OpenLargePortableContainerPayload.STREAM_CODEC.encode(buffer, new OpenLargePortableContainerPayload(kind));

                OpenLargePortableContainerPayload decoded = OpenLargePortableContainerPayload.STREAM_CODEC.decode(buffer);

                assertEquals(kind, decoded.containerKind());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void largePortableContainerPayloadRejectsUnknownContainerKind() {
        RegistryFriendlyByteBuf buffer = buffer();
        try {
            buffer.writeInt(99);

            assertThrows(IllegalArgumentException.class,
                    () -> OpenLargePortableContainerPayload.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(),
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }
}
