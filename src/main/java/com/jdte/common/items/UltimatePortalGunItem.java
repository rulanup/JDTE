package com.jdte.common.items;

import com.direwolf20.justdirethings.common.entities.PortalProjectile;
import com.direwolf20.justdirethings.common.items.PortalGunV2;
import com.direwolf20.justdirethings.common.items.interfaces.FluidContainingItem;
import com.direwolf20.justdirethings.common.items.interfaces.PoweredItem;
import com.direwolf20.justdirethings.util.NBTHelpers;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 顶级传送枪：高级传送枪（PortalGunV2）的增强版。
 *
 * <ul>
 *     <li>发射机制、能量与传送流体行为完全复用 JDT 高级传送枪。</li>
 *     <li>单个中大型传送流体储罐（1000 B = 1,000,000 mB）。</li>
 *     <li>传送槽位无上限，按 V 打开分页轮盘管理。</li>
 *     <li>编辑槽位可自由选择注册表内任意维度并手动输入坐标；手动坐标槽传送固定消耗 10 B 传送流体。</li>
 * </ul>
 */
public class UltimatePortalGunItem extends PortalGunV2 {
    /** 1000 B = 1,000,000 mB 传送流体。 */
    public static final int MAX_MB = 1_000_000;
    public static final int SEGMENTS_PER_PAGE = 12;
    /** 手动坐标槽跨维度传送固定消耗 1000 B = 1,000,000 mB。 */
    public static final int MANUAL_CROSS_DIMENSION_COST = 1_000_000;
    /** 手动坐标槽同维度每米 1 B = 1000 mB。 */
    public static final int MANUAL_PER_BLOCK_COST = 1000;
    /** 手动坐标槽同维度上限 500 B = 500,000 mB。 */
    public static final int MANUAL_MAX_DISTANCE_COST = 500_000;

    public UltimatePortalGunItem() {
        super();
    }

