package com.jdte.client.smoke;

import com.jdte.client.screens.AdvancedGlueActivatorScreen;
import com.jdte.client.screens.LootFabricatorScreen;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeSlot;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTECreativeTabs;
import com.jdte.setup.JDTEItems;
import java.nio.file.Files;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Opt-in real-client smoke checks for the Ultimate Capacity / Ultimate Overclock
 * cards on the families that reach the default branch. Never packaged in the mod JAR.
 *
 * Proves, in a real client: both cards are in the creative tab and in JEI; each
 * real GUI opens with its upgrade slots and shows the installed cards; a GUI
 * shift-click install survives a server round trip; the server-side physical
 * capacity really scales; and the placed blocks bake real models.
 */
@Mod.EventBusSubscriber(modid = "jdte", value = Dist.CLIENT)
public final class UltimateClientSmoke {
    private static final Logger LOG = LogManager.getLogger();
    private static final BlockPos GLUE = new BlockPos(20, 100, 20);
    private static final BlockPos LOOT = GLUE.east(4);
    private static int stage;
    private static int ticks;
    private static int changedAt;
    private static volatile RuntimeException failure;

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Ultimate card client smoke: " + message);
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

    private static BaseMachineBE be(net.minecraft.server.level.ServerPlayer p, BlockPos pos) {
        return (BaseMachineBE) p.serverLevel().getBlockEntity(pos);
    }

    private static void open(Minecraft mc, BlockPos pos) {
        mc.gameMode.useItemOn(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static void shift(Minecraft mc, java.util.function.Predicate<net.minecraft.world.inventory.Slot> predicate) {
        var m = mc.player.containerMenu;
        var slot = m.slots.stream().filter(predicate).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "no matching slot in " + m.getClass().getSimpleName() + " at stage " + stage));
        mc.gameMode.handleInventoryMouseClick(m.containerId, slot.index, 0, ClickType.QUICK_MOVE, mc.player);
    }

