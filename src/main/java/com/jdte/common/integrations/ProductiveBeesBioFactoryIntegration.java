package com.jdte.common.integrations;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import cy.jdkdigital.productivebees.ProductiveBeesConfig;
import cy.jdkdigital.productivebees.common.block.entity.AmberBlockEntity;
import cy.jdkdigital.productivebees.common.entity.bee.ConfigurableBee;
import cy.jdkdigital.productivebees.common.entity.bee.ProductiveBee;
import cy.jdkdigital.productivebees.common.item.AmberItem;
import cy.jdkdigital.productivebees.common.item.BeeCage;
import cy.jdkdigital.productivebees.common.recipe.AdvancedBeehiveRecipe;
import cy.jdkdigital.productivebees.compat.jei.ingredients.BeeIngredient;
import cy.jdkdigital.productivebees.init.ModEntities;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivebees.init.ModRecipeTypes;
import cy.jdkdigital.productivebees.init.ModTags;
import cy.jdkdigital.productivebees.setup.BeeReloadListener;
import cy.jdkdigital.productivebees.util.BeeAttributes;
import cy.jdkdigital.productivebees.util.BeeCreator;
import cy.jdkdigital.productivebees.util.BeeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Forge 1.20.1 bridge for Productive Bees' public API. */
public final class ProductiveBeesBioFactoryIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ProductiveBeesBioFactoryIntegration() {
    }

    public static List<ItemStack> getLifeFluidBeeCreativeItems() {
        ItemStack spawnEgg = new ItemStack(ModItems.CONFIGURABLE_SPAWN_EGG.get());
        BeeCreator.setTag("productivebees:life_fluid", spawnEgg);
        ItemStack honeycomb = new ItemStack(ModItems.CONFIGURABLE_HONEYCOMB.get());
        BeeCreator.setTag("productivebees:life_fluid", honeycomb);
        return List.of(spawnEgg, honeycomb);
    }

    public static boolean isBeeSpecimen(ItemStack stack) {
        return stack.getItem() instanceof BeeCage && BeeCage.isFilled(stack)
                || stack.getItem() instanceof SpawnEggItem;
    }

    public static Bee createBee(ItemStack stack, Level level) {
        if (stack.getItem() instanceof BeeCage && BeeCage.isFilled(stack)) {
            return BeeCage.getEntityFromStack(stack, level, true);
        }
        if (stack.getItem() instanceof SpawnEggItem egg) {
            EntityType<?> type = egg.getType(stack.getTag());
            Entity entity = type == null ? null : type.create(level);
            if (entity instanceof Bee bee) {
                // Forge 1.20.1 keeps spawn-egg entity data in EntityTag. This is
                // needed for Productive Bees' configurable spawn egg, whose
                // EntityType itself is shared by every configured bee.
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("EntityTag", Tag.TAG_COMPOUND)) {
                    bee.load(tag.getCompound("EntityTag"));
                }
                return bee;
            }
        }
        return null;
    }

    public static boolean isValidFood(Bee bee, ItemStack food, ItemStack fluidBucket) {
        if (bee instanceof ConfigurableBee configurable) {
            return matchesConfiguredEntityFlower(configurable, food)
                    || configurable.isFlowerItem(food)
                    || matchesConfiguredBlockFlower(configurable, food)
                    || configurable.isFlowerItem(fluidBucket);
        }
        return bee != null && bee.isFood(food);
    }

    private static boolean matchesConfiguredEntityFlower(ConfigurableBee bee, ItemStack stack) {
        if (!(stack.getItem() instanceof AmberItem)) return false;
        CompoundTag data = getBeeData(bee);
        CompoundTag containedEntity = getContainedAmberEntity(stack);
        if (containedEntity == null || !containedEntity.contains("entityType", Tag.TAG_STRING)) return false;

        ResourceLocation entityId = ResourceLocation.tryParse(containedEntity.getString("entityType"));
        if (entityId == null) return false;
        if (usesWannabeeAmberFlower(bee, data)) return BuiltInRegistries.ENTITY_TYPE.containsKey(entityId);
        if (!usesEntityTypeFlowers(data)) return false;
        return BuiltInRegistries.ENTITY_TYPE.getOptional(entityId)
                .map(type -> matchesConfiguredEntityType(data, type)).orElse(false);
    }

    private static CompoundTag getContainedAmberEntity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) return null;
        CompoundTag blockEntity = tag.getCompound("BlockEntityTag");
        return blockEntity.contains("EntityData", Tag.TAG_COMPOUND)
                ? blockEntity.getCompound("EntityData") : null;
    }

    private static boolean matchesConfiguredBlockFlower(ConfigurableBee bee, ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        CompoundTag data = getBeeData(bee);
        if (data == null || !"blocks".equals(data.getString("flowerType"))) return false;
        try {
            if (data.contains("flowerBlock")
                    && blockItem.getBlock() == BuiltInRegistries.BLOCK.get(
                    ResourceLocation.tryParse(data.getString("flowerBlock")))) return true;
            if (data.contains("flowerTag")) {
                TagKey<net.minecraft.world.level.block.Block> tag = TagKey.create(Registries.BLOCK,
                        ResourceLocation.tryParse(data.getString("flowerTag")));
                return blockItem.getBlock().builtInRegistryHolder().is(tag);
            }
        } catch (RuntimeException ignored) {
            // Datapack-defined bee data is optional and may be invalid after a reload.
        }
        return false;
    }

    private static CompoundTag getBeeData(ConfigurableBee bee) {
        return BeeReloadListener.INSTANCE.getData(bee.getBeeType());
    }

    private static boolean usesEntityTypeFlowers(CompoundTag data) {
        return data != null && "entity_types".equals(data.getString("flowerType"))
                && data.contains("flowerTag", Tag.TAG_STRING);
    }

    private static boolean usesWannabeeAmberFlower(ConfigurableBee bee, CompoundTag data) {
        return data != null && "productivebees:wanna".equals(bee.getBeeType())
                && "productivebees:amber".equals(data.getString("flowerBlock"));
    }

    private static boolean matchesConfiguredEntityType(CompoundTag data, EntityType<?> entityType) {
        try {
            String configuredTag = data.getString("flowerTag");
            boolean inverse = data.getBoolean("inverseFlower") || configuredTag.startsWith("!");
            if (configuredTag.startsWith("!")) configuredTag = configuredTag.substring(1);
            TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE,
                    ResourceLocation.tryParse(configuredTag));
            return inverse != entityType.is(tag);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static boolean canOperate(Level level, Bee bee) {
        if (!(bee instanceof ProductiveBee productive)) return true;
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
        Integer productivity = bee instanceof ProductiveBee productive
                ? productive.getAttributeValue(BeeAttributes.PRODUCTIVITY) : null;
        if (productivity == null || productivity <= 0) return outputs;
        outputs.forEach(stack -> applyProductivity(stack, productivity));
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
        mob.setPos(origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D);

        ResourceLocation lootId = mob.getLootTable();
        net.minecraft.world.level.storage.loot.LootTable lootTable =
                serverLevel.getServer().getLootData().getLootTable(lootId);
        if (lootTable.equals(net.minecraft.world.level.storage.loot.LootTable.EMPTY)) return List.of();
        FakePlayer wannabee = FakePlayerFactory.get(serverLevel,
                new GameProfile(ModEntities.WANNA_BEE_UUID, "wanna_bee"));
        net.minecraft.world.level.storage.loot.LootParams params =
                new net.minecraft.world.level.storage.loot.LootParams.Builder(serverLevel)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.LAST_DAMAGE_PLAYER, wannabee)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE,
                                level.damageSources().generic())
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL,
                                new ItemStack(Items.DIAMOND_AXE))
                        .withOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DIRECT_KILLER_ENTITY, wannabee)
                        .withOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.KILLER_ENTITY, wannabee)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, contained)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                                contained.position())
                        .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
        List<ItemStack> candidates = lootTable.getRandomItems(params).stream()
                .filter(stack -> !stack.is(ModTags.WANNABEE_LOOT_BLACKLIST)).toList();
        if (candidates.isEmpty()) return List.of();

        int rolls = (int) Math.floor(Math.max(0.0D, multiplier));
        if (level.random.nextDouble() < multiplier - Math.floor(multiplier)) rolls++;
        List<ItemStack> outputs = new ArrayList<>(rolls);
        for (int roll = 0; roll < rolls; roll++) {
            outputs.add(candidates.get(level.random.nextInt(candidates.size())).copy());
        }
        return outputs;
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
        for (AdvancedBeehiveRecipe recipe : recipes.getAllRecipesFor(ModRecipeTypes.ADVANCED_BEEHIVE_TYPE.get())) {
            BeeIngredient ingredient = recipe.ingredient.get();
            Entity entity = ingredient.getCachedEntity(level);
            if (!(entity instanceof Bee bee)) continue;

            ItemStack cage = new ItemStack(ModItems.BEE_CAGE.get());
            BeeCage.captureEntity(bee, cage);
            FloweringInputs flowering = getFloweringInputs(bee);
            List<JeiOutput> outputs = getDisplayOutputs(recipe.getRecipeOutputs());
            if (outputs.isEmpty()) continue;
            result.add(new JeiRecipe(recipe.getId(), cage, flowering.items(), flowering.fluid(), outputs));
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
            } else if (data != null) {
                if (data.contains("flowerTag", Tag.TAG_STRING)) addFlowerTag(result, data.getString("flowerTag"));
                if (data.contains("flowerBlock", Tag.TAG_STRING)) addBlock(result, data.getString("flowerBlock"));
                if (data.contains("flowerItem", Tag.TAG_STRING)) addItem(result, data.getString("flowerItem"));
                if (data.contains("flowerFluid", Tag.TAG_STRING)) fluid = resolveFluid(data.getString("flowerFluid"));
            }
        } else {
            BuiltInRegistries.ITEM.getTag(ItemTags.FLOWERS).ifPresent(tag ->
                    tag.forEach(item -> addUnique(result, new ItemStack(item.value()))));
        }
        return new FloweringInputs(List.copyOf(result), fluid);
    }

    private static void addEntityTypeFlowers(List<ItemStack> result, CompoundTag data) {
        BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> matchesConfiguredEntityType(data, type))
                .map(ProductiveBeesBioFactoryIntegration::createAmberFlower)
                .forEach(stack -> addUnique(result, stack));
    }

    /**
     * Productive Bees' Forge amber item stores the contained entity in the
     * legacy BlockEntityTag format. getFakeAmberItem only creates the display
     * name, so JEI entries need the entityType field added before they can be
     * used as a real flowering item.
     */
    private static ItemStack createAmberFlower(EntityType<?> entityType) {
        ItemStack stack = AmberItem.getFakeAmberItem(entityType);
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (id == null) return stack;

        CompoundTag blockEntity = stack.getOrCreateTagElement("BlockEntityTag");
        CompoundTag entityData = blockEntity.getCompound("EntityData");
        entityData.putString("entityType", id.toString());
        blockEntity.put("EntityData", entityData);
        return stack;
    }

    private static void addBlock(List<ItemStack> result, String id) {
        try {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location != null) BuiltInRegistries.BLOCK.getOptional(location)
                    .ifPresent(block -> addUnique(result, new ItemStack(block.asItem())));
        } catch (RuntimeException ignored) {
        }
    }

    private static void addFlowerTag(List<ItemStack> result, String id) {
        try {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location == null) return;
            TagKey<net.minecraft.world.level.block.Block> blockTag = TagKey.create(Registries.BLOCK, location);
            BuiltInRegistries.BLOCK.getTag(blockTag).ifPresent(values -> values.forEach(holder ->
                    addUnique(result, new ItemStack(holder.value().asItem()))));
            if (result.isEmpty()) {
                TagKey<net.minecraft.world.item.Item> itemTag = TagKey.create(Registries.ITEM, location);
                BuiltInRegistries.ITEM.getTag(itemTag).ifPresent(values -> values.forEach(holder ->
                        addUnique(result, new ItemStack(holder.value()))));
            }
        } catch (RuntimeException ignored) {
        }
    }

    private static void addItem(List<ItemStack> result, String id) {
        try {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location != null) BuiltInRegistries.ITEM.getOptional(location)
                    .ifPresent(item -> addUnique(result, new ItemStack(item)));
        } catch (RuntimeException ignored) {
        }
    }

    private static Optional<ResourceLocation> resolveFluid(String value) {
        try {
            if (value.startsWith("#")) {
                ResourceLocation location = ResourceLocation.tryParse(value.substring(1));
                if (location == null) return Optional.empty();
                TagKey<Fluid> tag = TagKey.create(Registries.FLUID, location);
                return BuiltInRegistries.FLUID.getTag(tag).flatMap(values -> values.stream()
                        .map(holder -> holder.value())
                        .filter(fluid -> fluid.getBucket() != Items.AIR)
                        .map(BuiltInRegistries.FLUID::getKey)
                        .findFirst());
            }
            ResourceLocation id = ResourceLocation.tryParse(value);
            return id != null && BuiltInRegistries.FLUID.getOptional(id).isPresent()
                    ? Optional.of(id) : Optional.empty();
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static void addUnique(List<ItemStack> result, ItemStack stack) {
        if (stack.isEmpty() || result.stream().anyMatch(existing -> ItemStack.isSameItemSameTags(existing, stack))) return;
        result.add(stack);
    }

    private static List<JeiOutput> getDisplayOutputs(Map<ItemStack, IntArrayTag> recipeOutputs) {
        List<JeiOutput> outputs = new ArrayList<>();
        for (Map.Entry<ItemStack, IntArrayTag> entry : recipeOutputs.entrySet()) {
            IntArrayTag values = entry.getValue();
            if (entry.getKey().isEmpty() || values.size() < 3) continue;
            int max = Math.max(1, values.get(1).getAsInt());
            float chance = Math.max(0.0F, Math.min(1.0F, values.get(2).getAsInt() / 100.0F));
            outputs.add(new JeiOutput(List.of(entry.getKey().copyWithCount(max)), chance));
        }
        return List.copyOf(outputs);
    }

    private record FloweringInputs(List<ItemStack> items, Optional<ResourceLocation> fluid) {
    }

    public record JeiRecipe(ResourceLocation id, ItemStack specimen, List<ItemStack> foods,
                            Optional<ResourceLocation> processFluid, List<JeiOutput> outputs) {
    }

    public record JeiOutput(List<ItemStack> stacks, float chance) {
    }
}
