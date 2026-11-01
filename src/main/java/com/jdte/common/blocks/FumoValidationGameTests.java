package com.jdte.common.blocks;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEItems;
import java.io.InputStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Dedicated-server (GameTestServer, no client) validation for the three Fumo dolls:
 * real placement facing through a BlockPlaceContext, real FakePlayer break drops,
 * blockstate NBT round-trip (the chunk-save form of a block with no block entity),
 * and resource-model/texture/loot consistency. Graphical rendering, creative-tab
 * display and JEI display are NOT covered here: they need a real client, which
 * this environment cannot observe, so they are deliberately not asserted.
 */
@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class FumoValidationGameTests {
    @GameTest(template = "mineral_empty")
    public static void cainiaoyoServerValidation(GameTestHelper helper) {
        validate(helper, JDTEBlocks.CAINIAOYO_FUMO.get(), JDTEItems.CAINIAOYO_FUMO.get(), "cainiaoyo_fumo", "cainiaoyo");
    }

    @GameTest(template = "mineral_empty")
    public static void maiqilinServerValidation(GameTestHelper helper) {
        validate(helper, JDTEBlocks.MAIQILIN_FUMO.get(), JDTEItems.MAIQILIN_FUMO.get(), "maiqilin_fumo", "maiqilin_fumo");
    }

    @GameTest(template = "mineral_empty")
    public static void xingyeServerValidation(GameTestHelper helper) {
        validate(helper, JDTEBlocks.XINGYE_FUMO.get(), JDTEItems.XINGYE_FUMO.get(), "xingye_fumo", "xingye_fumo");
    }

    private static void validate(GameTestHelper helper, Block block, net.minecraft.world.item.Item item, String id, String texture) {
        var level = helper.getLevel();
        var pos = new BlockPos(2, 2, 2);
        var abs = helper.absolutePos(pos);
        // Config default: hidden but registered. Creative/JEI code reads exactly this value.
        helper.assertTrue(!JDTEConfig.COMMON.showFumoDolls.get(), "showFumoDolls defaults to false");
        // No block entity: there is no BE state to lose across save/unload/restart.
        helper.assertTrue(!(block instanceof net.minecraft.world.level.block.EntityBlock), "Fumo has no block entity");
        // Real placement facing: the placed block faces opposite the player's direction.
        var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level);
        player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setPos(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5);
        for (var playerFacing : Direction.Plane.HORIZONTAL) {
            player.setYRot(yRotFor(playerFacing));
            var hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
            var context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, new ItemStack(item), hit);
            var placed = block.getStateForPlacement(context);
            helper.assertTrue(placed != null && placed.getValue(DecorativeFumoBlock.FACING) == playerFacing.getOpposite(),
                    "placement facing opposite player: player " + playerFacing + " -> " + (placed == null ? "null" : placed.getValue(DecorativeFumoBlock.FACING)));
            helper.setBlock(pos, placed);
            helper.assertTrue(helper.getBlockState(pos).getValue(DecorativeFumoBlock.FACING) == playerFacing.getOpposite(), "placed state retained");
            helper.assertTrue(helper.getBlockEntity(pos) == null, "placed Fumo has no block entity");
            // Blockstate NBT round-trip: the form a chunk save uses for a BE-less block.
            var tag = NbtUtils.writeBlockState(helper.getBlockState(pos));
            var restored = NbtUtils.readBlockState(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK), tag);
            helper.assertTrue(restored == helper.getBlockState(pos), "blockstate survives NBT round-trip for " + playerFacing);
        }
        // Real break drop on the dedicated server: FakePlayer game-mode break, not a loot-table query.
        helper.setBlock(pos, block.defaultBlockState().setValue(DecorativeFumoBlock.FACING, Direction.NORTH));
        player.gameMode.destroyBlock(abs);
        helper.assertTrue(helper.getBlockState(pos).isAir(), "broken Fumo leaves air");
        var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new AABB(abs).inflate(2))
                .stream().filter(e -> ItemStack.isSameItem(e.getItem(), new ItemStack(item))).mapToInt(e -> e.getItem().getCount()).sum();
        helper.assertTrue(drops == 1, "real break drops exactly one self item, got " + drops);
        // Resource consistency from the shipped JAR resources.
        var blockstate = checkResource(helper, "assets/jdte/blockstates/" + id + ".json", null, "blockstate readable");
        for (var facing : Direction.Plane.HORIZONTAL) {
            helper.assertTrue(blockstate.contains("facing=" + facing.getName()), "blockstate has " + facing + " variant");
        }
        var model = checkResource(helper, "assets/jdte/models/block/" + id + ".json", null, "block model readable");
        helper.assertTrue(model.contains("\"elements\"") && model.split("\"elements\"").length == 2, "block model has one elements array");
        helper.assertTrue(model.contains("jdte:block/" + texture), "block model references its own texture");
        var itemModel = checkResource(helper, "assets/jdte/models/item/" + id + ".json", "\"jdte:block/" + id + "\"", "item model parents the block model");
        var loot = checkResource(helper, "data/jdte/loot_tables/blocks/" + id + ".json", "\"jdte:" + id + "\"", "loot table drops itself");
        helper.assertTrue(itemModel != null && loot != null, "item model and loot readable");
        try (var in = resource("assets/jdte/textures/block/" + texture + ".png")) {
            helper.assertTrue(in != null, "texture exists: " + texture);
            var image = javax.imageio.ImageIO.read(in);
            helper.assertTrue(image != null && image.getWidth() > 0 && image.getHeight() > 0, "texture is a readable PNG");
        } catch (Exception e) {
            helper.assertTrue(false, "texture read failed: " + e);
        }
        helper.succeed();
    }

    private static float yRotFor(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0f;
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> 270f;
            default -> 0f;
        };
    }

    private static InputStream resource(String path) {
        return FumoValidationGameTests.class.getClassLoader().getResourceAsStream(path);
    }

    private static String checkResource(GameTestHelper helper, String path, String mustContain, String message) {
        try (var in = resource(path)) {
            helper.assertTrue(in != null, "resource exists: " + path);
            var text = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            if (mustContain != null) helper.assertTrue(text.contains(mustContain), message + " (" + path + ")");
            return text;
        } catch (Exception e) {
            helper.assertTrue(false, "resource read failed " + path + ": " + e);
            return "";
        }
    }
}
