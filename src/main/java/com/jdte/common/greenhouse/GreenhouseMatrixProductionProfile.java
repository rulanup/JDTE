package com.jdte.common.greenhouse;

import com.jdte.common.recipes.GreenhouseCropDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/** Immutable identity and production settings for one matrix-managed planting lane. */
public final class GreenhouseMatrixProductionProfile {
    public enum MachineKind { NORMAL, LARGE }

    private final MachineKind machineKind;
    private final ItemStack seed;
    private final int templateCount;
    private final String definitionKey;
    private final ResourceLocation fluid;
    private final long recipeGeneration;
    private final int selectedMultiplier;
    private final int structureMultiplier;
    private final int fortuneLevel;
    private final boolean creative;
    private final boolean overclocked;
    private final int energyPerHarvest;
    private final int fluidPerHarvest;
    private final int matrixSpeed;
    private final int matrixEfficiency;
    private final boolean seedConversion;
    private final boolean essenceConversion;
    private final int growthWork;
    private final long workPerTickPerUnit;
    private final GreenhouseCropDefinition definition;
    private final BlockPos representativePos;

    public GreenhouseMatrixProductionProfile(MachineKind machineKind, ItemStack seed, int templateCount,
                                             String definitionKey, ResourceLocation fluid, long recipeGeneration,
                                             int selectedMultiplier, int structureMultiplier, int fortuneLevel,
                                             boolean creative, boolean overclocked,
                                             int energyPerHarvest, int fluidPerHarvest,
                                             int matrixSpeed, int matrixEfficiency,
                                             boolean seedConversion, boolean essenceConversion,
                                             int growthWork, long workPerTickPerUnit) {
        this(machineKind, seed, templateCount, definitionKey, fluid, recipeGeneration, selectedMultiplier,
                structureMultiplier, fortuneLevel, creative, overclocked, energyPerHarvest, fluidPerHarvest,
                matrixSpeed, matrixEfficiency, seedConversion, essenceConversion, growthWork,
                workPerTickPerUnit, null, BlockPos.ZERO);
    }

    public GreenhouseMatrixProductionProfile(MachineKind machineKind, ItemStack seed, int templateCount,
                                             String definitionKey, ResourceLocation fluid, long recipeGeneration,
                                             int selectedMultiplier, int structureMultiplier, int fortuneLevel,
                                             boolean creative, boolean overclocked,
                                             int energyPerHarvest, int fluidPerHarvest,
                                             int matrixSpeed, int matrixEfficiency,
                                             boolean seedConversion, boolean essenceConversion,
                                             int growthWork, long workPerTickPerUnit,
                                             GreenhouseCropDefinition definition, BlockPos representativePos) {
        this.machineKind = Objects.requireNonNull(machineKind);
        if (seed.isEmpty()) throw new IllegalArgumentException("seed must not be empty");
        this.seed = seed.copyWithCount(1);
        this.templateCount = Math.max(1, templateCount);
        this.definitionKey = Objects.requireNonNull(definitionKey);
        this.fluid = Objects.requireNonNull(fluid);
        this.recipeGeneration = recipeGeneration;
        this.selectedMultiplier = Math.max(1, selectedMultiplier);
        this.structureMultiplier = Math.max(1, structureMultiplier);
        this.fortuneLevel = Math.max(0, fortuneLevel);
        this.creative = creative;
        this.overclocked = overclocked;
        this.energyPerHarvest = Math.max(0, energyPerHarvest);
        this.fluidPerHarvest = Math.max(0, fluidPerHarvest);
        this.matrixSpeed = Math.max(0, matrixSpeed);
        this.matrixEfficiency = Math.max(0, matrixEfficiency);
        this.seedConversion = seedConversion;
        this.essenceConversion = essenceConversion;
        this.growthWork = Math.max(1, growthWork);
        this.workPerTickPerUnit = Math.max(0L, workPerTickPerUnit);
        this.definition = definition;
        this.representativePos = representativePos.immutable();
    }

