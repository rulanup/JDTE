package com.jdte.common.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.direwolf20.justdirethings.common.items.interfaces.FluidContainingItem;
import com.direwolf20.justdirethings.JustDireThings;
import com.jdte.JDTE;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import com.direwolf20.justdirethings.common.items.FluidCanister;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * 大型流体储罐：操作逻辑与 JDT 原版 FluidCanister 一致，容量 1000 B。
 * 填充模式共 4 种：NONE / JDTONLY / JDTEONLY / ALL。可放入饰品栏新槽位 "tank"。
 */
public class BigFluidTankItem extends Item implements FluidContainingItem {
    public enum FillMode {
        NONE("none"),
        JDTONLY("jdtonly"),
        JDTEONLY("jdteonly"),
        ALL("all");

        private final String baseName;

        FillMode(String baseName) {
            this.baseName = baseName;
        }

        public Component getTooltip() {
            return Component.translatable(JustDireThings.MODID + ".fillmode." + baseName);
        }

        public FillMode next() {
            FillMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    /** 1000 B = 1,000,000 mB。 */
    public static final int MAX_MB = 1_000_000;

    public BigFluidTankItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getMaxMB() {
        return MAX_MB;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (blockhitresult.getType() == HitResult.Type.BLOCK) {
            if (player.isShiftKeyDown()) {
                if (placeFluid(level, player, itemStack, blockhitresult)) {
                    return InteractionResult.SUCCESS;
                }
            } else {
                if (pickupFluid(level, player, itemStack, blockhitresult)) {
                    return InteractionResult.SUCCESS;
                } else {
                    if (placeFluid(level, player, itemStack, blockhitresult)) {
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        } else {
            if (player.isShiftKeyDown()) {
                nextFillMode(itemStack);
                player.sendOverlayMessage(Component.translatable("justdirethings.fillmode.changed",
                        getFillMode(itemStack).getTooltip()));
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack itemStack, @NotNull ServerLevel world,
                              @NotNull Entity entity, @Nullable EquipmentSlot slot) {
        FillMode mode = getFillMode(itemStack);
        if (mode == FillMode.NONE || !(entity instanceof Player player)) return;
        var source = ItemAccess.forStack(itemStack).getCapability(Capabilities.Fluid.ITEM);
        if (source == null || source.getResource(0).isEmpty()) return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack target = player.getInventory().getItem(i);
            if (target.isEmpty() || target.getItem() instanceof BigFluidTankItem) continue;
            String modId = target.getItem().getCreatorModId(world.registryAccess(), target);
            if (mode == FillMode.JDTONLY && !JustDireThings.MODID.equals(modId)
                    || mode == FillMode.JDTEONLY && !JDTE.MODID.equals(modId)) continue;
            var access = i <= net.minecraft.world.entity.player.Inventory.SLOT_OFFHAND
                    ? ItemAccess.forPlayerSlot(player, i) : ItemAccess.forStack(target);
            ResourceHandlerUtil.move(source, access.getCapability(Capabilities.Fluid.ITEM), fluid -> true, 100, null);
            if (source.getAmountAsInt(0) == 0) break;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, display, tooltip, flagIn);
        Level level = context.level();
        if (level == null) {
            return;
        }
        var fluidHandler = ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM);
        if (fluidHandler == null) {
            return;
        }
        FluidStack fluidStack = fluidHandler.getResource(0).toStack(fluidHandler.getAmountAsInt(0));
        int fluidColor = getFluidColor(stack);
        Style fluidStyle = Style.EMPTY.withColor(TextColor.fromRgb(fluidColor));
        tooltip.accept(Component.translatable("justdirethings.fluidname").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(fluidStack.getHoverName().getString()).withStyle(fluidStyle)));
        tooltip.accept(Component.translatable("justdirethings.fluidamt").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(fluidStack.getAmount() + "/" + getMaxMB()).withStyle(ChatFormatting.GREEN)));
        tooltip.accept(Component.translatable("justdirethings.fillmode").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(getFillMode(stack).getTooltip().getString()).withStyle(ChatFormatting.GREEN)));
    }

    public boolean placeFluid(Level level, Player player, ItemStack itemStack, BlockHitResult blockhitresult) {
        BlockPos blockpos = blockhitresult.getBlockPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.getBlock() instanceof LiquidBlock && !player.isShiftKeyDown()) {
            return false;
        }
        var fluidHandler = ItemAccess.forStack(itemStack).getCapability(Capabilities.Fluid.ITEM);
        if (fluidHandler == null) {
            return false;
        }
        FluidStack fluidStack = fluidHandler.getResource(0).toStack(fluidHandler.getAmountAsInt(0));
        if (fluidStack.isEmpty() || fluidStack.getAmount() < 1000) {
            return false;
        }
        Direction direction = blockhitresult.getDirection();
        BlockPos blockpos1 = blockpos.relative(direction);
        BlockPos blockpos2 = canBlockContainFluid(player, level, blockpos, blockstate, fluidStack.getFluid())
                ? blockpos : blockpos1;
        try (var tx = Transaction.openRoot()) {
            if (fluidHandler.extract(0, FluidResource.of(fluidStack), 1000, tx) != 1000) return false;
            // The handler already reflects the tentative extraction, including when it emptied the tank.
            if (!emptyFluid(player, level, blockpos2, fluidStack.getFluid())) return false;
            tx.commit();
            return true;
        }
    }

