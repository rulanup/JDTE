package com.jdte.client.smoke;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTECreativeTabs;
import com.jdte.setup.JDTEItems;
import java.nio.file.Files;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Opt-in real-client smoke checks for the three Fumo dolls. Never packaged in the mod JAR. */
@Mod.EventBusSubscriber(modid = "jdte", value = Dist.CLIENT)
public final class FumoClientSmoke {
    private static int stage;
    private static int ticks;
    private static int changedAt;
    private static BlockPos rowPos;
    private static boolean show;

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Fumo client smoke: " + message);
    }

    private static void next() { stage++; changedAt = ticks; }

    private static void screenshot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), msg -> {});
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Boolean.getBoolean("jdte.fumoSmoke")) return;
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        if (ticks > 6000) throw new IllegalStateException("Fumo client smoke timed out at stage " + stage);
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        int elapsed = ticks - changedAt;
        switch (stage) {
            case 0 -> {
                if (mc.player.tickCount < 100 || !mc.player.onGround()) return;
                show = JDTEConfig.COMMON.showFumoDolls.get();
                var server = mc.getSingleplayerServer();
                var id = mc.player.getUUID();
                rowPos = mc.player.blockPosition().offset(4, 0, 0);
                server.execute(() -> {
                    var level = server.overworld();
                    var player = server.getPlayerList().getPlayer(id);
                    // Clear a wide, tree-free area so the dolls are actually visible on camera:
                    // stone floor at the doll level, everything above it cleared to air.
                    for (int x = -12; x <= 12; x++) {
                        for (int z = -12; z <= 16; z++) {
                            var p = rowPos.offset(x, 0, z);
                            level.setBlockAndUpdate(p.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                            for (int y = 0; y <= 12; y++) {
                                level.setBlockAndUpdate(p.offset(0, y, 0), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                    com.mojang.logging.LogUtils.getLogger().info("Fumo client smoke: rowPos={}, player={}", rowPos, player.blockPosition());
                    // One row of Cainiaoyo in all four facings, plus one of each doll facing north.
                    var facings = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
                    for (int i = 0; i < 4; i++) {
                        var p = rowPos.offset(i, 0, 0);
                        level.setBlockAndUpdate(p, JDTEBlocks.CAINIAOYO_FUMO.get().defaultBlockState().setValue(com.jdte.common.blocks.DecorativeFumoBlock.FACING, facings.get(i)));
                    }
                    var dolls = List.of(JDTEBlocks.MAIQILIN_FUMO.get(), JDTEBlocks.XINGYE_FUMO.get());
                    for (int i = 0; i < dolls.size(); i++) {
                        var p = rowPos.offset(4 + i, 0, 0);
                        level.setBlockAndUpdate(p, dolls.get(i).defaultBlockState().setValue(com.jdte.common.blocks.DecorativeFumoBlock.FACING, Direction.NORTH));
                    }
                    player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
                    player.getAbilities().flying = true;
                    player.onUpdateAbilities();
                    player.teleportTo(level, rowPos.getX() + 2.5, rowPos.getY(), rowPos.getZ() + 4, 180, 0);
                });
                next();
            }
            case 1 -> {
                if (elapsed < 40) return;
                // Wide shot of the whole facing row first, then a close-up in stage 2.
                var centre = new net.minecraft.world.phys.Vec3(rowPos.getX() + 2.5, rowPos.getY() + 0.5, rowPos.getZ() + 0.5);
                mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, centre);
                if (elapsed < 60) return;
                com.mojang.logging.LogUtils.getLogger().info("Fumo client smoke: player={} yaw={} pitch={} hit={} doll0={} doll1={} doll2={}",
                        mc.player.blockPosition(), mc.player.getYRot(), mc.player.getXRot(), mc.hitResult,
                        mc.level.getBlockState(rowPos), mc.level.getBlockState(rowPos.offset(4, 0, 0)), mc.level.getBlockState(rowPos.offset(5, 0, 0)));
                screenshot(mc, "jdte-fumo-world.png");
                // Move in close to the first doll for a texture close-up.
                var server = mc.getSingleplayerServer();
                var id = mc.player.getUUID();
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayer(id);
                    player.teleportTo(server.overworld(), rowPos.getX() + 0.5, rowPos.getY(), rowPos.getZ() + 1, 180, 0);
                });
                stage = 11; changedAt = ticks;
            }
            case 11 -> {
                if (elapsed < 40) return;
                var centre = new net.minecraft.world.phys.Vec3(rowPos.getX() + 0.5, rowPos.getY() + 0.4, rowPos.getZ() + 0.5);
                mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, centre);
                if (elapsed < 60) return;
                screenshot(mc, "jdte-fumo-close.png");
                stage = 2; changedAt = ticks;
            }
            case 2 -> {
                if (elapsed < 20) return;
                try {
                    var field = com.jdte.common.jei.JDTEJeiPlugin.class.getDeclaredField("runtime");
                    field.setAccessible(true);
                    var runtime = (mezz.jei.api.runtime.IJeiRuntime) field.get(null);
                    check(runtime != null, "JEI runtime exists");
                    var ingredients = runtime.getIngredientManager().getAllIngredients(mezz.jei.api.constants.VanillaTypes.ITEM_STACK);
                    for (var item : List.of(JDTEItems.CAINIAOYO_FUMO.get(), JDTEItems.MAIQILIN_FUMO.get(), JDTEItems.XINGYE_FUMO.get())) {
                        boolean present = ingredients.stream().anyMatch(s -> ItemStack.isSameItem(s, new ItemStack(item)));
                        check(present == show, "JEI presence for " + item + " is " + present + " but showFumoDolls=" + show);
                    }
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Cannot inspect JEI runtime", e);
                }
                // Creative tab contents use the same config value through the real tab generator.
                var tab = JDTECreativeTabs.TAB.get();
                tab.buildContents(new CreativeModeTab.ItemDisplayParameters(mc.player.connection.enabledFeatures(), true, mc.player.connection.registryAccess()));
                for (var item : List.of(JDTEItems.CAINIAOYO_FUMO.get(), JDTEItems.MAIQILIN_FUMO.get(), JDTEItems.XINGYE_FUMO.get())) {
                    boolean present = tab.getDisplayItems().stream().anyMatch(s -> ItemStack.isSameItem(s, new ItemStack(item)));
                    check(present == show, "creative tab presence for " + item + " is " + present + " but showFumoDolls=" + show);
                }
                // Creative screen is best-effort: the client game-mode sync is not
                // guaranteed here, and the tab contents are already verified above.
                if (mc.gameMode != null && mc.gameMode.getPlayerMode() == net.minecraft.world.level.GameType.CREATIVE) {
                    mc.setScreen(new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), false));
                }
                next();
            }
            case 3 -> {
                if (elapsed < 40) return;
                screenshot(mc, "jdte-fumo-creative-jei.png");
                mc.setScreen(null);
                try {
                    Files.writeString(mc.gameDirectory.toPath().resolve("fumo-client-smoke-success.txt"),
                            "PASS: real client rendered Fumo row, JEI presence == showFumoDolls=" + show + ", creative tab contents == showFumoDolls=" + show + ", creative screen best-effort only\n");
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
