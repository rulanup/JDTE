package com.jdte.common.recipes;

import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineralExtractorResourcesRecipeTest {
    private static final net.minecraft.world.item.crafting.RecipeSerializer<MineralExtractorResourcesRecipe> SERIALIZER =
            MineralExtractorResourcesRecipe.Serializer.create();

    @Test
    void decodesRegisteredModdedSourceFluids() {
        Identifier fortune = BuiltInRegistries.FLUID.getKey(JDTRegistration.XP_FLUID_SOURCE.get());
        Identifier acceleration = BuiltInRegistries.FLUID.getKey(JDTRegistration.TIME_FLUID_SOURCE.get());

        MineralExtractorResourcesRecipe recipe = decode(fortune, acceleration).getOrThrow();

        assertEquals(fortune, recipe.fortuneFluid());
        assertEquals(acceleration, recipe.accelerationFluid());
    }

    @Test
    void rejectsUnknownFluidDuringMapDecode() {
        assertDecodeError(Identifier.fromNamespaceAndPath("jdte", "missing_test_fluid"),
                Identifier.withDefaultNamespace("water"), "Unknown");
    }

    @Test
    void rejectsEmptyFluidDuringMapDecode() {
        assertDecodeError(Identifier.withDefaultNamespace("empty"),
                Identifier.withDefaultNamespace("water"), "cannot be empty");
    }

    @Test
    void rejectsFlowingFluidDuringMapDecode() {
        assertDecodeError(Identifier.withDefaultNamespace("flowing_water"),
                Identifier.withDefaultNamespace("lava"), "source fluid");
    }

    @Test
    void networkRoundTripPreservesValidSourceFluids() {
        MineralExtractorResourcesRecipe original = new MineralExtractorResourcesRecipe(
                Identifier.withDefaultNamespace("lava"),
                Identifier.withDefaultNamespace("water"));
        RegistryFriendlyByteBuf buffer = buffer();
        try {
            SERIALIZER.streamCodec().encode(buffer, original);

            MineralExtractorResourcesRecipe decoded = SERIALIZER.streamCodec().decode(buffer);

            assertEquals(original, decoded);
        } finally {
            buffer.release();
        }
    }

    @Test
    void networkDecodeRejectsFluidThatMapDecodeRejects() {
        RegistryFriendlyByteBuf buffer = buffer();
        try {
            Identifier.STREAM_CODEC.encode(buffer, Identifier.withDefaultNamespace("flowing_water"));
            Identifier.STREAM_CODEC.encode(buffer, Identifier.withDefaultNamespace("water"));

            assertThrows(IllegalStateException.class, () -> SERIALIZER.streamCodec().decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void rejectsMatchingFortuneAndAccelerationFluids() {
        DataResult<MineralExtractorResourcesRecipe> result = decode(
                Identifier.withDefaultNamespace("water"),
                Identifier.withDefaultNamespace("water"));

        assertTrue(result.error().isPresent());
        assertTrue(result.error().orElseThrow().message().contains("must differ"));
    }

    private static void assertDecodeError(Identifier fortune, Identifier acceleration,
                                          String expectedMessage) {
        DataResult<MineralExtractorResourcesRecipe> result = decode(fortune, acceleration);

        assertTrue(result.error().isPresent());
        assertTrue(result.error().orElseThrow().message().contains(expectedMessage),
                () -> "Expected error containing '" + expectedMessage + "' but got "
                        + result.error().orElseThrow().message());
    }

    private static DataResult<MineralExtractorResourcesRecipe> decode(Identifier fortune,
                                                                       Identifier acceleration) {
        return SERIALIZER.codec().codec().parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {
                  "fortune_fluid": "%s",
                  "acceleration_fluid": "%s"
                }
                """.formatted(fortune, acceleration)));
    }

    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(),
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }
}
