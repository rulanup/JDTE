package com.jdte.common.capabilities;

import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.blockentities.ExtendedBioCrusherBE;
import com.jdte.common.blockentities.FactoryPackerBE;
import com.jdte.common.blockentities.FluidReceiverBE;
import com.jdte.common.blockentities.FluidSenderBE;
import com.jdte.common.blockentities.GelGeneratorBE;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.blockentities.InfusionMachineBE;
import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.blockentities.LifeExtractorBE;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blockentities.TimeAcceleratorBE;
import com.jdte.common.blocks.LargeGreenhousePartBlock;
import com.jdte.common.blocks.LargeMineralExtractorPartBlock;
import com.jdte.common.blocks.LifeSynthesisPartBlock;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 数据驱动的机器能力注册表。
 *
 * <p>每台机器在 {@link #MACHINES} 表中声明自己的能力（能量/流体/物品）。
 * 注册时按 (能力类型, provider 引用) 分组，将共享同一 provider 的机器合并为一次
 * {@code registerBlock} 调用——例如所有实现 {@code PoweredMachineBE} 的机器共用
 * 同一个能量 provider。</p>
 *
 * <p>新增机器时只需在表中加一行；若机器能力与已有模式一致（如能量用
 * {@code POWERED_ENERGY}），无需修改任何注册代码。</p>
 */
public final class MachineCapabilities {
    private MachineCapabilities() {
    }

    // ============================================================
    // 共享 provider：多个机器合并注册的前提是 provider 引用相同，
    // 因此这些常量必须被表中所有相关机器复用（不要各自 new lambda）。
    // ============================================================

    /** 所有实现 {@code PoweredMachineBE} 的机器的能量存储。 */
    private static final IBlockCapabilityProvider<IEnergyStorage, Direction> POWERED_ENERGY =
            (level, pos, state, be, side) -> be instanceof PoweredMachineBE powered ? powered.getEnergyStorage() : null;

    /** 与 JDT 原版传输器一致，只允许朝向面上的相邻设备向传输器输入 FE。 */
    private static final IBlockCapabilityProvider<IEnergyStorage, Direction> TRANSMITTER_INPUT_ENERGY =
            (level, pos, state, be, side) -> be instanceof AdvancedEnergyTransmitterBE transmitter
                    && side != null && side == state.getValue(BlockStateProperties.FACING)
                    ? transmitter.getEnergyStorage() : null;

    /** 标准机器物品槽（机器自身的机器 handler）。 */
    private static final IBlockCapabilityProvider<IItemHandler, Direction> MACHINE_ITEMS =
            (level, pos, state, be, side) -> be instanceof BaseMachineBE machine ? machine.getMachineHandler() : null;

    /**
     * 只进不出的机器物品槽。机器 handler 中放的是工具/消耗品/待发送物品，
     * 外部运输（管道、漏斗、AE 输入总线/存储总线）只能放入、绝不能抽出，
     * 否则稿子、方块、桶等会被"吞"掉。
     */
    private static final IBlockCapabilityProvider<IItemHandler, Direction> INSERT_ONLY_MACHINE_ITEMS =
            (level, pos, state, be, side) -> be instanceof BaseMachineBE machine
                    ? new InsertOnlyItemHandler(machine.getMachineHandler()) : null;

    /** 实现 {@code FluidMachineBE} 的机器的流体槽。 */
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> FLUID_MACHINE_TANK =
            (level, pos, state, be, side) -> be instanceof FluidMachineBE fluidMachine ? fluidMachine.getFluidTank() : null;

    /** 时间加速器家族的流体槽（含水晶培育器）。 */
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> TIME_ACCELERATOR_TANK =
            (level, pos, state, be, side) -> be instanceof TimeAcceleratorBE accelerator ? accelerator.getFluidTank() : null;

    /** 大型温室结构部件：转发到其控制器方块实体。 */
    private static final IBlockCapabilityProvider<IEnergyStorage, Direction> LARGE_GREENHOUSE_PART_ENERGY =
            (level, pos, state, be, side) -> state.getBlock() instanceof LargeGreenhousePartBlock part
                    && part.getController(level, pos, state) != null
                    ? part.getController(level, pos, state).getEnergyStorage() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> LARGE_GREENHOUSE_PART_FLUID =
            (level, pos, state, be, side) -> state.getBlock() instanceof LargeGreenhousePartBlock part
                    && part.getController(level, pos, state) != null
                    ? part.getController(level, pos, state).getFluidTank() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> LARGE_GREENHOUSE_PART_ITEMS =
            (level, pos, state, be, side) -> state.getBlock() instanceof LargeGreenhousePartBlock part
                    && part.getController(level, pos, state) != null
                    ? part.getController(level, pos, state).getAutomationItemHandler() : null;

    /** 生命合成舱结构部件：转发到其控制器方块实体。 */
    private static final IBlockCapabilityProvider<IEnergyStorage, Direction> LIFE_SYNTHESIS_PART_ENERGY =
            (level, pos, state, be, side) -> state.getBlock() instanceof LifeSynthesisPartBlock part
                    && part.getController(level, pos, state) != null
                    ? part.getController(level, pos, state).getEnergyStorage() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> LIFE_SYNTHESIS_PART_FLUID =
            (level, pos, state, be, side) -> state.getBlock() instanceof LifeSynthesisPartBlock part
                    && part.getController(level, pos, state) != null
                    ? part.getController(level, pos, state).getCombinedFluidHandler() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> LIFE_SYNTHESIS_PART_ITEMS =
            (level, pos, state, be, side) -> state.getBlock() instanceof LifeSynthesisPartBlock part
                    && part.getController(level, pos, state) != null
                    ? part.getController(level, pos, state).getAutomationItemHandler() : null;

    /** 大型矿物提取机部件：将自动化能力转发到控制器。 */
    private static final IBlockCapabilityProvider<IEnergyStorage, Direction> LARGE_MINERAL_EXTRACTOR_PART_ENERGY =
            (level, pos, state, be, side) -> state.getBlock() instanceof LargeMineralExtractorPartBlock part
                    && part.getController(level, pos, state) != null
                    ? part.getController(level, pos, state).getEnergyStorage() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> LARGE_MINERAL_EXTRACTOR_PART_FLUID =
            (level, pos, state, be, side) -> state.getBlock() instanceof LargeMineralExtractorPartBlock part
                    && part.getController(level, pos, state) != null
                    ? part.getController(level, pos, state).getCombinedFluidHandler() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> LARGE_MINERAL_EXTRACTOR_PART_ITEMS =
            (level, pos, state, be, side) -> state.getBlock() instanceof LargeMineralExtractorPartBlock part
                    && part.getController(level, pos, state) != null
                    ? part.getController(level, pos, state).getAutomationItemHandler() : null;

    // 机器特有的 provider（仅当能力不满足上述通用模式时使用）

    private static final IBlockCapabilityProvider<IFluidHandler, Direction> GEL_GENERATOR_FLUID =
            (level, pos, state, be, side) -> be instanceof GelGeneratorBE generator ? generator.getFluidHandler() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> GEL_GENERATOR_ITEMS =
            (level, pos, state, be, side) -> be instanceof GelGeneratorBE generator ? generator.getAutomationItemHandler() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> FLUID_SENDER_TANK =
            (level, pos, state, be, side) -> be instanceof FluidSenderBE sender ? sender.getFluidTank() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> FLUID_RECEIVER_TANK =
            (level, pos, state, be, side) -> be instanceof FluidReceiverBE receiver ? receiver.getFluidTank() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> BIO_CRUSHER_TANK =
            (level, pos, state, be, side) -> be instanceof BioCrusherBE crusher ? crusher.getFluidTank() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> BIO_CRUSHER_OUTPUT =
            (level, pos, state, be, side) -> be instanceof ExtendedBioCrusherBE crusher ? crusher.getOutputItemHandler() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> LIFE_EXTRACTOR_TANK =
            (level, pos, state, be, side) -> be instanceof LifeExtractorBE extractor ? extractor.getFluidTank() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> INFUSION_MACHINE_TANK =
            (level, pos, state, be, side) -> be instanceof InfusionMachineBE infusion ? infusion.getFluidTank() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> LOOT_FABRICATOR_FLUID =
            (level, pos, state, be, side) -> be instanceof LootFabricatorBE fabricator ? fabricator.getFluidHandler() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> LOOT_FABRICATOR_ITEMS =
            (level, pos, state, be, side) -> be instanceof LootFabricatorBE fabricator ? fabricator.getAutomationItemHandler() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> MINERAL_EXTRACTOR_FLUID =
            (level, pos, state, be, side) -> be instanceof MineralExtractorBE extractor ? extractor.getCombinedFluidHandler() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> MINERAL_EXTRACTOR_ITEMS =
            (level, pos, state, be, side) -> be instanceof MineralExtractorBE extractor ? extractor.getAutomationItemHandler() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> POTION_BREWER_FLUID =
            (level, pos, state, be, side) -> be instanceof AdvancedPotionBrewerBE brewer ? brewer.getFluidHandler() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> POTION_BREWER_ITEMS =
            (level, pos, state, be, side) -> be instanceof AdvancedPotionBrewerBE brewer ? brewer.getAutomationItemHandler(side) : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> GREENHOUSE_FLUID =
            (level, pos, state, be, side) -> be instanceof GreenhouseBE greenhouse ? greenhouse.getFluidTank() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> GREENHOUSE_ITEMS =
            (level, pos, state, be, side) -> be instanceof GreenhouseBE greenhouse ? greenhouse.getAutomationItemHandler() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> LARGE_GREENHOUSE_FLUID =
            (level, pos, state, be, side) -> be instanceof LargeGreenhouseBE greenhouse ? greenhouse.getFluidTank() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> LARGE_GREENHOUSE_ITEMS =
            (level, pos, state, be, side) -> be instanceof LargeGreenhouseBE greenhouse ? greenhouse.getAutomationItemHandler() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> BIO_FACTORY_FLUID =
            (level, pos, state, be, side) -> be instanceof BioFactoryBE factory ? factory.getCombinedFluidHandler() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> BIO_FACTORY_ITEMS =
            (level, pos, state, be, side) -> be instanceof BioFactoryBE factory ? factory.getAutomationItemHandler() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> LIFE_SYNTHESIS_FLUID =
            (level, pos, state, be, side) -> be instanceof LifeSynthesisVatBE vat ? vat.getCombinedFluidHandler() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> LIFE_SYNTHESIS_ITEMS =
            (level, pos, state, be, side) -> be instanceof LifeSynthesisVatBE vat ? vat.getAutomationItemHandler() : null;
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> LIFE_BREEDER_FLUID =
            (level, pos, state, be, side) -> be instanceof LifeBreederBE breeder ? breeder.getFluidTank() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> LIFE_BREEDER_ITEMS =
            (level, pos, state, be, side) -> be instanceof LifeBreederBE breeder ? breeder.getAutomationItemHandler() : null;
    private static final IBlockCapabilityProvider<IItemHandler, Direction> FACTORY_PACKER_ITEMS =
            (level, pos, state, be, side) -> be instanceof FactoryPackerBE packer ? packer.getMachineHandler() : null;
    /** Clicker 流体：仅当装有流体存储升级时暴露（也作用于 JDT 自身的 Clicker T1/T2）。 */
    private static final IBlockCapabilityProvider<IFluidHandler, Direction> CLICKER_FLUID =
            (level, pos, state, be, side) -> be instanceof ClickerT1BE clicker && UpgradeHelper.hasFluidStorageUpgrade(clicker)
                    ? UpgradeHelper.getClickerFluidTank(clicker) : null;

    // ============================================================
    // 机器能力表：一行一台机器。energy/fluid/items 均为 null 表示无该能力。
    // ============================================================

    private static final List<MachineSpec> MACHINES = List.of(
            // --- 扩展版 JDT T2 机器 ---
            // 工具/消耗品槽对自动化只进不出，防止稿子等内容被管道抽出。
            machine(JDTEBlocks.EXTENDED_CLICKER, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_BLOCK_BREAKER, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_BLOCK_PLACER, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_BLOCK_SWAPPER, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_DROPPER, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_SENSOR, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_FLUID_COLLECTOR, energy(POWERED_ENERGY), fluid(FLUID_MACHINE_TANK), items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_FLUID_PLACER, energy(POWERED_ENERGY), fluid(FLUID_MACHINE_TANK), items(INSERT_ONLY_MACHINE_ITEMS)),

            // --- 时间加速器与水晶培育器 ---
            machine(JDTEBlocks.BASIC_TIME_ACCELERATOR, fluid(TIME_ACCELERATOR_TANK)),
            machine(JDTEBlocks.ADVANCED_TIME_ACCELERATOR, energy(POWERED_ENERGY), fluid(TIME_ACCELERATOR_TANK)),
            machine(JDTEBlocks.EXTENDED_TIME_ACCELERATOR, energy(POWERED_ENERGY), fluid(TIME_ACCELERATOR_TANK)),
            machine(JDTEBlocks.CRYSTAL_INCUBATOR, energy(POWERED_ENERGY), fluid(TIME_ACCELERATOR_TANK), items(MACHINE_ITEMS)),

            // --- 时间定格器 ---
            machine(JDTEBlocks.TIME_FREEZER, energy(POWERED_ENERGY), fluid(FLUID_MACHINE_TANK)),
            machine(JDTEBlocks.EXTENDED_TIME_FREEZER, energy(POWERED_ENERGY), fluid(FLUID_MACHINE_TANK)),

            // --- 胶水激活器 ---
            machine(JDTEBlocks.BASIC_GLUE_ACTIVATOR, items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.ADVANCED_GLUE_ACTIVATOR, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_GLUE_ACTIVATOR, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),

            // --- 凝胶生成器 ---
            machine(JDTEBlocks.ADVANCED_GEL_GENERATOR, energy(POWERED_ENERGY), fluid(GEL_GENERATOR_FLUID), items(GEL_GENERATOR_ITEMS)),
            machine(JDTEBlocks.EXTENDED_GEL_GENERATOR, energy(POWERED_ENERGY), fluid(GEL_GENERATOR_FLUID), items(GEL_GENERATOR_ITEMS)),

            // --- 流体稳定器 ---
            machine(JDTEBlocks.BASIC_FLUID_STABILIZER, items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.ADVANCED_FLUID_STABILIZER, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_FLUID_STABILIZER, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),

            // --- 物品发送/接收器 ---
            // 发送器槽为待发送物品（只进不出）；接收器槽为收到的输出（可抽出）。
            machine(JDTEBlocks.BASIC_ITEM_SENDER, items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.ADVANCED_ITEM_SENDER, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_ITEM_SENDER, energy(POWERED_ENERGY), items(INSERT_ONLY_MACHINE_ITEMS)),
            machine(JDTEBlocks.BASIC_ITEM_RECEIVER, items(MACHINE_ITEMS)),
            machine(JDTEBlocks.ADVANCED_ITEM_RECEIVER, energy(POWERED_ENERGY), items(MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_ITEM_RECEIVER, energy(POWERED_ENERGY), items(MACHINE_ITEMS)),

            // --- 流体发送/接收器 ---
            machine(JDTEBlocks.BASIC_FLUID_SENDER, fluid(FLUID_SENDER_TANK)),
            machine(JDTEBlocks.ADVANCED_FLUID_SENDER, energy(POWERED_ENERGY), fluid(FLUID_SENDER_TANK)),
            machine(JDTEBlocks.EXTENDED_FLUID_SENDER, energy(POWERED_ENERGY), fluid(FLUID_SENDER_TANK)),
            machine(JDTEBlocks.BASIC_FLUID_RECEIVER, fluid(FLUID_RECEIVER_TANK)),
            machine(JDTEBlocks.ADVANCED_FLUID_RECEIVER, energy(POWERED_ENERGY), fluid(FLUID_RECEIVER_TANK)),
            machine(JDTEBlocks.EXTENDED_FLUID_RECEIVER, energy(POWERED_ENERGY), fluid(FLUID_RECEIVER_TANK)),

            // --- 生物粉碎机 ---
            machine(JDTEBlocks.ADVANCED_BIO_CRUSHER, energy(POWERED_ENERGY), fluid(BIO_CRUSHER_TANK)),
            machine(JDTEBlocks.EXTENDED_BIO_CRUSHER, energy(POWERED_ENERGY), fluid(BIO_CRUSHER_TANK), items(BIO_CRUSHER_OUTPUT)),

            // --- 生命提取器 ---
            machine(JDTEBlocks.ADVANCED_LIFE_EXTRACTOR, energy(POWERED_ENERGY), fluid(LIFE_EXTRACTOR_TANK)),
            machine(JDTEBlocks.EXTENDED_LIFE_EXTRACTOR, energy(POWERED_ENERGY), fluid(LIFE_EXTRACTOR_TANK)),

            // --- 注入机 ---
            machine(JDTEBlocks.ADVANCED_INFUSION_MACHINE, energy(POWERED_ENERGY), fluid(INFUSION_MACHINE_TANK), items(MACHINE_ITEMS)),
            machine(JDTEBlocks.EXTENDED_INFUSION_MACHINE, energy(POWERED_ENERGY), fluid(INFUSION_MACHINE_TANK), items(MACHINE_ITEMS)),

            // --- 独立机器 ---
            machine(JDTEBlocks.ENTITY_SUPPRESSOR, energy(POWERED_ENERGY)),
            machine(JDTEBlocks.RANGE_BLOCKER, energy(POWERED_ENERGY)),
            machine(JDTEBlocks.FACTORY_PACKER, energy(POWERED_ENERGY), items(FACTORY_PACKER_ITEMS)),
            machine(JDTEBlocks.GREENHOUSE, energy(POWERED_ENERGY), fluid(GREENHOUSE_FLUID), items(GREENHOUSE_ITEMS)),
            machine(JDTEBlocks.LARGE_GREENHOUSE, energy(POWERED_ENERGY), fluid(LARGE_GREENHOUSE_FLUID), items(LARGE_GREENHOUSE_ITEMS)),
            machine(JDTEBlocks.LARGE_GREENHOUSE_PART, energy(LARGE_GREENHOUSE_PART_ENERGY),
                    fluid(LARGE_GREENHOUSE_PART_FLUID), items(LARGE_GREENHOUSE_PART_ITEMS)),
            machine(JDTEBlocks.LIFE_SYNTHESIS_VAT, energy(POWERED_ENERGY), fluid(LIFE_SYNTHESIS_FLUID), items(LIFE_SYNTHESIS_ITEMS)),
            machine(JDTEBlocks.LIFE_SYNTHESIS_PART, energy(LIFE_SYNTHESIS_PART_ENERGY),
                    fluid(LIFE_SYNTHESIS_PART_FLUID), items(LIFE_SYNTHESIS_PART_ITEMS)),
            machine(JDTEBlocks.BIO_FACTORY, energy(POWERED_ENERGY), fluid(BIO_FACTORY_FLUID), items(BIO_FACTORY_ITEMS)),
            machine(JDTEBlocks.LIFE_BREEDER, energy(POWERED_ENERGY), fluid(LIFE_BREEDER_FLUID), items(LIFE_BREEDER_ITEMS)),
            machine(JDTEBlocks.LOOT_FABRICATOR, energy(POWERED_ENERGY), fluid(LOOT_FABRICATOR_FLUID), items(LOOT_FABRICATOR_ITEMS)),
            machine(JDTEBlocks.MINERAL_EXTRACTOR, energy(POWERED_ENERGY), fluid(MINERAL_EXTRACTOR_FLUID), items(MINERAL_EXTRACTOR_ITEMS)),
            machine(JDTEBlocks.LARGE_MINERAL_EXTRACTOR, energy(POWERED_ENERGY),
                    fluid(MINERAL_EXTRACTOR_FLUID), items(MINERAL_EXTRACTOR_ITEMS)),
            machine(JDTEBlocks.LARGE_MINERAL_EXTRACTOR_PART, energy(LARGE_MINERAL_EXTRACTOR_PART_ENERGY),
                    fluid(LARGE_MINERAL_EXTRACTOR_PART_FLUID), items(LARGE_MINERAL_EXTRACTOR_PART_ITEMS)),
            machine(JDTEBlocks.ADVANCED_POTION_BREWER, energy(POWERED_ENERGY), fluid(POTION_BREWER_FLUID), items(POTION_BREWER_ITEMS)),

            // --- Advanced Energy Transmitter ---
            machine(JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER, energy(TRANSMITTER_INPUT_ENERGY), items(MACHINE_ITEMS))
    );

    /** 跨越 jdte 自身机器表的注册（如 Clicker 流体同时作用于 JDT 的 Clicker T1/T2）。 */
    private static final List<Standalone> STANDALONE = List.of(
            new Standalone(fluid(CLICKER_FLUID), List.of(
                    Registration.ClickerT1.get(),
                    Registration.ClickerT2.get(),
                    JDTEBlocks.EXTENDED_CLICKER.get()))
    );

    // ============================================================
    // 注册逻辑
    // ============================================================

    public static void register(RegisterCapabilitiesEvent event) {
        // 按 (能力类型, provider 引用) 分组，将同 provider 的机器合并为一次注册
        Map<GroupKey, List<Block>> groups = new LinkedHashMap<>();
        for (MachineSpec spec : MACHINES) {
            collect(groups, spec.energy(), spec.block());
            collect(groups, spec.fluid(), spec.block());
            collect(groups, spec.item(), spec.block());
        }
        for (Standalone standalone : STANDALONE) {
            collect(groups, standalone.capability(), standalone.blocks().toArray(Block[]::new));
        }
        groups.forEach((key, blocks) -> register(event, key.key(), key.provider(), blocks));
    }

    private static void collect(Map<GroupKey, List<Block>> groups, CapDef capability, Block... blocks) {
        if (capability == null) {
            return;
        }
        groups.computeIfAbsent(new GroupKey(capability.key(), capability.provider()), k -> new ArrayList<>())
                .addAll(List.of(blocks));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void register(RegisterCapabilitiesEvent event, CapabilityKey key,
                                 IBlockCapabilityProvider provider, List<Block> blocks) {
        // provider 与能力类型的匹配由表中 energy()/fluid()/items() 的泛型签名保证
        event.registerBlock(key.capability, provider, blocks.toArray(Block[]::new));
    }

    // ============================================================
    // 表结构
    // ============================================================

    private enum CapabilityKey {
        ENERGY(Capabilities.EnergyStorage.BLOCK),
        FLUID(Capabilities.FluidHandler.BLOCK),
        ITEM(Capabilities.ItemHandler.BLOCK);

        final BlockCapability<?, Direction> capability;

        CapabilityKey(BlockCapability<?, Direction> capability) {
            this.capability = capability;
        }
    }

    private record CapDef(CapabilityKey key, IBlockCapabilityProvider<?, ?> provider) {
    }

    private record MachineSpec(Block block, CapDef energy, CapDef fluid, CapDef item) {
    }

    private record Standalone(CapDef capability, List<Block> blocks) {
    }

    private record GroupKey(CapabilityKey key, IBlockCapabilityProvider<?, ?> provider) {
    }

    // ============================================================
    // DSL 辅助
    // ============================================================

    private static MachineSpec machine(Supplier<? extends Block> block, CapDef... capabilities) {
        CapDef energy = null;
        CapDef fluid = null;
        CapDef item = null;
        for (CapDef capability : capabilities) {
            switch (capability.key()) {
                case ENERGY -> energy = capability;
                case FLUID -> fluid = capability;
                case ITEM -> item = capability;
            }
        }
        return new MachineSpec(block.get(), energy, fluid, item);
    }

    private static CapDef energy(IBlockCapabilityProvider<IEnergyStorage, Direction> provider) {
        return new CapDef(CapabilityKey.ENERGY, provider);
    }

    private static CapDef fluid(IBlockCapabilityProvider<IFluidHandler, Direction> provider) {
        return new CapDef(CapabilityKey.FLUID, provider);
    }

    private static CapDef items(IBlockCapabilityProvider<IItemHandler, Direction> provider) {
        return new CapDef(CapabilityKey.ITEM, provider);
    }
}
