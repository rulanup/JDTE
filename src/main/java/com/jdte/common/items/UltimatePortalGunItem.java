package com.jdte.common.items;

import com.direwolf20.justdirethings.common.entities.PortalProjectile;
import com.direwolf20.justdirethings.common.capabilities.FluidHandlerItemStack;
import com.direwolf20.justdirethings.common.items.PortalGunV2;
import com.direwolf20.justdirethings.common.items.interfaces.FluidContainingItem;
import com.direwolf20.justdirethings.common.items.interfaces.PoweredItem;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.NBTHelpers;
import com.direwolf20.justdirethings.util.TooltipHelpers;
import com.jdte.setup.JDTEConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Forge 1.20.1 port of the 0.5.8 unlimited-slot advanced portal gun. */
public class UltimatePortalGunItem extends PortalGunV2 {
    public static final int MAX_MB = 1_000_000;
    public static final int SEGMENTS_PER_PAGE = 12;
    public static final int MANUAL_CROSS_DIMENSION_COST = 1_000_000;
    public static final int MANUAL_PER_BLOCK_COST = 1000;
    public static final int MANUAL_MAX_DISTANCE_COST = 500_000;

    private static final String DESTINATIONS_KEY = "JDTEUltimateDestinations";
    private static final String POSITION_KEY = "JDTEUltimateFavoritePos";
    private static final String MANUAL_SLOTS_KEY = "JDTEUltimateManualSlots";

    public UltimatePortalGunItem() {
        super();
    }

