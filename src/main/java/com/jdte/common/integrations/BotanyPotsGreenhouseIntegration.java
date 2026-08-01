package com.jdte.common.integrations;

import com.jdte.common.recipes.GreenhouseCropDefinition;
import com.jdte.setup.JDTEConfig;
import net.darkhax.botanypots.common.api.context.BotanyPotContext;
import net.darkhax.botanypots.common.api.data.display.types.Display;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.darkhax.botanypots.common.impl.Helpers;
import net.darkhax.botanypots.common.impl.data.display.types.AgingDisplayState;
import net.darkhax.botanypots.common.impl.data.display.types.PhasedDisplayState;
import net.darkhax.botanypots.common.impl.data.display.types.SimpleDisplayState;
import net.darkhax.botanypots.common.impl.data.recipe.crop.BasicCrop;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class BotanyPotsGreenhouseIntegration {
    private BotanyPotsGreenhouseIntegration() {
    }

    public static GreenhouseCropDefinition find(Level level, ItemStack seed) {
        if (level == null || seed.isEmpty()) return null;

        for (var cropHolder : level.getRecipeManager().getAllRecipesFor(Crop.TYPE.get())) {
            Crop crop = cropHolder.value();
            GreenhouseCropDefinition definition = createDefinition(level, seed, crop);
            if (definition != null) return definition;
        }
        return null;
    }

    /** Enumerates the concrete planting items exposed by every loaded Botany Pots data recipe. */
    public static List<DiscoveredCrop> getCrops(Level level) {
        if (level == null) return List.of();
        List<DiscoveredCrop> result = new ArrayList<>();
        for (var cropHolder : level.getRecipeManager().getAllRecipesFor(Crop.TYPE.get())) {
            if (!(cropHolder.value() instanceof BasicCrop basicCrop)) continue;
            for (ItemStack seed : basicCrop.getBasicProperties().input().getItems()) {
                if (seed.isEmpty()) continue;
                GreenhouseCropDefinition definition = createDefinition(level, seed, cropHolder.value());
                if (definition != null) {
                    result.add(new DiscoveredCrop(cropHolder.id(), seed.copyWithCount(1), definition));
                }
            }
        }
        return List.copyOf(result);
    }

    private static GreenhouseCropDefinition createDefinition(Level level, ItemStack seed, Crop crop) {
        GreenhouseContext probe = new GreenhouseContext(level, BlockPos.ZERO, ItemStack.EMPTY,
                seed.copyWithCount(1), ItemStack.EMPTY, crop, null, 1);
        if (!crop.couldMatch(seed, probe, level) || !crop.matches(probe, level)) return null;

        SoilMatch soilMatch = findCompatibleSoil(level, seed, crop);
        if (soilMatch == null) return null;

        GreenhouseContext context = soilMatch.context();
        int growthWork = Math.max(1, Helpers.getRequiredGrowthTicks(context, level, crop, soilMatch.soil()));
        List<ItemStack> previewOutputs = getPreviewOutputs(crop, seed);
        ResourceLocation displayBlock = getDisplayBlock(crop, context, level, seed, previewOutputs);
        ItemStack soilItem = soilMatch.soilItem().copyWithCount(1);
        ItemStack seedItem = seed.copyWithCount(1);

        return new GreenhouseCropDefinition(previewOutputs, displayBlock, displayBlock, false,
                growthWork, JDTEConfig.COMMON.greenhouseGenericFluidCost.get(),
                (serverLevel, pos, tool) -> harvest(serverLevel, pos, tool, seedItem, soilItem, crop,
                        soilMatch.soil(), growthWork));
    }

    private static SoilMatch findCompatibleSoil(Level level, ItemStack seed, Crop crop) {
        for (var soilHolder : level.getRecipeManager().getAllRecipesFor(Soil.TYPE.get())) {
            Soil soil = soilHolder.value();
            for (var item : BuiltInRegistries.ITEM) {
                ItemStack soilItem = item.getDefaultInstance();
                if (soilItem.isEmpty()) continue;
                GreenhouseContext context = new GreenhouseContext(level, BlockPos.ZERO, soilItem,
                        seed.copyWithCount(1), ItemStack.EMPTY, crop, soil, 1);
                if (soil.couldMatch(soilItem, context, level)
                        && soil.matches(context, level)
                        && crop.isGrowthSustained(context, level)) {
                    return new SoilMatch(soil, soilItem.copyWithCount(1), context);
                }
            }
        }
        return null;
    }

    private static List<ItemStack> getPreviewOutputs(Crop crop, ItemStack seed) {
        List<ItemStack> outputs = new ArrayList<>();
        if (crop instanceof BasicCrop basicCrop) {
            basicCrop.getBasicProperties().drops().stream()
                    .flatMap(provider -> provider.getDisplayItems().stream())
                    .filter(stack -> !stack.isEmpty())
                    .forEach(stack -> addUnique(outputs, stack));
        }
        if (outputs.isEmpty()) outputs.add(seed.copyWithCount(1));
        return List.copyOf(outputs);
    }

    private static ResourceLocation getDisplayBlock(Crop crop, BotanyPotContext context, Level level,
                                                    ItemStack seed, List<ItemStack> outputs) {
        List<Display> displays = crop.getDisplayState(context, level);
        for (int index = displays.size() - 1; index >= 0; index--) {
            BlockState state = getDisplayState(displays.get(index));
            if (state != null) return BuiltInRegistries.BLOCK.getKey(state.getBlock());
        }
        if (seed.getItem() instanceof BlockItem blockItem) {
            return BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
        }
        for (ItemStack output : outputs) {
            if (output.getItem() instanceof BlockItem blockItem) {
                return BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
            }
        }
        return BuiltInRegistries.BLOCK.getKey(Blocks.WHEAT);
    }

    private static BlockState getDisplayState(Display display) {
        if (display instanceof SimpleDisplayState simple) return simple.getState();
        if (display instanceof AgingDisplayState aging) return aging.getBlock().defaultBlockState();
        if (display instanceof PhasedDisplayState phased) {
            List<Display> phases = phased.getDisplayPhases();
            return phases.isEmpty() ? null : getDisplayState(phases.getLast());
        }
        return null;
    }

    private static List<ItemStack> harvest(ServerLevel level, BlockPos pos, ItemStack tool,
                                           ItemStack seed, ItemStack soilItem, Crop crop, Soil soil,
                                           int growthTicks) {
        GreenhouseContext context = new GreenhouseContext(level, pos, soilItem, seed, tool, crop, soil, growthTicks);
        if (!crop.canHarvest(context, level) || !crop.isGrowthSustained(context, level)) return List.of();

        List<ItemStack> drops = new ArrayList<>();
        int rolls = Helpers.getLootRolls(context, level, crop, soil);
        for (int roll = 0; roll < rolls; roll++) {
            crop.onHarvest(context, level, stack -> {
                if (!stack.isEmpty()) drops.add(stack.copy());
            });
        }
        return List.copyOf(drops);
    }

    private static void addUnique(List<ItemStack> outputs, ItemStack candidate) {
        for (ItemStack output : outputs) {
            if (ItemStack.isSameItemSameComponents(output, candidate)) return;
        }
        outputs.add(candidate.copy());
    }

    private record SoilMatch(Soil soil, ItemStack soilItem, GreenhouseContext context) {
    }

    public record DiscoveredCrop(ResourceLocation recipeId, ItemStack seed,
                                 GreenhouseCropDefinition definition) {
    }

    private record GreenhouseContext(Level level, BlockPos pos, ItemStack soilItem, ItemStack seedItem,
                                     ItemStack harvestItem, Crop crop, Soil soil,
                                     int requiredGrowthTicks) implements BotanyPotContext {
        @Override
        public ItemStack getSoilItem() {
            return soilItem;
        }

        @Override
        public ItemStack getSeedItem() {
            return seedItem;
        }

        @Override
        public ItemStack getHarvestItem() {
            return harvestItem;
        }

        @Override
        public LootParams createLootParams(BlockState state) {
            if (!(level instanceof ServerLevel serverLevel)) {
                throw new IllegalStateException("Can not create Botany Pots loot parameters on the client");
            }
            return new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.BLOCK_STATE,
                            state == null ? serverLevel.getBlockState(pos) : state)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.TOOL, harvestItem)
                    .create(LootContextParamSets.BLOCK);
        }

        @Override
        public void runFunction(ResourceLocation functionId) {
            if (!(level instanceof ServerLevel serverLevel)) return;
            serverLevel.getServer().getCommands().performPrefixedCommand(
                    serverLevel.getServer().createCommandSourceStack()
                            .withPosition(Vec3.atCenterOf(pos)).withSuppressedOutput(),
                    "function " + functionId);
        }

        @Override
        public Player getPlayer() {
            return null;
        }

        @Override
        public ItemStack getInteractionItem() {
            return harvestItem;
        }

        @Override
        public int getRequiredGrowthTicks() {
            return requiredGrowthTicks;
        }

        @Override
        public boolean isServerThread() {
            return !level.isClientSide;
        }

        @Override
        public Crop getCrop() {
            return crop;
        }

        @Override
        public Soil getSoil() {
            return soil;
        }

        @Override
        public ItemStack getItem(int slot) {
            return switch (slot) {
                case 0 -> soilItem;
                case 1 -> seedItem;
                case 2 -> harvestItem;
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public int size() {
            return 3;
        }
    }
}
