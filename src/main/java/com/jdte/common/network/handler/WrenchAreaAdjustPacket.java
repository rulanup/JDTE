package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.jdte.common.integrations.JDTEUltimineIntegration;
import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.network.data.WrenchAreaAdjustPayload;
import com.jdte.common.network.data.WrenchAreaAdjustResultPayload;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collection;
import java.util.List;

public class WrenchAreaAdjustPacket {
    private static final WrenchAreaAdjustPacket INSTANCE = new WrenchAreaAdjustPacket();

    public static WrenchAreaAdjustPacket get() {
        return INSTANCE;
    }

    public void handle(WrenchAreaAdjustPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.mayBuild() || !isHoldingWrench(player)) {
                return;
            }

            int delta = Integer.compare(payload.delta(), 0);
            if (delta == 0 || !isCloseEnough(player, payload.blockPos())) {
                return;
            }

            Level level = player.level();
            BlockState targetState = level.getBlockState(payload.blockPos());
            if (!EclipseAlloyWrenchItem.canHandleMachine(targetState)) {
                return;
            }
            Collection<BlockPos> positions = List.of(payload.blockPos());
            if (ModList.get().isLoaded("ftbultimine")) {
                positions = JDTEUltimineIntegration.getCurrentSelection(player).orElse(positions);
            }

            double lastRadius = 0;
            double lastMaxRadius = 0;
            boolean adjusted = false;

            for (BlockPos pos : positions) {
                if (!level.getBlockState(pos).is(targetState.getBlock())) {
                    continue;
                }
                double[] result = adjustArea(level, pos, delta);
                if (result != null) {
                    lastRadius = result[0];
                    lastMaxRadius = result[1];
                    adjusted = true;
                }
            }

            if (adjusted) {
                PacketDistributor.sendToPlayer(player, new WrenchAreaAdjustResultPayload(lastRadius, lastMaxRadius));
            }
        });
    }

    private static boolean isCloseEnough(ServerPlayer player, BlockPos pos) {
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    private static boolean isHoldingWrench(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.getItem() instanceof EclipseAlloyWrenchItem || offHand.getItem() instanceof EclipseAlloyWrenchItem;
    }

    private static double[] adjustArea(Level level, BlockPos pos, int delta) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof AreaAffectingBE areaAffectingBE) || !(blockEntity instanceof BaseMachineBE machine)) {
            return null;
        }

        AreaAffectingData data = areaAffectingBE.getAreaAffectingData();
        double maxRadius = UpgradeHelper.getMaxAreaRadius(machine);
        double newRadius = Math.max(0, Math.min(data.xRadius + delta, maxRadius));

        areaAffectingBE.setAreaSettings(
                data.xRadius + delta,
                data.yRadius + delta,
                data.zRadius + delta,
                data.xOffset,
                data.yOffset,
                data.zOffset,
                data.renderArea
        );

        return new double[]{newRadius, maxRadius};
    }
}
