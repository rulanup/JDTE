package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.SharpnessUpgradeItem;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class UpgradeCardInsertionHelper {
    private UpgradeCardInsertionHelper() {
    }

    public static InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()
                || !(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof BaseMachineBE)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return insertAll(context.getLevel(), context.getClickedPos(), player, context.getItemInHand()) > 0
                ? InteractionResult.SUCCESS
                : InteractionResult.PASS;
    }

    public static boolean isUpgradeCard(ItemStack stack) {
        return stack.getItem() instanceof UpgradeCardItem
                || stack.getItem() instanceof LootingUpgradeItem
                || stack.getItem() instanceof SharpnessUpgradeItem
                || BioFactoryUpgradeItemStackHandler.getProductivityTier(stack) > 0;
    }

    public static int insertAll(Level level, BlockPos pos, Player player, ItemStack heldStack) {
        if (heldStack.isEmpty() || !isUpgradeCard(heldStack)) {
            return 0;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BaseMachineBE machine)) {
            return 0;
        }

        if (machine instanceof BioCrusherBE crusher) {
            if (heldStack.getItem() instanceof LootingUpgradeItem) {
                return insertStacked(crusher.getLootingHandler(), player, heldStack,
                        JDTEConfig.COMMON.maxLootingUpgrades.get());
            }
            if (heldStack.getItem() instanceof SharpnessUpgradeItem) {
                return insertStacked(crusher.getSharpnessHandler(), player, heldStack,
                        JDTEConfig.COMMON.maxSharpnessUpgrades.get());
            }
        }

        if (!(heldStack.getItem() instanceof UpgradeCardItem)
                && !(machine instanceof LootFabricatorBE && heldStack.getItem() instanceof LootingUpgradeItem)
                && !(machine instanceof BioFactoryBE && (heldStack.getItem() instanceof LootingUpgradeItem
                || BioFactoryUpgradeItemStackHandler.getProductivityTier(heldStack) > 0))) {
            return 0;
        }

        UpgradeItemStackHandler handler = UpgradeHelper.getUpgradeHandler(machine);
        int inserted = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (heldStack.isEmpty() && !player.getAbilities().instabuild) {
                break;
            }
            if (handler.insertItem(slot, heldStack.copyWithCount(1), false).isEmpty()) {
                if (!player.getAbilities().instabuild) {
                    heldStack.shrink(1);
                }
                inserted++;
            }
        }
        return inserted;
    }

    private static int insertStacked(ItemStackHandler handler, Player player, ItemStack heldStack, int maxCount) {
        int currentCount = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            currentCount += handler.getStackInSlot(slot).getCount();
        }
        int available = Math.max(0, maxCount - currentCount);
        if (available == 0) {
            return 0;
        }

        ItemStack toInsert = heldStack.copyWithCount(Math.min(heldStack.getCount(), available));
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, toInsert, false);
        int inserted = toInsert.getCount() - remainder.getCount();
        if (inserted > 0 && !player.getAbilities().instabuild) {
            heldStack.shrink(inserted);
        }
        return inserted;
    }
}
