package com.jdte.common.greenhouse;

import com.jdte.common.blockentities.GreenhouseProductionEngine;
import com.jdte.common.recipes.GreenhouseCropResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Consumer;

/** Bounded profile rebuild and group-only fixed-point work advancement. */
public final class GreenhouseMatrixSimulation {
    private Map<GreenhouseMatrixProductionProfile, GreenhouseMatrixProductionGroup> activeGroups = new LinkedHashMap<>();
    private Map<GreenhouseMatrixProductionProfile, GreenhouseMatrixProductionGroup> buildingGroups = new LinkedHashMap<>();
    private List<BlockPos> rebuildingMembers = List.of();
    private Function<BlockPos, List<GreenhouseMatrixProductionProfile>> resolver;
    private int rebuildCursor;
    private boolean rebuilding;
    private final Map<GreenhouseMatrixProductionProfile, Long> restoredWork = new LinkedHashMap<>();

    public void beginRebuild(List<BlockPos> members,
                             Function<BlockPos, List<GreenhouseMatrixProductionProfile>> resolver) {
        rebuildingMembers = new ArrayList<>(members);
        this.resolver = resolver;
        buildingGroups = new LinkedHashMap<>();
        rebuildCursor = 0;
        rebuilding = true;
        if (rebuildingMembers.isEmpty()) finishRebuild();
    }

    public void rebuildStep(int budget) {
        if (!rebuilding || resolver == null) return;
        int remaining = Math.max(1, budget);
        while (remaining-- > 0 && rebuildCursor < rebuildingMembers.size()) {
            List<GreenhouseMatrixProductionProfile> profiles = resolver.apply(rebuildingMembers.get(rebuildCursor++));
            if (profiles == null) continue;
            for (GreenhouseMatrixProductionProfile profile : profiles) {
                if (profile != null) buildingGroups.computeIfAbsent(profile,
                        GreenhouseMatrixProductionGroup::new).addUnit();
            }
        }
        if (rebuildCursor >= rebuildingMembers.size()) finishRebuild();
    }

    private void finishRebuild() {
        for (Map.Entry<GreenhouseMatrixProductionProfile, GreenhouseMatrixProductionGroup> entry
                : buildingGroups.entrySet()) {
            GreenhouseMatrixProductionGroup old = activeGroups.get(entry.getKey());
            if (old != null) {
                entry.getValue().setWorkRemainder(old.workRemainder());
            } else {
                Long restored = restoredWork.remove(entry.getKey());
                if (restored != null) entry.getValue().setWorkRemainder(restored);
            }
        }
        activeGroups = buildingGroups;
        buildingGroups = new LinkedHashMap<>();
        rebuildingMembers = List.of();
        resolver = null;
        rebuildCursor = 0;
        rebuilding = false;
    }

    public void advanceWork(long elapsedTicks, long harvestBudgetPerUnit, long maxPendingWorkPerUnit) {
        if (elapsedTicks <= 0L) return;
        for (GreenhouseMatrixProductionGroup group : activeGroups.values()) {
            GreenhouseMatrixProductionProfile profile = group.profile();
            long harvestBudget = saturatingMultiply(harvestBudgetPerUnit, group.units());
            long maxPendingWork = saturatingMultiply(maxPendingWorkPerUnit, group.units());
            GreenhouseProductionEngine.GroupWorkWindow window = GreenhouseProductionEngine.accumulateGroup(
                    group.workRemainder(), elapsedTicks, profile.workPerTickPerUnit(),
                    (int) Math.min(Integer.MAX_VALUE, group.units()), profile.growthWork(),
                    harvestBudget, maxPendingWork);
            group.setWorkRemainder(window.availableWork());
            group.setPendingHarvests(window.requestedHarvests());
        }
    }

    public boolean rebuilding() { return rebuilding; }
    public int rebuildCursor() { return rebuildCursor; }
    public int rebuildTotal() { return rebuildingMembers.size(); }
    public int groupCount() { return activeGroups.size(); }
    public Collection<GreenhouseMatrixProductionGroup> groups() { return List.copyOf(activeGroups.values()); }

