package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.fluid.LifeFluid;
import com.jdte.common.fluid.LifeFluidBlock;
import com.jdte.common.fluid.LifeFluidType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class JDTEFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, JDTE.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, JDTE.MODID);
    public static final DeferredRegister<Block> FLUID_BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, JDTE.MODID);
    public static final DeferredRegister<Item> BUCKET_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JDTE.MODID);

    public static final RegistryObject<FluidType> LIFE_FLUID_TYPE = FLUID_TYPES.register(
            "life_fluid_type", LifeFluidType::new);
    public static final RegistryObject<FlowingFluid> LIFE_FLUID_FLOWING = FLUIDS.register(
            "life_fluid_flowing", LifeFluid.Flowing::new);
    public static final RegistryObject<FlowingFluid> LIFE_FLUID_SOURCE = FLUIDS.register(
            "life_fluid_source", LifeFluid.Source::new);
    public static final RegistryObject<LiquidBlock> LIFE_FLUID_BLOCK = FLUID_BLOCKS.register(
            "life_fluid_block", LifeFluidBlock::new);
    public static final RegistryObject<BucketItem> LIFE_FLUID_BUCKET = BUCKET_ITEMS.register(
            "life_fluid_bucket", () -> new BucketItem(LIFE_FLUID_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
}