    /**
     * JDT attaches a generic fluid handler to every {@link FluidContainingItem}.
     * The portal gun needs a more specific handler so pipes and tanks cannot
     * fill it with Time Fluid or any other fluid.
     */
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        IFluidHandlerItem handler = new PortalFluidHandler(stack, getMaxMB());
        LazyOptional<IFluidHandlerItem> optional = LazyOptional.of(() -> handler);
        return new ICapabilityProvider() {
            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
                return capability == ForgeCapabilities.FLUID_HANDLER_ITEM ? optional.cast() : LazyOptional.empty();
            }
        };
    }

    public static ItemStack find(Player player) {
        if (player.getMainHandItem().getItem() instanceof UltimatePortalGunItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof UltimatePortalGunItem) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    public static List<NBTHelpers.PortalDestination> getDestinations(ItemStack stack) {
        List<NBTHelpers.PortalDestination> destinations = readDestinations(stack);
        if (destinations.isEmpty()) {
            ensurePage(stack);
            destinations = readDestinations(stack);
        }
        return destinations;
    }

    private static List<NBTHelpers.PortalDestination> readDestinations(ItemStack stack) {
        List<NBTHelpers.PortalDestination> destinations = new ArrayList<>();
        ListTag tag = stack.getOrCreateTag().getList(DESTINATIONS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < tag.size(); i++) {
            CompoundTag entry = tag.getCompound(i);
            destinations.add(entry.isEmpty() ? null : NBTHelpers.PortalDestination.fromNBT(entry));
        }
        if (destinations.isEmpty()) {
            List<NBTHelpers.PortalDestination> legacy = PortalGunV2.getFavorites(stack);
            if (legacy.stream().anyMatch(destination -> destination != null)) {
                destinations.addAll(legacy);
            }
        }
        return destinations;
    }

    private static void writeDestinations(ItemStack stack, List<NBTHelpers.PortalDestination> destinations) {
        ListTag tag = new ListTag();
        for (NBTHelpers.PortalDestination destination : destinations) {
            tag.add(destination == null ? new CompoundTag() : destination.toNBT());
        }
        stack.getOrCreateTag().put(DESTINATIONS_KEY, tag);
        alignManualFlags(stack, destinations.size());
    }

    private static void normalizeList(ItemStack stack) {
        List<NBTHelpers.PortalDestination> destinations = readDestinations(stack);
        while (destinations.size() % SEGMENTS_PER_PAGE != 0) {
            destinations.add(null);
        }
        writeDestinations(stack, destinations);
    }

    public static void ensurePage(ItemStack stack) {
        normalizeList(stack);
        List<NBTHelpers.PortalDestination> destinations = readDestinations(stack);
        for (int i = 0; i < SEGMENTS_PER_PAGE; i++) {
            destinations.add(null);
        }
        writeDestinations(stack, destinations);
    }

    public static void trimEmptyLastPage(ItemStack stack) {
        List<NBTHelpers.PortalDestination> destinations = readDestinations(stack);
        while (destinations.size() > SEGMENTS_PER_PAGE
                && destinations.subList(destinations.size() - SEGMENTS_PER_PAGE, destinations.size()).stream()
                .allMatch(destination -> destination == null)) {
            destinations.subList(destinations.size() - SEGMENTS_PER_PAGE, destinations.size()).clear();
        }
        writeDestinations(stack, destinations);
        setFavoritePosition(stack, Math.min(getFavoritePosition(stack), Math.max(0, destinations.size() - 1)));
    }

    public static void fillDestination(ItemStack stack, int position, NBTHelpers.PortalDestination destination) {
        normalizeList(stack);
        List<NBTHelpers.PortalDestination> destinations = readDestinations(stack);
        while (position >= destinations.size()) {
            ensurePage(stack);
            destinations = readDestinations(stack);
        }
        destinations.set(Math.max(0, position), destination);
        writeDestinations(stack, destinations);
        setManualFlag(stack, Math.max(0, position), false);
    }

    public static void setDestination(ItemStack stack, int position, NBTHelpers.PortalDestination destination) {
        List<NBTHelpers.PortalDestination> destinations = getDestinations(stack);
        if (position >= 0 && position < destinations.size()) {
            destinations.set(position, destination);
            writeDestinations(stack, destinations);
            setManualFlag(stack, position, true);
        }
    }

    public static void clearDestination(ItemStack stack, int position) {
        List<NBTHelpers.PortalDestination> destinations = getDestinations(stack);
        if (position >= 0 && position < destinations.size()) {
            destinations.set(position, null);
            writeDestinations(stack, destinations);
            setManualFlag(stack, position, false);
        }
    }

    public static NBTHelpers.PortalDestination getSelectedDestination(ItemStack stack) {
        List<NBTHelpers.PortalDestination> destinations = getDestinations(stack);
        int position = getFavoritePosition(stack);
        return position >= 0 && position < destinations.size() ? destinations.get(position) : null;
    }

    public static int getFavoritePosition(ItemStack stack) {
        List<NBTHelpers.PortalDestination> destinations = getDestinationsWithoutCreating(stack);
        return Math.max(0, Math.min(stack.getOrCreateTag().getInt(POSITION_KEY), Math.max(0, destinations.size() - 1)));
    }

    public static void setFavoritePosition(ItemStack stack, int position) {
        int size = Math.max(1, getDestinationsWithoutCreating(stack).size());
        stack.getOrCreateTag().putInt(POSITION_KEY, Math.max(0, Math.min(position, size - 1)));
    }

    private static List<NBTHelpers.PortalDestination> getDestinationsWithoutCreating(ItemStack stack) {
        return readDestinations(stack);
    }

    public static boolean isManualSlot(ItemStack stack, int index) {
        ListTag flags = stack.getOrCreateTag().getList(MANUAL_SLOTS_KEY, Tag.TAG_BYTE);
        return index >= 0 && index < flags.size()
                && ((net.minecraft.nbt.ByteTag) flags.get(index)).getAsByte() != 0;
    }

    private static void setManualFlag(ItemStack stack, int index, boolean manual) {
        if (index < 0) {
            return;
        }
        ListTag flags = stack.getOrCreateTag().getList(MANUAL_SLOTS_KEY, Tag.TAG_BYTE);
        while (flags.size() <= index) {
            flags.add(net.minecraft.nbt.ByteTag.valueOf(false));
        }
        flags.set(index, net.minecraft.nbt.ByteTag.valueOf(manual));
        stack.getOrCreateTag().put(MANUAL_SLOTS_KEY, flags);
    }

    private static void alignManualFlags(ItemStack stack, int size) {
        ListTag flags = stack.getOrCreateTag().getList(MANUAL_SLOTS_KEY, Tag.TAG_BYTE);
        while (flags.size() < size) {
            flags.add(net.minecraft.nbt.ByteTag.valueOf(false));
        }
        while (flags.size() > size) {
            flags.remove(flags.size() - 1);
        }
        stack.getOrCreateTag().put(MANUAL_SLOTS_KEY, flags);
    }

    @Override
    public int getMaxMB() {
        return JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunFluidCapacity.get();
    }

    @Override
    public int getMaxEnergy() {
        return JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunEnergyCapacity.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK
                && isPortalFluid(level.getFluidState(hit.getBlockPos()).getType())
                && FluidContainingItem.pickupFluid(level, player, stack, hit)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            return spawnProjectile(level, player, stack, true);
        }
        return InteractionResultHolder.pass(stack);
    }

    public static InteractionResultHolder<ItemStack> spawnProjectile(Level level, Player player, ItemStack stack, boolean primary) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }
        NBTHelpers.PortalDestination destination = player.isShiftKeyDown()
                ? PortalGunV2.getPrevious(stack) : getSelectedDestination(stack);
        if (destination == null || isDimensionBlacklisted(destination.dimension())) {
            return InteractionResultHolder.fail(stack);
        }
        int cost = calculateActualFluidCost(serverLevel, player, stack, destination);
        int energyCost = JDTEConfig.COMMON.ultimatePortalGun.energyCost.get();
        if (!hasEnoughPortalFluid(stack, cost)) {
            player.displayClientMessage(Component.translatable("justdirethings.lowportalfluid"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!PoweredItem.hasEnoughEnergy(stack, energyCost)) {
            player.displayClientMessage(Component.translatable("justdirethings.lowenergy"), true);
            return InteractionResultHolder.fail(stack);
        }
        int lifespan = com.direwolf20.justdirethings.common.items.PortalGunV2.getStayOpen(stack)
                ? -1 : Config.PORTAL_GUN_LIFESPAN.get();
        PortalProjectile projectile = new PortalProjectile(serverLevel, player,
                PortalGunV2.getOrCreateGunUUID(stack), primary, true, destination, lifespan);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.0F, 1.0F);
        serverLevel.addFreshEntity(projectile);
        PoweredItem.consumeEnergy(stack, energyCost);
        FluidContainingItem.consumeFluid(stack, cost);
        PortalGunV2.setPrevious(player, stack);
        return InteractionResultHolder.success(stack);
    }

    private static int calculateActualFluidCost(ServerLevel level, Player player, ItemStack stack,
                                                NBTHelpers.PortalDestination destination) {
        if (!isManualSlot(stack, getFavoritePosition(stack))) {
            return PortalGunV2.calculateFluidCost(level, player, destination);
        }
        if (!destination.dimension().equals(level.dimension())) {
            return MANUAL_CROSS_DIMENSION_COST;
        }
        return Math.min((int) Math.ceil(destination.position().distanceTo(player.position()) * MANUAL_PER_BLOCK_COST),
                MANUAL_MAX_DISTANCE_COST);
    }

    public static boolean isPortalFluid(net.minecraft.world.level.material.Fluid fluid) {
        return fluid.isSame(Registration.PORTAL_FLUID_SOURCE.get())
                || fluid.isSame(Registration.PORTAL_FLUID_FLOWING.get());
    }

    private static final class PortalFluidHandler extends FluidHandlerItemStack {
        private PortalFluidHandler(ItemStack stack, int capacity) {
            super(stack, capacity);
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            FluidStack stored = super.getFluidInTank(tank);
            if (!stored.isEmpty() && !isPortalFluid(stored.getFluid())) {
                // Remove wrong-fluid contents left by older builds instead of
                // exposing them as valid portal-gun fuel.
                CompoundTag tag = getContainer().getTag();
                if (tag != null) {
                    tag.remove(FluidHandlerItemStack.FLUID_NBT_KEY);
                }
                return FluidStack.EMPTY;
            }
            return stored;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack fluid) {
            return isPortalFluid(fluid.getFluid());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return !resource.isEmpty() && isPortalFluid(resource.getFluid())
                    ? super.fill(resource, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return !resource.isEmpty() && isPortalFluid(resource.getFluid())
                    ? super.drain(resource, action) : FluidStack.EMPTY;
        }
    }

    private static boolean hasEnoughPortalFluid(ItemStack stack, int amount) {
        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null) {
            return false;
        }
        FluidStack fluid = handler.getFluidInTank(0);
        return !fluid.isEmpty() && isPortalFluid(fluid.getFluid()) && fluid.getAmount() >= amount;
    }

    public static boolean isDimensionBlacklisted(net.minecraft.resources.ResourceKey<Level> dimension) {
        return JDTEConfig.COMMON.ultimatePortalGun.teleportDimensionBlacklist.get().contains(
                dimension.location().toString());
    }

    public static int getFullness(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null || handler.getFluidInTank(0).isEmpty()) {
            return 0;
        }
        double fullness = handler.getFluidInTank(0).getAmount() / (double) getCapacity(stack);
        return fullness <= 1.0D / 3.0D ? 1 : fullness <= 2.0D / 3.0D ? 2 : 3;
    }

    private static int getCapacity(ItemStack stack) {
        return Math.max(1, ((UltimatePortalGunItem) stack.getItem()).getMaxMB());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        TooltipHelpers.appendFEText(stack, tooltip);
        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler != null) {
            tooltip.add(Component.translatable("tooltip.jdte.ultimate_portal_gun.fluid",
                    handler.getFluidInTank(0).getAmount(), getMaxMB()).withStyle(ChatFormatting.GREEN));
        }
        tooltip.add(Component.translatable("tooltip.jdte.ultimate_portal_gun.slots",
                getDestinations(stack).size()).withStyle(ChatFormatting.AQUA));
    }
}
