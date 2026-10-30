package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.ExperienceUtils;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.interfacehelpers.*;
import com.jdte.mixin.ExperienceOrbAccessor;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/** Source standalone XP store: no FE or separate fluid buffer. */
public class ExtendedExperienceHolderBE extends BaseMachineBE implements AreaAffectingBE, RedstoneControlledBE, ExtendedUpgradeMachine {
    public AreaAffectingData areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING).getOpposite());
    public RedstoneControlData redstoneControlData = getDefaultRedstoneData();
    public int exp;
    public int targetExp;
    public boolean collectExp;
    public boolean ownerOnly;
    public boolean showParticles = true;
    private Player currentPlayer;
    private long capabilityGeneration;
    private LazyOptional<IFluidHandler> fluidCapability = LazyOptional.empty();

    public ExtendedExperienceHolderBE(BlockPos pos, BlockState state) { super(JDTEBlockEntities.EXTENDED_EXPERIENCE_HOLDER.get(), pos, state); }
    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public AreaAffectingData getAreaAffectingData() { return areaAffectingData; }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneControlData; }
    @Override public RedstoneControlData getDefaultRedstoneData() { return new RedstoneControlData(MiscHelpers.RedstoneMode.PULSE); }
    @Override public AreaAffectingData getDefaultAreaData() { return new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING).getOpposite()); }

    private boolean live() {
        return level instanceof ServerLevel server && server.getServer().isSameThread() && !server.getServer().isStopped()
                && !isRemoved() && server.hasChunkAt(worldPosition) && server.getBlockEntity(worldPosition) == this;
    }
    private boolean permitted(Player player) {
        return live() && player != null && !player.isRemoved() && player.isAlive() && player.level() == level
                && (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.connection == null || !serverPlayer.hasDisconnected())
                && (!ownerOnly || player.getUUID().equals(placedByUUID));
    }
    public void changeSettings(Player player, int target, boolean owner, boolean collect, boolean particles) {
        if (!permitted(player) || target < 0 || target > 1000) return;
        targetExp = target; ownerOnly = owner; collectExp = collect; showParticles = particles;
        currentPlayer = null; markDirtyClient();
    }
    public int addExp(int addition) {
        if (!live() || addition <= 0) return Math.max(0, addition);
        int accepted = ExperienceStorageMath.acceptedXp(exp, addition);
        exp += accepted;
        if (accepted != 0) markDirtyClient();
        return addition - accepted;
    }
    public int subExp(int subtraction) {
        if (!live() || subtraction <= 0) return Math.max(0, subtraction);
        int taken = Math.min(exp, subtraction); exp -= taken;
        if (taken != 0) markDirtyClient();
        return subtraction - taken;
    }
    /** Source level buttons count a partial level as their first level; only accepted XP leaves the player. */
    public void storeExp(Player player, int levels) {
        if (!permitted(player) || (levels != -1 && levels != 1 && levels != 10)) return;
        int total = ExperienceUtils.getPlayerTotalExperience(player);
        int target = levels == -1 ? 0 : ExperienceUtils.getTotalExperienceForLevel(Math.max(0,
                player.experienceLevel - levels + (player.experienceProgress > 0 ? 1 : 0)));
        int amount = ExperienceStorageMath.acceptedXp(exp, Math.max(0, total - target));
        if (amount == 0) return;
        setPlayerExperience(player, total - amount); exp += amount; markDirtyClient();
    }
    public void extractExp(Player player, int levels) {
        if (!permitted(player) || (levels != -1 && levels != 1 && levels != 10)) return;
        int total = ExperienceUtils.getPlayerTotalExperience(player);
        int wanted = levels == -1 ? Integer.MAX_VALUE : Math.max(0,
                ExperienceUtils.getTotalExperienceForLevel(player.experienceLevel + levels) - total);
        int amount = Math.min(exp, ExperienceStorageMath.acceptedXp(total, wanted));
        if (amount == 0) return;
        setPlayerExperience(player, total + amount); exp -= amount; markDirtyClient();
    }
    private static void setPlayerExperience(Player player, int total) {
        int level = ExperienceUtils.getLevelFromTotalExperience(total);
        player.experienceLevel = level;
        player.experienceProgress = (float) (total - ExperienceUtils.getTotalExperienceForLevel(level))
                / ExperienceUtils.getExperienceForNextLevel(level);
        player.totalExperience = total;
        // Notify ServerPlayer's vanilla XP packet tracker without a potentially overflowing delta.
        player.giveExperiencePoints(0);
    }
    @Override public void tickClient() {}
    @Override public void tickServer() {
        if (!live()) { currentPlayer = null; return; }
        super.tickServer();
        if (collectExp && operationTicks == 0) collectOrbs();
        if (currentPlayer != null && (!permitted(currentPlayer) || !getAABB(worldPosition).intersects(currentPlayer.getBoundingBox()))) currentPlayer = null;
        if (currentPlayer == null && isActiveRedstone() && canRun()) {
            for (Player player : level.getEntitiesOfClass(Player.class, getAABB(worldPosition), this::permitted)) {
                if (player.experienceLevel != targetExp || player.experienceProgress > 0.01f) { currentPlayer = player; break; }
            }
        }
        if (currentPlayer == null) return;
        Player player = currentPlayer;
        int before = exp;
        if (player.experienceLevel < targetExp && exp > 0) extractExp(player, 1);
        else if (player.experienceLevel > targetExp || (player.experienceLevel == targetExp && player.experienceProgress > 0.01f)) storeExp(player, 1);
        else { currentPlayer = null; return; }
        if (exp != before) doParticles(player.getEyePosition().subtract(0, 0.25, 0), exp > before);
        if (exp == before || exp == 0) currentPlayer = null;
    }
    private void collectOrbs() {
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, getAABB(worldPosition), e -> e.isAlive() && !e.isRemoved())) {
            var accessor = (ExperienceOrbAccessor) orb;
            int count = accessor.jdte$getCount();
            int accepted = ExperienceStorageMath.acceptedOrbCount(exp, orb.getValue(), count);
            if (accepted == 0) continue;
            // Whole multiplicities preserve merged-orb XP and leave any overflow in the world.
            exp += accepted * orb.getValue();
            if (accepted == count) orb.discard(); else accessor.jdte$setCount(count - accepted);
            doParticles(orb.position(), true); markDirtyClient();
        }
    }
    private void doParticles(Vec3 source, boolean toBlock) {
        if (!showParticles || !(level instanceof ServerLevel server)) return;
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        Vec3 tip = Vec3.atCenterOf(worldPosition).subtract(facing.getStepX()*0.3, facing.getStepY()*0.3, facing.getStepZ()*0.3);
        Vec3 start = toBlock ? source : tip, end = toBlock ? tip : source;
        server.sendParticles(new ItemFlowParticleData(new ItemStack(Items.EXPERIENCE_BOTTLE),end.x,end.y,end.z,1),start.x,start.y,start.z,10,0,0,0,0);
    }
    @Override public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("exp", exp); tag.putInt("targetExp", targetExp); tag.putBoolean("collectExp", collectExp);
        tag.putBoolean("ownerOnly", ownerOnly); tag.putBoolean("showParticles", showParticles);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); exp = Math.max(0, tag.getInt("exp")); targetExp = Math.max(0,Math.min(1000,tag.getInt("targetExp")));
        collectExp = tag.getBoolean("collectExp"); ownerOnly = tag.getBoolean("ownerOnly");
        showParticles = !tag.contains("showParticles") || tag.getBoolean("showParticles"); currentPlayer = null;
    }
    @Override public boolean isDefaultSettings() { return super.isDefaultSettings() && exp == 0 && targetExp == 0 && !collectExp && !ownerOnly && showParticles; }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (!fluidCapability.isPresent()) { long generation = capabilityGeneration; fluidCapability = LazyOptional.of(() -> new XpFluidHandler(generation)); }
            return fluidCapability.cast();
        }
        return super.getCapability(capability, side);
    }
    public IFluidHandler getXpFluidHandler() { return getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new); }
    @Override public void invalidateCaps() { capabilityGeneration++; fluidCapability.invalidate(); fluidCapability = LazyOptional.empty(); currentPlayer = null; super.invalidateCaps(); }
    @Override public void reviveCaps() { super.reviveCaps(); capabilityGeneration++; fluidCapability = LazyOptional.empty(); }
    @Override public void onChunkUnloaded() { currentPlayer = null; super.onChunkUnloaded(); invalidateCaps(); }
    private final class XpFluidHandler implements IFluidHandler {
        private final long generation;
        XpFluidHandler(long generation) { this.generation = generation; }
        private boolean active() { return generation == capabilityGeneration && live(); }
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { return tank == 0 && active() && exp > 0 ? new FluidStack(Registration.XP_FLUID_SOURCE.get(),ExperienceStorageMath.fluidAmount(exp)) : FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return tank == 0 ? Integer.MAX_VALUE : 0; }
        @Override public boolean isFluidValid(int tank, FluidStack fluid) { return tank == 0 && !fluid.isEmpty() && fluid.getFluid() == Registration.XP_FLUID_SOURCE.get(); }
        @Override public int fill(FluidStack fluid, FluidAction action) {
            if (!active() || !isFluidValid(0,fluid)) return 0;
            int accepted = ExperienceStorageMath.fillAmount(exp,fluid.getAmount());
            if (action.execute() && accepted > 0) { exp += accepted/20; markDirtyClient(); }
            return accepted;
        }
        @Override public FluidStack drain(FluidStack fluid, FluidAction action) { return isFluidValid(0,fluid) ? drain(fluid.getAmount(),action) : FluidStack.EMPTY; }
        @Override public FluidStack drain(int amount, FluidAction action) {
            int taken = active() ? ExperienceStorageMath.drainAmount(exp,amount) : 0;
            if (taken <= 0) return FluidStack.EMPTY;
            if (action.execute()) { exp -= taken/20; markDirtyClient(); }
            return new FluidStack(Registration.XP_FLUID_SOURCE.get(),taken);
        }
    }
}
