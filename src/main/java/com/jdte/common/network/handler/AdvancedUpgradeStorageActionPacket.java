package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler;
import com.jdte.common.items.AdvancedUpgradeStorageItem;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.SharpnessUpgradeItem;
import com.jdte.common.items.UpgradeStorageItem;
import com.jdte.common.network.data.AdvancedUpgradeStorageActionPayload;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import com.jdte.setup.JDTEConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AdvancedUpgradeStorageActionPacket {
    private AdvancedUpgradeStorageActionPacket() {
    }

    public static void handle(AdvancedUpgradeStorageActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (!(player.containerMenu instanceof BaseMachineContainer menu)) {
                return;
            }
            BaseMachineBE baseMachineBE = menu.baseMachineBE;
            if (baseMachineBE == null) {
                return;
            }
            ItemStack storageStack = AdvancedUpgradeStorageItem.findStorage(player);
            if (storageStack.isEmpty()) {
                return;
            }
            AdvancedUpgradeStorageHandler storageHandler = new AdvancedUpgradeStorageHandler(storageStack);
            int slot = payload.slotIndex();
            if (slot < 0 || slot >= storageHandler.getSlots()) {
                return;
            }

            int action = payload.action();
            if (action == AdvancedUpgradeStorageActionPayload.ACTION_INSERT_ONE || action == AdvancedUpgradeStorageActionPayload.ACTION_INSERT_MAX) {
                ItemStack sourceStack = storageHandler.getStackInSlot(slot);
                if (sourceStack.isEmpty()) {
                    return;
                }
                int toInsert = (action == AdvancedUpgradeStorageActionPayload.ACTION_INSERT_MAX) ? sourceStack.getCount() : 1;
                int inserted = insertIntoMachine(baseMachineBE, storageHandler, slot, toInsert);
                if (inserted > 0) {
                    baseMachineBE.setChanged();
                    menu.broadcastChanges();
                    player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.2F);
                }
            } else if (action == AdvancedUpgradeStorageActionPayload.ACTION_PICKUP_CURSOR) {
                ItemStack carried = menu.getCarried();
                if (carried.isEmpty()) {
                    ItemStack extracted = storageHandler.extractItem(slot, 1, false);
                    if (!extracted.isEmpty()) {
                        menu.setCarried(extracted);
                        menu.broadcastChanges();
                        player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.0F);
                    }
                }
            } else if (action == AdvancedUpgradeStorageActionPayload.ACTION_WITHDRAW_INVENTORY) {
                ItemStack sourceStack = storageHandler.getStackInSlot(slot);
                if (!sourceStack.isEmpty()) {
                    int maxWithdraw = Math.min(sourceStack.getCount(), sourceStack.getMaxStackSize());
                    ItemStack simulated = storageHandler.extractItem(slot, maxWithdraw, true);
                    if (!simulated.isEmpty()) {
                        ItemStack remainder = ItemHandlerHelper.insertItem(new InvWrapper(player.getInventory()), simulated, false);
                        int actualExtracted = simulated.getCount() - remainder.getCount();
                        if (actualExtracted > 0) {
                            storageHandler.extractItem(slot, actualExtracted, false);
                            menu.broadcastChanges();
                            player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.0F);
                        }
                    }
                }
            } else if (action == AdvancedUpgradeStorageActionPayload.ACTION_DEPOSIT_CURSOR || action == AdvancedUpgradeStorageActionPayload.ACTION_DEPOSIT_CURSOR_ONE) {
                ItemStack carried = menu.getCarried();
                if (!carried.isEmpty() && UpgradeStorageItem.isAllowedUpgrade(carried)) {
                    int amountToDeposit = (action == AdvancedUpgradeStorageActionPayload.ACTION_DEPOSIT_CURSOR_ONE) ? 1 : carried.getCount();
                    ItemStack toDeposit = carried.copyWithCount(amountToDeposit);
                    ItemStack remainder = storageHandler.insertItem(slot, toDeposit, false);
                    int deposited = amountToDeposit - remainder.getCount();
                    if (deposited > 0) {
                        carried.shrink(deposited);
                        menu.setCarried(carried);
                        menu.broadcastChanges();
                        player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 0.8F);
                    }
                }
            }
        });
    }

    public static int insertIntoMachine(BaseMachineBE machine, AdvancedUpgradeStorageHandler storageHandler, int slot, int toInsert) {
        int totalInserted = 0;
        for (int i = 0; i < toInsert; i++) {
            ItemStack single = storageHandler.getStackInSlot(slot).copyWithCount(1);
            if (single.isEmpty()) {
                break;
            }

            // Check BioCrusher special slots first
            if (machine instanceof BioCrusherBE crusher) {
                if (single.getItem() instanceof LootingUpgradeItem) {
                    if (tryInsertIntoSpecialHandler(crusher.getLootingHandler(), JDTEConfig.COMMON.maxLootingUpgrades.get(), single)) {
                        storageHandler.extractItem(slot, 1, false);
                        totalInserted++;
                        continue;
                    }
                }
                if (single.getItem() instanceof SharpnessUpgradeItem) {
                    if (tryInsertIntoSpecialHandler(crusher.getSharpnessHandler(), JDTEConfig.COMMON.maxSharpnessUpgrades.get(), single)) {
                        storageHandler.extractItem(slot, 1, false);
                        totalInserted++;
                        continue;
                    }
                }
            }

            // Standard machine upgrade handler
            UpgradeItemStackHandler handler = UpgradeHelper.getUpgradeHandler(machine);
            if (handler == null) {
                break;
            }

            boolean placed = false;
            for (int s = 0; s < handler.getSlots(); s++) {
                if (handler.insertItem(s, single, true).isEmpty()) {
                    ItemStack extracted = storageHandler.extractItem(slot, 1, false);
                    handler.insertItem(s, extracted, false);
                    totalInserted++;
                    placed = true;
                    break;
                }
            }

            if (!placed) {
                break;
            }
        }
        return totalInserted;
    }

    private static boolean tryInsertIntoSpecialHandler(ItemStackHandler handler, int maxLimit, ItemStack single) {
        int current = 0;
        for (int s = 0; s < handler.getSlots(); s++) {
            current += handler.getStackInSlot(s).getCount();
        }
        if (current >= maxLimit) {
            return false;
        }
        for (int s = 0; s < handler.getSlots(); s++) {
            if (handler.insertItem(s, single, false).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
