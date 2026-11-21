package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.direwolf20.justdirethings.util.ExperienceUtils;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public class ExtendedExperienceHolderBE extends BaseMachineBE
        implements AreaAffectingBE, RedstoneControlledBE {
    public AreaAffectingData areaAffectingData = new AreaAffectingData(
            getBlockState().getValue(BlockStateProperties.FACING).getOpposite());
    public RedstoneControlData redstoneControlData = getDefaultRedstoneData();
    public int exp;
    public int targetExp;
    private Player currentPlayer;
    public boolean collectExp;
    public boolean ownerOnly;
    public boolean showParticles = true;
    private final ResourceHandler<FluidResource> xpFluidHandler = new XpFluidHandler();

    public ExtendedExperienceHolderBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_EXPERIENCE_HOLDER.get(), pos, state);
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public RedstoneControlData getRedstoneControlData() {
        return redstoneControlData;
    }

    @Override
    public AreaAffectingData getAreaAffectingData() {
        return areaAffectingData;
    }

    public void changeSettings(Player player, int targetExp, boolean ownerOnly, boolean collectExp, boolean showParticles) {
        if (this.ownerOnly && !player.getUUID().equals(placedByUUID)) return;
        this.targetExp = targetExp;
        this.ownerOnly = ownerOnly;
        this.collectExp = collectExp;
        this.showParticles = showParticles;
        markDirtyClient();
    }

    public int addExp(int addition) {
        if (this.exp > Integer.MAX_VALUE - addition) {
            int remainingExp = addition - (Integer.MAX_VALUE - this.exp);
            this.exp = Integer.MAX_VALUE;
            return remainingExp;
        } else {
            this.exp += addition;
            return 0;
        }
    }

    public int subExp(int subtraction) {
        int amtToRemove = Math.min(exp, subtraction);
        this.exp = this.exp - amtToRemove;
        return subtraction - amtToRemove;
    }

    public void storeExp(Player player, int levelChange) {
        if (ownerOnly && !player.getUUID().equals(placedByUUID)) return;
        if (levelChange == -1) {
            int totalExp = ExperienceUtils.getPlayerTotalExperience(player);
            int remaining = addExp(totalExp);
            player.giveExperiencePoints(-totalExp);
            player.giveExperienceLevels(-1);
            if (remaining > 0)
                player.giveExperiencePoints(remaining);
        } else if (levelChange > 0) {
            int expInCurrentLevel = (int) (player.experienceProgress * player.getXpNeededForNextLevel());

            if (player.experienceProgress > 0.0f) {
                int expRemoved = ExperienceUtils.removePoints(player, expInCurrentLevel);
                int remaining = addExp(expRemoved);
                levelChange--;
                player.experienceProgress = 0f;
                if (remaining > 0)
                    player.giveExperiencePoints(remaining);
            }

            if (levelChange > 0) {
                int expRemoved = ExperienceUtils.removeLevels(player, levelChange);
                int remaining = addExp(expRemoved);
                if (remaining > 0)
                    player.giveExperiencePoints(remaining);
            }
        }

        markDirtyClient();
    }

    public void extractExp(Player player, int levelChange) {
        if (exp == 0) return;
        if (ownerOnly && !player.getUUID().equals(placedByUUID)) return;

        if (levelChange == -1) {
            int expToGive = exp;
            player.giveExperiencePoints(expToGive);
            this.exp = 0;
        } else if (levelChange > 0) {
            if (roundUpToNextLevel(player))
                levelChange--;

            if (levelChange > 0 && this.exp > 0) {
                int expForNextLevels = ExperienceUtils.getTotalExperienceForLevel(player.experienceLevel + levelChange)
                        - ExperienceUtils.getPlayerTotalExperience(player);
                int expToGive = Math.min(this.exp, expForNextLevels);
                player.giveExperiencePoints(expToGive);
                this.exp -= expToGive;
                roundUpToNextLevel(player);
            }
        }

        markDirtyClient();
    }

    public boolean roundUpToNextLevel(Player player) {
        if (this.exp <= 0) return false;
        int expInCurrentLevel = (int) (player.experienceProgress * player.getXpNeededForNextLevel());
        if (expInCurrentLevel > 0) {
            int expToGive = Math.min(exp, ExperienceUtils.getExpNeededForNextLevel(player));
            player.giveExperiencePoints(expToGive);
            this.exp -= expToGive;
            return true;
        }
        return false;
    }

    public void tickClient() {
    }

    public void tickServer() {
        super.tickServer();
        if (collectExp)
            collectExp();
        handleExperience();
    }

    public void doParticles(ItemStack itemStack, Vec3 sourcePos, boolean toBlock) {
        if (!showParticles) return;
        Direction direction = getBlockState().getValue(BlockStateProperties.FACING);
        BlockPos blockPos = getBlockPos();
        Vec3 baubleSpot = new Vec3(
                blockPos.getX() + 0.5f - (0.3 * direction.getStepX()),
                blockPos.getY() + 0.5f - (0.3 * direction.getStepY()),
                blockPos.getZ() + 0.5f - (0.3 * direction.getStepZ()));
        double d0 = sourcePos.x();
        double d1 = sourcePos.y();
        double d2 = sourcePos.z();
        if (toBlock) {
            ItemFlowParticleData data = new ItemFlowParticleData(itemStack, baubleSpot.x, baubleSpot.y, baubleSpot.z, 1);
            ((ServerLevel) level).sendParticles(data, d0, d1, d2, 10, 0, 0, 0, 0);
        } else {
            ItemFlowParticleData data = new ItemFlowParticleData(itemStack, d0, d1, d2, 1);
            ((ServerLevel) level).sendParticles(data, baubleSpot.x, baubleSpot.y, baubleSpot.z, 10, 0, 0, 0, 0);
        }
    }

    private void handleExperience() {
        assert level != null;
        if (isActiveRedstone() && canRun() && currentPlayer == null)
            findTargetPlayer();

        if (currentPlayer == null) return;

        int currentLevel = currentPlayer.experienceLevel;
        if (currentLevel < targetExp && exp > 0) {
            extractExp(currentPlayer, 1);
            doParticles(new ItemStack(Items.EXPERIENCE_BOTTLE),
                    currentPlayer.getEyePosition().subtract(0, 0.25f, 0), false);
            if (exp == 0)
                currentPlayer = null;
        } else if (currentLevel > targetExp
                || (currentLevel == targetExp && currentPlayer.experienceProgress > 0.01f)) {
            storeExp(currentPlayer, 1);
            doParticles(new ItemStack(Items.EXPERIENCE_BOTTLE),
                    currentPlayer.getEyePosition().subtract(0, 0.25f, 0), true);
        } else {
            currentPlayer = null;
        }
    }

    private void collectExp() {
        if (operationTicks != 0)
            return;
        assert level != null;
        AABB searchArea = getAABB(getBlockPos());

        List<ExperienceOrb> entityList = level.getEntitiesOfClass(ExperienceOrb.class, searchArea, entity -> true)
                .stream().toList();

        if (entityList.isEmpty()) return;

        for (ExperienceOrb experienceOrb : entityList) {
            int orbValue = experienceOrb.getValue();
            addExp(orbValue);
            doParticles(new ItemStack(Items.EXPERIENCE_BOTTLE), experienceOrb.position(), true);
            experienceOrb.discard();
        }
        markDirtyClient();
    }

    private void findTargetPlayer() {
        assert level != null;
        AABB searchArea = getAABB(getBlockPos());

        List<Player> entityList = level.getEntitiesOfClass(Player.class, searchArea, entity -> true)
                .stream().toList();

        if (entityList.isEmpty()) return;

        for (Player player : entityList) {
            if (ownerOnly && !player.getUUID().equals(placedByUUID))
                continue;
            if (player.experienceLevel != targetExp || player.experienceProgress > 0.01f) {
                this.currentPlayer = player;
                return;
            }
        }
    }

    public ResourceHandler<FluidResource> getXpFluidHandler() {
        return xpFluidHandler;
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putInt("exp", exp);
        tag.putInt("targetExp", targetExp);
        tag.putBoolean("collectExp", collectExp);
        tag.putBoolean("ownerOnly", ownerOnly);
        tag.putBoolean("showParticles", showParticles);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        exp = tag.getIntOr("exp", 0);
        targetExp = tag.getIntOr("targetExp", 0);
        collectExp = tag.getBooleanOr("collectExp", false);
        ownerOnly = tag.getBooleanOr("ownerOnly", false);
        showParticles = tag.getBooleanOr("showParticles", false);
    }

    @Override
    public AreaAffectingData getDefaultAreaData(AreaAffectingBE areaAffectingBE) {
        return areaAffectingBE.getDefaultAreaData(
                getBlockState().getValue(BlockStateProperties.FACING).getOpposite());
    }

    @Override
    public RedstoneControlData getDefaultRedstoneData() {
        return new RedstoneControlData(MiscHelpers.RedstoneMode.PULSE);
    }

    @Override
    public boolean isDefaultSettings() {
        if (!super.isDefaultSettings())
            return false;
        if (exp != 0)
            return false;
        if (targetExp != 0)
            return false;
        if (collectExp)
            return false;
        if (ownerOnly)
            return false;
        return true;
    }

    private final class XpFluidHandler extends SnapshotJournal<Integer> implements ResourceHandler<FluidResource> {
        private static final int MB_PER_XP = 20;

        @Override public int size() { return 1; }
        @Override public FluidResource getResource(int index) {
            return exp > 0 ? FluidResource.of(JDTRegistration.XP_FLUID_SOURCE.get()) : FluidResource.EMPTY;
        }
        @Override public long getAmountAsLong(int index) {
            return Math.min(Integer.MAX_VALUE, Math.max(0L, (long) exp * MB_PER_XP));
        }
        @Override public long getCapacityAsLong(int index, FluidResource resource) {
            return isValid(index, resource) ? Integer.MAX_VALUE : 0;
        }
        @Override public boolean isValid(int index, FluidResource resource) {
            return index == 0 && (resource.isEmpty() || resource.equals(FluidResource.of(JDTRegistration.XP_FLUID_SOURCE.get())));
        }
        @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || !isValid(index, resource)) return 0;
            int points = (int) (Math.min(amount, Integer.MAX_VALUE - getAmountAsLong(index)) / MB_PER_XP);
            if (points <= 0) return 0;
            updateSnapshots(transaction);
            exp += points;
            return points * MB_PER_XP;
        }
        @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || !isValid(index, resource)) return 0;
            int points = Math.min(exp, amount / MB_PER_XP);
            if (points <= 0) return 0;
            updateSnapshots(transaction);
            exp -= points;
            return points * MB_PER_XP;
        }
        @Override protected Integer createSnapshot() { return exp; }
        @Override protected void revertToSnapshot(Integer snapshot) { exp = snapshot; }
        @Override protected void onRootCommit(Integer previous) { if (previous != exp) setChanged(); }
    }
}
