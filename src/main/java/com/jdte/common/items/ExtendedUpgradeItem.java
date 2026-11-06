package com.jdte.common.items;

import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedUpgradeItem extends Item {
    private static final Map<Block, Block> UPGRADE_MAP = new HashMap<>();

    static {
        UPGRADE_MAP.put(Registration.ExperienceHolder.get(), JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_ENTITY_ACCELERATOR.get(), JDTEBlocks.EXTENDED_ENTITY_ACCELERATOR.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get(), JDTEBlocks.EXTENDED_BIO_EXTRACTOR.get());
        UPGRADE_MAP.put(Registration.GeneratorFluidT1.get(), JDTEBlocks.EXTENDED_FLUID_GENERATOR.get());
        UPGRADE_MAP.put(Registration.GeneratorT1.get(), JDTEBlocks.EXTENDED_GENERATOR.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_GEL_GENERATOR.get(), JDTEBlocks.EXTENDED_GEL_GENERATOR.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_FLUID_MIXER.get(),JDTEBlocks.EXTENDED_FLUID_MIXER.get());
        UPGRADE_MAP.put(Registration.EnergyTransmitter.get(), JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get());
        UPGRADE_MAP.put(Registration.ClickerT2.get(), JDTEBlocks.EXTENDED_CLICKER.get());
        UPGRADE_MAP.put(Registration.BlockBreakerT2.get(), JDTEBlocks.EXTENDED_BLOCK_BREAKER.get());
        UPGRADE_MAP.put(Registration.BlockPlacerT2.get(), JDTEBlocks.EXTENDED_BLOCK_PLACER.get());
        UPGRADE_MAP.put(Registration.BlockSwapperT2.get(), JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get());
        UPGRADE_MAP.put(Registration.DropperT2.get(), JDTEBlocks.EXTENDED_DROPPER.get());
        UPGRADE_MAP.put(Registration.SensorT2.get(), JDTEBlocks.EXTENDED_SENSOR.get());
        UPGRADE_MAP.put(Registration.FluidCollectorT2.get(), JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get());
        UPGRADE_MAP.put(Registration.FluidPlacerT2.get(), JDTEBlocks.EXTENDED_FLUID_PLACER.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get(), JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get());
        UPGRADE_MAP.put(JDTEBlocks.TIME_FREEZER.get(), JDTEBlocks.EXTENDED_TIME_FREEZER.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_FLUID_STABILIZER.get(), JDTEBlocks.EXTENDED_FLUID_STABILIZER.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get(), JDTEBlocks.EXTENDED_LIFE_EXTRACTOR.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_INFUSION_MACHINE.get(), JDTEBlocks.EXTENDED_INFUSION_MACHINE.get());
    }

    public ExtendedUpgradeItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        Block extendedBlock = UPGRADE_MAP.get(block);
        if (extendedBlock == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        return MachineTierConversion.convert(context, extendedBlock, this::replaceBlock);
    }

    static Block targetFor(Block source) { return UPGRADE_MAP.get(source); }

    protected boolean replaceBlock(Level level, BlockPos pos, BlockState target) {
        // Neighbors are notified only after data and the placement permission event succeed.
        return level.setBlock(pos, target, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.extended_upgrade").withStyle(ChatFormatting.GRAY));
    }
}
