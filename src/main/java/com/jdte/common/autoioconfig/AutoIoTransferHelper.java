package com.jdte.common.autoioconfig;

import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.ResourceHandler;
import com.direwolf20.justdirethings.common.blockentities.BlockBreakerT1BE;
import com.direwolf20.justdirethings.common.blockentities.BlockPlacerT1BE;
import com.direwolf20.justdirethings.common.blockentities.BlockSwapperT1BE;
import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.common.blockentities.DropperT1BE;
import com.direwolf20.justdirethings.common.blockentities.FluidCollectorT1BE;
import com.direwolf20.justdirethings.common.blockentities.FluidPlacerT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.FluidMixerBE;
import com.jdte.common.blockentities.FluidReceiverBE;
import com.jdte.common.blockentities.FluidSenderBE;
import com.jdte.common.blockentities.FluidStabilizerBE;
import com.jdte.common.blockentities.GelGeneratorBE;
import com.jdte.common.blockentities.GlueActivatorBE;
import com.jdte.common.blockentities.InfusionMachineBE;
import com.jdte.common.blockentities.ItemReceiverBE;
import com.jdte.common.blockentities.ItemSenderBE;
import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.jdte.common.blockentities.LargeMineralExtractorBE;
import com.jdte.common.blocks.LargeGreenhouseStructure;
import com.jdte.common.blocks.LargeMineralExtractorStructure;
import com.jdte.common.blocks.LifeSynthesisStructure;
import com.jdte.common.blockentities.LifeExtractorBE;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blockentities.TimeAcceleratorBE;
import com.jdte.common.blockentities.CrystalIncubatorBE;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.greenhouse.ICreativeGreenhouse;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.greenhouse.GreenhouseMatrixRuntime;
import com.jdte.setup.JDTEAttachments;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class AutoIoTransferHelper {
    private static final int SUCCESS_COOLDOWN_TICKS = 0;

    private static final int IO_SIDE_NORTH = 0;
    private static final int IO_SIDE_SOUTH = 1;
    private static final int IO_SIDE_WEST = 2;
    private static final int IO_SIDE_EAST = 3;
    private static final int IO_SIDE_UP = 4;
    private static final int IO_SIDE_DOWN = 5;

    private static final int[] NO_SLOTS = new int[0];
    private static final Map<BaseMachineBE, Map<EndpointKey, CachedItemEndpoint>> EVENT_OUTPUT_ENDPOINTS =
            new WeakHashMap<>();

    private AutoIoTransferHelper() {
    }

    public static boolean supportsInput(BaseMachineBE machine) {
        if (machine == null) {
            return false;
        }
        IoRoutes routes = getRoutes(machine);
        return routes.hasItemInputs() || routes.fluidInput() != null;
    }

    public static boolean supportsOutput(BaseMachineBE machine) {
        if (machine == null) {
            return false;
        }
        IoRoutes routes = getRoutes(machine);
        return routes.hasItemOutputs() || routes.fluidOutput() != null;
    }

    public static AEOutputRoutes getAEOutputRoutes(BaseMachineBE machine) {
        if (machine == null || machine instanceof ItemReceiverBE || machine instanceof FluidReceiverBE) {
            return AEOutputRoutes.EMPTY;
        }
        IoRoutes routes = getRoutes(machine);
        return new AEOutputRoutes(routes.itemOutputs().clone(), routes.fluidOutput());
    }

    public static boolean supportsAEOutput(BaseMachineBE machine) {
        // AE 返回升级现已面向任意机器开放；无输出路由的机器装卡后不会回流
        return machine != null;
    }

    public static void tick(BaseMachineBE machine) {
        if (machine == null || !(machine.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!AutoIoConfigHelper.hasConfigurableIo(machine)) {
            return;
        }

        AutoIoConfigData data = machine.getData(JDTEAttachments.AUTO_IO_CONFIG.get());
        if (!data.beginRealServerTick(serverLevel.getGameTime())) {
            return;
        }
        int inputMask = data.getInputMask();
        int outputMask = data.getOutputMask();
        if (UpgradeHelper.hasAEOutputUpgrade(machine) || GreenhouseMatrixRuntime.hasAEOutput(machine)) outputMask = 0;
        boolean eventDrivenOutput = machine instanceof GreenhouseBE || machine instanceof ICreativeGreenhouse || machine instanceof LargeGreenhouseBE
                || machine instanceof MineralExtractorBE;
        int periodicOutputMask = eventDrivenOutput ? 0 : outputMask;
        if (inputMask == 0 && periodicOutputMask == 0) {
            data.resetTransferState();
            return;
        }
        if (OverclockDirectTransferHelper.isEnabled(machine)) {
            data.resetTransferState();
            return;
        }

        int cooldown = data.getTransferCooldown();
        if (cooldown > 0) {
            data.setTransferCooldown(cooldown - 1);
            return;
        }

        IoRoutes routes = getRoutes(machine);
        if (routes.isEmpty()) {
            data.setTransferCooldown(getMaxFailureBackoff());
            return;
        }

        boolean moved = transferEnabledSides(serverLevel, machine, inputMask, periodicOutputMask, routes);
        if (moved) {
            data.setFailureBackoff(0);
            data.setTransferCooldown(SUCCESS_COOLDOWN_TICKS);
            machine.setChanged();
            return;
        }

        int nextBackoff = data.getFailureBackoff() <= 0
                ? getFailureBackoffStart()
                : Math.min(getMaxFailureBackoff(), data.getFailureBackoff() * 2);
        data.setFailureBackoff(nextBackoff);
        data.setTransferCooldown(nextBackoff);
    }

    public static EventOutputResult flushEventDrivenOutput(BaseMachineBE machine, int[] sourceSlots) {
        if (!(machine.getLevel() instanceof ServerLevel level) || sourceSlots.length == 0) {
            return new EventOutputResult(false, false);
        }
        AutoIoConfigData data = machine.getData(JDTEAttachments.AUTO_IO_CONFIG.get());
        if (UpgradeHelper.hasAEOutputUpgrade(machine) || GreenhouseMatrixRuntime.hasAEOutput(machine)) {
            return new EventOutputResult(false, true);
        }
        int outputMask = data.getOutputMask();
        if (outputMask == 0 || OverclockDirectTransferHelper.isEnabled(machine)) {
            return new EventOutputResult(false, false);
        }

        boolean moved = false;
        Set<EndpointKey> visited = new HashSet<>();
        Map<EndpointKey, CachedItemEndpoint> endpointCache = EVENT_OUTPUT_ENDPOINTS
                .computeIfAbsent(machine, ignored -> new java.util.HashMap<>());
        int startSide = (int) (level.getGameTime() % AutoIoConfigData.SIDE_COUNT);
        for (int i = 0; i < AutoIoConfigData.SIDE_COUNT; i++) {
            int uiSide = (startSide + i) % AutoIoConfigData.SIDE_COUNT;
            if ((outputMask & (1 << uiSide)) == 0) continue;
            Direction side = directionForUiSide(machine, uiSide);
            Direction neighborSide = side.getOpposite();
            for (BlockPos neighborPos : externalNeighbors(level, machine, side)) {
                EndpointKey key = new EndpointKey(neighborPos.immutable(), neighborSide);
                if (!visited.add(key)) continue;
                ResourceHandler<ItemResource> target = resolveCachedItemEndpoint(level, key, endpointCache);
                if (target != null) {
                    if (machine instanceof ICreativeGreenhouse creativeGreenhouse) {
                        moved |= pushCreativeGreenhouseItems(creativeGreenhouse, sourceSlots, target,
                                JDTEConfig.COMMON.greenhouseEventOutputTypeBudget.get(),
                                level.getGameTime() + key.hashCode());
                    } else {
                        moved |= pushGroupedOversizedItems(machine.getMachineHandler(), sourceSlots, target,
                                JDTEConfig.COMMON.greenhouseEventOutputItemBudget.get(),
                                JDTEConfig.COMMON.greenhouseEventOutputTypeBudget.get(),
                                level.getGameTime() + key.hashCode());
                    }
                }
            }
        }
        endpointCache.keySet().retainAll(visited);
        if (moved) machine.setChanged();
        return new EventOutputResult(moved, true);
    }

    public static void forgetEventDrivenOutput(BaseMachineBE machine) {
        EVENT_OUTPUT_ENDPOINTS.remove(machine);
    }

    private static ResourceHandler<ItemResource> resolveCachedItemEndpoint(ServerLevel level, EndpointKey key,
                                                          Map<EndpointKey, CachedItemEndpoint> cache) {
        BlockState state = level.getBlockState(key.pos());
        BlockEntity blockEntity = level.getBlockEntity(key.pos());
        CachedItemEndpoint cached = cache.get(key);
        if (cached != null && cached.state().equals(state) && cached.blockEntity() == blockEntity
                && (blockEntity == null || !blockEntity.isRemoved())) {
            return cached.handler();
        }
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, key.pos(), key.side());
        if (handler == null) cache.remove(key);
        else cache.put(key, new CachedItemEndpoint(state, blockEntity, handler));
        return handler;
    }

    public record EventOutputResult(boolean moved, boolean outputEnabled) {
    }

    public record AEOutputRoutes(int[] itemSlots, ResourceHandler<FluidResource> fluidOutput) {
        private static final AEOutputRoutes EMPTY = new AEOutputRoutes(NO_SLOTS, null);
    }

    private record EndpointKey(BlockPos pos, Direction side) {
    }

    private record CachedItemEndpoint(BlockState state, BlockEntity blockEntity, ResourceHandler<ItemResource> handler) {
    }

    private static boolean transferEnabledSides(ServerLevel level, BaseMachineBE machine,
                                                int inputMask, int outputMask, IoRoutes routes) {
        boolean moved = false;
        int startSide = (int) (level.getGameTime() % AutoIoConfigData.SIDE_COUNT);
        for (int i = 0; i < AutoIoConfigData.SIDE_COUNT; i++) {
            int uiSide = (startSide + i) % AutoIoConfigData.SIDE_COUNT;
            int bit = 1 << uiSide;
            boolean inputEnabled = (inputMask & bit) != 0;
            boolean outputEnabled = (outputMask & bit) != 0;
            if (!inputEnabled && !outputEnabled) {
                continue;
            }
            Direction side = directionForUiSide(machine, uiSide);
            if (transferSide(level, machine, side, routes, inputEnabled, outputEnabled)) {
                moved = true;
            }
        }
        return moved;
    }

    private static boolean transferSide(ServerLevel level, BaseMachineBE machine, Direction side, IoRoutes routes,
                                        boolean inputEnabled, boolean outputEnabled) {
        boolean moved = false;
        ResourceHandler<ItemResource> internalItems = machine.getMachineHandler();
        for (BlockPos neighborPos : externalNeighbors(level, machine, side)) {
            Direction neighborSide = side.getOpposite();
            if (transferNeighbor(level, machine, neighborPos, neighborSide, routes, inputEnabled, outputEnabled,
                    internalItems)) {
                moved = true;
            }
        }
        return moved;
    }

    private static boolean transferNeighbor(ServerLevel level, BaseMachineBE machine, BlockPos neighborPos,
                                            Direction neighborSide, IoRoutes routes, boolean inputEnabled,
                                            boolean outputEnabled, ResourceHandler<ItemResource> internalItems) {
        boolean moved = false;
        if (outputEnabled && internalItems != null && routes.hasItemOutputs()) {
            ResourceHandler<ItemResource> externalItems = level.getCapability(Capabilities.Item.BLOCK, neighborPos, neighborSide);
            if (externalItems != null) {
                moved |= pushItems(internalItems, routes.itemOutputs(), externalItems,
                        JDTEConfig.COMMON.autoIoItemTransferRate.get());
            }
        }
        if (outputEnabled && routes.fluidOutput() != null) {
            ResourceHandler<FluidResource> externalFluid = level.getCapability(Capabilities.Fluid.BLOCK, neighborPos, neighborSide);
            if (externalFluid != null) {
                moved |= moveFluids(routes.fluidOutput(), externalFluid,
                        JDTEConfig.COMMON.autoIoFluidTransferRate.get(), level.getGameTime()) > 0;
            }
        }
        if (inputEnabled && internalItems != null && routes.hasItemInputs()) {
            ResourceHandler<ItemResource> externalItems = level.getCapability(Capabilities.Item.BLOCK, neighborPos, neighborSide);
            if (externalItems != null) {
                ResourceHandler<ItemResource> input = machine instanceof AdvancedPotionBrewerBE brewer
                        ? brewer.getAutomationItemHandler(neighborSide.getOpposite()) : internalItems;
                moved |= pullItems(externalItems, input, routes.itemInputs(),
                        JDTEConfig.COMMON.autoIoItemTransferRate.get());
            }
        }
        if (inputEnabled && routes.fluidInput() != null) {
            ResourceHandler<FluidResource> externalFluid = level.getCapability(Capabilities.Fluid.BLOCK, neighborPos, neighborSide);
            if (externalFluid != null) {
                moved |= moveFluids(externalFluid, routes.fluidInput(),
                        JDTEConfig.COMMON.autoIoFluidTransferRate.get(), level.getGameTime()) > 0;
            }
        }

        return moved;
    }

    private static List<BlockPos> externalNeighbors(ServerLevel level, BaseMachineBE machine, Direction side) {
        if (machine instanceof LargeMineralExtractorBE extractor) {
            return extractor.getBoundaryNeighbors().stream()
                    .filter(neighbor -> neighbor.exposedSide() == side)
                    .map(LargeMineralExtractorStructure.BoundaryNeighbor::pos)
                    .distinct()
                    .toList();
        }
        if (machine instanceof LargeGreenhouseBE greenhouse) {
            return greenhouse.getBoundaryNeighbors().stream()
                    .filter(neighbor -> neighbor.exposedSide() == side)
                    .map(LargeGreenhouseStructure.BoundaryNeighbor::pos)
                    .distinct()
                    .toList();
        }
        if (machine instanceof LifeSynthesisVatBE vat) {
            return vat.getBoundaryNeighbors().stream()
                    .filter(neighbor -> neighbor.exposedSide() == side)
                    .map(LifeSynthesisStructure.BoundaryNeighbor::pos)
                    .distinct()
                    .toList();
        }
        return List.of(machine.getBlockPos().relative(side));
    }

    private static IoRoutes getRoutes(BaseMachineBE machine) {
        ResourceHandler<ItemResource> handler = machine.getMachineHandler();
        int[] itemInputs = NO_SLOTS;
        int[] itemOutputs = NO_SLOTS;
        ResourceHandler<FluidResource> fluidInput = null;
        ResourceHandler<FluidResource> fluidOutput = null;

        if (machine instanceof GelGeneratorBE generator) {
            itemInputs = boundedSlots(handler,
                    GelGeneratorBE.GEL_SLOT,
                    GelGeneratorBE.FOOD_SLOT,
                    GelGeneratorBE.INPUT_START_SLOT,
                    GelGeneratorBE.INPUT_START_SLOT + 1,
                    GelGeneratorBE.INPUT_START_SLOT + 2,
                    GelGeneratorBE.INPUT_START_SLOT + 3);
            itemOutputs = boundedSlots(handler, range(GelGeneratorBE.OUTPUT_START_SLOT, GelGeneratorBE.OUTPUT_SLOTS));
            fluidInput = generator.getFluidTank();
            fluidOutput = generator.getOutputFluidTank();
        } else if (machine instanceof FluidMixerBE mixer) {
            itemInputs = boundedSlots(handler, FluidMixerBE.CATALYST_SLOT);
            fluidInput = mixer.getCombinedFluidHandler();
            fluidOutput = mixer.outputTank;
        } else if (machine instanceof InfusionMachineBE infusion) {
            itemInputs = boundedSlots(handler, InfusionMachineBE.INPUT_SLOT);
            itemOutputs = boundedSlots(handler, InfusionMachineBE.OUTPUT_SLOT);
            fluidInput = infusion.getFluidTank();
        } else if (machine instanceof AdvancedPotionBrewerBE brewer) {
            itemInputs = boundedSlots(handler,
                    AdvancedPotionBrewerBE.BOTTLE_SLOT_0,
                    AdvancedPotionBrewerBE.BOTTLE_SLOT_1,
                    AdvancedPotionBrewerBE.BOTTLE_SLOT_2,
                    AdvancedPotionBrewerBE.FUEL_SLOT,
                    AdvancedPotionBrewerBE.INGREDIENT_SLOT,
                    AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START,
                    AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + 1,
                    AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + 2,
                    AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + 3,
                    AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + 4);
            itemOutputs = boundedSlots(handler, range(AdvancedPotionBrewerBE.OUTPUT_SLOT_START, AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT));
            fluidInput = brewer.getFluidHandler();
        } else if (machine instanceof LootFabricatorBE fabricator) {
            itemInputs = boundedSlots(handler, range(0, LootFabricatorBE.INPUT_SLOTS));
            itemOutputs = boundedSlots(handler, range(LootFabricatorBE.INPUT_SLOTS, fabricator.getActiveOutputSlots()));
            fluidInput = fabricator.getFluidHandler();
        } else if (machine instanceof MineralExtractorBE extractor) {
            itemInputs = boundedSlots(handler, range(0, extractor.surveySlotCount()));
            itemOutputs = boundedSlots(handler, range(extractor.outputStartSlot(), extractor.getActiveOutputSlots()));
            fluidInput = extractor.getCombinedFluidHandler();
        } else if (machine instanceof GlueActivatorBE) {
            itemInputs = boundedSlots(handler, GlueActivatorBE.REVIVE_SLOT);
        } else if (machine instanceof FluidStabilizerBE) {
            itemInputs = boundedSlots(handler, FluidStabilizerBE.CATALYST_SLOT);
        } else if (machine instanceof ItemSenderBE) {
            itemInputs = allSlots(handler);
        } else if (machine instanceof ItemReceiverBE) {
            itemOutputs = allSlots(handler);
        } else if (machine instanceof FluidSenderBE sender) {
            fluidInput = sender.getFluidTank();
        } else if (machine instanceof FluidReceiverBE receiver) {
            fluidOutput = receiver.getFluidTank();
        } else if (machine instanceof BioCrusherBE crusher) {
            itemOutputs = crusher.hasOutputInventory() ? boundedSlots(handler, range(0, crusher.getActiveOutputSlotCount())) : NO_SLOTS;
            fluidOutput = crusher.getFluidTank();
        } else if (machine instanceof LifeExtractorBE extractor) {
            fluidOutput = extractor.getFluidTank();
        } else if (machine instanceof ICreativeGreenhouse greenhouse) {
            itemInputs = boundedSlots(handler, range(0, greenhouse.inputSlots()));
            itemOutputs = boundedSlots(handler, range(greenhouse.outputStartSlot(),
                    greenhouse.distinctOutputTypes()));
        } else if (machine instanceof GreenhouseBE greenhouse) {
            itemInputs = boundedSlots(handler, range(0, GreenhouseBE.INPUT_SLOTS));
            itemOutputs = boundedSlots(handler, range(GreenhouseBE.OUTPUT_START_SLOT, greenhouse.getActiveOutputSlots()));
            fluidInput = greenhouse.getFluidTank();
        } else if (machine instanceof com.jdte.common.blockentities.LargeGreenhouseBE greenhouse) {
            itemInputs = boundedSlots(handler, range(0, com.jdte.common.blockentities.LargeGreenhouseBE.INPUT_SLOTS));
            itemOutputs = boundedSlots(handler, range(com.jdte.common.blockentities.LargeGreenhouseBE.OUTPUT_START_SLOT,
                    greenhouse.getActiveOutputSlots()));
            fluidInput = greenhouse.getFluidTank();
        } else if (machine instanceof LifeSynthesisVatBE vat) {
            // 合成舱：培养基输入 12 槽；流体输入走组合罐（养分/时间），生命流体输出走生命罐
            itemInputs = boundedSlots(handler, range(0, LifeSynthesisVatBE.INPUT_SLOTS));
            fluidInput = vat.getCombinedFluidHandler();
            fluidOutput = vat.getLifeFluidTank();
        } else if (machine instanceof com.jdte.common.blockentities.BioFactoryBE factory) {
            itemInputs = boundedSlots(handler, com.jdte.common.blockentities.BioFactoryBE.SPECIMEN_SLOT,
                    com.jdte.common.blockentities.BioFactoryBE.FOOD_SLOT,
                    com.jdte.common.blockentities.BioFactoryBE.SECONDARY_INPUT_SLOT,
                    com.jdte.common.blockentities.BioFactoryBE.TERTIARY_INPUT_SLOT);
            itemOutputs = boundedSlots(handler, range(com.jdte.common.blockentities.BioFactoryBE.OUTPUT_START_SLOT,
                    factory.getActiveOutputSlots()));
            fluidInput = factory.getInputFluidHandler();
            fluidOutput = factory.getOutputFluidHandler();
        } else if (machine instanceof LifeBreederBE breeder) {
            itemInputs = boundedSlots(handler, range(0, LifeBreederBE.FEED_SLOTS));
            itemOutputs = boundedSlots(handler, range(LifeBreederBE.OUTPUT_START_SLOT, LifeBreederBE.OUTPUT_SLOTS));
            fluidInput = breeder.getFluidTank();
        } else if (machine instanceof CrystalIncubatorBE incubator) {
            itemOutputs = allSlots(handler);
            fluidInput = incubator.getFluidTank();
        } else if (machine instanceof TimeAcceleratorBE accelerator) {
            fluidInput = accelerator.getFluidTank();
        } else if (machine instanceof DropperT1BE) {
            itemInputs = allSlots(handler);
        } else if (machine instanceof ClickerT1BE
                || machine instanceof BlockBreakerT1BE
                || machine instanceof BlockPlacerT1BE
                || machine instanceof FluidCollectorT1BE
                || machine instanceof FluidPlacerT1BE) {
            itemInputs = boundedSlots(handler, 0);
        } else if (!(machine instanceof BlockSwapperT1BE) && handler != null && handler.size() > 0) {
            itemInputs = allSlots(handler);
        }

        if (machine instanceof FluidCollectorT1BE collector) {
            fluidOutput = collector.getFluidTank();
        } else if (machine instanceof FluidPlacerT1BE placer) {
            fluidInput = placer.getFluidTank();
        } else if (machine instanceof ClickerT1BE && UpgradeHelper.hasFluidStorageUpgrade(machine)) {
            fluidInput = UpgradeHelper.getClickerFluidTank(machine);
        } else if (fluidInput == null && fluidOutput == null && machine instanceof FluidMachineBE fluidMachine) {
            fluidInput = fluidMachine.getFluidTank();
        }

        return new IoRoutes(itemInputs, itemOutputs, fluidInput, fluidOutput);
    }

    private static ResourceHandler<ItemResource> selectedSlots(ResourceHandler<ItemResource> inventory, int[] slots) {
        return new CombinedResourceHandler<>(Arrays.stream(slots)
                .mapToObj(slot -> RangedResourceHandler.ofSingleIndex(inventory, slot)).toList());
    }

    private static boolean pullItems(ResourceHandler<ItemResource> source, ResourceHandler<ItemResource> target,
                                     int[] targetSlots, int limit) {
        return ResourceHandlerUtil.move(source, selectedSlots(target, targetSlots), item -> true, limit, null) > 0;
    }

    private static boolean pushItems(ResourceHandler<ItemResource> source, int[] sourceSlots,
                                     ResourceHandler<ItemResource> target, int limit) {
        return ResourceHandlerUtil.moveStacking(selectedSlots(source, sourceSlots), target, item -> true, limit, null) > 0;
    }

    private static boolean pushGroupedOversizedItems(ResourceHandler<ItemResource> source, int[] sourceSlots, ResourceHandler<ItemResource> target,
                                                     int itemBudget, int typeBudget, long rotationSeed) {
        Map<ItemResource, OversizedSourceGroup> grouped = new LinkedHashMap<>();
        for (int sourceSlot : sourceSlots) {
            if (sourceSlot < 0 || sourceSlot >= source.size()) continue;
            ItemResource resource = source.getResource(sourceSlot);
            if (resource.isEmpty()) continue;
            grouped.computeIfAbsent(resource, OversizedSourceGroup::new)
                    .add(sourceSlot, source.getAmountAsInt(sourceSlot));
        }
        if (grouped.isEmpty()) return false;

        List<OversizedSourceGroup> groups = new ArrayList<>(grouped.values());
        int start = (int) Math.floorMod(rotationSeed, groups.size());
        int remainingItemBudget = Math.max(1, itemBudget);
        int remainingTypeBudget = Math.max(1, typeBudget);
        boolean moved = false;
        for (int offset = 0; offset < groups.size()
                && remainingItemBudget > 0 && remainingTypeBudget > 0; offset++) {
            OversizedSourceGroup group = groups.get((start + offset) % groups.size());
            remainingTypeBudget--;
            int offeredCount = (int) Math.min(group.totalCount, remainingItemBudget);
            if (offeredCount <= 0) continue;

            try (var transaction = Transaction.openRoot()) {
                int accepted = ResourceHandlerUtil.insertStacking(target, group.key, offeredCount, transaction);
                if (accepted == 0) continue;
                int extracted = 0;
                for (int slot : group.sourceSlots) {
                    extracted += source.extract(slot, group.key, accepted - extracted, transaction);
                    if (extracted == accepted) break;
                }
                if (extracted != accepted) continue;
                transaction.commit();
                remainingItemBudget -= accepted;
                moved = true;
            }
        }
        return moved;
    }

    /**
     * Offers one maximum-width integer batch for each selected creative catalog type. The receiver
     * remains authoritative about its capacity, and the source is intentionally never consumed.
     */
    private static boolean pushCreativeGreenhouseItems(ICreativeGreenhouse source, int[] sourceSlots,
                                                        ResourceHandler<ItemResource> target, int typeBudget, long rotationSeed) {
        if (sourceSlots.length == 0 || typeBudget <= 0) return false;
        int start = (int) Math.floorMod(rotationSeed, sourceSlots.length);
        int remainingTypes = typeBudget;
        boolean moved = false;
        for (int offset = 0; offset < sourceSlots.length && remainingTypes-- > 0; offset++) {
            int absoluteSlot = sourceSlots[(start + offset) % sourceSlots.length];
            int catalogSlot = absoluteSlot - source.outputStartSlot();
            ItemStack offered = source.catalogPrototypeAt(catalogSlot);
            if (offered.isEmpty()) continue;
            try (var transaction = Transaction.openRoot()) {
                int inserted = ResourceHandlerUtil.insertStacking(target, ItemResource.of(offered),
                        offered.getMaxStackSize(), transaction);
                if (inserted > 0) {
                    transaction.commit();
                    moved = true;
                }
            }
        }
        return moved;
    }

    private static final class OversizedSourceGroup {
        private final ItemResource key;
        private final List<Integer> sourceSlots = new ArrayList<>();
        private long totalCount;

        private OversizedSourceGroup(ItemResource key) {
            this.key = key;
        }

        private void add(int sourceSlot, int count) {
            sourceSlots.add(sourceSlot);
            totalCount += count;
        }
    }

    static int moveFluids(ResourceHandler<FluidResource> source, ResourceHandler<FluidResource> target,
                          int limit, long cycle) {
        if (source == target || limit <= 0) return 0;
        if (source.size() <= 1) return ResourceHandlerUtil.move(source, target, fluid -> true, limit, null);
        var types = new java.util.LinkedHashSet<FluidResource>();
        for (int slot = 0; slot < source.size(); slot++) {
            FluidResource resource = source.getResource(slot);
            if (!resource.isEmpty()) types.add(resource);
        }
        if (types.isEmpty()) return 0;
        var fluids = List.copyOf(types);
        int start = Math.floorMod(cycle, fluids.size());
        int remaining = limit;
        try (var tx = Transaction.openRoot()) {
            for (int i = 0; i < fluids.size() && remaining > 0; i++) {
                FluidResource fluid = fluids.get((start + i) % fluids.size());
                int share = Math.ceilDiv(remaining, fluids.size() - i);
                remaining -= ResourceHandlerUtil.move(source, target, fluid::equals, share, tx);
            }
            // Redistribute unused shares when a tank is empty, full, or rejects a fluid.
            if (remaining > 0) remaining -= ResourceHandlerUtil.move(source, target, fluid -> true, remaining, tx);
            tx.commit();
        }
        return limit - remaining;
    }

    private static Direction directionForUiSide(BaseMachineBE machine, int uiSide) {
        return switch (uiSide) {
            case IO_SIDE_NORTH -> Direction.NORTH;
            case IO_SIDE_SOUTH -> Direction.SOUTH;
            case IO_SIDE_WEST -> Direction.WEST;
            case IO_SIDE_EAST -> Direction.EAST;
            case IO_SIDE_UP -> Direction.UP;
            case IO_SIDE_DOWN -> Direction.DOWN;
            default -> Direction.NORTH;
        };
    }

    private static int getFailureBackoffStart() {
        return Math.min(JDTEConfig.COMMON.transferFailureBackoffStart.get(), getMaxFailureBackoff());
    }

    private static int getMaxFailureBackoff() {
        return JDTEConfig.COMMON.transferFailureBackoffMax.get();
    }

    private static int[] allSlots(ResourceHandler<ItemResource> handler) {
        if (handler == null || handler.size() <= 0) {
            return NO_SLOTS;
        }
        return range(0, handler.size());
    }

    private static int[] boundedSlots(ResourceHandler<ItemResource> handler, int... slots) {
        if (handler == null || handler.size() <= 0 || slots.length == 0) {
            return NO_SLOTS;
        }
        int[] bounded = Arrays.stream(slots)
                .filter(slot -> slot >= 0 && slot < handler.size())
                .toArray();
        return bounded.length == 0 ? NO_SLOTS : bounded;
    }

    private static int[] range(int start, int count) {
        if (count <= 0) {
            return NO_SLOTS;
        }
        int[] slots = new int[count];
        for (int i = 0; i < count; i++) {
            slots[i] = start + i;
        }
        return slots;
    }

    private record IoRoutes(int[] itemInputs, int[] itemOutputs, ResourceHandler<FluidResource> fluidInput, ResourceHandler<FluidResource> fluidOutput) {
        boolean hasItemInputs() {
            return itemInputs.length > 0;
        }

        boolean hasItemOutputs() {
            return itemOutputs.length > 0;
        }

        boolean isEmpty() {
            return !hasItemInputs() && !hasItemOutputs() && fluidInput == null && fluidOutput == null;
        }
    }
}
