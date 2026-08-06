package com.jdte.setup;

import com.jdte.JDTE;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.EntityType;

public final class JDTETags {
    public static final TagKey<EntityType<?>> BIO_CRUSHER_BLACKLIST = entityTypeTag("bio_crusher_blacklist");
    public static final TagKey<EntityType<?>> BIO_CRUSHER_FORCE_KILL_BLACKLIST = entityTypeTag("bio_crusher_force_kill_blacklist");
    public static final TagKey<Block> CRYSTAL_INCUBATOR_BUDDING_BLOCKS = blockTag("crystal_incubator_budding_blocks");
    public static final TagKey<Block> CRYSTAL_INCUBATOR_HARVESTABLE_CRYSTALS = blockTag("crystal_incubator_harvestable_crystals");

    private JDTETags() {
    }

    private static TagKey<EntityType<?>> entityTypeTag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(JDTE.MODID, path));
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(JDTE.MODID, path));
    }
}
