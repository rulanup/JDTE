package com.jdte.common.blockentities;

import com.jdte.common.recipes.MachineRecipeHelper;
import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.datagen.recipes.FluidDropRecipe;
import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public abstract class FluidStabilizerBE extends BaseMachineBE implements FilterableBE, RedstoneControlledBE, AreaAffectingBE, BaseFilterMachine {
    public static final int CATALYST_SLOT = 0;

    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    protected final ItemStacksResourceHandler itemHandler;

    protected FluidStabilizerBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = 1;
        areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
        itemHandler = new MachineInventoryHandler(1) {
            @Override
            protected void onContentsChanged(int slot, ItemStack previous) {
                setChanged();
            }

            @Override
            public boolean isValid(int slot, ItemResource resource) {
                return slot == CATALYST_SLOT && isValidCatalyst(resource.toStack());
            }
        };
    }

    @Override
    public ItemStacksResourceHandler getMachineHandler() {
        return itemHandler;
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if (isActiveRedstone() && canRun()) {
            stabilizeFluid();
        }
    }

    private void stabilizeFluid() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack catalystStack = itemHandler.getResource(CATALYST_SLOT).toStack(itemHandler.getAmountAsInt(CATALYST_SLOT));
        if (catalystStack.isEmpty()) return;

        int energyCost = getStandardEnergyCost();
        if (energyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this) && !hasEnoughPower(energyCost)) {
            return;
        }

        AABB area = getAABB(getBlockPos());
        for (BlockPos targetPos : BlockPos.betweenClosed(
                (int) area.minX, (int) area.minY, (int) area.minZ,
                (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)) {
            if (tryStabilizeAt(serverLevel, targetPos, catalystStack, energyCost)) {
                return;
            }
        }
    }

    private boolean tryStabilizeAt(ServerLevel serverLevel, BlockPos targetPos, ItemStack catalystStack, int energyCost) {
        BlockState inputState = serverLevel.getBlockState(targetPos);
        if (!(inputState.getBlock() instanceof LiquidBlock liquidBlock) || !inputState.getFluidState().isSource()) {
            return false;
        }
        if (!isStackValidFilter(liquidBlock)) {
            return false;
        }

        FluidDropRecipe recipe = findFluidDropRecipe(inputState, catalystStack);
        if (recipe == null) {
            return false;
        }

        BlockState outputState = recipe.getOutput();
        if (outputState.isAir() || outputState.equals(inputState) || !serverLevel.setBlockAndUpdate(targetPos, outputState)) {
            return false;
        }

        handleOutputPlacement(serverLevel, targetPos, outputState);
        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            if (energyCost > 0) {
                extractEnergy(energyCost, false);
            }
            catalystStack.shrink(1);
            itemHandler.set(CATALYST_SLOT, ItemResource.of(catalystStack), catalystStack.getCount());
        }
        setChanged();
        return true;
    }

    private void handleOutputPlacement(ServerLevel serverLevel, BlockPos targetPos, BlockState outputState) {
        // Fluid stabilizer forces fluid placement regardless of dimension restrictions
        serverLevel.playSound(null, targetPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private FluidDropRecipe findFluidDropRecipe(BlockState inputState, ItemStack catalystStack) {
        if (level == null) {
            return null;
        }
        for (RecipeHolder<?> recipe : MachineRecipeHelper.recipes(level).byType(JDTRegistration.FLUID_DROP_RECIPE_TYPE.get())) {
            if (recipe.value() instanceof FluidDropRecipe fluidDropRecipe && fluidDropRecipe.matches(inputState, catalystStack)) {
                return fluidDropRecipe;
            }
        }
        return null;
    }

    private boolean isValidCatalyst(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (level == null) {
            return true;
        }
        for (RecipeHolder<?> recipe : MachineRecipeHelper.recipes(level).byType(JDTRegistration.FLUID_DROP_RECIPE_TYPE.get())) {
            if (recipe.value() instanceof FluidDropRecipe fluidDropRecipe && stack.is(fluidDropRecipe.getCatalyst())) {
                return true;
            }
        }
        return false;
    }

    public abstract int getStandardEnergyCost();

    public abstract boolean hasEnoughPower(int energyCost);

    public abstract int extractEnergy(int energy, boolean simulate);

    protected int getAreaEnergyScale() {
        return Math.max(1, 1
                + (int) Math.ceil(areaAffectingData.xRadius)
                + (int) Math.ceil(areaAffectingData.yRadius)
                + (int) Math.ceil(areaAffectingData.zRadius));
    }

    @Override
    public FilterBasicHandler getFilterHandler() {
        return getData(JDTRegistration.HANDLER_BASIC_FILTER);
    }

    @Override
    public FilterData getFilterData() {
        return filterData;
    }

    @Override
    public RedstoneControlData getRedstoneControlData() {
        return redstoneControlData;
    }

    @Override
    public AreaAffectingData getAreaAffectingData() {
        return areaAffectingData;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putChild("inventory", itemHandler);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        tag.child("inventory").ifPresent(itemHandler::deserialize);
    }
}
