package com.jdte.common.containers;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class LargePortableContainerBinding {
    private final ItemStack boundStack;
    private final Supplier<ItemStack> stackResolver;
    private final Predicate<ItemStack> validator;

    public LargePortableContainerBinding(ItemStack boundStack, Supplier<ItemStack> stackResolver,
                                         Predicate<ItemStack> validator) {
        this.boundStack = Objects.requireNonNull(boundStack, "boundStack");
        this.stackResolver = Objects.requireNonNull(stackResolver, "stackResolver");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    public ItemStack boundStack() {
        return boundStack;
    }

    public ItemStack currentStack() {
        return stackResolver.get();
    }

    public boolean isStillValid() {
        ItemStack currentStack = currentStack();
        return currentStack == boundStack && !currentStack.isEmpty() && validator.test(currentStack);
    }
}
