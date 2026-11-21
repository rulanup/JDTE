package com.jdte.common.integrations;

import net.darkhax.botanypots.common.api.context.BotanyPotContext;
import net.darkhax.botanypots.common.api.data.display.types.Display;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.impl.data.recipe.crop.BasicCrop;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BotanyPotsGreenhouseIntegrationTest {

    @Test
    void basicCropTriesOnlyItsDeclaredSoils() {
        BasicCrop crop = basicCrop(Ingredient.of(Items.DIRT, Items.SAND));
        List<ItemStack> tried = new ArrayList<>();
        AtomicInteger fallbackCalls = new AtomicInteger();

        String match = BotanyPotsGreenhouseIntegration.findCompatibleSoil(
                crop,
                soil -> {
                    tried.add(soil.copy());
                    return soil.is(Items.SAND) ? "sand" : null;
                },
                () -> {
                    fallbackCalls.incrementAndGet();
                    return "fallback";
                });

        assertEquals("sand", match);
        assertEquals(List.of(Items.DIRT, Items.SAND), tried.stream().map(ItemStack::getItem).toList());
        assertEquals(List.of(1, 1), tried.stream().map(ItemStack::getCount).toList());
        assertEquals(0, fallbackCalls.get());
    }

    @Test
    void basicCropWithNoMatchingDeclaredSoilDoesNotScanFallback() {
        BasicCrop crop = basicCrop(Ingredient.of(Items.DIRT, Items.SAND));
        List<ItemStack> tried = new ArrayList<>();
        AtomicInteger fallbackCalls = new AtomicInteger();

        String match = BotanyPotsGreenhouseIntegration.findCompatibleSoil(
                crop,
                soil -> {
                    tried.add(soil.copy());
                    return null;
                },
                () -> {
                    fallbackCalls.incrementAndGet();
                    return "fallback";
                });

        assertNull(match);
        assertEquals(List.of(Items.DIRT, Items.SAND), tried.stream().map(ItemStack::getItem).toList());
        assertEquals(0, fallbackCalls.get());
    }

    @Test
    void customCropUsesCompatibilityFallbackWithoutTryingBasicSoils() {
        AtomicInteger candidateCalls = new AtomicInteger();
        AtomicInteger fallbackCalls = new AtomicInteger();

        String match = BotanyPotsGreenhouseIntegration.findCompatibleSoil(
                new CustomCrop(),
                soil -> {
                    candidateCalls.incrementAndGet();
                    return null;
                },
                () -> {
                    fallbackCalls.incrementAndGet();
                    return "fallback";
                });

        assertEquals("fallback", match);
        assertEquals(0, candidateCalls.get());
        assertEquals(1, fallbackCalls.get());
    }

    private static BasicCrop basicCrop(Ingredient soils) {
        return new BasicCrop(new BasicCrop.Properties(
                Ingredient.of(Items.WHEAT_SEEDS),
                soils,
                20,
                List.of(),
                0,
                List.of(),
                Optional.empty(),
                Optional.empty(),
                1.0F,
                1.0F));
    }

    private static final class CustomCrop extends Crop {
        @Override
        public boolean matches(BotanyPotContext input, Level level) {
            return false;
        }

        @Override
        public boolean couldMatch(ItemStack candidate, BotanyPotContext context, Level level) {
            return false;
        }

        @Override
        public void onHarvest(BotanyPotContext context, Level level, Consumer<ItemStack> drops) {
        }

        @Override
        public List<Display> getDisplayState(BotanyPotContext context, Level level) {
            return List.of();
        }

        @Override
        public int getRequiredGrowthTicks(BotanyPotContext context, Level level) {
            return 1;
        }

        @Override
        public boolean isGrowthSustained(BotanyPotContext context, Level level) {
            return false;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return null;
        }
    }
}
