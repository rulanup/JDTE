package com.jdte.common.recipes;

import com.direwolf20.justdirethings.setup.Registration;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineralExtractorResourcesRecipeTest {
    private static final MineralExtractorResourcesRecipe.Serializer SERIALIZER =
            new MineralExtractorResourcesRecipe.Serializer();

    @Test
    void decodesRegisteredModdedSourceFluids() {
        ResourceLocation fortune = BuiltInRegistries.FLUID.getKey(Registration.XP_FLUID_SOURCE.get());
        ResourceLocation acceleration = BuiltInRegistries.FLUID.getKey(Registration.TIME_FLUID_SOURCE.get());

        MineralExtractorResourcesRecipe recipe = decode(fortune, acceleration).getOrThrow();

        assertEquals(fortune, recipe.fortuneFluid());
        assertEquals(acceleration, recipe.accelerationFluid());
    }

    @Test
    void rejectsUnknownFluidDuringMapDecode() {
        assertDecodeError(ResourceLocation.fromNamespaceAndPath("jdte", "missing_test_fluid"),
                ResourceLocation.withDefaultNamespace("water"), "Unknown");
    }

    @Test
    void rejectsEmptyFluidDuringMapDecode() {
        assertDecodeError(ResourceLocation.withDefaultNamespace("empty"),
                ResourceLocation.withDefaultNamespace("water"), "cannot be empty");
    }

    @Test
    void rejectsFlowingFluidDuringMapDecode() {
        assertDecodeError(ResourceLocation.withDefaultNamespace("flowing_water"),
                ResourceLocation.withDefaultNamespace("lava"), "source fluid");
    }

    @Test
    void networkRoundTripPreservesValidSourceFluids() {
        MineralExtractorResourcesRecipe original = new MineralExtractorResourcesRecipe(
                ResourceLocation.withDefaultNamespace("lava"),
                ResourceLocation.withDefaultNamespace("water"));
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
            ResourceLocation.STREAM_CODEC.encode(buffer, ResourceLocation.withDefaultNamespace("flowing_water"));
            ResourceLocation.STREAM_CODEC.encode(buffer, ResourceLocation.withDefaultNamespace("water"));

            assertThrows(IllegalStateException.class, () -> SERIALIZER.streamCodec().decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void rejectsMatchingFortuneAndAccelerationFluids() {
        DataResult<MineralExtractorResourcesRecipe> result = decode(
                ResourceLocation.withDefaultNamespace("water"),
                ResourceLocation.withDefaultNamespace("water"));

        assertTrue(result.error().isPresent());
        assertTrue(result.error().orElseThrow().message().contains("must differ"));
    }

    private static void assertDecodeError(ResourceLocation fortune, ResourceLocation acceleration,
                                          String expectedMessage) {
        DataResult<MineralExtractorResourcesRecipe> result = decode(fortune, acceleration);

        assertTrue(result.error().isPresent());
        assertTrue(result.error().orElseThrow().message().contains(expectedMessage),
                () -> "Expected error containing '" + expectedMessage + "' but got "
                        + result.error().orElseThrow().message());
    }

    private static DataResult<MineralExtractorResourcesRecipe> decode(ResourceLocation fortune,
                                                                       ResourceLocation acceleration) {
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
