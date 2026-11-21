package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Behaviour of the standalone JDTE items that are not machines. */
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class ItemBehaviourGameTests {
    /**
     * The big canister is a FluidCanister subclass whose capability carries the JDTE capacity, so the
     * ordinary item handler must report 1,000,000 mB and accept fluid through the normal capability.
     */
    @GameTest(template = "mineral_empty")
    public static void bigFluidTankExposesItsFullCapacityThroughTheItemCapability(GameTestHelper h) {
        ItemStack stack = new ItemStack(JDTEItems.BIG_FLUID_TANK.get());
        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .orElseThrow(() -> new AssertionError("the big fluid tank exposes no item fluid handler"));

        h.assertTrue(handler.getTanks() >= 1, "the canister reports at least one tank, tanks=" + handler.getTanks());
        h.assertTrue(handler.getTankCapacity(0) == 1_000_000,
                "the canister reports its JDTE capacity, capacity=" + handler.getTankCapacity(0));

        int filled = handler.fill(new FluidStack(Fluids.WATER, 4_000), IFluidHandler.FluidAction.EXECUTE);
        h.assertTrue(filled == 4_000, "the canister accepted the full request, filled=" + filled);
        h.assertTrue(handler.getFluidInTank(0).getAmount() == 4_000,
                "the canister reports the stored fluid, stored=" + handler.getFluidInTank(0).getAmount());
        h.succeed();
    }

    /**
     * The freezer freezes the dimension clock, not block ticks: activate it with time
     * freeze enabled and the manager must snap the day time back to the captured value.
     */
    @GameTest(template = "mineral_empty")
    public static void timeFreezerPinsTheDayTimeOfItsDimension(GameTestHelper h) {
        h.setBlock(net.minecraft.core.BlockPos.ZERO.offset(2, 1, 2), JDTEBlocks.TIME_FREEZER.get());
        var pos = new net.minecraft.core.BlockPos(2, 1, 2);
        TimeFreezerBE freezer = (TimeFreezerBE) h.getBlockEntity(pos);
        freezer.setTimeFreezeEnabled(true);
        ServerLevel level = h.getLevel();

        TimeFreezerManager.activate(freezer, level);
        long frozen;
        try {
            frozen = level.getDayTime();
            level.setDayTime(frozen + 6_000L);
            TimeFreezerManager.refreshActive(freezer, level);
            h.assertTrue(level.getDayTime() == frozen,
                    "the freezer pinned the day time, expected=" + frozen + " actual=" + level.getDayTime());
        } finally {
            // Never leave the dimension clock pinned for the tests that run after this one.
            TimeFreezerManager.deactivate(freezer);
        }

        h.succeed();
    }

    /** Eating a life apple applies its permanent max-health modifier to the eater. */
    @GameTest(template = "mineral_empty")
    public static void lifeAppleRaisesThePermanentMaxHealthOfItsEater(GameTestHelper h) {
        var player = h.makeMockSurvivalPlayer();
        double before = player.getAttributeValue(Attributes.MAX_HEALTH);
        ItemStack apple = new ItemStack(JDTEItems.LIFE_APPLE.get());
        apple.finishUsingItem(h.getLevel(), player);
        double after = player.getAttributeValue(Attributes.MAX_HEALTH);
        h.assertTrue(after > before, "max health rose, before=" + before + " after=" + after);
        h.succeed();
    }




    /**
     * JDTE contributes only the registration for these two eggs, so the test covers that
     * contribution end to end: the item is a spawn egg carrying the expected EntityType, and
     * that type really is summonable in a live world. The vanilla right-click glue is not
     * exercised here because a GameTest mock-player context returns PASS before it runs.
     */
    @GameTest(template = "mineral_empty")
    public static void bossSpawnEggsCarrySummonableTypes(GameTestHelper h) {
        assertSummonableEgg(h, JDTEItems.WITHER_SPAWN_EGG.get(), EntityType.WITHER,
                net.minecraft.world.entity.boss.wither.WitherBoss.class, new BlockPos(2, 2, 2));
        assertSummonableEgg(h, JDTEItems.ENDER_DRAGON_SPAWN_EGG.get(), EntityType.ENDER_DRAGON,
                net.minecraft.world.entity.boss.enderdragon.EnderDragon.class, new BlockPos(5, 2, 5));
        h.succeed();
    }

    private static <T extends Entity> void assertSummonableEgg(GameTestHelper h, net.minecraft.world.item.Item item,
                                                              EntityType<?> expected, Class<T> type, BlockPos relative) {
        h.assertTrue(item instanceof ForgeSpawnEggItem,
                "the registered item is a ForgeSpawnEggItem, actual=" + item.getClass().getName());
        EntityType<?> carried = ((ForgeSpawnEggItem) item).getType(null);
        h.assertTrue(carried == expected, "the egg carries " + expected + ", actual=" + carried);

        h.setBlock(relative.below(), Blocks.STONE);
        Entity spawned = expected.spawn((ServerLevel) h.getLevel(), h.absolutePos(relative), MobSpawnType.COMMAND);
        h.assertTrue(spawned != null, "the carried type spawns in a live world: " + expected);
        var found = h.getLevel().getEntitiesOfClass(type, new net.minecraft.world.phys.AABB(h.absolutePos(relative)).inflate(8));
        h.assertTrue(!found.isEmpty(), "the spawned " + expected + " is present in the level, found=" + found.size());
        found.forEach(Entity::discard);
    }
}
