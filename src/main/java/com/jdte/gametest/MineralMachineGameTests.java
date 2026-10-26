package com.jdte.gametest;

import com.jdte.common.blockentities.*;
import com.jdte.common.blocks.LargeMineralExtractorStructure;
import com.jdte.common.items.MineralSurveyItem;
import com.jdte.common.minerals.*;
import com.jdte.common.network.MineralPackets;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Integration tests run in a real Forge server, with registries, recipes, capabilities and mixins. */
@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class MineralMachineGameTests {
    private static final BlockPos POS = new BlockPos(3, 1, 2);
    private static MineralExtractorBE machine(GameTestHelper h) {
        h.setBlock(POS, JDTEBlocks.MINERAL_EXTRACTOR.get());
        MineralExtractorBE be = (MineralExtractorBE) h.getBlockEntity(POS);
        survey(h, be, 0, MineralSurveyIndex.version(h.getLevel().getServer()));
        be.getEnergyStorage().setEnergy(2_000_000);
        return be;
    }
    private static void survey(GameTestHelper h, MineralExtractorBE be, int slot, long version) {
        ItemStack survey = new ItemStack(JDTEItems.MINERAL_SURVEY.get());
        MineralSurveyItem.setSurvey(survey, MineralSurveyData.create(version,
                ResourceLocation.withDefaultNamespace("plains"), h.getLevel().dimension().location(),
                List.of(new MineralEntry(ResourceLocation.withDefaultNamespace("iron_ore"), 1, -64, 64, 9, MineralEntry.Confidence.EXACT))));
        be.getMachineHandler().setStackInSlot(slot, survey);
    }
    private static int output(MineralExtractorBE be) {
        int count = 0;
        for (int i = be.outputStartSlot(); i < be.getMachineHandler().getSlots(); i++) count += be.getMachineHandler().getStackInSlot(i).getCount();
        return count;
    }
    private static void equal(GameTestHelper h, int actual, int expected, String label) {
        h.assertTrue(actual == expected, label + ": expected " + expected + ", got " + actual);
    }
    @GameTest(template = "mineral_empty")
    public static void baseProductionNeedsOnlyEnergy(GameTestHelper h) {
        MineralExtractorBE be = machine(h);
        be.setMultiplier(32); // No Time Fluid: keep the original 64-work base production.
        be.tickServer();
        equal(h, output(be), 3, "base cycles");
        equal(h, be.getEnergyStorage().getEnergyStored(), 1_985_000, "base FE");
        equal(h, be.getMachineHandler().getStackInSlot(0).getCount(), 1, "reusable survey");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void fluidsPayOnlyForCompletedBonusCycles(GameTestHelper h) {
        MineralExtractorBE be = machine(h);
        be.setMultiplier(32);
        be.getTimeFluidTank().setFluid(new FluidStack(BuiltInRegistries.FLUID.get(new ResourceLocation("justdirethings:time_fluid")), 10));
        be.getExperienceFluidTank().setFluid(new FluidStack(BuiltInRegistries.FLUID.get(new ResourceLocation("justdirethings:xp_fluid")), 125));
        be.tickServer();
        equal(h, output(be), 10, "5 cycles with 100 percent fortune");
        equal(h, be.getEnergyStorage().getEnergyStored(), 1_975_000, "5 cycles FE");
        equal(h, be.getTimeFluidTank().getFluidAmount(), 0, "2 accelerated cycles Time Fluid");
        equal(h, be.getExperienceFluidTank().getFluidAmount(), 0, "5 fortune cycles XP Fluid");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void fullOutputDoesNotConsumeResources(GameTestHelper h) {
        MineralExtractorBE be = machine(h);
        for (int i = 0; i < 16; i++) be.getMachineHandler().setStackInSlot(be.outputStartSlot() + i, new ItemStack(Items.COBBLESTONE, 64));
        be.tickServer();
        equal(h, be.getEnergyStorage().getEnergyStored(), 2_000_000, "blocked FE");
        h.assertTrue(be.getMachineState() == MineralExtractorBE.State.OUTPUT_FULL, "output-full state");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void staleSurveyPausesProduction(GameTestHelper h) {
        MineralExtractorBE be = machine(h);
        survey(h, be, 0, MineralSurveyIndex.version(h.getLevel().getServer()) + 1);
        be.tickServer();
        equal(h, output(be), 0, "stale survey output");
        equal(h, be.getEnergyStorage().getEnergyStored(), 2_000_000, "stale survey FE");
        h.assertTrue(be.getMachineState() == MineralExtractorBE.State.STALE_SURVEY, "stale survey state");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void smelterUsesRecipesAndDoubleEnergy(GameTestHelper h) {
        MineralExtractorBE be = machine(h);
        UpgradeHelper.getUpgradeHandler(be).insertItem(0, new ItemStack(com.direwolf20.justdirethings.setup.Registration.UPGRADE_SMELTER.get()), false);
        be.tickServer();
        h.assertTrue(be.getMachineHandler().getStackInSlot(be.outputStartSlot()).is(Items.IRON_INGOT), "actual smelting recipe output");
        equal(h, output(be), 3, "smelted amount");
        equal(h, be.getEnergyStorage().getEnergyStored(), 1_970_000, "double FE");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void oversizedInventoryAndSurveySurviveSave(GameTestHelper h) {
        MineralExtractorBE be = machine(h);
        UpgradeHelper.getUpgradeHandler(be).insertItem(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false);
        be.getMachineHandler().setStackInSlot(be.outputStartSlot() + 63, new ItemStack(Items.IRON_ORE, 6_400_000));
        be.setMultiplier(20);
        var saved = be.saveWithFullMetadata();
        h.setBlock(new BlockPos(1, 1, 1), JDTEBlocks.MINERAL_EXTRACTOR.get());
        MineralExtractorBE restored = (MineralExtractorBE) h.getBlockEntity(new BlockPos(1, 1, 1));
        restored.load(saved);
        equal(h, restored.getMachineHandler().getStackInSlot(restored.outputStartSlot() + 63).getCount(), 6_400_000, "oversized NBT");
        h.assertTrue(MineralSurveyItem.getSurvey(restored.getMachineHandler().getStackInSlot(0)) != null, "survey NBT");
        h.assertTrue(UpgradeHelper.hasUltimateCapacity(restored), "upgrade capability NBT");
        equal(h, restored.getMultiplier(), 20, "multiplier NBT");
        equal(h, restored.getActiveOutputSlots(), 64, "preserved output slots");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void largeMachinePartsForwardAndDismantle(GameTestHelper h) {
        h.setBlock(POS, JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get());
        BlockPos controller = h.absolutePos(POS);
        h.assertTrue(LargeMineralExtractorStructure.placeParts(h.getLevel(), controller, Direction.NORTH), "place 17 parts");
        LargeMineralExtractorBE be = (LargeMineralExtractorBE) h.getBlockEntity(POS);
        survey(h, be, 0, MineralSurveyIndex.version(h.getLevel().getServer()));
        be.getEnergyStorage().setEnergy(2_000_000);
        equal(h, be.surveySlotCount(), 4, "large survey slots");
        h.assertTrue(be.getCapability(ForgeCapabilities.ENERGY).isPresent(), "controller FE capability matches source");
        h.assertTrue(be.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent(), "controller fluid capability");
        h.assertTrue(be.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent(), "controller restricted item capability");
        BlockPos partPos = LargeMineralExtractorStructure.position(controller, Direction.NORTH, -1, 1, 1);
        var part = h.getLevel().getBlockEntity(partPos);
        h.assertTrue(part instanceof LargeMineralExtractorPartBE, "part Forge capability relay");
        h.assertTrue(part.getCapability(ForgeCapabilities.ENERGY).orElseThrow(AssertionError::new).getEnergyStored() == be.getEnergyStorage().getEnergyStored(), "shared FE store");
        var itemCap = part.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(AssertionError::new);
        h.assertTrue(itemCap.extractItem(0, 1, false).isEmpty(), "pipes cannot remove surveys");
        h.assertTrue(part.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent(), "part fluid input");
        be.tickServer();
        equal(h, output(be), 12, "4 times base work");
        equal(h, be.getEnergyStorage().getEnergyStored(), 1_940_000, "large FE");
        var captured = part.getCapability(ForgeCapabilities.ENERGY);
        h.getLevel().destroyBlock(partPos, true);
        h.assertTrue(h.getLevel().getBlockEntity(controller) == null, "controller removed with part");
        h.assertTrue(!captured.isPresent(), "removed relay invalidates cached capability");
        for (int y = 0; y < 2; y++) for (int depth = 0; depth < 3; depth++) for (int x = -1; x <= 1; x++)
            h.assertTrue(h.getLevel().isEmptyBlock(LargeMineralExtractorStructure.position(controller, Direction.NORTH, x, depth, y)), "whole structure removed");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void networkPreservesOversizedCounts(GameTestHelper h) {
        var stacks = new java.util.ArrayList<ItemStack>();
        stacks.add(new ItemStack(Items.IRON_ORE, 6_400_000));
        for (int i = 1; i < 16; i++) stacks.add(ItemStack.EMPTY);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            MineralPackets.writeSlots(new MineralPackets.Slots(42, 3, stacks), buffer);
            var decoded = MineralPackets.readSlots(buffer);
            equal(h, decoded.stacks().get(0).getCount(), 6_400_000, "network count");
            equal(h, decoded.page(), 3, "network page");
            equal(h, decoded.containerId(), 42, "network menu ID");
        } finally { buffer.release(); }
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void worldgenIndexContainsVanillaOresAndLocalProduction(GameTestHelper h) {
        var profiles = MineralSurveyIndex.profiles(h.getLevel().getServer());
        var plains = profiles.get(ResourceLocation.withDefaultNamespace("plains"));
        if (net.minecraftforge.fml.ModList.get().isLoaded("mekanism"))
            h.assertTrue(profiles.values().stream().flatMap(List::stream).anyMatch(e -> e.oreId().getNamespace().equals("mekanism")), "Mekanism ore feature analysis");
        h.assertTrue(plains != null && plains.stream().anyMatch(e -> e.oreId().equals(ResourceLocation.withDefaultNamespace("iron_ore"))), "vanilla biome feature analysis");
        MineralExtractorBE be = machine(h);
        be.getMachineHandler().setStackInSlot(0, ItemStack.EMPTY);
        be.tickServer();
        h.assertTrue(!be.isSurveySource(), "no survey uses local biome");
        h.assertTrue(output(be) > 0, "local biome production");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void outputPageAndShiftClickKeepInventoryBoundaries(GameTestHelper h) {
        MineralExtractorBE be = machine(h);
        UpgradeHelper.getUpgradeHandler(be).insertItem(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false);
        be.getMachineHandler().setStackInSlot(be.outputStartSlot() + 48, new ItemStack(Items.IRON_ORE, 4096));
        var player = h.makeMockSurvivalPlayer();
        var menu = new com.jdte.common.containers.MineralExtractorContainer(1, player.getInventory(), be.getBlockPos());
        equal(h, menu.slots.size(), 70, "17 machine + 9 filter + 8 upgrade + 36 player slots");
        menu.setOutputPage(3);
        equal(h, menu.slots.get(1).getItem().getCount(), 4096, "page 4 selects actual slot 49");
        equal(h, menu.slots.get(1).remove(4096).getCount(), 64, "cursor receives a vanilla-sized stack");
        menu.clicked(1, 0, net.minecraft.world.inventory.ClickType.QUICK_MOVE, player);
        equal(h, player.getInventory().countItem(Items.IRON_ORE), 2304, "shift click fills 36 player slots");
        equal(h, be.getMachineHandler().getStackInSlot(be.outputStartSlot() + 48).getCount(), 1728, "remaining oversized output");
        menu.jdte$setFilterPage(Integer.MAX_VALUE);
        equal(h, menu.jdte$getFilterPage(), 0, "untrusted filter page bounded");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void autoOutputWorksOnEveryAbsoluteSide(GameTestHelper h) {
        MineralExtractorBE be = machine(h);
        h.assertTrue(com.jdte.common.autoioconfig.AutoIoConfigHelper.hasConfigurableIo(be), "mineral auto I/O enabled");
        com.jdte.common.autoioconfig.AutoIoConfigHelper.setMasks(be, 0, 63);
        int outputSlot = be.outputStartSlot();
        for (Direction side : Direction.values()) {
            BlockPos target = be.getBlockPos().relative(side);
            h.getLevel().setBlockAndUpdate(target, net.minecraft.world.level.block.Blocks.BARREL.defaultBlockState());
            be.getMachineHandler().setStackInSlot(outputSlot, new ItemStack(Items.IRON_ORE, 8));
            com.jdte.common.autoioconfig.AutoIoTransferHelper.flushEventDrivenGreenhouseOutput(be, new int[]{outputSlot});
            var barrel = (net.minecraft.world.level.block.entity.BarrelBlockEntity) h.getLevel().getBlockEntity(target);
            equal(h, barrel.countItem(Items.IRON_ORE), 8, "auto output " + side);
            equal(h, be.getMachineHandler().getStackInSlot(outputSlot).getCount(), 0, "drained output " + side);
            h.getLevel().removeBlock(target, false);
        }
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void pendingPaidWorkSurvivesSave(GameTestHelper h) {
        MineralExtractorBE be = machine(h);
        for (int i = 0; i < 16; i++) be.getMachineHandler().setStackInSlot(be.outputStartSlot() + i, new ItemStack(Items.COBBLESTONE, 64));
        be.tickServer();
        var saved = be.saveWithFullMetadata();
        h.assertTrue(saved.getLong("pendingBaseWork") == 64, "backpressure retains base work");
        h.setBlock(new BlockPos(1, 1, 1), JDTEBlocks.MINERAL_EXTRACTOR.get());
        MineralExtractorBE restored = (MineralExtractorBE) h.getBlockEntity(new BlockPos(1, 1, 1));
        restored.load(saved);
        restored.getMachineHandler().setStackInSlot(restored.outputStartSlot(), ItemStack.EMPTY);
        restored.tickServer();
        equal(h, restored.getMachineHandler().getStackInSlot(restored.outputStartSlot()).getCount(), 6, "resumed work plus this tick");
        equal(h, restored.getEnergyStorage().getEnergyStored(), 1_970_000, "charge once after output fits");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void resourceRecipeExistsAndRejectsInvalidFluids(GameTestHelper h) {
        var recipe = h.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("jdte", "mineral_extractor_resources"));
        h.assertTrue(recipe.isPresent() && recipe.get() instanceof com.jdte.common.recipes.MineralExtractorResourcesRecipe, "settings recipe parsed");
        var roles = com.jdte.common.recipes.MineralExtractorResourceResolver.resolve(h.getLevel());
        h.assertTrue(roles.matchesAcceleration(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get()), "Forge Time Fluid ID");
        h.assertTrue(roles.matchesFortune(com.direwolf20.justdirethings.setup.Registration.XP_FLUID_SOURCE.get()), "Forge XP Fluid ID");
        boolean rejected = false;
        try { new com.jdte.common.recipes.MineralExtractorResourcesRecipe(ResourceLocation.fromNamespaceAndPath("jdte", "invalid"),
                ResourceLocation.withDefaultNamespace("water"), ResourceLocation.withDefaultNamespace("flowing_water")); }
        catch (IllegalArgumentException expected) { rejected = true; }
        h.assertTrue(rejected, "flowing fluid rejected explicitly");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void surveyNbtAndNetworkRoundTrip(GameTestHelper h) {
        var survey = MineralSurveyData.create(MineralSurveyIndex.version(h.getLevel().getServer()),
                ResourceLocation.withDefaultNamespace("plains"), h.getLevel().dimension().location(),
                List.of(new MineralEntry(ResourceLocation.withDefaultNamespace("iron_ore"), Long.MAX_VALUE, -64, 64, 9, MineralEntry.Confidence.EXACT)));
        var stack = new ItemStack(JDTEItems.MINERAL_SURVEY.get());
        MineralSurveyItem.setSurvey(stack, survey);
        h.assertTrue(survey.equals(MineralSurveyItem.getSurvey(ItemStack.of(stack.save(new net.minecraft.nbt.CompoundTag())))), "survey NBT codec round trip");
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            MineralSurveyData.write(buffer, survey);
            h.assertTrue(survey.equals(MineralSurveyData.read(buffer)), "survey network codec round trip");
        } finally { buffer.release(); }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void blankSurveyRecordsTheActualBiome(GameTestHelper h) {
        var player = h.makeMockSurvivalPlayer();
        var pos = h.absolutePos(POS);
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        var stack = new ItemStack(JDTEItems.MINERAL_SURVEY.get());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        JDTEItems.MINERAL_SURVEY.get().use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        var survey = MineralSurveyItem.getSurvey(stack);
        h.assertTrue(survey != null && !survey.entries().isEmpty(), "right click records minerals");
        h.assertTrue(survey.biomeId().equals(h.getLevel().getBiome(pos).unwrapKey().orElseThrow().location()), "clicked biome ID");
        h.assertTrue(survey.indexVersion() == MineralSurveyIndex.version(h.getLevel().getServer()), "record current index version");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void redstoneAndOreFiltersGateProduction(GameTestHelper h) {
        MineralExtractorBE be = machine(h);
        be.getRedstoneControlData().redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;
        be.tickServer();
        equal(h, output(be), 0, "no signal pauses production");
        equal(h, be.getEnergyStorage().getEnergyStored(), 2_000_000, "redstone pause FE");
        h.setBlock(new BlockPos(1, 1, 1), JDTEBlocks.MINERAL_EXTRACTOR.get());
        MineralExtractorBE filtered = (MineralExtractorBE) h.getBlockEntity(new BlockPos(1, 1, 1));
        survey(h, filtered, 0, MineralSurveyIndex.version(h.getLevel().getServer()));
        filtered.getEnergyStorage().setEnergy(2_000_000);
        filtered.getFilterData().allowlist = false;
        filtered.getFilterHandler().setStackInSlot(0, new ItemStack(Items.IRON_ORE));
        filtered.tickServer();
        equal(h, output(filtered), 0, "blacklisted ore excluded");
        h.assertTrue(filtered.getMachineState() == MineralExtractorBE.State.FILTERED, "filtered state");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void largePartAcceptsFluidBuckets(GameTestHelper h) {
        h.setBlock(POS, JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get());
        var controller = h.absolutePos(POS);
        LargeMineralExtractorStructure.placeParts(h.getLevel(), controller, Direction.NORTH);
        var partPos = LargeMineralExtractorStructure.position(controller, Direction.NORTH, -1, 1, 1);
        var state = h.getLevel().getBlockState(partPos);
        var player = h.makeMockSurvivalPlayer();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get().getBucket()));
        var result = state.getBlock().use(state, h.getLevel(), partPos, player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(partPos), Direction.UP, partPos, false));
        h.assertTrue(result.consumesAction(), "fluid bucket interaction");
        var be = (LargeMineralExtractorBE) h.getLevel().getBlockEntity(controller);
        equal(h, be.getTimeFluidTank().getFluidAmount(), 1000, "bucket fills controller through part");
        h.assertTrue(player.getMainHandItem().is(Items.BUCKET), "empty bucket returned");
        h.succeed();
    }
    @GameTest(template = "mineral_empty")
    public static void ultimateUpgradesHaveRealEffectsAndConflictWithOverclock(GameTestHelper h) {
        MineralExtractorBE be = machine(h);
        var upgrades = UpgradeHelper.getUpgradeHandler(be);
        h.assertTrue(upgrades.insertItem(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false).isEmpty(), "install ultimate capacity");
        equal(h, be.getMaxEnergy(), 100_000_000, "50 times FE capacity");
        equal(h, be.getMaxFluidCapacity(), 3_200_000, "50 times fluid capacity");
        equal(h, be.getOutputSlotLimit(), 6_400_000, "ultimate output stack limit");
        h.assertTrue(upgrades.insertItem(1, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()), false).isEmpty(), "install ultimate overclock");
        equal(h, be.getMaxSelectableMultiplier(), 640, "ultimate selectable multiplier");
        h.assertTrue(!upgrades.isItemValid(2, new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get())), "speed upgrades conflict");
        h.succeed();
    }

}
