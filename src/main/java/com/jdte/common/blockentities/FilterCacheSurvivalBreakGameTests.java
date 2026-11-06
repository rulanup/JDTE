package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Regression tests for the post-removal save hazard fixed by caching the
 * filter handler at construction in every Filterable JDTE block entity.
 *
 * <p>Vanilla survival {@code destroyBlock} removes the block entity
 * (invalidating its capabilities) BEFORE {@code dropResources}, and JDT
 * {@code getDrops} then calls {@code saveAdditional}, which reads
 * {@code getFilterHandler()} for Filterable machines. Before the fix, every
 * machine listed here crashed a production server with
 * "Missing JDTE filter capability" when a non-default machine was broken in
 * survival. A non-empty filter slot is the simplest universal non-default
 * trigger: JDT {@code isDefaultSettings()} returns false as soon as any
 * filter slot holds an item, independent of upgrade compatibility, area
 * defaults, or stored energy.</p>
 *
 * <p>Each test breaks one representative of a fixed root family and asserts
 * the break completes, leaves air with no block entity, and drops exactly
 * one self item. ExtendedSensorBE is deliberately absent: it delegates to
 * its JDT parent, which returns a field handler and never touched a
 * capability on this path.</p>
 */
@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class FilterCacheSurvivalBreakGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private static int countSelf(net.minecraft.world.level.Level level, AABB area, Item expected) {
        int n = 0;
        for (var e : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (e.getItem().is(expected)) n += e.getItem().getCount();
        }
        return n;
    }

    private static void breakNonDefaultMachineSurvives(GameTestHelper h, Block block, Item expected, String name) {
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, block);
        var be = h.getBlockEntity(POS);
        h.assertTrue(be instanceof FilterableBE, name + " has a FilterableBE");
        var filter = ((FilterableBE) be).getFilterHandler();
        h.assertTrue(filter != null, name + " filter handler present");
        filter.setStackInSlot(0, new ItemStack(Items.STONE));
        // Snapshot before the break: GameTest structures sit close together
        // and ItemEntities persist across tests and runs, so an absolute
        // count would also catch neighbours' drops. Only the delta produced
        // by this break is asserted. The pre-break filter state is recorded
        // for diagnosis: a machine whose filter rejects the stone would stay
        // on the default-settings drop path.
        var abs = h.absolutePos(POS);
        var level = h.getLevel();
        var area = new AABB(abs).inflate(2);
        int before = countSelf(level, area, expected);
        var filter0 = filter.getStackInSlot(0).copy();
        boolean isDefault = be instanceof BaseMachineBE base && base.isDefaultSettings();
        var player = FakePlayerFactory.getMinecraft(level);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setPos(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5);
        // The FakePlayer must hold a tool: GameMode destroyBlock only calls
        // playerDestroy (and therefore drops anything) when canHarvestBlock
        // is true, and the life breeder requires the correct tool for drops.
        // A bare-handed break correctly removes the block but drops nothing,
        // which is vanilla behaviour, not a missing drop.
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        var modeAtBreak = player.gameMode.getGameModeForPlayer();
        var stateAtBreak = level.getBlockState(abs);
        var canHarvestAtBreak = stateAtBreak.canHarvestBlock(level, abs, player);
        var hasCorrectToolAtBreak = player.hasCorrectToolForDrops(stateAtBreak);
        var handAtBreak = player.getMainHandItem().getItem();
        var inPickaxeTagAtBreak = stateAtBreak.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE);
        var registryTagAtBreak = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.BLOCK)
                .getTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE);
        var registryTagHasBreederAtBreak = registryTagAtBreak
                .map(holders -> holders.stream().anyMatch(holder -> holder.value() == JDTEBlocks.LIFE_BREEDER.get()))
                .orElse(false);
        var registryTagHasTimeAccelAtBreak = registryTagAtBreak
                .map(holders -> holders.stream().anyMatch(holder -> holder.value() == JDTEBlocks.BASIC_TIME_ACCELERATOR.get()))
                .orElse(false);
        var registryTagSizeAtBreak = registryTagAtBreak.map(holders -> holders.size()).orElse(-1);
        var registryTagJdteEntriesAtBreak = registryTagAtBreak
                .map(holders -> holders.stream()
                        .map(holder -> net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(holder.value()).toString())
                        .filter(id -> id.startsWith("jdte:")).toList().toString())
                .orElse("(no tag)");
        var tierOkAtBreak = net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(
                net.minecraft.world.item.Tiers.IRON, stateAtBreak);
        h.assertTrue(player.gameMode.destroyBlock(abs), name + " breaks in survival");
        h.assertTrue(level.getBlockState(abs).isAir() && level.getBlockEntity(abs) == null,
                name + " leaves air and no BE");
        int after = countSelf(level, area, expected);
        var seen = new StringBuilder();
        for (var e : level.getEntitiesOfClass(ItemEntity.class, area)) {
            seen.append(e.getItem().getItem()).append('x').append(e.getItem().getCount()).append(';');
        }
        var wide = new StringBuilder();
        for (var e : level.getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(8))) {
            if (e.getItem().is(expected)) wide.append(e.getItem().getCount()).append(';');
        }
        h.assertTrue(after - before == 1, name + " drops exactly one new self item, before=" + before
                + ", after=" + after + ", filter0=" + filter0 + ", isDefault=" + isDefault
                + ", modeAtBreak=" + modeAtBreak + ", canHarvest=" + canHarvestAtBreak
                + ", hasCorrectTool=" + hasCorrectToolAtBreak + ", hand=" + handAtBreak
                + ", inPickaxeTag=" + inPickaxeTagAtBreak + ", registryTagHasBreeder=" + registryTagHasBreederAtBreak
                + ", registryTagHasTimeAccel=" + registryTagHasTimeAccelAtBreak
                + ", registryTagSize=" + registryTagSizeAtBreak
                + ", registryTagJdte=" + registryTagJdteEntriesAtBreak
                + ", tierOk=" + tierOkAtBreak
                + ", wideSelf=[" + wide + "]"
                + ", seen=[" + seen + "]");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultAdvancedEnergyTransmitterDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get(),
                JDTEItems.ADVANCED_ENERGY_TRANSMITTER.get(), "advanced_energy_transmitter");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultAdvancedEntityAcceleratorDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.ADVANCED_ENTITY_ACCELERATOR.get(),
                JDTEItems.ADVANCED_ENTITY_ACCELERATOR.get(), "advanced_entity_accelerator");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultAdvancedItemCollectorDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.ADVANCED_ITEM_COLLECTOR.get(),
                JDTEItems.ADVANCED_ITEM_COLLECTOR.get(), "advanced_item_collector");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultAdvancedBioExtractorDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get(),
                JDTEItems.ADVANCED_BIO_EXTRACTOR.get(), "advanced_bio_extractor");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultEntitySuppressorDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.ENTITY_SUPPRESSOR.get(),
                JDTEItems.ENTITY_SUPPRESSOR.get(), "entity_suppressor");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultExtendedBlockBreakerDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.EXTENDED_BLOCK_BREAKER.get(),
                JDTEItems.EXTENDED_BLOCK_BREAKER.get(), "extended_block_breaker");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultExtendedBlockPlacerDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.EXTENDED_BLOCK_PLACER.get(),
                JDTEItems.EXTENDED_BLOCK_PLACER.get(), "extended_block_placer");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultExtendedBlockSwapperDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get(),
                JDTEItems.EXTENDED_BLOCK_SWAPPER.get(), "extended_block_swapper");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultExtendedClickerDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.EXTENDED_CLICKER.get(),
                JDTEItems.EXTENDED_CLICKER.get(), "extended_clicker");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultExtendedFluidCollectorDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get(),
                JDTEItems.EXTENDED_FLUID_COLLECTOR.get(), "extended_fluid_collector");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultExtendedFluidPlacerDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.EXTENDED_FLUID_PLACER.get(),
                JDTEItems.EXTENDED_FLUID_PLACER.get(), "extended_fluid_placer");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultBasicFluidReceiverDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.BASIC_FLUID_RECEIVER.get(),
                JDTEItems.BASIC_FLUID_RECEIVER.get(), "basic_fluid_receiver");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultBasicFluidSenderDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.BASIC_FLUID_SENDER.get(),
                JDTEItems.BASIC_FLUID_SENDER.get(), "basic_fluid_sender");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultBasicFluidStabilizerDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.BASIC_FLUID_STABILIZER.get(),
                JDTEItems.BASIC_FLUID_STABILIZER.get(), "basic_fluid_stabilizer");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultAdvancedGelGeneratorDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.ADVANCED_GEL_GENERATOR.get(),
                JDTEItems.ADVANCED_GEL_GENERATOR.get(), "advanced_gel_generator");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultBasicGlueActivatorDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.BASIC_GLUE_ACTIVATOR.get(),
                JDTEItems.BASIC_GLUE_ACTIVATOR.get(), "basic_glue_activator");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultBasicItemReceiverDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.BASIC_ITEM_RECEIVER.get(),
                JDTEItems.BASIC_ITEM_RECEIVER.get(), "basic_item_receiver");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultLifeBreederDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.LIFE_BREEDER.get(),
                JDTEItems.LIFE_BREEDER.get(), "life_breeder");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultAdvancedLifeExtractorDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get(),
                JDTEItems.ADVANCED_LIFE_EXTRACTOR.get(), "advanced_life_extractor");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultMineralExtractorDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.MINERAL_EXTRACTOR.get(),
                JDTEItems.MINERAL_EXTRACTOR.get(), "mineral_extractor");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultRangeBlockerDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.RANGE_BLOCKER.get(),
                JDTEItems.RANGE_BLOCKER.get(), "range_blocker");
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultBasicTimeAcceleratorDoesNotCrash(GameTestHelper h) {
        breakNonDefaultMachineSurvives(h, JDTEBlocks.BASIC_TIME_ACCELERATOR.get(),
                JDTEItems.BASIC_TIME_ACCELERATOR.get(), "basic_time_accelerator");
    }

    @GameTest(template = "mineral_empty")
    public static void lifeBreederDropLogicYieldsSelfItem(GameTestHelper h) {
        // The GameMode survival break of a non-default life breeder drops
        // nothing (before=0, after=0), which contradicts JDT getDrops: the
        // loot table yields one self item, and the non-default branch clears
        // the list and adds one self item. Isolate which stage loses it.
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, JDTEBlocks.LIFE_BREEDER.get());
        var be = h.getBlockEntity(POS);
        h.assertTrue(be instanceof FilterableBE, "life breeder has a FilterableBE");
        ((FilterableBE) be).getFilterHandler().setStackInSlot(0, new ItemStack(Items.STONE));
        boolean isDefault = be instanceof BaseMachineBE base && base.isDefaultSettings();
        var level = h.getLevel();
        var abs = h.absolutePos(POS);
        var state = level.getBlockState(abs);
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.atCenterOf(abs))
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY, be)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, ItemStack.EMPTY);
        var direct = JDTEBlocks.LIFE_BREEDER.get().getDrops(state, params);
        h.assertTrue(direct.size() == 1 && direct.get(0).is(JDTEItems.LIFE_BREEDER.get()),
                "direct getDrops yields one self item, got=" + direct + ", isDefault=" + isDefault);
        var area = new AABB(abs).inflate(2);
        int before = countSelf(level, area, JDTEItems.LIFE_BREEDER.get());
        h.assertTrue(level.destroyBlock(abs, true), "level destroyBlock succeeds");
        int after = countSelf(level, area, JDTEItems.LIFE_BREEDER.get());
        h.assertTrue(after - before == 1, "level destroy drops one new self item, before=" + before + ", after=" + after);
        h.succeed();
    }
}
