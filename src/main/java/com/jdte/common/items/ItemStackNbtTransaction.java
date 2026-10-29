package com.jdte.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;
import java.util.function.Predicate;

/** Stage on a real capability-bearing copy; commit only if callbacks left the original unchanged. */
final class ItemStackNbtTransaction {
    private final CompoundTag before;
    private final ItemStack staged;
    private ItemStackNbtTransaction(CompoundTag before,ItemStack staged){this.before=before;this.staged=staged;}
    static Optional<ItemStackNbtTransaction> prepare(ItemStack source,Predicate<ItemStack> payment){
        var before=source.save(new CompoundTag());var staged=source.copy();
        return payment.test(staged)?Optional.of(new ItemStackNbtTransaction(before,staged)):Optional.empty();
    }
    boolean commit(ItemStack source){if(!source.save(new CompoundTag()).equals(before))return false;source.setTag(staged.hasTag()?staged.getTag().copy():null);return true;}
}
