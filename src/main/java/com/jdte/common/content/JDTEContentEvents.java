package com.jdte.common.content;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Runtime boundaries for content disabled through the common content config. */
public final class JDTEContentEvents {
    private JDTEContentEvents() {
    }

    /** Prevents a disabled block from being introduced by a placement event. */
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        JDTEContentControl control = JDTEContentControl.current();
        if (!control.isBlockEnabled(event.getPlacedBlock().getBlock())) {
            event.setCanceled(true);
        }
    }

    /** Prevents opening or otherwise using a disabled block. */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!JDTEContentControl.current().isBlockEnabled(
                event.getLevel().getBlockState(event.getPos()).getBlock())) {
            event.setCanceled(true);
        }
    }

    /** Removes disabled block items from both the parent and search creative lists. */
    public static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        JDTEContentControl control = JDTEContentControl.current();
        removeDisabledEntries(event, control, event.getParentEntries(),
                CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
        removeDisabledEntries(event, control, event.getSearchEntries(),
                CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
    }

    private static void removeDisabledEntries(BuildCreativeModeTabContentsEvent event,
                                              JDTEContentControl control,
                                              Collection<ItemStack> entries,
                                              CreativeModeTab.TabVisibility visibility) {
        // The event exposes immutable views; copy before removing from the backing sets.
        List<ItemStack> snapshot = new ArrayList<>();
        entries.forEach(snapshot::add);
        for (ItemStack stack : snapshot) {
            if (stack.getItem() instanceof BlockItem blockItem
                    && !control.isBlockEnabled(blockItem.getBlock())) {
                event.remove(stack, visibility);
            }
        }
    }
}