    public static ItemStack find(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof UltimatePortalGunItem) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof UltimatePortalGunItem) {
            return off;
        }
        return ItemStack.EMPTY;
    }

    // ============================================================
    // 槽位数据（每页固定 12 槽，空槽 EMPTY 占位）
    // ============================================================

    public static List<NBTHelpers.PortalDestination> getDestinations(ItemStack stack) {
        List<NBTHelpers.PortalDestination> list = new ArrayList<>(PortalGunV2.getFavorites(stack));
        if (list.isEmpty()) {
            ensurePage(stack);
            return new ArrayList<>(PortalGunV2.getFavorites(stack));
        }
        return list;
    }

    /** 槽位列表恒为 12 的倍数（空槽用 EMPTY 占位），每页固定 12 槽。 */
    private static void normalizeList(ItemStack stack) {
        List<NBTHelpers.PortalDestination> list = new ArrayList<>(PortalGunV2.getFavorites(stack));
        while (list.size() % SEGMENTS_PER_PAGE != 0) {
            list.add(NBTHelpers.PortalDestination.EMPTY);
        }
        PortalGunV2.setFavorites(stack, list);
        alignManualFlags(stack, list.size());
    }

    /** 追加一页（12 个空槽）。 */
    public static void ensurePage(ItemStack stack) {
        normalizeList(stack);
        List<NBTHelpers.PortalDestination> list = new ArrayList<>(PortalGunV2.getFavorites(stack));
        for (int i = 0; i < SEGMENTS_PER_PAGE; i++) {
            list.add(NBTHelpers.PortalDestination.EMPTY);
        }
        PortalGunV2.setFavorites(stack, list);
        alignManualFlags(stack, list.size());
    }

    /** 若最后一页全部为空，删除该页。 */
    public static void trimEmptyLastPage(ItemStack stack) {
        List<NBTHelpers.PortalDestination> list = new ArrayList<>(PortalGunV2.getFavorites(stack));
        if (list.size() <= SEGMENTS_PER_PAGE) {
            return;
        }
        int start = list.size() - SEGMENTS_PER_PAGE;
        boolean allEmpty = true;
        for (int i = start; i < list.size(); i++) {
            NBTHelpers.PortalDestination destination = list.get(i);
            if (destination == null || !destination.equals(NBTHelpers.PortalDestination.EMPTY)) {
                allEmpty = false;
                break;
            }
        }
        if (allEmpty) {
            for (int i = 0; i < SEGMENTS_PER_PAGE; i++) {
                list.remove(list.size() - 1);
            }
            PortalGunV2.setFavorites(stack, list);
        }
        int position = PortalGunV2.getFavoritePosition(stack);
        if (!list.isEmpty() && position >= list.size()) {
            PortalGunV2.setFavoritePosition(stack, list.size() - 1);
        }
        alignManualFlags(stack, list.size());
    }

    /** 填充指定槽位（当前位置快捷添加，非手动坐标）。 */
    public static void fillDestination(ItemStack stack, int position, NBTHelpers.PortalDestination destination) {
        normalizeList(stack);
        List<NBTHelpers.PortalDestination> list = new ArrayList<>(PortalGunV2.getFavorites(stack));
        if (position >= list.size()) {
            ensurePage(stack);
            list = new ArrayList<>(PortalGunV2.getFavorites(stack));
        }
        list.set(Math.max(0, position), destination);
        PortalGunV2.setFavorites(stack, list);
        setManualFlag(stack, Math.max(0, position), false);
    }

    /** 覆写指定槽位（手动坐标编辑，标记为手动槽）。 */
    public static void setDestination(ItemStack stack, int index, NBTHelpers.PortalDestination destination) {
        List<NBTHelpers.PortalDestination> list = new ArrayList<>(PortalGunV2.getFavorites(stack));
        if (index >= 0 && index < list.size()) {
            list.set(index, destination);
            PortalGunV2.setFavorites(stack, list);
            setManualFlag(stack, index, true);
        }
    }

    /** 清空指定槽位（置为 EMPTY，保持分页结构）。 */
    public static void clearDestination(ItemStack stack, int index) {
        List<NBTHelpers.PortalDestination> list = new ArrayList<>(PortalGunV2.getFavorites(stack));
        if (index >= 0 && index < list.size()) {
            list.set(index, NBTHelpers.PortalDestination.EMPTY);
            PortalGunV2.setFavorites(stack, list);
            setManualFlag(stack, index, false);
        }
    }

    /** 当前选中槽位（越界安全）。 */
    public static NBTHelpers.PortalDestination getSelectedDestination(ItemStack stack) {
        List<NBTHelpers.PortalDestination> list = getDestinations(stack);
        int position = PortalGunV2.getFavoritePosition(stack);
        if (position < 0 || position >= list.size()) {
            return null;
        }
        return list.get(position);
    }

    // ============================================================
    // 手动坐标标记
    // ============================================================

    public static boolean isManualSlot(ItemStack stack, int index) {
        List<Boolean> flags = stack.getOrDefault(JDTEDataComponents.ULTIMATE_PORTAL_GUN_MANUAL_SLOTS.get(), List.of());
        return index >= 0 && index < flags.size() && Boolean.TRUE.equals(flags.get(index));
    }

    private static void setManualFlag(ItemStack stack, int index, boolean manual) {
        List<Boolean> flags = new ArrayList<>(stack.getOrDefault(
                JDTEDataComponents.ULTIMATE_PORTAL_GUN_MANUAL_SLOTS.get(), List.of()));
        while (flags.size() <= index) {
            flags.add(false);
        }
        flags.set(index, manual);
        stack.set(JDTEDataComponents.ULTIMATE_PORTAL_GUN_MANUAL_SLOTS.get(), flags);
    }

    private static void alignManualFlags(ItemStack stack, int size) {
        List<Boolean> flags = new ArrayList<>(stack.getOrDefault(
                JDTEDataComponents.ULTIMATE_PORTAL_GUN_MANUAL_SLOTS.get(), List.of()));
        while (flags.size() < size) {
            flags.add(false);
        }
        if (flags.size() > size) {
            flags = new ArrayList<>(flags.subList(0, size));
        }
        stack.set(JDTEDataComponents.ULTIMATE_PORTAL_GUN_MANUAL_SLOTS.get(), flags);
    }

    // ============================================================
    // 发射与消耗
    // ============================================================

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (blockhitresult.getType() == HitResult.Type.BLOCK) {
            if (pickupFluid(level, player, itemStack, blockhitresult)) {
                return InteractionResultHolder.fail(itemStack);
            }
        }
        if (!level.isClientSide) {
            spawnProjectile(level, player, itemStack, true);
        }
        return InteractionResultHolder.fail(itemStack);
    }

    /** 手动坐标槽固定消耗 10 B；直接添加的槽沿用 JDT 的距离/跨维规则。 */
    public static void spawnProjectile(Level level, Player player, ItemStack itemStack, boolean isPrimaryType) {
        NBTHelpers.PortalDestination portalDestination = player.isShiftKeyDown()
                ? PortalGunV2.getPrevious(itemStack) : getSelectedDestination(itemStack);
        if (portalDestination == null || portalDestination.equals(NBTHelpers.PortalDestination.EMPTY)) {
            return;
        }
        int cost = calculateActualFluidCost((ServerLevel) level, player, itemStack, portalDestination);
        if (!FluidContainingItem.hasEnoughFluid(itemStack, cost)) {
            player.displayClientMessage(Component.translatable("justdirethings.lowportalfluid"), true);
            player.playNotifySound(SoundEvents.VAULT_INSERT_ITEM_FAIL, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
        if (!PoweredItem.consumeEnergy(itemStack, com.direwolf20.justdirethings.setup.Config.PORTAL_GUN_V2_RF_COST.get())) {
            player.displayClientMessage(Component.translatable("justdirethings.lowenergy"), true);
            player.playNotifySound(SoundEvents.VAULT_INSERT_ITEM_FAIL, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
        PortalProjectile projectile = new PortalProjectile(level, player, PortalGunV2.getUUID(itemStack), isPrimaryType, true, portalDestination);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1F, 1.0F);
        level.addFreshEntity(projectile);
        FluidContainingItem.consumeFluid(itemStack, cost);
        PortalGunV2.setPrevious(player, itemStack);
    }

    private static int calculateActualFluidCost(ServerLevel sourceLevel, Player player, ItemStack stack,
                                                NBTHelpers.PortalDestination destination) {
        int position = PortalGunV2.getFavoritePosition(stack);
        if (!isManualSlot(stack, position)) {
            return calculateFluidCost(sourceLevel, player, destination);
        }
        // 手动坐标槽：跨维度 1000 B；同维度 1 B/米，上限 500 B
        if (!destination.globalVec3().dimension().equals(sourceLevel.dimension())) {
            return MANUAL_CROSS_DIMENSION_COST;
        }
        double distance = destination.globalVec3().position().distanceTo(player.position());
        return Math.min((int) Math.ceil(distance * MANUAL_PER_BLOCK_COST), MANUAL_MAX_DISTANCE_COST);
    }

    // ============================================================
    // 显示
    // ============================================================

    /** 0 = 空，1 = ≤1/3，2 = ≤2/3，3 = 满；驱动物品模型的大储罐变体。 */
    public static int getFullness(ItemStack stack) {
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null && !fluidHandler.getFluidInTank(0).isEmpty()) {
            float percentFull = ((float) fluidHandler.getFluidInTank(0).getAmount() / MAX_MB) * 100;
            if (percentFull > 0 && percentFull <= 33) {
                return 1;
            } else if (percentFull > 33 && percentFull <= 66) {
                return 2;
            } else if (percentFull > 66) {
                return 3;
            }
        }
        return 0;
    }

    public static int getEnergyCapacity() {
        return JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunEnergyCapacity.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 不调用 JDT 的 super（其流体行使用 8000 mB 容量），自行显示完整信息
        com.direwolf20.justdirethings.util.TooltipHelpers.appendFEText(stack, tooltip);
        Level level = context.level();
        if (level == null) {
            return;
        }
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null) {
            tooltip.add(Component.translatable("tooltip.jdte.ultimate_portal_gun.fluid",
                    fluidHandler.getFluidInTank(0).getAmount(), MAX_MB).withStyle(ChatFormatting.GREEN));
        }
        int count = getDestinations(stack).size();
        tooltip.add(Component.translatable("tooltip.jdte.ultimate_portal_gun.slots", count).withStyle(ChatFormatting.AQUA));
    }
}