    public long totalPendingHarvests() {
        long total = 0L;
        for (GreenhouseMatrixProductionGroup group : activeGroups.values()) {
            long value = group.pendingHarvests();
            if (value > Long.MAX_VALUE - total) return Long.MAX_VALUE;
            total += value;
        }
        return total;
    }

    public CompoundTag save(HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();
        ListTag groups = new ListTag();
        for (GreenhouseMatrixProductionGroup group : activeGroups.values()) {
            GreenhouseMatrixProductionProfile profile = group.profile();
            if (group.workRemainder() <= 0L) continue;
            CompoundTag saved = new CompoundTag();
            saved.putInt("machineKind", profile.machineKind().ordinal());
            saved.put("seed", profile.seed().saveOptional(provider));
            saved.putInt("templateCount", profile.templateCount());
            saved.putString("definitionKey", profile.definitionKey());
            saved.putLong("recipeGeneration", profile.recipeGeneration());
            saved.putInt("selectedMultiplier", profile.selectedMultiplier());
            saved.putInt("structureMultiplier", profile.structureMultiplier());
            saved.putInt("fortuneLevel", profile.fortuneLevel());
            saved.putBoolean("creative", profile.creative());
            saved.putBoolean("overclocked", profile.overclocked());
            saved.putInt("energyPerHarvest", profile.energyPerHarvest());
            saved.putInt("fluidPerHarvest", profile.fluidPerHarvest());
            saved.putInt("matrixSpeed", profile.matrixSpeed());
            saved.putInt("matrixEfficiency", profile.matrixEfficiency());
            saved.putBoolean("seedConversion", profile.seedConversion());
            saved.putBoolean("essenceConversion", profile.essenceConversion());
            saved.putInt("growthWork", profile.growthWork());
            saved.putLong("workPerTickPerUnit", profile.workPerTickPerUnit());
            saved.putLong("workRemainder", group.workRemainder());
            groups.add(saved);
        }
        root.put("groups", groups);
        return root;
    }

    public void load(CompoundTag root, HolderLookup.Provider provider, Consumer<String> warning) {
        activeGroups = new LinkedHashMap<>();
        buildingGroups = new LinkedHashMap<>();
        restoredWork.clear();
        ListTag groups = root.getList("groups", Tag.TAG_COMPOUND);
        for (int index = 0; index < groups.size(); index++) {
            try {
                CompoundTag saved = groups.getCompound(index);
                ItemStack seed = saved.contains("seed", Tag.TAG_COMPOUND)
                        ? ItemStack.parseOptional(provider, saved.getCompound("seed")) : ItemStack.EMPTY;
                long work = saved.getLong("workRemainder");
                GreenhouseMatrixProductionProfile.MachineKind[] kinds = GreenhouseMatrixProductionProfile.MachineKind.values();
                int kindIndex = saved.getInt("machineKind");
                if (seed.isEmpty() || work <= 0L || kindIndex < 0 || kindIndex >= kinds.length) {
                    warning.accept("Skipped invalid matrix simulation group " + index);
                    continue;
                }
                GreenhouseMatrixProductionProfile profile = new GreenhouseMatrixProductionProfile(
                        kinds[kindIndex], seed, saved.getInt("templateCount"), saved.getString("definitionKey"),
                        GreenhouseCropResolver.cacheGeneration(), saved.getInt("selectedMultiplier"),
                        saved.getInt("structureMultiplier"), saved.getInt("fortuneLevel"),
                        saved.getBoolean("creative"), saved.getBoolean("overclocked"),
                        saved.getInt("energyPerHarvest"), saved.getInt("fluidPerHarvest"),
                        saved.getInt("matrixSpeed"), saved.getInt("matrixEfficiency"),
                        saved.getBoolean("seedConversion"), saved.getBoolean("essenceConversion"),
                        saved.getInt("growthWork"), saved.getLong("workPerTickPerUnit"));
                restoredWork.merge(profile, work, Math::max);
            } catch (RuntimeException exception) {
                warning.accept("Failed to decode matrix simulation group " + index + ": " + exception.getMessage());
            }
        }
    }

    private static long saturatingMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) return 0L;
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }
}