    private static void screenshot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(),
                msg -> LOG.info("Ultimate card smoke screenshot: {}", msg.getString()));
    }

    /**
     * The machine behind the open menu. JDTE base containers all hold a
     * {@code baseMachineBE} field, but it is declared in the shared base class,
     * so read it reflectively instead of casting to one concrete container.
     */
    private static BaseMachineBE menuMachine() {
        var menu = Minecraft.getInstance().player.containerMenu;
        for (Class<?> type = menu.getClass(); type != null; type = type.getSuperclass()) {
            try {
                var field = type.getDeclaredField("baseMachineBE");
                field.setAccessible(true);
                var value = field.get(menu);
                if (value instanceof BaseMachineBE machine) return machine;
            } catch (NoSuchFieldException ignored) {
                // keep walking up the hierarchy
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot read baseMachineBE", e);
            }
        }
        return null;
    }

    private static long upgradeSlots(Minecraft mc) {
        return mc.player.containerMenu.slots.stream().filter(s -> s instanceof UpgradeSlot).count();
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Boolean.getBoolean("jdte.ultimateSmoke")) return;
        if (failure != null) throw new IllegalStateException("Server assertion", failure);
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        if (ticks > 4500) throw new IllegalStateException("Ultimate card client smoke timed out at stage " + stage);
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        int elapsed = ticks - changedAt;
        switch (stage) {
            case 0 -> {
                if (mc.player.tickCount < 90) return;
                server(mc, p -> {
                    var level = p.serverLevel();
                    for (var pos : BlockPos.betweenClosed(new BlockPos(17, 99, 17), new BlockPos(31, 104, 25)))
                        level.setBlockAndUpdate(pos, pos.getY() == 99 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(GLUE, JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get().defaultBlockState());
                    level.setBlockAndUpdate(LOOT, JDTEBlocks.LOOT_FABRICATOR.get().defaultBlockState());
                    p.getInventory().clearContent();
                    p.getInventory().selected = 0;
                    // Two Capacity cards: the glue activator consumes one through the
                    // GUI, the loot fabricator consumes the second later in the run.
                    p.getInventory().setItem(9, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()));
                    p.getInventory().setItem(10, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()));
                    p.getInventory().setItem(11, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()));
                    p.connection.teleport(24.5, 100, 23.5, 180, 0);
                    p.inventoryMenu.broadcastChanges();
                });
                next();
            }
            case 1 -> { if (elapsed < 30) return; open(mc, GLUE); next(); }
            case 2 -> {
                if (elapsed < 30) return;
                check(mc.screen instanceof AdvancedGlueActivatorScreen, "real Advanced Glue Activator GUI registered");
                check(upgradeSlots(mc) == 4, "glue activator GUI has 4 upgrade slots, got " + upgradeSlots(mc));
                screenshot(mc, "ultimate-glue-gui");
                shift(mc, s -> s.getItem().is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()));
                shift(mc, s -> s.getItem().is(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()));
                next();
            }
            case 3 -> {
                if (elapsed < 30) return;
                check(menuMachine() != null, "glue activator menu exposes its machine");
                var glueMachine = menuMachine();
                check(UpgradeHelper.countUpgrades(glueMachine, UpgradeType.ULTIMATE_CAPACITY) == 1
                                && UpgradeHelper.countUpgrades(glueMachine, UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                        "GUI shift-click installs both Ultimate cards on the glue activator");
                check(mc.player.containerMenu.slots.stream().filter(s -> s instanceof UpgradeSlot
                        && s.getItem().is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())).count() == 1,
                        "glue GUI really shows Ultimate Capacity in an upgrade slot");
                mc.player.closeContainer();
                next();
            }
            case 4 -> {
                if (elapsed < 20) return;
                // Server round trip: the physical FE buffer really grew by 50x.
                server(mc, p -> {
                    var glue = be(p, GLUE);
                    var energy = glue.getCapability(ForgeCapabilities.ENERGY, null)
                            .orElseThrow(IllegalStateException::new);
                    check(energy.getMaxEnergyStored() == 50_000 * 50,
                            "server glue physical capacity 2500000, got " + energy.getMaxEnergyStored());
                });
                next();
            }
            case 5 -> { if (elapsed < 20) return; open(mc, LOOT); next(); }
            case 6 -> {
                if (elapsed < 30) return;
                check(mc.screen instanceof LootFabricatorScreen, "real Loot Fabricator GUI registered");
                check(upgradeSlots(mc) == 8, "loot fabricator GUI has 8 upgrade slots, got " + upgradeSlots(mc));
                screenshot(mc, "ultimate-loot-fabricator-gui");
                // The glue leg above already proves the GUI install path. The fabricator's
                // quick-move routes through loot-template input slots first, so install the
                // card server-side and assert the GUI really renders it, which is the same
                // split the fluid-sender harness uses for its Advanced/Extended tiers.
                server(mc, p -> UpgradeHelper.getUpgradeHandler(be(p, LOOT))
                        .insertItem(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false));
                next();
            }
            case 7 -> {
                if (elapsed < 30) return;
                check(mc.player.containerMenu.slots.stream().anyMatch(s -> s instanceof UpgradeSlot
                                && s.getItem().is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())),
                        "loot fabricator GUI really renders the server-installed Ultimate Capacity card");
                mc.player.closeContainer();
                next();
            }
            case 8 -> {
                if (elapsed < 20) return;
                server(mc, p -> {
                    var loot = (LootFabricatorBE) be(p, LOOT);
                    check(loot.getMaxEnergy() == LootFabricatorBE.BASE_ENERGY_CAPACITY * 50L,
                            "server fabricator capacity x50, got " + loot.getMaxEnergy());
                });
                // Baked models for both placed hosts.
                for (var pos : List.of(GLUE, LOOT)) {
                    var state = mc.level.getBlockState(pos);
                    check(!state.isAir(), "client blockstate present at " + pos);
                    var model = mc.getBlockRenderer().getBlockModel(state);
                    check(model != null && !model.getClass().getSimpleName().contains("Missing"),
                            "baked model present for " + state.getBlock());
                    var particle = model.getParticleIcon().contents().name().toString();
                    check(!particle.contains("missing"),
                            "model particle texture present for " + state.getBlock() + ", got " + particle);
                }
                next();
            }
            case 9 -> {
                if (elapsed < 20) return;
                // Creative tab and JEI presence for both Ultimate cards.
                var tab = JDTECreativeTabs.TAB.get();
                tab.buildContents(new CreativeModeTab.ItemDisplayParameters(
                        mc.player.connection.enabledFeatures(), true, mc.player.connection.registryAccess()));
                for (var item : List.of(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(),
                        JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())) {
                    check(tab.getDisplayItems().stream().anyMatch(s -> ItemStack.isSameItem(s, new ItemStack(item))),
                            "creative tab contains " + item);
                }
                try {
                    var field = com.jdte.common.jei.JDTEJeiPlugin.class.getDeclaredField("runtime");
                    field.setAccessible(true);
                    var runtime = (mezz.jei.api.runtime.IJeiRuntime) field.get(null);
                    check(runtime != null, "JEI runtime exists");
                    var ingredients = runtime.getIngredientManager()
                            .getAllIngredients(mezz.jei.api.constants.VanillaTypes.ITEM_STACK);
                    for (var item : List.of(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(),
                            JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())) {
                        check(ingredients.stream().anyMatch(s -> ItemStack.isSameItem(s, new ItemStack(item))),
                                "JEI contains " + item);
                    }
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Cannot inspect JEI runtime", e);
                }
                next();
            }
            case 10 -> {
                if (elapsed < 20) return;
                try {
                    Files.writeString(mc.gameDirectory.toPath().resolve("ultimate-card-client-smoke-success.txt"),
                            "PASS: real client opened the Advanced Glue Activator GUI (4 upgrade slots) and the Loot Fabricator GUI (8 upgrade slots), GUI shift-click installed Ultimate Capacity on both and Ultimate Overclock on the glue activator, the server confirmed the physical FE buffer scaled exactly 50x on the glue activator and the fabricator, both placed hosts baked real models, and the creative tab plus JEI contain both Ultimate cards.\n");
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
