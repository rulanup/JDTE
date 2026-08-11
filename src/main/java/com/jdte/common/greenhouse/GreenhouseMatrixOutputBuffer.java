package com.jdte.common.greenhouse;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Compact output storage for a matrix. One entry stores one item/component identity and a long count;
 * the item capability exposes those entries as extraction-only virtual slots.
 */
public final class GreenhouseMatrixOutputBuffer {
    private final int maxTypes;
    private final long maxTotal;
    private final List<Entry> entries = new ArrayList<>();
    private final IItemHandler itemView = new OutputView();
    private long totalCount;

    public GreenhouseMatrixOutputBuffer(int maxTypes, long maxTotal) {
        if (maxTypes < 1) throw new IllegalArgumentException("maxTypes must be positive");
        if (maxTotal < 0L) throw new IllegalArgumentException("maxTotal must not be negative");
        this.maxTypes = maxTypes;
        this.maxTotal = maxTotal;
    }

    /**
     * @return the amount that could not be accepted
     */
    public long insert(ItemStack stack, long amount, boolean simulate) {
        if (amount <= 0L) return 0L;
        if (stack.isEmpty()) return amount;
        int index = find(stack);
        if (index < 0 && entries.size() >= maxTypes) return amount;

        long accepted = Math.min(amount, Math.max(0L, maxTotal - totalCount));
        if (accepted <= 0L) return amount;
        if (!simulate) {
            if (index >= 0) {
                Entry entry = entries.get(index);
                entry.count = saturatingAdd(entry.count, accepted);
            } else {
                entries.add(new Entry(stack.copyWithCount(1), accepted));
            }
            totalCount = saturatingAdd(totalCount, accepted);
        }
        return amount - accepted;
    }

    public long count(ItemStack stack) {
        int index = find(stack);
        return index < 0 ? 0L : entries.get(index).count;
    }

    public int distinctTypes() {
        return entries.size();
    }

    public long totalCount() {
        return totalCount;
    }

    public long remainingCapacity() {
        return Math.max(0L, maxTotal - totalCount);
    }

    public ItemStack prototypeAt(int entry) {
        return entry >= 0 && entry < entries.size() ? entries.get(entry).prototype.copy() : ItemStack.EMPTY;
    }

    public long amountAt(int entry) {
        return entry >= 0 && entry < entries.size() ? entries.get(entry).count : 0L;
    }

    public long removeAmount(int entry, long amount) {
        if (entry < 0 || entry >= entries.size() || amount <= 0L) return 0L;
        Entry stored = entries.get(entry);
        long removed = Math.min(stored.count, amount);
        stored.count -= removed;
        totalCount -= removed;
        if (stored.count == 0L) entries.remove(entry);
        return removed;
    }

