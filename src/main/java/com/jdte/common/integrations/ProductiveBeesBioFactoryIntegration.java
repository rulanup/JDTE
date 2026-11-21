package com.jdte.common.integrations;

import com.jdte.common.utils.SpawnEggEntityData;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import cy.jdkdigital.productivebees.ProductiveBees;
import cy.jdkdigital.productivebees.ProductiveBeesConfig;
import cy.jdkdigital.productivebees.capabilities.attributes.BeeAttributesHandler;
import cy.jdkdigital.productivebees.common.block.entity.AmberBlockEntity;
import cy.jdkdigital.productivebees.common.crafting.ingredient.BeeIngredient;
import cy.jdkdigital.productivebees.common.entity.bee.ConfigurableBee;
import cy.jdkdigital.productivebees.common.item.AmberItem;
import cy.jdkdigital.productivebees.common.item.BeeCage;
import cy.jdkdigital.productivebees.common.recipe.AdvancedBeehiveRecipe;
import cy.jdkdigital.productivebees.init.ModDataComponents;
import cy.jdkdigital.productivebees.init.ModEntities;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivebees.init.ModRecipeTypes;
import cy.jdkdigital.productivebees.init.ModTags;
import cy.jdkdigital.productivebees.setup.BeeData;
import cy.jdkdigital.productivebees.util.BeeHelper;
import cy.jdkdigital.productivebees.util.GeneAttribute;
import cy.jdkdigital.productivebees.util.GeneValue;
import cy.jdkdigital.productivelib.common.recipe.TagOutputRecipe.ChancedOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;

public final class ProductiveBeesBioFactoryIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ProductiveBeesBioFactoryIntegration() { }

    public static List<ItemStack> getLifeFluidBeeCreativeItems() {
        Identifier beeType = Identifier.fromNamespaceAndPath("productivebees", "life_fluid");

        CompoundTag entityData = new CompoundTag();
        entityData.putString("type", beeType.toString());
        ItemStack spawnEgg = new ItemStack(ModItems.CONFIGURABLE_SPAWN_EGG.get());
        spawnEgg.set(DataComponents.ENTITY_DATA, TypedEntityData.of(ModEntities.CONFIGURABLE_BEE.get(), entityData));

        ItemStack honeycomb = new ItemStack(ModItems.CONFIGURABLE_HONEYCOMB.get());
        honeycomb.set(ModDataComponents.BEE_TYPE.get(), beeType);
        return List.of(spawnEgg, honeycomb);
    }

    public static boolean isBeeSpecimen(ItemStack stack) {
        return stack.getItem() instanceof BeeCage && BeeCage.isFilled(stack)
                || stack.getItem() instanceof SpawnEggItem;
    }

    public static Bee createBee(ItemStack stack, Level level) {
        if (stack.getItem() instanceof BeeCage && BeeCage.isFilled(stack)) {
            // withInfo=true so the caged bee's NBT (including gene attribute attachments
            // like bee_behavior/bee_weather_tolerance/bee_productivity) is loaded onto the entity
            return BeeCage.getEntityFromStack(stack, level, true);
        }
        if (stack.getItem() instanceof SpawnEggItem egg) {
            Entity entity = egg.getType(stack).create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            if (entity != null) {
                var data = stack.get(DataComponents.ENTITY_DATA);
                if (data != null) data.loadInto(entity);
            }
            return entity instanceof Bee bee ? bee : null;
        }
        return null;
    }

    public static boolean isValidFood(Bee bee, ItemStack food, ItemStack fluidBucket) {
        if (bee instanceof ConfigurableBee configurable) {
            return matchesConfiguredEntityFlower(configurable, food)
                    || configurable.isFlowerItem(food) || matchesConfiguredBlockFlower(configurable, food)
                    || configurable.isFlowerItem(fluidBucket);
        }
        return bee != null && bee.isFood(food);
    }

    private static boolean matchesConfiguredEntityFlower(ConfigurableBee bee, ItemStack stack) {
        if (!(stack.getItem() instanceof AmberItem)) return false;
        BeeData data = bee.getBeeData();
        CompoundTag containedEntity = getContainedAmberEntity(stack);
        if (containedEntity == null) return false;
        Identifier entityId = Identifier.tryParse(containedEntity.getStringOr("id", ""));
        if (entityId == null) return false;
        if (usesWannabeeAmberFlower(bee, data)) {
            return BuiltInRegistries.ENTITY_TYPE.containsKey(entityId);
        }
        if (!usesEntityTypeFlowers(data)) return false;
        return BuiltInRegistries.ENTITY_TYPE.getOptional(entityId)
                .map(entityType -> matchesConfiguredEntityType(data, entityType)).orElse(false);
    }

    private static CompoundTag getContainedAmberEntity(ItemStack stack) {
        var entityData = SpawnEggEntityData.copy(stack);
        if (entityData.isPresent()) return entityData.get();
        var blockData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        return blockData == null ? null : blockData.copyTagWithoutId().getCompound("EntityData").orElse(null);
    }

    private static boolean matchesConfiguredBlockFlower(ConfigurableBee bee, ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        BeeData data = bee.getBeeData();
        if (data == null || !"blocks".equals(data.flowerType())) return false;
        try {
            if (data.flowerBlock().isPresent()) {
                Block expected = BuiltInRegistries.BLOCK.getValue(Identifier.parse(data.flowerBlock().orElseThrow()));
                if (blockItem.getBlock() == expected) return true;
            }
            if (data.flowerTag().isPresent()) {
                TagKey<Block> tag = TagKey.create(Registries.BLOCK,
                        Identifier.parse(data.flowerTag().orElseThrow()));
                return blockItem.getBlock().builtInRegistryHolder().is(tag);
            }
            return false;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean usesEntityTypeFlowers(BeeData data) {
        return data != null && "entity_types".equals(data.flowerType()) && data.flowerTag().isPresent();
    }

    private static boolean usesWannabeeAmberFlower(ConfigurableBee bee, BeeData data) {
        return data != null
                && Identifier.fromNamespaceAndPath("productivebees", "wanna").equals(bee.getBeeType())
                && "productivebees:amber".equals(data.flowerBlock().orElse(""));
    }

    private static boolean matchesConfiguredEntityType(BeeData data, EntityType<?> entityType) {
        try {
            String configuredTag = data.flowerTag().orElseThrow();
            boolean inverse = data.inverseFlower() || configuredTag.startsWith("!");
            if (configuredTag.startsWith("!")) configuredTag = configuredTag.substring(1);
            TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(configuredTag));
            return inverse != entityType.builtInRegistryHolder().is(tag);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static boolean canOperate(Level level, Bee bee) {
        if (!(bee instanceof cy.jdkdigital.productivebees.common.entity.bee.ProductiveBee productive)) return true;
        return (!level.isDarkOutside() || productive.canOperateDuringNight())
                && (!level.isRaining() || productive.canOperateDuringRain())
                && (!level.isThundering() || productive.canOperateDuringThunder());
    }

    public static List<ItemStack> produce(Level level, Bee bee, ItemStack food, BlockPos origin,
                                          boolean blockOutput, double multiplier) {
        if (bee == null) return List.of();
        List<ItemStack> outputs = isWannabeeWithAmber(bee, food)
                ? getWannabeeProduce(level, food, origin, multiplier)
                : BeeHelper.getBeeProduce(level, bee, blockOutput, multiplier);
        GeneValue productivity = getAttribute(bee, GeneAttribute.PRODUCTIVITY);
        if (productivity == null || productivity.getValue() <= 0) return outputs;
        int value = productivity.getValue();
        outputs.forEach(stack -> applyProductivity(stack, value));
        return outputs;
    }

    private static boolean isWannabeeWithAmber(Bee bee, ItemStack food) {
        return bee instanceof ConfigurableBee configurable
                && food.getItem() instanceof AmberItem
                && usesWannabeeAmberFlower(configurable, configurable.getBeeData());
    }

    private static List<ItemStack> getWannabeeProduce(Level level, ItemStack amber, BlockPos origin, double multiplier) {
        if (!(level instanceof ServerLevel serverLevel)) return List.of();
        CompoundTag entityData = getContainedAmberEntity(amber);
        if (entityData == null) return List.of();
        Entity contained = AmberBlockEntity.createEntity(serverLevel, entityData);
        if (!(contained instanceof Mob mob)) return List.of();
        mob.setPos(Vec3.atCenterOf(origin));

        LootTable lootTable = mob.getLootTable()
                .map(serverLevel.getServer().reloadableRegistries()::getLootTable).orElse(LootTable.EMPTY);
        if (lootTable.equals(LootTable.EMPTY)) return List.of();
        FakePlayer wannabee = FakePlayerFactory.get(serverLevel,
                new GameProfile(ModEntities.WANNA_BEE_UUID, "wanna_bee"));
        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, wannabee)
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.DIAMOND_AXE))
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, wannabee)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, wannabee)
                .withParameter(LootContextParams.THIS_ENTITY, contained)
                .withParameter(LootContextParams.ORIGIN,
                        new Vec3(origin.getX(), origin.getY(), origin.getZ()))
                .create(LootContextParamSets.ENTITY);
        List<ItemStack> candidates = lootTable.getRandomItems(params).stream()
                .filter(stack -> !stack.is(ModTags.WANNABEE_LOOT_BLACKLIST))
                .toList();
        if (candidates.isEmpty()) return List.of();

        int rolls = (int) Math.floor(multiplier);
        if (level.getRandom().nextDouble() < multiplier % 1.0D) rolls++;
        List<ItemStack> outputs = new ArrayList<>(Math.max(0, rolls));
        for (int roll = 0; roll < rolls; roll++) {
            outputs.add(candidates.get(level.getRandom().nextInt(candidates.size())).copy());
        }
        return outputs;
    }

    private static GeneValue getAttribute(Bee bee, GeneAttribute attribute) {
        if (!bee.hasData(ProductiveBees.ATTRIBUTE_HANDLER)) return null;
        BeeAttributesHandler attributes = bee.getData(ProductiveBees.ATTRIBUTE_HANDLER);
        return attributes.getAttributeValue(attribute);
    }

    private static void applyProductivity(ItemStack stack, int value) {
        if (stack.isEmpty()) return;
        if (stack.getCount() == 1) {
            stack.grow(value);
            return;
        }
        float bonus = 1.0F / (value + 2.0F) + (value + 1.0F) / 2.0F;
        stack.grow(Math.round(bonus * stack.getCount()));
    }

    public static double getProductivityMultiplier(int alpha, int beta, int gamma, int omega) {
        return 1.0D
                + ProductiveBeesConfig.UPGRADES.productivityMultiplier.get() * alpha
                + ProductiveBeesConfig.UPGRADES.productivityMultiplier2.get() * beta
                + ProductiveBeesConfig.UPGRADES.productivityMultiplier3.get() * gamma
                + ProductiveBeesConfig.UPGRADES.productivityMultiplier4.get() * omega;
    }

    public static void syncHiveRecipes(net.neoforged.neoforge.event.OnDatapackSyncEvent event) {
        event.sendRecipes(ModRecipeTypes.ADVANCED_BEEHIVE_TYPE.get());
    }

    public static List<JeiRecipe> getJeiRecipes(Level level, RecipeMap recipes) {
        List<JeiRecipe> result = new ArrayList<>();
        for (var holder : recipes.byType(ModRecipeTypes.ADVANCED_BEEHIVE_TYPE.get())) {
            try {
                addJeiRecipe(result, level, holder);
            } catch (RuntimeException | StackOverflowError exception) {
                LOGGER.warn("Skipping Productive Bees Bio Factory JEI recipe {}: {}",
                        holder.id(), exception.toString());
            }
        }
        LOGGER.info("Prepared {} Productive Bees Bio Factory JEI recipes", result.size());
        return result;
    }

    private static void addJeiRecipe(List<JeiRecipe> result, Level level,
                                     RecipeHolder<AdvancedBeehiveRecipe> holder) {
        AdvancedBeehiveRecipe recipe = holder.value();
        BeeIngredient ingredient = recipe.ingredient.get();
        Entity entity = ingredient.getCachedEntity(level);
        if (!(entity instanceof Bee bee)) return;

        ItemStack cage = new ItemStack(ModItems.BEE_CAGE.get());
        BeeCage.captureEntity(bee, cage);
        FloweringInputs flowering = getFloweringInputsSafely(bee, holder.id().identifier());
        List<JeiOutput> outputs = getDisplayOutputs(recipe.getRecipeOutputs());
        if (outputs.isEmpty()) return;

        Identifier id = Identifier.fromNamespaceAndPath("jdte",
                "bio_factory/productivebees/" + holder.id().identifier().getPath());
        result.add(new JeiRecipe(id, cage, flowering.items(), flowering.fluid(), outputs));
    }

    private static FloweringInputs getFloweringInputsSafely(Bee bee, Identifier recipeId) {
        try {
            return getFloweringInputs(bee);
        } catch (RuntimeException | StackOverflowError exception) {
            LOGGER.warn("Skipping Productive Bees flowering inputs for JEI recipe {}: {}",
                    recipeId, exception.toString());
            return FloweringInputs.empty();
        }
    }

    private static FloweringInputs getFloweringInputs(Bee bee) {
        FloweringInputsBuilder builder = new FloweringInputsBuilder();
        Optional<Identifier> fluid = Optional.empty();
        if (bee instanceof ConfigurableBee configurable) {
            BeeData data = configurable.getBeeData();
            if (usesEntityTypeFlowers(data)) {
                addEntityTypeFlowers(builder, data);
            } else {
                if (data != null && data.flowerTag().isPresent()) addFlowerTag(builder, data.flowerTag().orElseThrow());
                if (data != null && data.flowerBlock().isPresent()) addBlock(builder, data.flowerBlock().orElseThrow());
                if (data != null && data.flowerItem().isPresent()) addItem(builder, data.flowerItem().orElseThrow());
                if (data != null && data.flowerFluid().isPresent()) fluid = resolveFluid(data.flowerFluid().orElseThrow());
            }
        } else {
            BuiltInRegistries.ITEM.get(ItemTags.BEE_FOOD).ifPresent(tag -> {
                for (var item : tag) {
                    if (builder.atLimit()) break;
                    builder.add(new ItemStack(item.value()));
                }
            });
        }
        return new FloweringInputs(builder.items(), fluid);
    }

    private static void addEntityTypeFlowers(FloweringInputsBuilder builder, BeeData data) {
        BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(entityType -> matchesConfiguredEntityType(data, entityType))
                .map(AmberItem::getFakeAmberItem)
                .limit(MAX_JEI_FLOWERING_ITEMS)
                .forEach(builder::add);
    }

    private static void addBlock(FloweringInputsBuilder builder, String id) {
        try {
            BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id))
                    .ifPresent(block -> builder.add(new ItemStack(block.asItem())));
        } catch (RuntimeException ignored) {
        }
    }

    private static void addFlowerTag(FloweringInputsBuilder builder, String id) {
        try {
            Identifier location = Identifier.parse(id);
            TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, location);
            int before = builder.size();
            expandBlockTag(builder, blockTag);
            if (builder.size() == before) expandItemTag(builder, id);
        } catch (RuntimeException | StackOverflowError exception) {
            LOGGER.debug("Skipping Productive Bees JEI flowering tag {}: {}", id, exception.toString());
        }
    }

    private static void expandBlockTag(FloweringInputsBuilder builder, TagKey<Block> tag) {
        if (!builder.enterTag(tag)) return;
        try {
            var values = BuiltInRegistries.BLOCK.get(tag);
            if (values.isEmpty()) return;
            for (var holder : values.get()) {
                if (builder.atLimit()) break;
                builder.add(new ItemStack(holder.value().asItem()));
            }
        } finally {
            builder.leaveTag(tag);
        }
    }

    private static void expandItemTag(FloweringInputsBuilder builder, String id) {
        try {
            TagKey<net.minecraft.world.item.Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(id));
            if (!builder.enterTag(tag)) return;
            try {
                var values = BuiltInRegistries.ITEM.get(tag);
                if (values.isEmpty()) return;
                for (var item : values.get()) {
                    if (builder.atLimit()) break;
                    builder.add(new ItemStack(item.value()));
                }
            } finally {
                builder.leaveTag(tag);
            }
        } catch (RuntimeException | StackOverflowError exception) {
            LOGGER.debug("Skipping Productive Bees JEI flowering item tag {}: {}", id, exception.toString());
        }
    }

    private static void addItem(FloweringInputsBuilder builder, String id) {
        try {
            BuiltInRegistries.ITEM.getOptional(Identifier.parse(id))
                    .ifPresent(item -> builder.add(new ItemStack(item)));
        } catch (RuntimeException ignored) {
        }
    }

    private static Optional<Identifier> resolveFluid(String value) {
        try {
            if (value.startsWith("#")) {
                TagKey<Fluid> tag = TagKey.create(Registries.FLUID, Identifier.parse(value.substring(1)));
                return BuiltInRegistries.FLUID.get(tag).flatMap(values -> values.stream()
                        .map(holder -> holder.value())
                        .filter(fluid -> fluid.getBucket() != net.minecraft.world.item.Items.AIR)
                        .map(BuiltInRegistries.FLUID::getKey)
                        .findFirst());
            }
            Identifier id = Identifier.parse(value);
            return BuiltInRegistries.FLUID.getOptional(id).isPresent() ? Optional.of(id) : Optional.empty();
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static List<JeiOutput> getDisplayOutputs(Map<ItemStack, ChancedOutput> recipeOutputs) {
        List<JeiOutput> outputs = new ArrayList<>();
        for (Map.Entry<ItemStack, ChancedOutput> entry : recipeOutputs.entrySet()) {
            if (entry.getKey().isEmpty()) continue;
            ChancedOutput output = entry.getValue();
            ItemStack stack = entry.getKey().copyWithCount(Math.max(1, output.max()));
            outputs.add(new JeiOutput(List.of(stack), output.chance()));
        }
        return List.copyOf(outputs);
    }

    private static final int MAX_JEI_FLOWERING_ITEMS = 128;
    private static final int MAX_JEI_FLOWERING_TAGS = 32;

    private static final class FloweringInputsBuilder {
        private final List<ItemStack> items = new ArrayList<>();
        private final java.util.Set<TagKey<?>> expandedTags = new java.util.HashSet<>();
        private final java.util.Set<TagKey<?>> activeTags = new java.util.HashSet<>();

        private void add(ItemStack stack) {
            if (stack.isEmpty() || atLimit()
                    || items.stream().anyMatch(existing -> ItemStack.isSameItemSameComponents(existing, stack))) return;
            items.add(stack);
        }

        private boolean atLimit() {
            return items.size() >= MAX_JEI_FLOWERING_ITEMS;
        }

        private int size() {
            return items.size();
        }

        private boolean enterTag(TagKey<?> tag) {
            if (activeTags.contains(tag) || expandedTags.size() >= MAX_JEI_FLOWERING_TAGS
                    || !expandedTags.add(tag)) return false;
            activeTags.add(tag);
            return true;
        }

        private void leaveTag(TagKey<?> tag) {
            activeTags.remove(tag);
        }

        private List<ItemStack> items() {
            return List.copyOf(items);
        }
    }

    private record FloweringInputs(List<ItemStack> items, Optional<Identifier> fluid) {
        private static FloweringInputs empty() {
            return new FloweringInputs(List.of(), Optional.empty());
        }
    }

    public record JeiRecipe(Identifier id, ItemStack specimen, List<ItemStack> foods,
                            Optional<Identifier> processFluid,
                            List<JeiOutput> outputs) { }

    public record JeiOutput(List<ItemStack> stacks, float chance) { }
}
