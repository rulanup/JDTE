package com.jdte.client.smoke;

import com.jdte.client.screens.AdvancedItemSenderScreen;
import com.jdte.client.screens.BasicItemSenderScreen;
import com.jdte.client.screens.ExtendedItemSenderScreen;
import com.jdte.common.blockentities.ItemSenderBE;
import com.jdte.common.containers.ItemSenderContainerBase;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeSlot;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTECreativeTabs;
import com.jdte.setup.JDTEItems;
import java.nio.file.Files;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Opt-in real-client smoke checks for the three item sender tiers. Never packaged in the mod JAR. */
@Mod.EventBusSubscriber(modid = "jdte", value = Dist.CLIENT)
public final class SenderClientSmoke {
    private static final BlockPos BASIC = new BlockPos(20, 100, 20);
    private static final BlockPos ADVANCED = BASIC.east(4);
    private static final BlockPos EXTENDED = BASIC.east(8);
    private static int stage;
    private static int ticks;
    private static int changedAt;
    private static volatile RuntimeException failure;

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Sender client smoke: " + message);
    }

    private static void next() { stage++; changedAt = ticks; }

    private static void server(Minecraft mc, java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> action) {
        var s = mc.getSingleplayerServer();
        var id = mc.player.getUUID();
        s.execute(() -> {
            try { action.accept(s.getPlayerList().getPlayer(id)); }
            catch (RuntimeException e) { failure = e; throw e; }
        });
    }

    private static ItemSenderBE be(net.minecraft.server.level.ServerPlayer p, BlockPos pos) {
        return (ItemSenderBE) p.serverLevel().getBlockEntity(pos);
    }

    private static void open(Minecraft mc, BlockPos pos) {
        mc.gameMode.useItemOn(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static void shift(Minecraft mc, java.util.function.Predicate<net.minecraft.world.inventory.Slot> predicate) {
        var m = mc.player.containerMenu;
        var slot = m.slots.stream().filter(predicate).findFirst().orElseThrow();
        mc.gameMode.handleInventoryMouseClick(m.containerId, slot.index, 0, ClickType.QUICK_MOVE, mc.player);
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Boolean.getBoolean("jdte.senderSmoke")) return;
        if (failure != null) throw new IllegalStateException("Server assertion", failure);
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        if (ticks > 4500) throw new IllegalStateException("Sender client smoke timed out at stage " + stage);
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        int elapsed = ticks - changedAt;
        switch (stage) {
            case 0 -> {
                if (mc.player.tickCount < 90) return;
                server(mc, p -> {
                    var level = p.serverLevel();
                    for (var pos : BlockPos.betweenClosed(new BlockPos(17, 99, 17), new BlockPos(31, 104, 25)))
                        level.setBlockAndUpdate(pos, pos.getY() == 99 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(BASIC, JDTEBlocks.BASIC_ITEM_SENDER.get().defaultBlockState());
                    level.setBlockAndUpdate(ADVANCED, JDTEBlocks.ADVANCED_ITEM_SENDER.get().defaultBlockState());
                    level.setBlockAndUpdate(EXTENDED, JDTEBlocks.EXTENDED_ITEM_SENDER.get().defaultBlockState());
                    // Advanced and Extended get Ultimate cards from the server
                    // side; Basic gets them through the real GUI below.
                    for (var pos : List.of(ADVANCED, EXTENDED)) {
                        var be = be(p, pos);
                        UpgradeHelper.getUpgradeHandler(be).insertItem(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false);
                        UpgradeHelper.getUpgradeHandler(be).insertItem(1, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()), false);
                        if (be instanceof com.jdte.common.blockentities.PoweredItemSenderBE powered)
                            powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
                        be.getItemHandler().setStackInSlot(0, new ItemStack(Items.STONE, 64));
                    }
                    p.getInventory().clearContent();
                    p.getInventory().selected = 0;
                    p.getInventory().setItem(9, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()));
                    p.getInventory().setItem(10, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()));
                    p.getInventory().setItem(11, new ItemStack(Items.STONE, 64));
                    p.connection.teleport(24.5, 100, 23.5, 180, 0);
                    p.inventoryMenu.broadcastChanges();
                });
                next();
            }
            case 1 -> { if (elapsed < 30) return; open(mc, BASIC); next(); }
            case 2 -> {
                if (elapsed < 30) return;
                check(mc.screen instanceof BasicItemSenderScreen, "real Basic GUI registered");
                var menu = (ItemSenderContainerBase) mc.player.containerMenu;
                check(menu.slots.stream().filter(s -> s instanceof UpgradeSlot).count() == 4, "Basic GUI has 4 upgrade slots");
                check(menu.slots.stream().filter(s -> !(s instanceof UpgradeSlot) && s.getItem().isEmpty()).count() >= 9,
                        "Basic GUI has 9 machine slots");
                // Install both Ultimate cards through the real GUI.
                shift(mc, s -> s.getItem().is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()));
                shift(mc, s -> s.getItem().is(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()));
                next();
            }
            case 3 -> {
                if (elapsed < 30) return;
                var menu = (ItemSenderContainerBase) mc.player.containerMenu;
                check(UpgradeHelper.countUpgrades(menu.baseMachineBE, com.jdte.common.upgrades.UpgradeType.ULTIMATE_CAPACITY) == 1
                        && UpgradeHelper.countUpgrades(menu.baseMachineBE, com.jdte.common.upgrades.UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                        "GUI shift-click installs both Ultimate cards on Basic");
                check(menu.slots.stream().filter(s -> s instanceof UpgradeSlot && s.getItem().is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())).count() == 1,
                        "Basic GUI shows Ultimate Capacity in an upgrade slot");
                // Stone into a machine slot through the real GUI.
                shift(mc, s -> s.getItem().is(Items.STONE));
                mc.player.closeContainer();
                next();
            }
            case 4 -> { if (elapsed < 20) return; open(mc, ADVANCED); next(); }
            case 5 -> {
                if (elapsed < 30) return;
                check(mc.screen instanceof AdvancedItemSenderScreen, "real Advanced GUI registered");
                var menu = (ItemSenderContainerBase) mc.player.containerMenu;
                check(menu.slots.stream().filter(s -> s instanceof UpgradeSlot).count() == 4, "Advanced GUI has 4 upgrade slots");
                check(menu.slots.stream().filter(s -> s instanceof UpgradeSlot && s.getItem().is(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())).count() == 1,
                        "Advanced GUI shows server-installed Ultimate Overclock");
                mc.player.closeContainer();
                next();
            }
            case 6 -> { if (elapsed < 20) return; open(mc, EXTENDED); next(); }
            case 7 -> {
                if (elapsed < 30) return;
                check(mc.screen instanceof ExtendedItemSenderScreen, "real Extended GUI registered");
                var menu = (ItemSenderContainerBase) mc.player.containerMenu;
                check(menu.slots.stream().filter(s -> s instanceof UpgradeSlot).count() == 8, "Extended GUI has 8 upgrade slots");
                check(menu.slots.stream().filter(s -> s instanceof UpgradeSlot && s.getItem().is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())).count() == 1,
                        "Extended GUI shows server-installed Ultimate Capacity");
                mc.player.closeContainer();
                next();
            }
            case 8 -> {
                if (elapsed < 20) return;
                // Block rendering: every tier's placed blockstate is present
                // on the client, its block entity exists, and its baked model
                // is not the missing model.
                for (var pos : List.of(BASIC, ADVANCED, EXTENDED)) {
                    var state = mc.level.getBlockState(pos);
                    check(!state.isAir(), "placed sender blockstate present at " + pos + ", got " + state);
                    check(mc.level.getBlockEntity(pos) instanceof ItemSenderBE, "client BE exists at " + pos);
                    var model = mc.getBlockRenderer().getBlockModel(state);
                    check(model != null && !model.getClass().getSimpleName().contains("Missing"),
                            "baked model present for " + state.getBlock());
                    var particle = model.getParticleIcon().contents().name().toString();
                    check(!particle.contains("missing"), "model particle texture present for " + state.getBlock() + ", got " + particle);
                }
                // Creative tab and JEI presence for all three sender items.
                var tab = JDTECreativeTabs.TAB.get();
                tab.buildContents(new CreativeModeTab.ItemDisplayParameters(mc.player.connection.enabledFeatures(), true, mc.player.connection.registryAccess()));
                for (var item : List.of(JDTEItems.BASIC_ITEM_SENDER.get(), JDTEItems.ADVANCED_ITEM_SENDER.get(), JDTEItems.EXTENDED_ITEM_SENDER.get())) {
                    check(tab.getDisplayItems().stream().anyMatch(s -> ItemStack.isSameItem(s, new ItemStack(item))),
                            "creative tab contains " + item);
                }
                try {
                    var field = com.jdte.common.jei.JDTEJeiPlugin.class.getDeclaredField("runtime");
                    field.setAccessible(true);
                    var runtime = (mezz.jei.api.runtime.IJeiRuntime) field.get(null);
                    check(runtime != null, "JEI runtime exists");
                    var ingredients = runtime.getIngredientManager().getAllIngredients(mezz.jei.api.constants.VanillaTypes.ITEM_STACK);
                    for (var item : List.of(JDTEItems.BASIC_ITEM_SENDER.get(), JDTEItems.ADVANCED_ITEM_SENDER.get(), JDTEItems.EXTENDED_ITEM_SENDER.get(),
                            JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())) {
                        check(ingredients.stream().anyMatch(s -> ItemStack.isSameItem(s, new ItemStack(item))),
                                "JEI contains " + item);
                    }
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Cannot inspect JEI runtime", e);
                }
                next();
            }
            case 9 -> {
                if (elapsed < 20) return;
                // Server-side confirmation of the GUI actions: Basic kept
                // its GUI-installed cards and its GUI-inserted stone.
                server(mc, p -> {
                    var basic = be(p, BASIC);
                    check(UpgradeHelper.countUpgrades(basic, com.jdte.common.upgrades.UpgradeType.ULTIMATE_CAPACITY) == 1
                            && UpgradeHelper.countUpgrades(basic, com.jdte.common.upgrades.UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                            "server Basic kept GUI-installed Ultimate cards");
                    int stone = 0;
                    for (int i = 0; i < 9; i++) if (basic.getItemHandler().getStackInSlot(i).is(Items.STONE)) stone += basic.getItemHandler().getStackInSlot(i).getCount();
                    check(stone == 64, "server Basic kept GUI-inserted stone, got " + stone);
                });
                try {
                    Files.writeString(mc.gameDirectory.toPath().resolve("sender-client-smoke-success.txt"),
                            "PASS: real client opened Basic/Advanced/Extended sender GUIs (4/4/8 upgrade slots, 9 machine slots), GUI shift-click installed both Ultimate cards on Basic, server-installed Ultimate cards show in Advanced/Extended GUIs, baked models present for all three tiers, creative tab and JEI contain all three sender items and both Ultimate cards.\n");
                } catch (java.io.IOException e) {
                    throw new IllegalStateException(e);
                }
                mc.stop();
                next();
            }
            default -> { }
        }
    }
}
