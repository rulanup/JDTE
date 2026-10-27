package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.abilityupgrades.Upgrade;
import com.jdte.common.containers.UpgradeStorageContainer;
import com.jdte.common.containers.handlers.UpgradeStorageHandler;
import com.jdte.setup.JDTEItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

/** Twenty upgrade-only slots; source behavior, persisted in the Forge item tag. */
public class UpgradeStorageItem extends Item {
    public static final TagKey<Item> UPGRADES_TAG = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("jdte", "upgrades"));
    public UpgradeStorageItem() { super(new Properties().stacksTo(1)); }
    public static boolean isAllowedUpgrade(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof UpgradeStorageItem) return false;
        return stack.getItem() instanceof UpgradeCardItem || stack.getItem() instanceof LootingUpgradeItem
                || stack.getItem() instanceof SharpnessUpgradeItem || stack.getItem() instanceof ExtendedUpgradeItem
                || stack.getItem() instanceof Upgrade || stack.is(UPGRADES_TAG);
    }
    public static ItemStack findStorage(Player player) {
        if (player == null) return ItemStack.EMPTY;
        if (player.getMainHandItem().is(JDTEItems.UPGRADE_STORAGE.get())) return player.getMainHandItem();
        if (player.getOffhandItem().is(JDTEItems.UPGRADE_STORAGE.get())) return player.getOffhandItem();
        return player.getInventory().items.stream().filter(stack -> stack.is(JDTEItems.UPGRADE_STORAGE.get())).findFirst().orElse(ItemStack.EMPTY);
    }
    protected UpgradeStorageHandler createHandler(ItemStack stack) { return new UpgradeStorageHandler(stack); }
    @Override public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IItemHandler> inventory = LazyOptional.of(() -> createHandler(stack));
            @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
                return capability == ForgeCapabilities.ITEM_HANDLER ? inventory.cast() : LazyOptional.empty();
            }
        };
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (id, inventory, ignored) -> new UpgradeStorageContainer(id, inventory, player, stack, hand),
                    Component.translatable("jdte.screen.upgrade_storage")), buffer -> buffer.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    protected String tooltipPrefix() { return "tooltip.jdte.upgrade_storage"; }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        var inventory = createHandler(stack);
        int filled = 0, total = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack item = inventory.getStackInSlot(i);
            if (!item.isEmpty()) { filled++; total += item.getCount(); }
        }
        if (filled == 0) tooltip.add(Component.translatable(tooltipPrefix() + ".empty").withStyle(ChatFormatting.GRAY));
        else {
            tooltip.add(Component.translatable(tooltipPrefix() + ".count", filled, inventory.getSlots()).withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable(tooltipPrefix() + ".total", total).withStyle(ChatFormatting.DARK_AQUA));
            boolean expanded = DistExecutor.unsafeRunForDist(() -> com.jdte.client.UpgradeStorageClient::shiftDown, () -> () -> false);
            if (expanded) for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (!item.isEmpty()) tooltip.add(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(item.getHoverName().copy().withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(" x" + item.getCount()).withStyle(ChatFormatting.YELLOW)));
            }
            else tooltip.add(Component.translatable("tooltip.jdte.upgrade_storage.shift_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable(tooltipPrefix() + ".open").withStyle(ChatFormatting.GOLD));
    }
}
