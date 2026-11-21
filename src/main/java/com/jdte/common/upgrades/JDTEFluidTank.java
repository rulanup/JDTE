package com.jdte.common.upgrades;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

import java.util.function.Predicate;

public class JDTEFluidTank extends FluidStacksResourceHandler {
    private final Predicate<FluidStack> validator;

    public JDTEFluidTank(int capacity) {
        this(capacity, stack -> true);
    }

    public JDTEFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(1, capacity);
        this.validator = validator;
    }

    public FluidStack getFluid() { return stacks.getFirst(); }
    public int getFluidAmount() { return getAmountAsInt(0); }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public boolean isFluidValid(FluidStack stack) { return validator.test(stack); }
    public void setFluid(FluidStack stack) { stacks.set(0, stack.copy()); }

    @Override public boolean isValid(int index, FluidResource resource) {
        return index == 0 && !resource.isEmpty() && validator.test(resource.toStack(1));
    }

    public int insertFluid(FluidStack stack) {
        if (stack.isEmpty()) return 0;
        try (var transaction = Transaction.openRoot()) {
            int inserted = insert(0, FluidResource.of(stack), stack.getAmount(), transaction);
            transaction.commit();
            return inserted;
        }
    }

    public boolean canStore(FluidStack stack) {
        if (stack.isEmpty()) return true;
        try (var transaction = Transaction.openRoot()) {
            return insert(0, FluidResource.of(stack), stack.getAmount(), transaction) == stack.getAmount();
        }
    }

    public FluidStack extractFluid(int amount) {
        var resource = getResource(0);
        if (amount <= 0 || resource.isEmpty()) return FluidStack.EMPTY;
        try (var transaction = Transaction.openRoot()) {
            int extracted = extract(0, resource, amount, transaction);
            transaction.commit();
            return resource.toStack(extracted);
        }
    }

    @Override protected void onContentsChanged(int index, FluidStack previousContents) {
        onContentsChanged();
    }
    protected void onContentsChanged() {}

    @Override public void serialize(ValueOutput output) {
        if (!getFluid().isEmpty()) output.store("Fluid", FluidStack.CODEC, getFluid());
    }

    @Override public void deserialize(ValueInput input) {
        setFluid(input.read("Fluid", FluidStack.CODEC).orElse(FluidStack.EMPTY));
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (!getFluid().isEmpty()) tag.store("Fluid", FluidStack.CODEC, provider.createSerializationContext(NbtOps.INSTANCE), getFluid());
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        setFluid(nbt.read("Fluid", FluidStack.CODEC, provider.createSerializationContext(NbtOps.INSTANCE)).orElse(FluidStack.EMPTY));
    }
}
