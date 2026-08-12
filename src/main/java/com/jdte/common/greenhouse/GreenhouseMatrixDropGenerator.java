package com.jdte.common.greenhouse;

import com.jdte.common.blockentities.GreenhouseEssenceConversionHelper;
import com.jdte.common.recipes.GreenhouseCropDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.ArrayList;
import java.util.List;

/** Generates one bounded representative batch and scales it to a complete production group settlement. */
public final class GreenhouseMatrixDropGenerator {
    private static final int FORTUNE_DENOMINATOR = 10;
    private static final int MAX_CONVERSION_CHUNK = 2_147_483_520;

    private GreenhouseMatrixDropGenerator() {
    }

    public static Result generate(ServerLevel level, GreenhouseMatrixProductionProfile profile,
                                  long harvests, int maxSamples, RandomSource random) {
        GreenhouseCropDefinition definition = profile.definition();
        if (definition == null || harvests <= 0L) return new Result(List.of(), 0);

        boolean sampled = definition.harvestGenerator() != null || definition.useLootTable();
        int samples = sampled ? (int) Math.min(harvests, Math.max(1, maxSamples)) : 1;
        long baseGroup = harvests / samples;
        long extraGroups = harvests % samples;
        List<Drop> result = new ArrayList<>();
        for (int sample = 0; sample < samples; sample++) {
            long groupHarvests = baseGroup + (sample < extraGroups ? 1L : 0L);
            List<ItemStack> drops = sampled
                    ? generateSingleHarvest(level, profile, definition)
                    : definition.outputs();
            if (profile.seedConversion() && !definition.outputs().isEmpty()) {
                drops = GreenhouseEssenceConversionHelper.replaceSeeds(
                        drops, profile.seed(), definition.outputs().getFirst());
            }
            for (ItemStack drop : drops) {
                long amount = fortuneAmount(drop.getCount(), groupHarvests, profile.fortuneLevel(), random);
                addConverted(level, result, drop, amount, profile.essenceConversion());
            }
        }
        return new Result(List.copyOf(result), sampled ? samples : 0);
    }

    private static List<ItemStack> generateSingleHarvest(ServerLevel level,
                                                         GreenhouseMatrixProductionProfile profile,
                                                         GreenhouseCropDefinition definition) {
        if (level == null) return definition.outputs();
        ItemStack tool = new ItemStack(Items.DIAMOND_HOE);
        if (definition.harvestGenerator() != null) {
            return definition.generateHarvest(level, profile.representativePos(), tool);
        }
        if (!definition.useLootTable() || definition.harvestBlock() == null
                || !BuiltInRegistries.BLOCK.containsKey(definition.harvestBlock())) {
            return definition.outputs();
        }
        Block block = BuiltInRegistries.BLOCK.get(definition.harvestBlock());
        BlockState mature = matureState(block);
        List<ItemStack> drops = Block.getDrops(mature, level, profile.representativePos(), null,
                FakePlayerFactory.getMinecraft(level), tool);
        return drops.isEmpty() ? definition.outputs() : drops;
    }

    private static BlockState matureState(Block block) {
        if (block instanceof CropBlock crop) return crop.getStateForAge(crop.getMaxAge());
        BlockState state = block.defaultBlockState();
        for (var property : state.getProperties()) {
            if (property instanceof IntegerProperty integer && "age".equals(integer.getName())) {
                int max = integer.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                return state.setValue(integer, max);
            }
        }
        return state;
    }

    private static long fortuneAmount(int perHarvest, long harvests, int fortune, RandomSource random) {
        long base = saturatingMultiply(Math.max(0, perHarvest), harvests);
        long numerator = saturatingMultiply(base, Math.max(0, fortune));
        long bonus = numerator / FORTUNE_DENOMINATOR;
        int remainder = (int) (numerator % FORTUNE_DENOMINATOR);
        if (remainder > 0 && random.nextInt(FORTUNE_DENOMINATOR) < remainder && bonus < Long.MAX_VALUE) bonus++;
        return saturatingAdd(base, bonus);
    }

    private static void addConverted(ServerLevel level, List<Drop> result, ItemStack stack,
                                     long amount, boolean convertEssence) {
        if (stack.isEmpty() || amount <= 0L) return;
        if (!convertEssence || level == null) {
            add(result, stack, amount);
            return;
        }
        long remaining = amount;
        while (remaining > 0L) {
            int chunk = (int) Math.min(remaining, MAX_CONVERSION_CHUNK);
            List<ItemStack> converted = GreenhouseEssenceConversionHelper.convert(
                    level, List.of(stack.copyWithCount(chunk)));
            for (ItemStack output : converted) add(result, output, output.getCount());
            remaining -= chunk;
        }
    }

    private static void add(List<Drop> drops, ItemStack stack, long amount) {
        for (int index = 0; index < drops.size(); index++) {
            Drop existing = drops.get(index);
            if (!ItemStack.isSameItemSameComponents(existing.stack, stack)) continue;
            drops.set(index, new Drop(existing.stack, saturatingAdd(existing.amount, amount)));
            return;
        }
        drops.add(new Drop(stack.copyWithCount(1), amount));
    }

    private static long saturatingMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) return 0L;
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }

    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    public record Drop(ItemStack stack, long amount) {
        public Drop {
            stack = stack.copyWithCount(1);
            amount = Math.max(0L, amount);
        }

        @Override public ItemStack stack() { return stack.copy(); }
    }

    public record Result(List<Drop> drops, int dynamicCalls) {
        public Result {
            drops = List.copyOf(drops);
        }
    }
}
