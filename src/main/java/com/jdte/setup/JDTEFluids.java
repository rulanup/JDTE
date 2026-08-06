package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.fluid.LifeFluid;
import com.jdte.common.fluid.LifeFluidBlock;
import com.jdte.common.fluid.LifeFluidType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class JDTEFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, JDTE.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, JDTE.MODID);
    public static final DeferredRegister.Blocks FLUID_BLOCKS = DeferredRegister.createBlocks(JDTE.MODID);
    public static final DeferredRegister.Items BUCKET_ITEMS = DeferredRegister.createItems(JDTE.MODID);

    public static final DeferredHolder<FluidType, FluidType> LIFE_FLUID_TYPE = FLUID_TYPES.register(
            "life_fluid_type", LifeFluidType::new);
    public static final DeferredHolder<Fluid, FlowingFluid> LIFE_FLUID_FLOWING = FLUIDS.register(
            "life_fluid_flowing", LifeFluid.Flowing::new);
    public static final DeferredHolder<Fluid, FlowingFluid> LIFE_FLUID_SOURCE = FLUIDS.register(
            "life_fluid_source", LifeFluid.Source::new);
    public static final DeferredHolder<Block, LiquidBlock> LIFE_FLUID_BLOCK = FLUID_BLOCKS.register(
            "life_fluid_block", LifeFluidBlock::new);
    public static final DeferredHolder<Item, BucketItem> LIFE_FLUID_BUCKET = BUCKET_ITEMS.register(
            "life_fluid_bucket", () -> new BucketItem(LIFE_FLUID_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
}
