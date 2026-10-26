package com.jdte.common.items;

import com.jdte.common.minerals.MineralEntry;
import com.jdte.common.minerals.MineralSurveyData;
import com.jdte.common.minerals.MineralSurveyIndex;

import net.minecraft.nbt.NbtOps;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import com.jdte.common.network.MineralPackets;

import java.util.List;
import java.util.Locale;

public final class MineralSurveyItem extends Item {
    private static final int TOOLTIP_ENTRY_LIMIT = 5;

    public MineralSurveyItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            interact(serverLevel, player, stack, player.blockPosition());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel serverLevel && context.getPlayer() != null) {
            interact(serverLevel, context.getPlayer(), context.getItemInHand(), context.getClickedPos());
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    private static void interact(ServerLevel level, Player player, ItemStack stack, BlockPos position) {
        MineralSurveyData survey = getSurvey(stack);
        if (survey != null) {
            if (player instanceof ServerPlayer serverPlayer) {
                MineralPackets.sendSurvey(serverPlayer, survey);
            }
            return;
        }

        Holder<Biome> biome = level.getBiome(position);
        ResourceLocation biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        if (biomeId == null) {
            player.displayClientMessage(Component.translatable("message.jdte.mineral_survey.unknown_biome"), true);
            return;
        }
        MineralSurveyIndex.Profile profile = MineralSurveyIndex.profile(level, biome);
        if (profile.entries().isEmpty()) {
            player.displayClientMessage(Component.translatable("message.jdte.mineral_survey.empty", biomeId.toString()), true);
            return;
        }

        MineralSurveyData recorded = MineralSurveyData.create(
                profile.version(), biomeId, level.dimension().location(), profile.entries());
        setSurvey(stack, recorded);
        player.displayClientMessage(Component.translatable(
                "message.jdte.mineral_survey.recorded", biomeId.toString(), recorded.entries().size()), true);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getSurvey(stack) != null || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
                                TooltipFlag tooltipFlag) {
        MineralSurveyData survey = getSurvey(stack);
        if (survey == null) {
            lines.add(Component.translatable("tooltip.jdte.mineral_survey.blank")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        lines.add(Component.translatable("tooltip.jdte.mineral_survey.source", survey.biomeId().toString())
                .withStyle(ChatFormatting.AQUA));
        long totalWeight = survey.totalWeight();
        survey.entries().stream().limit(TOOLTIP_ENTRY_LIMIT).forEach(entry -> lines.add(
                Component.translatable("tooltip.jdte.mineral_survey.entry",
                                displayName(entry), formatPercent(entry.weight(), totalWeight))
                        .withStyle(ChatFormatting.GRAY)));
        if (survey.entries().size() > TOOLTIP_ENTRY_LIMIT) {
            lines.add(Component.translatable("tooltip.jdte.mineral_survey.more",
                            survey.entries().size() - TOOLTIP_ENTRY_LIMIT)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        lines.add(Component.translatable("tooltip.jdte.mineral_survey.open")
                .withStyle(ChatFormatting.YELLOW));
        lines.add(Component.translatable("tooltip.jdte.mineral_survey.estimated")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static MineralSurveyData getSurvey(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("JDTE_MineralSurvey")) return null;
        return MineralSurveyData.CODEC.parse(NbtOps.INSTANCE, stack.getTag().get("JDTE_MineralSurvey"))
                .resultOrPartial(message -> LogUtils.getLogger().warn("Invalid mineral survey NBT: {}", message)).orElse(null);
    }

    public static void setSurvey(ItemStack stack, MineralSurveyData data) {
        stack.getOrCreateTag().put("JDTE_MineralSurvey", MineralSurveyData.CODEC.encodeStart(NbtOps.INSTANCE, data)
                .getOrThrow(false, message -> { throw new IllegalArgumentException(message); }));
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
