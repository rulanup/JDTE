package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.interfaces.FluidContainingItem;
import com.direwolf20.justdirethings.common.items.interfaces.PoweredItem;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.entities.UltimateTimeWandEntity;
import com.jdte.common.integrations.ae2.ExtendedTimeAcceleratorAE2Integration;
import com.jdte.setup.JDTEConfig;
import com.jdte.common.items.UltimateTimeWandData.WandState;
import com.jdte.common.manager.EntityAccelerationManager;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.List;

/** Applies one server-owned, bounded Ultimate Time Wand request to the clicked target. */
public class UltimateTimeWandItem extends Item implements FluidContainingItem, PoweredItem {
    private static final ThreadLocal<Boolean> ACTIVATING=ThreadLocal.withInitial(()->false);
    public static final String MODE="jdte:ultimate_time_wand_mode",PENDING_FLUID="jdte:ultimate_time_wand_pending_fluid",ENTITY_UPGRADE=EntityAccelerationUpgradeItem.WAND_FLAG,ULTIMATE_OVERCLOCK="jdte:ultimate_time_wand_ultimate_overclock";
    private static boolean readBoolean(ItemStack stack,String key){return stack.hasTag()&&stack.getTag().getBoolean(key);}
    private static String readString(ItemStack stack,String key,String fallback){return stack.hasTag()&&stack.getTag().contains(key)?stack.getTag().getString(key):fallback;}
    private static double readDouble(ItemStack stack,String key){double value=stack.hasTag()?stack.getTag().getDouble(key):0;return Double.isFinite(value)&&value>=0&&value<1?value:0;}
    private static int energyIn(ItemStack stack){return stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);}
    private static boolean canInteract(ServerLevel level,Player player,ItemStack stack,BlockPos pos){return level.getServer().isSameThread()&&level.getServer().isRunning()&&!EntityAccelerationManager.isSuppressingAi()
        && player.level()==level && player.isAlive() && !player.isSpectator() && player.getAbilities().mayBuild
        && stack.getItem() instanceof UltimateTimeWandItem && (player.getMainHandItem()==stack||player.getOffhandItem()==stack)
        && level.isLoaded(pos) && level.mayInteract(player,pos);}
    @Override public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack,net.minecraft.nbt.CompoundTag nbt){return new WandItemCapabilities(stack);}

    public enum InteractionTarget {
        AIR,
        BLOCK
    }

    enum InteractionAction {
        PASS,
        CYCLE_MODE,
        ACCELERATE
    }

    public UltimateTimeWandItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public int getMaxMB() {
        return configuredFluidCapacity();
    }

    @Override
    public int getMaxEnergy() {
        return configuredEnergyCapacity();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (useInteractionTarget(player.isShiftKeyDown()) != InteractionAction.CYCLE_MODE) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide()) {
            cycleMode(player, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (useOnInteractionTarget(player.isShiftKeyDown()) != InteractionAction.ACCELERATE) {
            return InteractionResult.PASS;
        }
        if(ACTIVATING.get())return InteractionResult.FAIL;
        ACTIVATING.set(true);
        try{return applyToTarget(serverLevel,player,stack,context.getClickedPos())?InteractionResult.SUCCESS:InteractionResult.FAIL;}finally{ACTIVATING.remove();}
    }

    private boolean applyToTarget(ServerLevel level, Player player, ItemStack stack, BlockPos pos) {
        if (!canInteract(level,player,stack,pos) || player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos))>Math.pow(player.getBlockReach()+1.5,2) || !isValidTarget(level, pos)) {
            return false;
        }

        UltimateTimeWandEntity existing = findExisting(level, pos);
        WandState before = existing == null
                ? UltimateTimeWandData.initialState(pos, 0, configuredDuration())
                : existing.state();
        UltimateTimeWandData.Mode mode = getMode(stack);
        int maxExponent = getMaxExponent(stack);
        int finalExponent = UltimateTimeWandData.addStep(before.exponent(), mode, maxExponent);
        if (finalExponent <= before.exponent()) {
            return false;
        }

        int multiplier = UltimateTimeWandData.multiplierForExponent(finalExponent);
        UltimateTimeWandData.FluidSettlement fluidSettlement = UltimateTimeWandData.settleFluid(
                pendingFluid(stack), fluidCost(multiplier),
                keepsFractionalFluidSettlement());
        int energyCost = UltimateTimeWandData.saturatingEnergyCost(multiplier, scaledEnergyBaseCost());
        UltimateTimeWandData.OperationResult operation = planWithResources(
                player, stack, before, mode, fluidSettlement.drainMb(), energyCost, maxExponent);
        if (!operation.success()) {
            return false;
        }

        boolean creative = player.getAbilities().instabuild;
        ServerCommitPort commitPort = new ServerCommitPort(level, stack, pos, existing, before);
        if (!creative && !hasFluidForSettlement(stack, fluidSettlement)) {
            return false;
        }
        if (!commitIfTargetValid(true, creative, operation, commitPort).success()) {
            return false;
        }
        if (!creative) {
            stack.getOrCreateTag().putDouble(PENDING_FLUID,fluidSettlement.remainingCost());
        }
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6F, 1.2F);
        return true;
    }

    private static boolean isValidTarget(ServerLevel level,BlockPos pos){return com.jdte.common.blockentities.UltimateTimeWandTargetRuntime.resolve(level,pos).isPresent();}

    private static UltimateTimeWandEntity findExisting(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(UltimateTimeWandEntity.class, new AABB(pos),
                        entity -> pos.equals(entity.getTarget()))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public static UltimateTimeWandData.Mode getMode(ItemStack stack) {
        return UltimateTimeWandData.Mode.fromName(readString(stack,MODE,"normal"));
    }

    public static double pendingFluid(ItemStack stack) {
        return Math.max(0.0D, readDouble(stack,PENDING_FLUID));
    }

    private static boolean hasFluidForSettlement(ItemStack stack, UltimateTimeWandData.FluidSettlement settlement) {
        var handler=stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if(handler==null || (!handler.getFluidInTank(0).isEmpty() && !(handler.getFluidInTank(0).getFluid() instanceof com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid)))return false;
        int available = FluidContainingItem.getAvailableFluid(stack);
        return available >= settlement.drainMb() && (settlement.drainMb() > 0 || settlement.remainingCost() <= 0.0D || available > 0);
    }

    private static double fluidCost(int multiplier) {
        return multiplier * (double) Config.TIME_WAND_FLUID_COST.get()
                * JDTEConfig.COMMON.ultimateTimeWandBaseCostMultiplier.get() / 600.0D;
    }

    private static int scaledEnergyBaseCost() {
        double scaled = Math.max(0.0D, Config.TIME_WAND_FE_COST.get()
                * JDTEConfig.COMMON.ultimateTimeWandEnergyCostMultiplier.get());
        return scaled >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) scaled;
    }

    static int configuredDuration() {
        return JDTEConfig.COMMON.ultimateTimeWandDuration.get();
    }

    static int configuredFluidCapacity() {
        return JDTEConfig.COMMON.ultimateTimeWandFluidCapacity.get();
    }

    static int configuredEnergyCapacity() {
        return JDTEConfig.COMMON.ultimateTimeWandEnergyCapacity.get();
    }

    static boolean keepsFractionalFluidSettlement() {
        return JDTEConfig.COMMON.ultimateTimeWandFractionalFluidSettlement.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.ultimate_time_wand").withStyle(ChatFormatting.GRAY));
        if (hasUltimateOverclock(stack)) {
            tooltip.add(Component.translatable("tooltip.jdte.ultimate_time_wand.ultimate_overclock_installed")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (hasEntityAcceleration(stack)) {
            tooltip.add(Component.translatable("tooltip.jdte.ultimate_time_wand.entity_acceleration_installed")
                    .withStyle(ChatFormatting.GOLD));
        }
        if (stack.hasTag() && stack.getTag().contains(MODE)) {
            tooltip.add(modeTooltipComponent(getMode(stack)));
        }
        tooltip.addAll(resourceTooltipComponents(Math.max(0, FluidContainingItem.getAvailableFluid(stack)),
                configuredFluidCapacity(), Math.max(0, energyIn(stack)),
                configuredEnergyCapacity()));
    }

    public static boolean hasEntityAcceleration(ItemStack stack) {
        return readBoolean(stack,ENTITY_UPGRADE);
    }

    public static void setEntityAcceleration(ItemStack stack, boolean enabled) {
        stack.getOrCreateTag().putBoolean(ENTITY_UPGRADE, enabled);
    }

    public static boolean hasUltimateOverclock(ItemStack stack) {
        return readBoolean(stack,ULTIMATE_OVERCLOCK);
    }

    public static void setUltimateOverclock(ItemStack stack, boolean enabled) {
        stack.getOrCreateTag().putBoolean(ULTIMATE_OVERCLOCK, enabled);
    }

    public static int getMaxExponent(ItemStack stack) {
        return hasUltimateOverclock(stack) ? UltimateTimeWandData.ULTIMATE_MAX_EXPONENT : UltimateTimeWandData.MAX_EXPONENT;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, net.minecraft.world.inventory.Slot slot,
                                            net.minecraft.world.inventory.ClickAction action, Player player,
                                            net.minecraft.world.entity.SlotAccess access) {
        if (action != net.minecraft.world.inventory.ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        if (other.is(com.jdte.setup.JDTEItems.ENTITY_ACCELERATION_UPGRADE.get()) && !hasEntityAcceleration(stack)) {
            setEntityAcceleration(stack, true);
            other.shrink(1);
            if (player != null) {
                player.playSound(SoundEvents.ARMOR_EQUIP_NETHERITE, 1.0F, 1.2F);
            }
            return true;
        }
        if (other.is(com.jdte.setup.JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()) && !hasUltimateOverclock(stack)) {
            setUltimateOverclock(stack, true);
            other.shrink(1);
            if (player != null) {
                player.playSound(SoundEvents.ARMOR_EQUIP_NETHERITE, 1.0F, 1.2F);
            }
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, net.minecraft.world.entity.LivingEntity interactionTarget, InteractionHand hand) {
        if (!hasEntityAcceleration(stack)) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("message.jdte.ultimate_time_wand.requires_entity_acceleration")
                                .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }

        Level level = player.level();
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return applyToEntityTarget(serverLevel, player, stack, interactionTarget)
                ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    private static Component modeTooltipComponent(UltimateTimeWandData.Mode currentMode) {
        UltimateTimeWandData.Mode activeMode = currentMode == null
                ? UltimateTimeWandData.Mode.NORMAL : currentMode;
        MutableComponent line = Component.literal("[").withStyle(ChatFormatting.GRAY);
        UltimateTimeWandData.Mode[] modes = UltimateTimeWandData.Mode.values();
        for (int index = 0; index < modes.length; index++) {
            if (index > 0) {
                line.append(Component.literal(" | ").withStyle(ChatFormatting.GRAY));
            }
            UltimateTimeWandData.Mode mode = modes[index];
            line.append(Component.translatable("tooltip.jdte.ultimate_time_wand.mode." + mode.serializedName())
                    .withStyle(mode == activeMode ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }
        return line.append(Component.literal(" ]").withStyle(ChatFormatting.GRAY));
    }

    public static List<Component> resourceTooltipComponents(int currentFluid, int maxFluid,
                                                           int currentEnergy, int maxEnergy) {
        return List.of(
                Component.translatable("justdirethings.festored",
                        MagicHelpers.formatted(currentEnergy), MagicHelpers.formatted(maxEnergy))
                        .withStyle(ChatFormatting.GREEN),
                Component.translatable("justdirethings.timefluidamt",
                        MagicHelpers.formatted(currentFluid), MagicHelpers.formatted(maxFluid))
                        .withStyle(ChatFormatting.GREEN));
    }

    InteractionAction useInteractionTarget(boolean shiftDown) {
        return dispatchInteraction(InteractionTarget.AIR, shiftDown);
    }

    InteractionAction useOnInteractionTarget(boolean shiftDown) {
        return dispatchInteraction(InteractionTarget.BLOCK, shiftDown);
    }

    private static InteractionAction dispatchInteraction(InteractionTarget target, boolean shiftDown) {
        return switch (target) {
            case BLOCK -> InteractionAction.ACCELERATE;
            case AIR -> shiftDown ? InteractionAction.CYCLE_MODE : InteractionAction.PASS;
        };
    }

    private static UltimateTimeWandData.OperationResult planWithResources(
            Player player, ItemStack stack, WandState state, UltimateTimeWandData.Mode mode,
            int fluidCost, int energyCost, int maxExponent) {
        if (player.getAbilities().instabuild) {
            return UltimateTimeWandData.planOperation(state, mode, fluidCost, energyCost, maxExponent);
        }
        return UltimateTimeWandData.applyIfAffordable(state, mode,
                FluidContainingItem.getAvailableFluid(stack), energyIn(stack), fluidCost, energyCost, maxExponent);
    }

    static CommitOutcome commitIfTargetValid(boolean targetValid, boolean creative,
                                             UltimateTimeWandData.OperationResult operation, CommitPort port) {
        if (!targetValid) {
            return CommitOutcome.TARGET_REJECTED;
        }
        if (!operation.success()) {
            return CommitOutcome.OPERATION_REJECTED;
        }
        if (!creative && !port.prepareResources(operation)) {
            return CommitOutcome.ROLLED_BACK;
        }
        if (!port.applyEntity(operation.state())) {
            return CommitOutcome.ENTITY_REJECTED;
        }
        if (creative) {
            return CommitOutcome.SUCCESS;
        }
        if (!port.commitResources()) {
            port.rollbackEntity();
            return CommitOutcome.ROLLED_BACK;
        }
        return CommitOutcome.SUCCESS;
    }

    enum CommitOutcome {
        SUCCESS,
        TARGET_REJECTED,
        OPERATION_REJECTED,
        ENTITY_REJECTED,
        ROLLED_BACK;

        boolean success() {
            return this == SUCCESS;
        }
    }

    interface CommitPort {
        boolean prepareResources(UltimateTimeWandData.OperationResult operation);

        boolean applyEntity(WandState after);

        void rollbackEntity();

        boolean commitResources();
    }

    private static final class ServerCommitPort implements CommitPort {
        private final ServerLevel level;
        private final ItemStack stack;
        private final BlockPos target;
        private final UltimateTimeWandEntity existing;
        private final WandState before;
        private UltimateTimeWandEntity created;
        private ItemStackNbtTransaction preparedResources;

        private ServerCommitPort(ServerLevel level, ItemStack stack, BlockPos target,
                                 UltimateTimeWandEntity existing, WandState before) {
            this.level = level;
            this.stack = stack;
            this.target = target;
            this.existing = existing;
            this.before = before;
        }

        @Override
        public boolean prepareResources(UltimateTimeWandData.OperationResult operation) {
            preparedResources = ItemStackNbtTransaction.prepare(stack, staged -> {
                if (operation.fluidCost() > 0) {
                    IFluidHandlerItem fluid = staged.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
                    if (fluid == null || fluid.drain(operation.fluidCost(), IFluidHandler.FluidAction.EXECUTE).getAmount()
                            != operation.fluidCost()) {
                        return false;
                    }
                }
                if (operation.energyCost() > 0) {
                    IEnergyStorage energy = staged.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                    if (energy == null || energy.extractEnergy(operation.energyCost(), false) != operation.energyCost()) {
                        return false;
                    }
                }
                return true;
            }).orElse(null);
            return preparedResources != null;
        }

        @Override
        public boolean applyEntity(WandState after) {
            if(!isValidTarget(level,target))return false;
            if (existing != null) {
                if(existing.isRemoved())return false;
                existing.applyWandState(after);
                return true;
            }
            created = new UltimateTimeWandEntity(level, target, after.exponent(), after.totalTime());
            return level.addFreshEntity(created);
        }

        @Override
        public void rollbackEntity() {
            if (existing != null) {
                existing.applyWandState(before);
            } else if (created != null) {
                created.discard();
            }
        }

        @Override
        public boolean commitResources() {
            return isValidTarget(level,target) && preparedResources != null && preparedResources.commit(stack);
        }
    }

    public static boolean isValidEntityTarget(net.minecraft.world.entity.Entity entity) {
        return entity != null
                && entity.isAlive()
                && !entity.isRemoved()
                && !(entity instanceof Player)
                && !(entity instanceof UltimateTimeWandEntity)
                && !(entity instanceof com.jdte.common.entities.TimeAcceleratorEffectEntity);
    }

    public static boolean applyToEntityTarget(ServerLevel level, Player player, ItemStack stack, net.minecraft.world.entity.Entity target) {
        if(level==null||player==null||stack==null||ACTIVATING.get())return false;ACTIVATING.set(true);
        try{return applyEntityTarget(level,player,stack,target);}finally{ACTIVATING.remove();}
    }

    private static boolean applyEntityTarget(ServerLevel level,Player player,ItemStack stack,net.minecraft.world.entity.Entity target){
        if (!hasEntityAcceleration(stack) || !canInteract(level,player,stack,target==null?player.blockPosition():target.blockPosition()) || target==null || target.level()!=level || !player.canReach(target,1.5) || !isValidEntityTarget(target)) {
            player.displayClientMessage(Component.translatable("message.jdte.ultimate_time_wand.invalid_target")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }

        var existingData=EntityAccelerationManager.get(target);
        WandState before = existingData == null || !existingData.isActive()
                ? UltimateTimeWandData.initialState(target.blockPosition(), 0, configuredDuration())
                : new WandState(target.blockPosition(), existingData.getExponent(), existingData.getTotalTime(), existingData.getRemainingTime());

        UltimateTimeWandData.Mode mode = getMode(stack);
        int maxExponent = getMaxExponent(stack);
        int finalExponent = UltimateTimeWandData.addStep(before.exponent(), mode, maxExponent);
        if (finalExponent <= before.exponent()) {
            player.displayClientMessage(Component.translatable("message.jdte.ultimate_time_wand.max_multiplier")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }

        int multiplier = UltimateTimeWandData.multiplierForExponent(finalExponent);
        UltimateTimeWandData.FluidSettlement fluidSettlement = UltimateTimeWandData.settleFluid(
                pendingFluid(stack), fluidCost(multiplier),
                keepsFractionalFluidSettlement());
        int energyCost = UltimateTimeWandData.saturatingEnergyCost(multiplier, scaledEnergyBaseCost());
        UltimateTimeWandData.OperationResult operation = planWithResources(
                player, stack, before, mode, fluidSettlement.drainMb(), energyCost, maxExponent);
        if (!operation.success()) {
            player.displayClientMessage(Component.translatable("message.jdte.ultimate_time_wand.insufficient_resources")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }

        boolean creative = player.getAbilities().instabuild;
        EntityCommitPort commitPort = new EntityCommitPort(target, stack, before);
        if (!creative && !hasFluidForSettlement(stack, fluidSettlement)) {
            player.displayClientMessage(Component.translatable("message.jdte.ultimate_time_wand.insufficient_resources")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (!commitIfTargetValid(true, creative, operation, commitPort).success()) {
            return false;
        }
        if (!creative) {
            stack.getOrCreateTag().putDouble(PENDING_FLUID,fluidSettlement.remainingCost());
        }
        level.playSound(null, target.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6F, 1.2F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                20, target.getBbWidth() * 0.5D, target.getBbHeight() * 0.25D, target.getBbWidth() * 0.5D, 0.1D);
        return true;
    }

    private static final class EntityCommitPort implements CommitPort {
        private final net.minecraft.world.entity.Entity target;
        private final ItemStack stack;
        private final WandState before;
        private final boolean hadEffect;
        private ItemStackNbtTransaction preparedResources;

        private EntityCommitPort(net.minecraft.world.entity.Entity target, ItemStack stack, WandState before) {
            this.target = target;
            this.stack = stack;
            this.before = before;
            this.hadEffect=EntityAccelerationManager.get(target)!=null;
        }

        @Override
        public boolean prepareResources(UltimateTimeWandData.OperationResult operation) {
            preparedResources = ItemStackNbtTransaction.prepare(stack, staged -> {
                if (operation.fluidCost() > 0) {
                    IFluidHandlerItem fluid = staged.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
                    if (fluid == null || fluid.drain(operation.fluidCost(), IFluidHandler.FluidAction.EXECUTE).getAmount()
                            != operation.fluidCost()) {
                        return false;
                    }
                }
                if (operation.energyCost() > 0) {
                    IEnergyStorage energy = staged.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                    if (energy == null || energy.extractEnergy(operation.energyCost(), false) != operation.energyCost()) {
                        return false;
                    }
                }
                return true;
            }).orElse(null);
            return preparedResources != null;
        }

        @Override
        public boolean applyEntity(WandState after) {
            if(!isValidEntityTarget(target)||target.level().isClientSide||!target.level().hasChunkAt(target.blockPosition()))return false;
            EntityAccelerationManager.set(target,new com.jdte.common.entities.EntityAccelerationData(after.exponent(),after.totalTime(),after.remainingTime()));
            return true;
        }

        @Override
        public void rollbackEntity() {
            if (hadEffect && before.remainingTime() > 0) {
                EntityAccelerationManager.set(target,new com.jdte.common.entities.EntityAccelerationData(before.exponent(),before.totalTime(),before.remainingTime()));
            } else {
                EntityAccelerationManager.clear(target);
            }
        }

        @Override
        public boolean commitResources() {
            return isValidEntityTarget(target) && preparedResources != null && preparedResources.commit(stack);
        }
    }

    private static void cycleMode(Player player, ItemStack stack) {
        UltimateTimeWandData.Mode next = getMode(stack).next();
        stack.getOrCreateTag().putString(MODE,next.serializedName());
        player.displayClientMessage(Component.translatable("message.jdte.ultimate_time_wand.mode", next.serializedName())
                .withStyle(ChatFormatting.AQUA), true);
    }
}