    public MachineKind machineKind() { return machineKind; }
    public ItemStack seed() { return seed.copy(); }
    public int templateCount() { return templateCount; }
    public String definitionKey() { return definitionKey; }
    public ResourceLocation fluid() { return fluid; }
    public long recipeGeneration() { return recipeGeneration; }
    public int selectedMultiplier() { return selectedMultiplier; }
    public int structureMultiplier() { return structureMultiplier; }
    public int fortuneLevel() { return fortuneLevel; }
    public boolean creative() { return creative; }
    public boolean overclocked() { return overclocked; }
    public int energyPerHarvest() { return energyPerHarvest; }
    public int fluidPerHarvest() { return fluidPerHarvest; }
    public int matrixSpeed() { return matrixSpeed; }
    public int matrixEfficiency() { return matrixEfficiency; }
    public boolean seedConversion() { return seedConversion; }
    public boolean essenceConversion() { return essenceConversion; }
    public int growthWork() { return growthWork; }
    public long workPerTickPerUnit() { return workPerTickPerUnit; }
    public GreenhouseCropDefinition definition() { return definition; }
    public BlockPos representativePos() { return representativePos; }

    public static String definitionKey(GreenhouseCropDefinition definition) {
        StringBuilder key = new StringBuilder()
                .append(definition.displayBlock()).append('|')
                .append(definition.harvestBlock()).append('|')
                .append(definition.useLootTable()).append('|')
                .append(definition.growthWork()).append('|')
                .append(definition.fluid()).append('|')
                .append(definition.timeFluid()).append('|')
                .append(definition.harvestGenerator() == null ? "static" : definition.harvestGenerator().getClass().getName());
        for (ItemStack output : definition.outputs()) {
            key.append('|').append(BuiltInRegistries.ITEM.getKey(output.getItem()))
                    .append('#').append(ItemStack.hashItemAndComponents(output))
                    .append('x').append(output.getCount());
        }
        return key.toString();
    }

    public static long workPerTick(int baseMultiplier, int selectedMultiplier, int templates,
                                   int structureMultiplier, int speedPercent) {
        long work = saturatingMultiply(Math.max(1, baseMultiplier), Math.max(1, selectedMultiplier));
        work = saturatingMultiply(work, Math.max(1, templates));
        work = saturatingMultiply(work, Math.max(1, structureMultiplier));
        return saturatingMultiply(work, 100L + Math.max(0, speedPercent)) / 100L;
    }

    private static long saturatingMultiply(long left, long right) {
        if (left == 0L || right == 0L) return 0L;
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GreenhouseMatrixProductionProfile profile)) return false;
        return templateCount == profile.templateCount
                && recipeGeneration == profile.recipeGeneration
                && selectedMultiplier == profile.selectedMultiplier
                && structureMultiplier == profile.structureMultiplier
                && fortuneLevel == profile.fortuneLevel
                && creative == profile.creative
                && overclocked == profile.overclocked
                && energyPerHarvest == profile.energyPerHarvest
                && fluidPerHarvest == profile.fluidPerHarvest
                && matrixSpeed == profile.matrixSpeed
                && matrixEfficiency == profile.matrixEfficiency
                && seedConversion == profile.seedConversion
                && essenceConversion == profile.essenceConversion
                && growthWork == profile.growthWork
                && workPerTickPerUnit == profile.workPerTickPerUnit
                && machineKind == profile.machineKind
                && definitionKey.equals(profile.definitionKey)
                && fluid.equals(profile.fluid)
                && ItemStack.isSameItemSameComponents(seed, profile.seed);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(machineKind, templateCount, definitionKey, fluid, recipeGeneration,
                selectedMultiplier, structureMultiplier, fortuneLevel, creative, overclocked,
                energyPerHarvest, fluidPerHarvest, matrixSpeed, matrixEfficiency,
                seedConversion, essenceConversion, growthWork, workPerTickPerUnit);
        return 31 * result + ItemStack.hashItemAndComponents(seed);
    }
}
