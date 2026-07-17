package com.jdte.client;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.JDTE;
import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.network.data.WrenchAreaAdjustPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class WrenchScrollHandler {
    private static final String JDT_MODID = "justdirethings";

    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (event.isCanceled()) return;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || !(minecraft.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        if (!isHoldingWrench(player)) {
            return;
        }

        BlockPos targetPos = hitResult.getBlockPos();
        BlockState targetState = minecraft.level.getBlockState(targetPos);
        if (!isJDTEMachine(targetState)) {
            return;
        }

        if (!JDTEKeyMappings.WRENCH_AREA_MODIFIER.isDown()) {
            return;
        }

        int delta = event.getScrollDeltaY() > 0.0D ? 1 : event.getScrollDeltaY() < 0.0D ? -1 : 0;
        if (delta == 0) {
            return;
        }

        BlockEntity blockEntity = minecraft.level.getBlockEntity(targetPos);
        if (!(blockEntity instanceof AreaAffectingBE)) {
            return;
        }

        PacketDistributor.sendToServer(new WrenchAreaAdjustPayload(targetPos, delta));
        event.setCanceled(true);
    }

    private static boolean isJDTEMachine(BlockState state) {
        Block block = state.getBlock();
        if (!(block instanceof BaseMachineBlock)) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return id != null && (JDTE.MODID.equals(id.getNamespace()) || JDT_MODID.equals(id.getNamespace()));
    }

    private static boolean isHoldingWrench(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.getItem() instanceof EclipseAlloyWrenchItem || offHand.getItem() instanceof EclipseAlloyWrenchItem;
    }
}
