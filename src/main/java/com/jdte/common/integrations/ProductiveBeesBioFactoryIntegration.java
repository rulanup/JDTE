package com.jdte.common.integrations;

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
import cy.jdkdigital.productivebees.setup.BeeReloadListener;
import cy.jdkdigital.productivebees.util.BeeHelper;
import cy.jdkdigital.productivebees.util.GeneAttribute;
import cy.jdkdigital.productivebees.util.GeneValue;
import cy.jdkdigital.productivelib.common.recipe.TagOutputRecipe.ChancedOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.component.CustomData;
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
        ResourceLocation beeType = ResourceLocation.fromNamespaceAndPath("productivebees", "life_fluid");

        CompoundTag entityData = new CompoundTag();
        entityData.putString("id", "productivebees:configurable_bee");
        entityData.putString("type", beeType.toString());
        ItemStack spawnEgg = new ItemStack(ModItems.CONFIGURABLE_SPAWN_EGG.get());
        spawnEgg.set(DataComponents.ENTITY_DATA, CustomData.of(entityData));

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
            Entity entity = egg.getType(stack).create(level);
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
        CompoundTag data = getBeeData(bee);
        CompoundTag containedEntity = getContainedAmberEntity(stack);
        if (containedEntity == null) return false;
        ResourceLocation entityId = ResourceLocation.tryParse(containedEntity.getString("id"));
        if (entityId == null) return false;
        if (usesWannabeeAmberFlower(bee, data)) {
            return BuiltInRegistries.ENTITY_TYPE.containsKey(entityId);
        }
        if (!usesEntityTypeFlowers(data)) return false;
        return BuiltInRegistries.ENTITY_TYPE.getOptional(entityId)
                .map(entityType -> matchesConfiguredEntityType(data, entityType)).orElse(false);
    }

    private static CompoundTag getContainedAmberEntity(ItemStack stack) {
        CustomData entityData = stack.get(DataComponents.ENTITY_DATA);
        if (entityData != null) return entityData.copyTag();

        CustomData blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null) return null;
        CompoundTag blockData = blockEntityData.copyTag();
        return blockData.contains("EntityData", net.minecraft.nbt.Tag.TAG_COMPOUND)
                ? blockData.getCompound("EntityData") : null;
    }

    private static boolean matchesConfiguredBlockFlower(ConfigurableBee bee, ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        CompoundTag data = getBeeData(bee);
        if (data == null || !"blocks".equals(data.getString("flowerType"))) return false;
        try {
            if (data.contains("flowerBlock")) {
                Block expected = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(data.getString("flowerBlock")));
                if (blockItem.getBlock() == expected) return true;
            }
            if (data.contains("flowerTag")) {
                TagKey<Block> tag = TagKey.create(Registries.BLOCK,
                        ResourceLocation.parse(data.getString("flowerTag")));
                return blockItem.getBlock().builtInRegistryHolder().is(tag);
            }
            return false;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static CompoundTag getBeeData(ConfigurableBee bee) {
        return BeeReloadListener.INSTANCE.getData(bee.getBeeType());
    }

    private static boolean usesEntityTypeFlowers(CompoundTag data) {
        return data != null && "entity_types".equals(data.getString("flowerType")) && data.contains("flowerTag");
    }

    private static boolean usesWannabeeAmberFlower(ConfigurableBee bee, CompoundTag data) {
        return data != null
                && ResourceLocation.fromNamespaceAndPath("productivebees", "wanna").equals(bee.getBeeType())
                && "productivebees:amber".equals(data.getString("flowerBlock"));
    }

    private static boolean matchesConfiguredEntityType(CompoundTag data, EntityType<?> entityType) {
        try {
            String configuredTag = data.getString("flowerTag");
            boolean inverse = data.getBoolean("inverseFlower") || configuredTag.startsWith("!");
            if (configuredTag.startsWith("!")) configuredTag = configuredTag.substring(1);
            TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(configuredTag));
            return inverse != entityType.is(tag);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static boolean canOperate(Level level, Bee bee) {
        if (!(bee instanceof cy.jdkdigital.productivebees.common.entity.bee.ProductiveBee productive)) return true;
        return (!level.isNight() || productive.canOperateDuringNight())
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
                && usesWannabeeAmberFlower(configurable, getBeeData(configurable));
    }

    private static List<ItemStack> getWannabeeProduce(Level level, ItemStack amber, BlockPos origin, double multiplier) {
        if (!(level instanceof ServerLevel serverLevel)) return List.of();
        CompoundTag entityData = getContainedAmberEntity(amber);
        if (entityData == null) return List.of();
        Entity contained = AmberBlockEntity.createEntity(serverLevel, entityData);
        if (!(contained instanceof Mob mob)) return List.of();
        mob.setPos(Vec3.atCenterOf(origin));

        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(mob.getLootTable());
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
        if (level.random.nextDouble() < multiplier % 1.0D) rolls++;
        List<ItemStack> outputs = new ArrayList<>(Math.max(0, rolls));
        for (int roll = 0; roll < rolls; roll++) {
            outputs.add(candidates.get(level.random.nextInt(candidates.size())).copy());
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

    public static List<JeiRecipe> getJeiRecipes(Level level, RecipeManager recipes) {
        List<JeiRecipe> result = new ArrayList<>();
        for (var holder : recipes.getAllRecipesFor(ModRecipeTypes.ADVANCED_BEEHIVE_TYPE.get())) {
            AdvancedBeehiveRecipe recipe = holder.value();
            BeeIngredient ingredient = recipe.ingredient.get();
            Entity entity = ingredient.getCachedEntity(level);
            if (!(entity instanceof Bee bee)) continue;

            ItemStack cage = new ItemStack(ModItems.BEE_CAGE.get());
            BeeCage.captureEntity(bee, cage);
            FloweringInputs flowering = getFloweringInputs(bee);
            List<JeiOutput> outputs = getDisplayOutputs(recipe.getRecipeOutputs());
            if (outputs.isEmpty()) continue;

            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("jdte",
                    "bio_factory/productivebees/" + holder.id().getPath());
            result.add(new JeiRecipe(id, cage, flowering.items(), flowering.fluid(), outputs));
        }
        LOGGER.info("Prepared {} Productive Bees Bio Factory JEI recipes", result.size());
        return result;
    }

    private static FloweringInputs getFloweringInputs(Bee bee) {
        List<ItemStack> result = new ArrayList<>();
        Optional<ResourceLocation> fluid = Optional.empty();
        if (bee instanceof ConfigurableBee configurable) {
            CompoundTag data = getBeeData(configurable);
            if (usesEntityTypeFlowers(data)) {
                addEntityTypeFlowers(result, data);
            } else {
                if (data != null && data.contains("flowerTag")) addFlowerTag(result, data.getString("flowerTag"));
                if (data != null && data.contains("flowerBlock")) addBlock(result, data.getString("flowerBlock"));
                if (data != null && data.contains("flowerItem")) addItem(result, data.getString("flowerItem"));
                if (data != null && data.contains("flowerFluid")) fluid = resolveFluid(data.getString("flowerFluid"));
            }
        } else {
            BuiltInRegistries.ITEM.getTag(ItemTags.BEE_FOOD).ifPresent(tag ->
                    tag.forEach(item -> addUnique(result, new ItemStack(item.value()))));
        }
        return new FloweringInputs(List.copyOf(result), fluid);
    }

    private static void addEntityTypeFlowers(List<ItemStack> result, CompoundTag data) {
        BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(entityType -> matchesConfiguredEntityType(data, entityType))
                .map(AmberItem::getFakeAmberItem)
                .forEach(stack -> addUnique(result, stack));
    }

    private static void addBlock(List<ItemStack> result, String id) {
        try {
            BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id))
                    .ifPresent(block -> addUnique(result, new ItemStack(block.asItem())));
        } catch (RuntimeException ignored) {
        }
    }

    private static void addFlowerTag(List<ItemStack> result, String id) {
        try {
            ResourceLocation location = ResourceLocation.parse(id);
            TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, location);
            BuiltInRegistries.BLOCK.getTag(blockTag).ifPresent(values -> values.forEach(holder ->
                    addUnique(result, new ItemStack(holder.value().asItem()))));
            if (result.isEmpty()) addItemTag(result, id);
        } catch (RuntimeException ignored) {
        }
    }

    private static void addItemTag(List<ItemStack> result, String id) {
        try {
            TagKey<net.minecraft.world.item.Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(id));
            BuiltInRegistries.ITEM.getTag(tag).ifPresent(values ->
                    values.forEach(item -> addUnique(result, new ItemStack(item.value()))));
        } catch (RuntimeException ignored) {
        }
    }

    private static void addItem(List<ItemStack> result, String id) {
        try {
            BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id))
                    .ifPresent(item -> addUnique(result, new ItemStack(item)));
        } catch (RuntimeException ignored) {
        }
    }

    private static Optional<ResourceLocation> resolveFluid(String value) {
        try {
            if (value.startsWith("#")) {
                TagKey<Fluid> tag = TagKey.create(Registries.FLUID, ResourceLocation.parse(value.substring(1)));
                return BuiltInRegistries.FLUID.getTag(tag).flatMap(values -> values.stream()
                        .map(holder -> holder.value())
                        .filter(fluid -> fluid.getBucket() != net.minecraft.world.item.Items.AIR)
                        .map(BuiltInRegistries.FLUID::getKey)
                        .findFirst());
            }
            ResourceLocation id = ResourceLocation.parse(value);
            return BuiltInRegistries.FLUID.getOptional(id).isPresent() ? Optional.of(id) : Optional.empty();
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static void addUnique(List<ItemStack> result, ItemStack stack) {
        if (stack.isEmpty() || result.stream().anyMatch(existing -> ItemStack.isSameItemSameComponents(existing, stack))) return;
        result.add(stack);
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

    private record FloweringInputs(List<ItemStack> items, Optional<ResourceLocation> fluid) { }

    public record JeiRecipe(ResourceLocation id, ItemStack specimen, List<ItemStack> foods,
                            Optional<ResourceLocation> processFluid,
                            List<JeiOutput> outputs) { }

    public record JeiOutput(List<ItemStack> stacks, float chance) { }
}
