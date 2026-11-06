package com.jdte.gametest;

import com.jdte.common.containers.UpgradeStorageContainer;
import com.jdte.common.containers.handlers.UpgradeStorageHandler;
import com.jdte.common.items.UpgradeStorageItem;
import com.jdte.setup.JDTEItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class UpgradeStorageGameTests {
    private static void equal(GameTestHelper h, int actual, int expected, String label) {
        h.assertTrue(actual == expected, label + ": expected " + expected + ", got " + actual);
    }
    @GameTest(template = "mineral_empty")
    public static void upgradeStorageStacksNativeUpgradesAndSurvivesItemSave(GameTestHelper h) {
        var bag = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
        var inventory = bag.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(AssertionError::new);
        equal(h, inventory.getSlots(), 20, "source 4x5 inventory");
        var upgrade = new ItemStack(com.direwolf20.justdirethings.setup.Registration.UPGRADE_SMELTER.get(), 64);
        h.assertTrue(inventory.insertItem(0, upgrade, false).isEmpty(), "64 native upgrades fit one slot");
        var restored = ItemStack.of(bag.save(new net.minecraft.nbt.CompoundTag()));
        var contents = restored.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(AssertionError::new);
        equal(h, contents.getStackInSlot(0).getCount(), 64, "saved compressed upgrade count");
        h.assertTrue(contents.getStackInSlot(0).is(upgrade.getItem()), "saved upgrade identity");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void upgradeStorageRejectsItemsAndNestedBags(GameTestHelper h) {
        var bag = new ItemStack(JDTEItems.UPGRADE_STORAGE.get()); var inventory = new UpgradeStorageHandler(bag);
        equal(h, inventory.insertItem(0, new ItemStack(Items.DIAMOND, 64), false).getCount(), 64, "ordinary items rejected");
        h.assertTrue(!inventory.insertItem(0, bag.copy(), false).isEmpty(), "recursive storage rejected");
        h.assertTrue(!inventory.isItemValid(0, new ItemStack(Items.STICK)), "capability validation");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void upgradeStorageSimulationDoesNotChangeSavedContents(GameTestHelper h) {
        var bag = new ItemStack(JDTEItems.UPGRADE_STORAGE.get()); var inventory = new UpgradeStorageHandler(bag);
        h.assertTrue(inventory.insertItem(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 64), true).isEmpty(), "simulate accepted insertion");
        h.assertTrue(!bag.hasTag(), "simulation leaves blank item NBT unchanged");
        inventory.insertItem(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 64), false);
        equal(h, inventory.extractItem(0, 20, true).getCount(), 20, "simulate extraction");
        equal(h, inventory.getStackInSlot(0).getCount(), 64, "simulation preserves count");
        equal(h, new UpgradeStorageHandler(bag).getStackInSlot(0).getCount(), 64, "simulation preserves saved count");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void upgradeStorageViewsShareCurrentItemState(GameTestHelper h) {
        var bag = new ItemStack(JDTEItems.UPGRADE_STORAGE.get()); var first = new UpgradeStorageHandler(bag);
        var second = new UpgradeStorageHandler(bag);
        first.insertItem(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 64), false);
        equal(h, second.extractItem(0, 20, false).getCount(), 20, "second wrapper sees first insertion");
        equal(h, first.getStackInSlot(0).getCount(), 44, "first wrapper refreshes after external extraction");
        second.insertItem(1, new ItemStack(JDTEItems.FILTER_UPGRADE.get(), 5), false);
        first.insertItem(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 10), false);
        equal(h, second.getStackInSlot(0).getCount(), 54, "current state is not overwritten");
        equal(h, first.getStackInSlot(1).getCount(), 5, "other slots preserved");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void upgradeStorageKeepsDifferentUpgradeNbtSeparate(GameTestHelper h) {
        var inventory = new UpgradeStorageHandler(new ItemStack(JDTEItems.UPGRADE_STORAGE.get()));
        var a = new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 20); a.getOrCreateTag().putString("test", "a");
        var b = a.copy(); b.getTag().putString("test", "b");
        inventory.insertItem(0, a, false);
        equal(h, inventory.insertItem(0, b, false).getCount(), 20, "different NBT does not merge");
        inventory.insertItem(1, b, false);
        h.assertTrue(inventory.getStackInSlot(0).getTag().getString("test").equals("a"), "first upgrade NBT");
        h.assertTrue(inventory.getStackInSlot(1).getTag().getString("test").equals("b"), "second upgrade NBT");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void upgradeStorageMenuProtectsTheBoundBag(GameTestHelper h) {
        var player = h.makeMockSurvivalPlayer(); var bag = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, bag);
        var menu = new UpgradeStorageContainer(1, player.getInventory(), player, bag, InteractionHand.MAIN_HAND);
        int held = UpgradeStorageContainer.SLOTS + 27 + player.getInventory().selected;
        menu.clicked(held, 0, ClickType.PICKUP, player);
        h.assertTrue(menu.getCarried().isEmpty() && player.getMainHandItem() == bag, "bound bag cannot be picked up");
        h.assertTrue(menu.quickMoveStack(player, held).isEmpty(), "bound bag cannot be shift moved");
        player.setItemInHand(InteractionHand.MAIN_HAND, bag.copy());
        h.assertTrue(!menu.stillValid(player), "replacement bag does not inherit an old menu");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void upgradeStorageOffhandSwapCannotRemoveTheBoundBag(GameTestHelper h) {
        var player = h.makeMockSurvivalPlayer(); var bag = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
        player.setItemInHand(InteractionHand.OFF_HAND, bag);
        var menu = new UpgradeStorageContainer(1, player.getInventory(), player, bag, InteractionHand.OFF_HAND);
        menu.clicked(20, 40, ClickType.SWAP, player);
        h.assertTrue(player.getOffhandItem() == bag && menu.stillValid(player), "offhand bag protected from F swap");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void upgradeStorageShiftTransfersPersistAndConserveCounts(GameTestHelper h) {
        var player = h.makeMockSurvivalPlayer(); var bag = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, bag);
        player.getInventory().setItem(9, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 64));
        var menu = new UpgradeStorageContainer(1, player.getInventory(), player, bag, InteractionHand.MAIN_HAND);
        menu.clicked(20, 0, ClickType.QUICK_MOVE, player);
        equal(h, new UpgradeStorageHandler(bag).getStackInSlot(0).getCount(), 64, "shift insertion saved");
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        equal(h, player.getInventory().countItem(JDTEItems.CAPACITY_UPGRADE.get()), 64, "shift extraction conserved");
        h.assertTrue(new UpgradeStorageHandler(bag).getStackInSlot(0).isEmpty(), "shift extraction saved");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void upgradeStorageTooltipWorksWithoutClientClasses(GameTestHelper h) {
        var bag = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
        new UpgradeStorageHandler(bag).insertItem(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 3), false);
        var tooltip = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        bag.getItem().appendHoverText(bag, h.getLevel(), tooltip, net.minecraft.world.item.TooltipFlag.Default.NORMAL);
        h.assertTrue(tooltip.size() >= 3, "dedicated-server tooltip has contents summary");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void upgradeStorageUnstackableShiftTransferUsesLegalPlayerStacks(GameTestHelper h) {
        var player = h.makeMockSurvivalPlayer(); var bag = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, bag);
        var item = com.direwolf20.justdirethings.setup.Registration.UPGRADE_SMELTER.get();
        var menu = new UpgradeStorageContainer(1, player.getInventory(), player, bag, InteractionHand.MAIN_HAND);
        menu.handler.insertItem(0, new ItemStack(item, 64), false);
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        equal(h, player.getInventory().countItem(item), 35, "one unstackable upgrade per free player slot");
        equal(h, new UpgradeStorageHandler(bag).getStackInSlot(0).getCount(), 29, "remainder stays in bag");
        h.assertTrue(player.getInventory().items.stream().filter(st -> st.is(item)).allMatch(st -> st.getCount() == 1), "player stack sizes stay legal");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void upgradeStorageNbtCannotResizeTheInventory(GameTestHelper h) {
        var bag = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
        var data = new net.minecraft.nbt.CompoundTag(); data.putInt("Size", Integer.MAX_VALUE);
        bag.getOrCreateTag().put(UpgradeStorageHandler.NBT_KEY, data);
        equal(h, new UpgradeStorageHandler(bag).getSlots(), 20, "untrusted NBT size is bounded");
        h.succeed();
    }

}
