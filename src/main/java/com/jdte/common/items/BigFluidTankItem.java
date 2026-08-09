package com.jdte.common.items;

import com.direwolf20.justdirethings.JustDireThings;
import com.direwolf20.justdirethings.common.items.FluidCanister;
import com.jdte.JDTE;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/** A 1000 bucket canister with the four fill modes introduced in JDTE 0.5.8. */
public class BigFluidTankItem extends FluidCanister implements ICurioItem {
    private static final String FILL_MODE_KEY = "JDTEBigFluidTankFillMode";
    public static final int MAX_MB = 1_000_000;

    public enum FillMode {
        NONE("none"), JDT_ONLY("jdtonly"), JDTE_ONLY("jdteonly"), ALL("all");

        private final String translation;

        FillMode(String translation) {
            this.translation = translation;
        }

        public Component tooltip() {
            return Component.translatable(JustDireThings.MODID + ".fillmode." + translation);
        }

        public FillMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public BigFluidTankItem() {
        super();
    }

    @Override
    public int getMaxMB() {
        return MAX_MB;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK) {
            if (player.isShiftKeyDown()) {
                if (placeFluid(level, player, stack, hit)) {
                    return InteractionResultHolder.success(stack);
                }
            } else if (pickupFluid(level, player, stack, hit)
                    || placeFluid(level, player, stack, hit)) {
                return InteractionResultHolder.success(stack);
            }
        } else if (player.isShiftKeyDown()) {
            nextFillMode(stack);
            player.displayClientMessage(Component.translatable("justdirethings.fillmode.changed",
                    getBigFillMode(stack).tooltip()), true);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        FillMode mode = getBigFillMode(stack);
        if (mode == FillMode.NONE) {
            return;
        }
        IFluidHandlerItem source = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (source == null || source.getFluidInTank(0).isEmpty()) {
            return;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack targetStack = player.getInventory().getItem(i);
            if (targetStack.isEmpty() || targetStack.getItem() instanceof BigFluidTankItem) {
                continue;
            }
            if (mode == FillMode.JDT_ONLY && !JustDireThings.MODID.equals(targetStack.getItem().getCreatorModId(targetStack))) {
                continue;
            }
            if (mode == FillMode.JDTE_ONLY && !JDTE.MODID.equals(targetStack.getItem().getCreatorModId(targetStack))) {
                continue;
            }
            IFluidHandlerItem target = targetStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
            if (target == null) {
                continue;
            }
            FluidStack fluid = source.getFluidInTank(0);
            int amount = Math.min(100, fluid.getAmount());
            int accepted = target.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) {
                continue;
            }
            FluidStack drained = source.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
            target.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            if (source.getFluidInTank(0).isEmpty()) {
                return;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler != null) {
            FluidStack fluid = handler.getFluidInTank(0);
            tooltip.add(Component.translatable("tooltip.jdte.big_fluid_tank.amount",
                    fluid.isEmpty() ? "-" : fluid.getDisplayName(), fluid.getAmount(), MAX_MB)
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.jdte.big_fluid_tank.mode",
                    getBigFillMode(stack).tooltip()).withStyle(ChatFormatting.GRAY));
        }
    }

    public static FillMode getBigFillMode(ItemStack stack) {
        if (!stack.hasTag()) {
            return FillMode.NONE;
        }
        int ordinal = stack.getTag().getInt(FILL_MODE_KEY);
        return ordinal >= 0 && ordinal < FillMode.values().length ? FillMode.values()[ordinal] : FillMode.NONE;
    }

    public static void nextFillMode(ItemStack stack) {
        stack.getOrCreateTag().putInt(FILL_MODE_KEY, getBigFillMode(stack).next().ordinal());
    }
}