    public List<GreenhouseMatrixDropGenerator.Drop> drainAll() {
        List<GreenhouseMatrixDropGenerator.Drop> drained = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            if (entry.count > 0L && !entry.prototype.isEmpty()) {
                drained.add(new GreenhouseMatrixDropGenerator.Drop(entry.prototype, entry.count));
            }
        }
        entries.clear();
        totalCount = 0L;
        return List.copyOf(drained);
    }

    public List<GreenhouseMatrixDropGenerator.Drop> snapshotDrops() {
        List<GreenhouseMatrixDropGenerator.Drop> snapshot = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            if (entry.count > 0L && !entry.prototype.isEmpty()) {
                snapshot.add(new GreenhouseMatrixDropGenerator.Drop(entry.prototype.copy(), entry.count));
            }
        }
        return List.copyOf(snapshot);
    }

    public IItemHandler itemView() {
        return itemView;
    }

    /** Atomically accepts all drops or leaves the buffer unchanged. Returns a positive rejected amount on failure. */
    public long insertBatch(List<GreenhouseMatrixDropGenerator.Drop> drops, boolean simulate) {
        long added = 0L;
        List<ItemStack> newTypes = new ArrayList<>();
        for (GreenhouseMatrixDropGenerator.Drop drop : drops) {
            ItemStack stack = drop.stack();
            long amount = drop.amount();
            if (stack.isEmpty() || amount <= 0L) continue;
            added = saturatingAdd(added, amount);
            if (find(stack) >= 0 || contains(newTypes, stack)) continue;
            newTypes.add(stack.copyWithCount(1));
        }
        long available = Math.max(0L, maxTotal - totalCount);
        if (added > available) return added - available;
        if (entries.size() + newTypes.size() > maxTypes) return Math.max(1L, added);
        if (!simulate) {
            for (GreenhouseMatrixDropGenerator.Drop drop : drops) {
                long remainder = insert(drop.stack(), drop.amount(), false);
                if (remainder != 0L) throw new IllegalStateException("Validated matrix output batch no longer fits");
            }
        }
        return 0L;
    }

    /**
     * Applies all crafting replacements to a private candidate and publishes it only when every
     * input, type and total-capacity check succeeds.
     */
    public boolean applyCraftingBatch(List<Transformation> transformations) {
        if (transformations == null || transformations.isEmpty()) return false;
        GreenhouseMatrixOutputBuffer candidate = new GreenhouseMatrixOutputBuffer(maxTypes, maxTotal);
        for (Entry entry : entries) {
            if (candidate.insert(entry.prototype, entry.count, false) != 0L) {
                throw new IllegalStateException("Existing matrix output does not fit its own limits");
            }
        }
        for (Transformation transformation : transformations) {
            if (!candidate.removeExact(transformation.input, transformation.inputAmount)) return false;
        }
        for (Transformation transformation : transformations) {
            if (candidate.insert(transformation.output, transformation.outputAmount, false) != 0L) return false;
        }

        entries.clear();
        for (Entry entry : candidate.entries) {
            entries.add(new Entry(entry.prototype.copy(), entry.count));
        }
        totalCount = candidate.totalCount;
        return true;
    }

    public CompoundTag save(HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();
        ListTag savedEntries = new ListTag();
        for (Entry entry : entries) {
            if (entry.count <= 0L || entry.prototype.isEmpty()) continue;
            CompoundTag savedEntry = new CompoundTag();
            savedEntry.put("stack", entry.prototype.saveOptional(provider));
            savedEntry.putLong("count", entry.count);
            savedEntries.add(savedEntry);
        }
        root.put("entries", savedEntries);
        return root;
    }

    public void load(CompoundTag root, HolderLookup.Provider provider, Consumer<String> warning) {
        entries.clear();
        totalCount = 0L;
        ListTag savedEntries = root.getList("entries", Tag.TAG_COMPOUND);
        for (int index = 0; index < savedEntries.size(); index++) {
            try {
                CompoundTag savedEntry = savedEntries.getCompound(index);
                long count = savedEntry.getLong("count");
                ItemStack stack = savedEntry.contains("stack", Tag.TAG_COMPOUND)
                        ? ItemStack.parseOptional(provider, savedEntry.getCompound("stack"))
                        : ItemStack.EMPTY;
                if (count <= 0L || stack.isEmpty()) {
                    warning.accept("Skipped invalid matrix output entry " + index);
                    continue;
                }
                long remainder = insert(stack, count, false);
                if (remainder > 0L) warning.accept("Matrix output entry " + index + " exceeded buffer limits");
            } catch (RuntimeException exception) {
                warning.accept("Failed to decode matrix output entry " + index + ": " + exception.getMessage());
            }
        }
    }

    private int find(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        for (int index = 0; index < entries.size(); index++) {
            if (ItemStack.isSameItemSameComponents(entries.get(index).prototype, stack)) return index;
        }
        return -1;
    }

    private boolean removeExact(ItemStack stack, long amount) {
        if (stack == null || stack.isEmpty() || amount <= 0L) return false;
        int index = find(stack);
        if (index < 0) return false;
        Entry entry = entries.get(index);
        if (entry.count < amount) return false;
        entry.count -= amount;
        totalCount -= amount;
        if (entry.count == 0L) entries.remove(index);
        return true;
    }

    private static boolean contains(List<ItemStack> stacks, ItemStack target) {
        for (ItemStack stack : stacks) {
            if (ItemStack.isSameItemSameComponents(stack, target)) return true;
        }
        return false;
    }

    private ItemStack extract(int slot, int amount, boolean simulate) {
        if (slot < 0 || slot >= entries.size() || amount <= 0) return ItemStack.EMPTY;
        Entry entry = entries.get(slot);
        int extracted = (int) Math.min(entry.count, amount);
        if (extracted <= 0) return ItemStack.EMPTY;
        ItemStack result = entry.prototype.copyWithCount(extracted);
        if (!simulate) {
            entry.count -= extracted;
            totalCount -= extracted;
            if (entry.count == 0L) entries.remove(slot);
        }
        return result;
    }

    private static long saturatingAdd(long left, long right) {
        if (right > Long.MAX_VALUE - left) return Long.MAX_VALUE;
        return left + right;
    }

    private static final class Entry {
        private final ItemStack prototype;
        private long count;

        private Entry(ItemStack prototype, long count) {
            this.prototype = prototype;
            this.count = count;
        }
    }

    public record Transformation(ItemStack input, long inputAmount, ItemStack output, long outputAmount) {
        public Transformation {
            if (input == null || input.isEmpty()) throw new IllegalArgumentException("input must not be empty");
            if (output == null || output.isEmpty()) throw new IllegalArgumentException("output must not be empty");
            if (inputAmount <= 0L) throw new IllegalArgumentException("inputAmount must be positive");
            if (outputAmount <= 0L) throw new IllegalArgumentException("outputAmount must be positive");
            input = input.copyWithCount(1);
            output = output.copyWithCount(1);
        }

        @Override
        public ItemStack input() {
            return input.copy();
        }

        @Override
        public ItemStack output() {
            return output.copy();
        }
    }

    private final class OutputView implements IItemHandler {
        @Override
        public int getSlots() {
            return entries.size();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (slot < 0 || slot >= entries.size()) return ItemStack.EMPTY;
            Entry entry = entries.get(slot);
            return entry.prototype.copyWithCount((int) Math.min(entry.count, entry.prototype.getMaxStackSize()));
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return extract(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < entries.size() ? entries.get(slot).prototype.getMaxStackSize() : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }
}