    public boolean pickupFluid(Level level, Player player, ItemStack itemStack, BlockHitResult blockhitresult) {
        BlockPos blockpos = blockhitresult.getBlockPos();
        BlockState blockstate1 = level.getBlockState(blockpos);
        if (blockstate1.getBlock() instanceof LiquidBlock liquidBlock) {
            var fluidHandler = ItemAccess.forStack(itemStack).getCapability(Capabilities.Fluid.ITEM);
            if (fluidHandler == null) {
                return false;
            }
            Fluid fluid = liquidBlock.fluid;
            FluidStack fluidStack = fluidHandler.getResource(0).toStack(fluidHandler.getAmountAsInt(0));
            if (!blockstate1.getFluidState().isSource()
                    || (!fluidStack.isEmpty() && !fluidStack.is(fluid))) {
                return false;
            }
            try (var tx = Transaction.openRoot()) {
                if (fluidHandler.insert(0, FluidResource.of(fluid), 1000, tx) != 1000) return false;
                ItemStack itemstack2 = liquidBlock.pickupBlock(player, level, blockpos, blockstate1);
                if (itemstack2.isEmpty()) return false;
                tx.commit();
                liquidBlock.getPickupSound(blockstate1).ifPresent(p_150709_ -> player.playSound(p_150709_, 1.0F, 1.0F));
                if (!level.isClientSide()) {
                    CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, itemstack2);
                }
            }
            return true;
        }
        return false;
    }

    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable ItemStack container) {
        SimpleFluidContent fluidData = getFluidData(container);
        if (fluidData == null || fluidData.isEmpty()) {
            return false;
        }
        return emptyFluid(player, level, pos, fluidData.getFluid());
    }

    private boolean emptyFluid(@Nullable Player player, Level level, BlockPos pos, Fluid fluid) {
        if (!(fluid instanceof FlowingFluid flowingFluid)) {
            return false;
        }
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();
        boolean canBeReplaced = blockState.canBeReplaced(fluid);
        if (!blockState.isAir() && !canBeReplaced) {
            if (block instanceof LiquidBlockContainer liquidBlockContainer
                    && liquidBlockContainer.canPlaceLiquid(player, level, pos, blockState, fluid)) {
                liquidBlockContainer.placeLiquid(level, pos, blockState, flowingFluid.getSource(false));
                playEmptySound(player, level, pos, fluid);
                return true;
            }
            return false;
        }
        FluidStack fluidStack = new FluidStack(fluid, 1000);
        if (fluid.getFluidType().isVaporizedOnPlacement(level, pos, fluidStack)) {
            fluid.getFluidType().onVaporize(player, level, pos, fluidStack);
            return true;
        }
        if (!level.isClientSide() && canBeReplaced && !blockState.liquid()) {
            level.destroyBlock(pos, true);
        }
        if (level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 11)
                || blockState.getFluidState().isSource()) {
            playEmptySound(player, level, pos, fluid);
            return true;
        }
        return false;
    }

    protected void playEmptySound(@Nullable Player pPlayer, LevelAccessor pLevel, BlockPos pPos, Fluid fluid) {
        SoundEvent soundevent = fluid.getFluidType().getSound(pPlayer, pLevel, pPos,
                net.neoforged.neoforge.common.SoundActions.BUCKET_EMPTY);
        if (soundevent == null) {
            soundevent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        }
        pLevel.playSound(pPlayer, pPos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
        pLevel.gameEvent(pPlayer, GameEvent.FLUID_PLACE, pPos);
    }

    protected boolean canBlockContainFluid(@Nullable Player player, Level worldIn, BlockPos posIn,
                                           BlockState blockstate, Fluid fluid) {
        return blockstate.getBlock() instanceof LiquidBlockContainer
                && ((LiquidBlockContainer) blockstate.getBlock()).canPlaceLiquid(player, worldIn, posIn, blockstate, fluid);
    }

    public static SimpleFluidContent getFluidData(ItemStack itemStack) {
        return itemStack.get(JustDireDataComponents.FLUID_CONTAINER);
    }

    public static Fluid getFluid(ItemStack itemStack) {
        SimpleFluidContent fluidData = getFluidData(itemStack);
        if (fluidData != null && !fluidData.isEmpty()) {
            return fluidData.getFluid();
        }
        return null;
    }

    public static int getFullness(ItemStack itemStack) {
        SimpleFluidContent fluidData = getFluidData(itemStack);
        if (fluidData != null && !fluidData.isEmpty()) {
            return (int) Math.ceil((double) fluidData.getAmount() / 1000);
        }
        return 0;
    }

    public static int getFluidColor(ItemStack itemStack) {
        return FluidCanister.getFluidColor(itemStack);
    }

    public static FillMode getFillMode(ItemStack itemStack) {
        return FillMode.values()[itemStack.getOrDefault(JustDireDataComponents.FLUID_CANISTER_MODE, 0) % FillMode.values().length];
    }

    public static void nextFillMode(ItemStack itemStack) {
        itemStack.set(JustDireDataComponents.FLUID_CANISTER_MODE, getFillMode(itemStack).next().ordinal());
    }
}
