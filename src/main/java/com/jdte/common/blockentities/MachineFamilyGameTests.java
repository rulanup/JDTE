package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.datagen.recipes.GooSpreadRecipe;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import com.direwolf20.justdirethings.common.blocks.gooblocks.GooBlock_Base;
import com.direwolf20.justdirethings.datagen.JustDireItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Item;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Behaviour of the JDTE machine families, driven through their own code paths. */
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class MachineFamilyGameTests {

    /**
     * The gel generator converts through JDT's own goo-spread data recipes. The test discovers
     * one at runtime instead of hardcoding an item pair, installs a Creative card so the
     * conversion needs neither fuel nor FE, and then asserts the mapped output item appears.
     */
    @GameTest(template = "mineral_empty")
    public static void advancedGelGeneratorConvertsARealGooSpreadRecipe(GameTestHelper h) {
        assertGelConversion(h, JDTEBlocks.ADVANCED_GEL_GENERATOR.get(), new BlockPos(2, 1, 2));
    }

    /** The extended tier is the same block entity with eight upgrade slots. */
    @GameTest(template = "mineral_empty")
    public static void extendedGelGeneratorConvertsARealGooSpreadRecipe(GameTestHelper h) {
        assertGelConversion(h, JDTEBlocks.EXTENDED_GEL_GENERATOR.get(), new BlockPos(4, 1, 2));
    }

    private static void assertGelConversion(GameTestHelper h, net.minecraft.world.level.block.Block block,
                                            BlockPos relativePos) {
        var recipes = h.getLevel().getRecipeManager()
                .getAllRecipesFor(Registration.GOO_SPREAD_RECIPE_TYPE.get());
        h.assertTrue(!recipes.isEmpty(), "JDT ships at least one goo spread recipe");
        GooSpreadRecipe recipe = recipes.stream()
                .filter(candidate -> candidate.getTierRequirement() <= 1)
                .findFirst().orElse(null);
        h.assertTrue(recipe != null, "at least one goo spread recipe works with tier-1 gel");

        ItemStack expected = GelGeneratorBE.getOutputItemForState(recipe.getOutput());
        h.assertTrue(!expected.isEmpty(), "the chosen recipe maps to a known output item");

        h.setBlock(relativePos, block);
        GelGeneratorBE generator = (GelGeneratorBE) h.getBlockEntity(relativePos);

        boolean creative = false;
        var upgrades = UpgradeHelper.getUpgradeHandler(generator);
        for (int i = 0; i < upgrades.getSlots(); i++) {
            if (upgrades.isItemValid(i, new ItemStack(JDTEItems.CREATIVE_UPGRADE.get()))) {
                upgrades.setStackInSlot(i, new ItemStack(JDTEItems.CREATIVE_UPGRADE.get()));
                creative = true;
                break;
            }
        }
        h.assertTrue(creative, "the generator has a slot that accepts the creative card");

        var handler = generator.getMachineHandler();
        handler.setStackInSlot(GelGeneratorBE.GEL_SLOT,
                new ItemStack(Registration.GooBlock_Tier1_ITEM.get()));
        handler.setStackInSlot(GelGeneratorBE.INPUT_START_SLOT,
                new ItemStack(recipe.getInput().getBlock().asItem()));

        for (int tick = 0; tick < generator.getConversionProgressMax(); tick++) {
            generator.processConversion();
        }

        boolean produced = false;
        for (int slot = GelGeneratorBE.OUTPUT_START_SLOT;
             slot < GelGeneratorBE.OUTPUT_START_SLOT + GelGeneratorBE.OUTPUT_SLOTS; slot++) {
            ItemStack out = handler.getStackInSlot(slot);
            if (!out.isEmpty() && out.getItem() == expected.getItem()) {
                produced = true;
                break;
            }
        }
        h.assertTrue(produced, "the generator converted " + recipe.getInput().getBlock()
                + " into " + expected.getItem());
        h.succeed();
    }

    /** The glue activator revives a dead goo block in its area using a tagged revive item. */
    @GameTest(template = "mineral_empty")
    public static void basicGlueActivatorRevivesADeadGooBlock(GameTestHelper h) {
        assertGlueRevival(h, JDTEBlocks.BASIC_GLUE_ACTIVATOR.get(), new BlockPos(2, 1, 2), new BlockPos(3, 1, 2));
    }

    @GameTest(template = "mineral_empty")
    public static void advancedGlueActivatorRevivesADeadGooBlock(GameTestHelper h) {
        assertGlueRevival(h, JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get(), new BlockPos(2, 1, 2), new BlockPos(3, 1, 2));
    }

    @GameTest(template = "mineral_empty")
    public static void extendedGlueActivatorRevivesADeadGooBlock(GameTestHelper h) {
        assertGlueRevival(h, JDTEBlocks.EXTENDED_GLUE_ACTIVATOR.get(), new BlockPos(2, 1, 2), new BlockPos(3, 1, 2));
    }

    private static void assertGlueRevival(GameTestHelper h, net.minecraft.world.level.block.Block activatorBlock,
                                          BlockPos machinePos, BlockPos gooPos) {
        ItemStack revive = findReviveItem();
        h.assertTrue(!revive.isEmpty(), "JDT ships at least one goo revive item in its revive tags");

        h.setBlock(gooPos, Registration.GooBlock_Tier1.get().defaultBlockState()
                .setValue(GooBlock_Base.ALIVE, false));
        h.setBlock(machinePos, activatorBlock);
        GlueActivatorBE machine = (GlueActivatorBE) h.getBlockEntity(machinePos);
        machine.setAreaSettings(1, 1, 1, 0, 0, 0, false);
        machine.getItemHandler().setStackInSlot(GlueActivatorBE.REVIVE_SLOT, revive);

        h.assertTrue(!h.getBlockState(gooPos).getValue(GooBlock_Base.ALIVE), "the goo starts dead");
        var gooEntity = h.getBlockEntity(gooPos);
        h.assertTrue(gooEntity instanceof com.direwolf20.justdirethings.common.blockentities.basebe.GooBlockBE_Base,
                "the placed goo carries a goo block entity, actual=" + gooEntity);
        h.assertTrue(machine.isValidRevivalItem(revive),
                "the activator accepts " + revive.getItem() + " as a revive item");

        // The powered tiers refuse to revive without FE, so charge them first.
        if (machine instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered) {
            powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
        }

        // Drive the revival itself: the area scan is covered by the tier area tests, and a
        // direct call keeps this assertion about the machine's own action.
        boolean revived = machine.tryReviveGoo(h.getLevel(), h.absolutePos(gooPos), h.getBlockState(gooPos));
        h.assertTrue(revived, "the activator revived the goo block with " + revive.getItem());
        h.assertTrue(h.getBlockState(gooPos).getValue(GooBlock_Base.ALIVE),
                "the activator revived the goo block with " + revive.getItem());
        h.succeed();
    }

    /** The life extractor turns a hostile mob in its area into Life Fluid and removes it. */
    @GameTest(template = "mineral_empty")
    public static void advancedLifeExtractorTurnsAZombieIntoLifeFluid(GameTestHelper h) {
        assertLifeExtraction(h, JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get());
    }

    @GameTest(template = "mineral_empty")
    public static void extendedLifeExtractorTurnsAZombieIntoLifeFluid(GameTestHelper h) {
        assertLifeExtraction(h, JDTEBlocks.EXTENDED_LIFE_EXTRACTOR.get());
    }

    private static void assertLifeExtraction(GameTestHelper h, net.minecraft.world.level.block.Block block) {
        BlockPos machinePos = new BlockPos(2, 1, 2);
        h.setBlock(machinePos, block);
        LifeExtractorBE machine = (LifeExtractorBE) h.getBlockEntity(machinePos);
        machine.setMode(LifeExtractorBE.MODE_HOSTILE);
        if (machine instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered) {
            powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
        }

        var zombie = h.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new BlockPos(3, 2, 2));
        zombie.setNoAi(true);
        h.assertTrue(machine.getFluidTank().getFluidAmount() == 0, "the tank starts empty");

        for (int i = 0; i < machine.getExtractInterval(); i++) {
            machine.extractLife();
        }

        h.assertTrue(zombie.isRemoved(), "the extractor consumed the zombie");
        h.assertTrue(machine.getFluidTank().getFluidAmount() > 0,
                "the extractor produced Life Fluid, amount=" + machine.getFluidTank().getFluidAmount());
        h.succeed();
    }

    /** The bio crusher kills a hostile mob in range through its own crushing path and banks its experience. */
    @GameTest(template = "mineral_empty")
    public static void advancedBioCrusherKillsAZombie(GameTestHelper h) {
        assertBioCrush(h, JDTEBlocks.ADVANCED_BIO_CRUSHER.get());
    }

    @GameTest(template = "mineral_empty")
    public static void extendedBioCrusherKillsAZombie(GameTestHelper h) {
        assertBioCrush(h, JDTEBlocks.EXTENDED_BIO_CRUSHER.get());
    }

    private static void assertBioCrush(GameTestHelper h, net.minecraft.world.level.block.Block block) {
        BlockPos machinePos = new BlockPos(2, 1, 2);
        h.setBlock(machinePos, block);
        BioCrusherBE machine = (BioCrusherBE) h.getBlockEntity(machinePos);
        machine.setMode(BioCrusherBE.MODE_HOSTILE);
        if (machine instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered) {
            powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
        }

        var zombie = h.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new BlockPos(3, 2, 2));
        zombie.setNoAi(true);
        zombie.setHealth(1.0F);
        int startFluid = machine.getFluidTank().getFluidAmount();

        for (int i = 0; i < machine.getExtractInterval(); i++) {
            machine.processCrushing();
        }

        h.assertTrue(zombie.isRemoved() || zombie.isDeadOrDying(), "the crusher killed the zombie");
        h.assertTrue(machine.getFluidTank().getFluidAmount() > startFluid,
                "the crusher banked experience fluid, amount=" + machine.getFluidTank().getFluidAmount());
        h.succeed();
    }

    /** The infusion machine fills a glass bottle from its water tank through its own process path. */
    @GameTest(template = "mineral_empty")
    public static void advancedInfusionMachineFillsAWaterBottle(GameTestHelper h) {
        assertBottleInfusion(h, JDTEBlocks.ADVANCED_INFUSION_MACHINE.get());
    }

    @GameTest(template = "mineral_empty")
    public static void extendedInfusionMachineFillsAWaterBottle(GameTestHelper h) {
        assertBottleInfusion(h, JDTEBlocks.EXTENDED_INFUSION_MACHINE.get());
    }

    private static void assertBottleInfusion(GameTestHelper h, net.minecraft.world.level.block.Block block) {
        BlockPos machinePos = new BlockPos(2, 1, 2);
        h.setBlock(machinePos, block);
        InfusionMachineBE machine = (InfusionMachineBE) h.getBlockEntity(machinePos);
        if (machine instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered) {
            powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
        }
        machine.getFluidTank().fill(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000),
                net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        int startFluid = machine.getFluidTank().getFluidAmount();
        h.assertTrue(startFluid == 1000, "the tank holds 1000 mB water, actual=" + startFluid);
        machine.getMachineHandler().setStackInSlot(InfusionMachineBE.INPUT_SLOT,
                new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE, 2));

        for (int i = 0; i < InfusionMachineBE.PROCESS_TIME; i++) {
            machine.processInfusion();
        }

        ItemStack out = machine.getMachineHandler().getStackInSlot(InfusionMachineBE.OUTPUT_SLOT);
        h.assertTrue(out.is(net.minecraft.world.item.Items.POTION) && out.getCount() == 1,
                "one water bottle was produced, got=" + out);
        h.assertTrue(machine.getMachineHandler().getStackInSlot(InfusionMachineBE.INPUT_SLOT).getCount() == 1,
                "exactly one glass bottle was consumed");
        h.assertTrue(machine.getFluidTank().getFluidAmount() < startFluid,
                "water was drained, left=" + machine.getFluidTank().getFluidAmount());
        h.succeed();
    }

    /** The loot fabricator turns a zombie spawn egg template into real loot, paying fluid and FE. */
    @GameTest(template = "mineral_empty", timeoutTicks = 200)
    public static void lootFabricatorProducesLootFromASpawnEgg(GameTestHelper h) {
        BlockPos pos = new BlockPos(2, 1, 2);
        h.setBlock(pos, JDTEBlocks.LOOT_FABRICATOR.get());
        LootFabricatorBE be = (LootFabricatorBE) h.getBlockEntity(pos);
        be.getRedstoneControlData().redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.IGNORED;
        be.getEnergyStorage().setEnergy(be.getMaxEnergy());
        be.getLifeFluidTank().setFluid(new net.minecraftforge.fluids.FluidStack(com.jdte.setup.JDTEFluids.LIFE_FLUID_SOURCE.get(), 64_000));
        be.getTimeFluidTank().setFluid(new net.minecraftforge.fluids.FluidStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(), 64_000));
        be.getMachineHandler().setStackInSlot(0, new ItemStack(net.minecraft.world.item.Items.ZOMBIE_SPAWN_EGG));
        int startEnergy = be.getEnergyStorage().getEnergyStored();

        boolean produced = false;
        for (int i = 0; i < 4000 && !produced; i++) {
            be.tickServer();
            for (int s = 0; s < be.getActiveOutputSlots(); s++) {
                if (!be.getMachineHandler().getStackInSlot(LootFabricatorBE.INPUT_SLOTS + s).isEmpty()) produced = true;
            }
        }
        h.assertTrue(produced, "the fabricator produced zombie loot");
        h.assertTrue(be.getMachineHandler().getStackInSlot(0).is(net.minecraft.world.item.Items.ZOMBIE_SPAWN_EGG),
                "the spawn egg template is reusable");
        h.assertTrue(be.getLifeFluidTank().getFluidAmount() < 64_000, "Life Fluid was spent");
        h.assertTrue(be.getTimeFluidTank().getFluidAmount() < 64_000, "Time Fluid was spent");
        h.assertTrue(be.getEnergyStorage().getEnergyStored() < startEnergy, "FE was spent");
        h.succeed();
    }

    /** The advanced item collector inserts a collected drop into the inventory it faces. */
    @GameTest(template = "mineral_empty")
    public static void advancedItemCollectorPutsCollectedDropsIntoTheFacingChest(GameTestHelper h) {
        BlockPos pos = new BlockPos(2, 1, 2);
        h.setBlock(pos, JDTEBlocks.ADVANCED_ITEM_COLLECTOR.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, net.minecraft.core.Direction.EAST));
        BlockPos chestPos = pos.east();
        h.setBlock(chestPos, net.minecraft.world.level.block.Blocks.CHEST);
        AdvancedItemCollectorBE be = (AdvancedItemCollectorBE) h.getBlockEntity(pos);
        be.getRedstoneControlData().redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.IGNORED;

        // The collection area is offset from the block by default, so aim at its centre.
        net.minecraft.world.phys.Vec3 at = be.getAABB(be.getBlockPos()).getCenter();
        ItemStack probe = new ItemStack(net.minecraft.world.item.Items.DIAMOND, 5);
        String diag = " canCollect=" + be.canCollect(probe) + " inArea=" + be.getAABB(be.getBlockPos()).contains(at)
                + " redstone=" + be.isActiveRedstone() + " filter=" + be.isStackValidFilter(probe);
        ItemStack left = be.collect(probe.copy(), at);
        h.assertTrue(left.isEmpty(), "the whole stack was collected, left=" + left + diag);

        var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) h.getBlockEntity(chestPos);
        int diamonds = 0;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (chest.getItem(i).is(net.minecraft.world.item.Items.DIAMOND)) diamonds += chest.getItem(i).getCount();
        }
        h.assertTrue(diamonds == 5, "the facing chest received all 5 diamonds, got=" + diamonds);

        ItemStack far = be.collect(new ItemStack(net.minecraft.world.item.Items.DIAMOND, 1), at.add(500, 0, 0));
        h.assertTrue(far.getCount() == 1, "a drop outside the area is not collected");
        h.succeed();
    }

    /** The crystal incubator discovers a budding block in range and harvests its mature cluster into its output. */
    @GameTest(template = "mineral_empty")
    public static void crystalIncubatorHarvestsAMatureAmethystCluster(GameTestHelper h) {
        BlockPos machinePos = new BlockPos(2, 1, 2);
        h.setBlock(machinePos, JDTEBlocks.CRYSTAL_INCUBATOR.get());
        BlockPos buddingPos = new BlockPos(2, 1, 3);
        BlockPos clusterPos = buddingPos.above();
        h.setBlock(buddingPos, net.minecraft.world.level.block.Blocks.BUDDING_AMETHYST);
        h.setBlock(clusterPos, net.minecraft.world.level.block.Blocks.AMETHYST_CLUSTER);
        CrystalIncubatorBE machine = (CrystalIncubatorBE) h.getBlockEntity(machinePos);

        for (int i = 0; i < 200 && h.getBlockState(clusterPos).is(net.minecraft.world.level.block.Blocks.AMETHYST_CLUSTER); i++) {
            machine.handleAccelerationTick();
        }

        h.assertFalse(h.getBlockState(clusterPos).is(net.minecraft.world.level.block.Blocks.AMETHYST_CLUSTER),
                "the mature cluster was harvested");
        int shards = 0;
        for (int slot = 0; slot < machine.getMachineHandler().getSlots(); slot++) {
            ItemStack s = machine.getMachineHandler().getStackInSlot(slot);
            if (s.is(net.minecraft.world.item.Items.AMETHYST_SHARD)) shards += s.getCount();
        }
        h.assertTrue(shards > 0, "amethyst shards landed in the output, got=" + shards);
        h.assertTrue(h.getBlockState(buddingPos).is(net.minecraft.world.level.block.Blocks.BUDDING_AMETHYST),
                "the budding block itself is kept");
        h.succeed();
    }

    /** The entity suppressor suppresses ticks of a hostile mob inside its area and pays FE for it. */
    @GameTest(template = "mineral_empty")
    public static void entitySuppressorSuppressesAHostileMobInItsArea(GameTestHelper h) {
        BlockPos pos = new BlockPos(2, 1, 2);
        h.setBlock(pos, JDTEBlocks.ENTITY_SUPPRESSOR.get());
        EntitySuppressorBE be = (EntitySuppressorBE) h.getBlockEntity(pos);
        be.getRedstoneControlData().redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.IGNORED;
        be.getEnergyStorage().setEnergy(be.getMaxEnergy());
        be.setSettings(EntitySuppressorBE.Mode.SUPPRESS_TICK.ordinal(), EntitySuppressorBE.Target.HOSTILE.ordinal(), false);

        net.minecraft.world.phys.Vec3 at = be.getAABB(be.getBlockPos()).getCenter();
        var zombie = net.minecraft.world.entity.EntityType.ZOMBIE.create(h.getLevel());
        zombie.setPos(at.x, at.y, at.z);
        int before = be.getEnergyStorage().getEnergyStored();
        h.assertTrue(EntitySuppressorManager.shouldSuppressEntityTick(zombie),
                "a zombie inside the area is suppressed");
        h.assertTrue(be.getEnergyStorage().getEnergyStored() < before,
                "suppression paid FE, before=" + before + " after=" + be.getEnergyStorage().getEnergyStored());

        var far = net.minecraft.world.entity.EntityType.ZOMBIE.create(h.getLevel());
        far.setPos(at.x + 500, at.y, at.z);
        h.assertFalse(EntitySuppressorManager.shouldSuppressEntityTick(far), "a zombie outside the area is not suppressed");
        var cow = net.minecraft.world.entity.EntityType.COW.create(h.getLevel());
        cow.setPos(at.x, at.y, at.z);
        h.assertFalse(EntitySuppressorManager.shouldSuppressEntityTick(cow), "a passive cow is not a hostile target");
        h.succeed();
    }

    /** Finds a qualifying revive item from JDT's own tags instead of hardcoding one. */
    private static ItemStack findReviveItem() {
        for (TagKey<Item> tag : java.util.List.of(JustDireItemTags.GOO_REVIVE_TIER_1,
                JustDireItemTags.GOO_REVIVE_TIER_2, JustDireItemTags.GOO_REVIVE_TIER_3,
                JustDireItemTags.GOO_REVIVE_TIER_4)) {
            ItemStack[] items = Ingredient.of(tag).getItems();
            if (items.length > 0) {
                return new ItemStack(items[0].getItem());
            }
        }
        return ItemStack.EMPTY;
    }
}
