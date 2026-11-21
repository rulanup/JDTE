package com.jdte.common.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.core.UUIDUtil;

import com.jdte.setup.JDTEItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FactoryPackageItem extends Item {
    private static final String PACKAGE_ID = "jdteFactoryPackageId";
    private static final String SIZE_X = "jdteFactoryPackageSizeX";
    private static final String SIZE_Y = "jdteFactoryPackageSizeY";
    private static final String SIZE_Z = "jdteFactoryPackageSizeZ";
    private static final String BLOCK_COUNT = "jdteFactoryPackageBlockCount";
    private static final String ENTITY_COUNT = "jdteFactoryPackageEntityCount";
    private static final String TARGET_POS = "jdteFactoryPackageTargetPos";
    private static final String TARGET_DIMENSION = "jdteFactoryPackageTargetDimension";
    private static final String ROTATION = "jdteFactoryPackageRotation";

    public FactoryPackageItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean isFactoryPackage(ItemStack stack) {
        return stack.is(JDTEItems.FACTORY_PACKAGE.get());
    }

    public static boolean isFilled(ItemStack stack) {
        return getPackageId(stack).isPresent();
    }

    public static Optional<UUID> getPackageId(ItemStack stack) {
        if (!isFactoryPackage(stack)) return Optional.empty();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.read(PACKAGE_ID, UUIDUtil.CODEC);
    }

    public static ItemStack createFilled(UUID packageId, Vec3i size, int blockCount) {
        return createFilled(packageId, size, blockCount, 0);
    }

    public static ItemStack createFilled(UUID packageId, Vec3i size, int blockCount, int entityCount) {
        ItemStack stack = new ItemStack(JDTEItems.FACTORY_PACKAGE.get());
        CompoundTag tag = new CompoundTag();
        tag.store(PACKAGE_ID, UUIDUtil.CODEC, packageId);
        tag.putInt(SIZE_X, size.getX());
        tag.putInt(SIZE_Y, size.getY());
        tag.putInt(SIZE_Z, size.getZ());
        tag.putInt(BLOCK_COUNT, blockCount);
        tag.putInt(ENTITY_COUNT, entityCount);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static Optional<Vec3i> getSize(ItemStack stack) {
        if (!isFilled(stack)) return Optional.empty();
        CompoundTag tag = getTag(stack);
        int x = tag.getIntOr(SIZE_X, 0);
        int y = tag.getIntOr(SIZE_Y, 0);
        int z = tag.getIntOr(SIZE_Z, 0);
        return x > 0 && y > 0 && z > 0 ? Optional.of(new Vec3i(x, y, z)) : Optional.empty();
    }

    public static Vec3i getRotatedSize(ItemStack stack) {
        Vec3i size = getSize(stack).orElse(Vec3i.ZERO);
        return getRotation(stack) % 2 == 0 ? size : new Vec3i(size.getZ(), size.getY(), size.getX());
    }

    public static int getRotation(ItemStack stack) {
        return Math.floorMod(getTag(stack).getIntOr(ROTATION, 0), 4);
    }

    public static void rotate(ItemStack stack, int delta) {
        CompoundTag tag = getTag(stack);
        tag.putInt(ROTATION, Math.floorMod(tag.getIntOr(ROTATION, 0) + delta, 4));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static Optional<PlacementTarget> getPlacementTarget(ItemStack stack) {
        if (!isFilled(stack)) return Optional.empty();
        CompoundTag tag = getTag(stack);
        if (!tag.contains(TARGET_POS) || !tag.contains(TARGET_DIMENSION)) return Optional.empty();
        Identifier dimension = Identifier.tryParse(tag.getStringOr(TARGET_DIMENSION, ""));
        return dimension == null ? Optional.empty()
                : Optional.of(new PlacementTarget(BlockPos.of(tag.getLongOr(TARGET_POS, 0L)), dimension));
    }

    public static void setPlacementTarget(ItemStack stack, BlockPos origin, Identifier dimension) {
        CompoundTag tag = getTag(stack);
        tag.putLong(TARGET_POS, origin.asLong());
        tag.putString(TARGET_DIMENSION, dimension.toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void clearPlacementTarget(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        tag.remove(TARGET_POS);
        tag.remove(TARGET_DIMENSION);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static CompoundTag getTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (!isFilled(stack) || getSize(stack).isEmpty()) return InteractionResult.PASS;
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            clearPlacementTarget(stack);
            if (!context.getLevel().isClientSide()) {
                context.getPlayer().sendOverlayMessage(
                        Component.translatable("message.jdte.factory_package.target_cleared"));
                context.getPlayer().playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.7F, 1.0F);
            }
            return context.getLevel().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }
        Vec3i size = getRotatedSize(stack);
        BlockPos origin = getPlacementOrigin(context.getClickedPos(), context.getClickedFace(), size);
        setPlacementTarget(stack, origin, context.getLevel().dimension().identifier());
        if (context.getPlayer() != null && !context.getLevel().isClientSide()) {
            context.getPlayer().sendOverlayMessage(
                    Component.translatable("message.jdte.factory_package.target_set",
                            origin.getX(), origin.getY(), origin.getZ()));
            context.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.7F, 1.1F);
        }
        return context.getLevel().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    public static BlockPos getPlacementOrigin(BlockPos clickedPos, Direction face, Vec3i size) {
        BlockPos adjacent = clickedPos.relative(face);
        return switch (face) {
            case WEST -> adjacent.offset(1 - size.getX(), 0, 0);
            case DOWN -> adjacent.offset(0, 1 - size.getY(), 0);
            case NORTH -> adjacent.offset(0, 0, 1 - size.getZ());
            default -> adjacent;
        };
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isFilled(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines,
                                TooltipFlag tooltipFlag) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.read(PACKAGE_ID, UUIDUtil.CODEC).isPresent()) {
            lines.accept(Component.translatable("tooltip.jdte.factory_package.empty")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        lines.accept(Component.translatable("tooltip.jdte.factory_package.size",
                        tag.getInt(SIZE_X), tag.getInt(SIZE_Y), tag.getInt(SIZE_Z))
                .withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.jdte.factory_package.blocks", tag.getInt(BLOCK_COUNT))
                .withStyle(ChatFormatting.GRAY));
        if (tag.getIntOr(ENTITY_COUNT, 0) > 0) {
            lines.accept(Component.translatable("tooltip.jdte.factory_package.entities", tag.getIntOr(ENTITY_COUNT, 0))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (getRotation(stack) != 0) {
            lines.accept(Component.translatable("tooltip.jdte.factory_package.rotation", getRotation(stack) * 90)
                    .withStyle(ChatFormatting.GRAY));
        }
        getPlacementTarget(stack).ifPresent(target -> lines.accept(
                Component.translatable("tooltip.jdte.factory_package.target",
                                target.origin().getX(), target.origin().getY(), target.origin().getZ(),
                                target.dimension().toString())
                        .withStyle(ChatFormatting.AQUA)));
        if (tooltipFlag.isAdvanced()) {
            lines.accept(Component.literal(tag.read(PACKAGE_ID, UUIDUtil.CODEC).orElseThrow().toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public record PlacementTarget(BlockPos origin, Identifier dimension) {}
}
