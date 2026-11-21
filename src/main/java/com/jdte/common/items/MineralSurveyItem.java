package com.jdte.common.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import com.jdte.common.minerals.MineralEntry;
import com.jdte.common.minerals.MineralSurveyData;
import com.jdte.common.minerals.MineralSurveyIndex;
import com.jdte.common.network.data.MineralSurveyOpenPayload;
import com.jdte.setup.JDTEDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;

public final class MineralSurveyItem extends Item {
    private static final int TOOLTIP_ENTRY_LIMIT = 5;

    public MineralSurveyItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            interact(serverLevel, player, stack, player.blockPosition());
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel serverLevel && context.getPlayer() != null) {
            interact(serverLevel, context.getPlayer(), context.getItemInHand(), context.getClickedPos());
        }
        return context.getLevel().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    private static void interact(ServerLevel level, Player player, ItemStack stack, BlockPos position) {
        MineralSurveyData survey = stack.get(JDTEDataComponents.MINERAL_SURVEY.get());
        if (survey != null) {
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MineralSurveyOpenPayload(survey));
            }
            return;
        }

        Holder<Biome> biome = level.getBiome(position);
        Identifier biomeId = biome.unwrapKey().map(ResourceKey::identifier).orElse(null);
        if (biomeId == null) {
            player.sendOverlayMessage(Component.translatable("message.jdte.mineral_survey.unknown_biome"));
            return;
        }
        MineralSurveyIndex.Profile profile = MineralSurveyIndex.profile(level, biome);
        if (profile.entries().isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.jdte.mineral_survey.empty", biomeId.toString()));
            return;
        }

        MineralSurveyData recorded = MineralSurveyData.create(
                profile.version(), biomeId, level.dimension().identifier(), profile.entries());
        stack.set(JDTEDataComponents.MINERAL_SURVEY.get(), recorded);
        player.sendOverlayMessage(Component.translatable(
                "message.jdte.mineral_survey.recorded", biomeId.toString(), recorded.entries().size()));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(JDTEDataComponents.MINERAL_SURVEY.get()) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines,
                                TooltipFlag tooltipFlag) {
        MineralSurveyData survey = stack.get(JDTEDataComponents.MINERAL_SURVEY.get());
        if (survey == null) {
            lines.accept(Component.translatable("tooltip.jdte.mineral_survey.blank")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        lines.accept(Component.translatable("tooltip.jdte.mineral_survey.source", survey.biomeId().toString())
                .withStyle(ChatFormatting.AQUA));
        long totalWeight = survey.totalWeight();
        survey.entries().stream().limit(TOOLTIP_ENTRY_LIMIT).forEach(entry -> lines.accept(
                Component.translatable("tooltip.jdte.mineral_survey.entry",
                                displayName(entry), formatPercent(entry.weight(), totalWeight))
                        .withStyle(ChatFormatting.GRAY)));
        if (survey.entries().size() > TOOLTIP_ENTRY_LIMIT) {
            lines.accept(Component.translatable("tooltip.jdte.mineral_survey.more",
                            survey.entries().size() - TOOLTIP_ENTRY_LIMIT)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        lines.accept(Component.translatable("tooltip.jdte.mineral_survey.open")
                .withStyle(ChatFormatting.YELLOW));
        lines.accept(Component.translatable("tooltip.jdte.mineral_survey.estimated")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static Component displayName(MineralEntry entry) {
        return BuiltInRegistries.BLOCK.getOptional(entry.oreId())
                .map(block -> block.getName())
                .orElseGet(() -> Component.literal(entry.oreId().toString()));
    }

    public static String formatPercent(long weight, long totalWeight) {
        if (weight <= 0L || totalWeight <= 0L) return "0.00%";
        return String.format(Locale.ROOT, "%.2f%%", weight * 100.0D / totalWeight);
    }
}