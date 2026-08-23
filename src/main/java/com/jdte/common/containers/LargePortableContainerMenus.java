package com.jdte.common.containers;

import com.jdte.common.items.LargeFuelCanisterItem;
import com.jdte.common.items.LargePocketGeneratorItem;
import com.jdte.common.items.LargePotionCanisterItem;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class LargePortableContainerMenus {
    public static final String LARGE_POCKET_GENERATOR_SLOT = "large_pocket_generator";
    public static final String LARGE_POTION_CANISTER_SLOT = "large_potion_canister";
    public static final String LARGE_FUEL_CANISTER_SLOT = "large_fuel_canister";

    private LargePortableContainerMenus() {
    }

    public static void openFromMainHand(Player player, OpenLargePortableContainerPayload.ContainerKind kind) {
        Optional<ItemStack> resolved = resolveHandheldStack(
                kind,
                InteractionHand.MAIN_HAND,
                player.getMainHandItem(),
                player.getOffhandItem()
        );
        if (resolved.isEmpty()) {
            return;
        }
        Predicate<ItemStack> validator = validator(kind);
        ItemStack stack = resolved.get();
        LargePortableContainerBinding binding = new LargePortableContainerBinding(
                stack,
                player::getMainHandItem,
                validator
        );
        openBoundMenu(player, kind, stack, binding, LargePortableContainerSource.mainHand());
    }

    public static boolean openFromCurios(ServerPlayer player, OpenLargePortableContainerPayload.ContainerKind kind) {
        Optional<ItemStack> resolved = resolveCuriosStack(
                kind,
                ModList.get().isLoaded("curios"),
                slotId -> resolveCuriosStack(player, slotId)
        );
        if (resolved.isEmpty()) {
            return false;
        }
        ItemStack stack = resolved.get();
        String slotId = curiosSlot(kind);
        LargePortableContainerBinding binding = new LargePortableContainerBinding(
                stack,
                () -> resolveCuriosStack(player, slotId).orElse(ItemStack.EMPTY),
                validator(kind)
        );
        openBoundMenu(player, kind, stack, binding, LargePortableContainerSource.curios(slotId));
        return true;
    }

    private static Optional<ItemStack> resolveCuriosStack(ServerPlayer player, String slotId) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.getStacksHandler(slotId))
                .filter(handler -> handler.getSlots() > 0)
                .map(handler -> handler.getStacks().getStackInSlot(0));
    }

    static Optional<ItemStack> resolveHandheldStack(OpenLargePortableContainerPayload.ContainerKind kind,
                                                    InteractionHand triggeringHand,
                                                    ItemStack mainHand,
                                                    ItemStack offHand) {
        return Optional.of(mainHand).filter(validator(kind));
    }

    static Optional<ItemStack> resolveCuriosStack(OpenLargePortableContainerPayload.ContainerKind kind,
                                                  boolean curiosAvailable,
                                                  Function<String, Optional<ItemStack>> slotLookup) {
        if (!curiosAvailable) {
            return Optional.empty();
        }
        return slotLookup.apply(curiosSlot(kind)).filter(validator(kind));
    }

    private static void openBoundMenu(Player player, OpenLargePortableContainerPayload.ContainerKind kind,
                                      ItemStack stack, LargePortableContainerBinding binding,
                                      LargePortableContainerSource source) {
        player.openMenu(menuProvider(kind, stack, binding), buf -> encodeOpenData(buf, stack, source));
    }

    static void encodeOpenData(RegistryFriendlyByteBuf buf, ItemStack stack, LargePortableContainerSource source) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
        source.encode(buf);
    }

    static DecodedOpenData decodeOpenData(RegistryFriendlyByteBuf buf) {
        return new DecodedOpenData(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), LargePortableContainerSource.decode(buf));
    }

    private static MenuProvider menuProvider(OpenLargePortableContainerPayload.ContainerKind kind,
                                             ItemStack stack,
                                             LargePortableContainerBinding binding) {
        return new SimpleMenuProvider(
                (windowId, inventory, player) -> createMenu(kind, windowId, inventory, player, stack, binding),
                titleFor(kind)
        );
    }

    private static AbstractContainerMenu createMenu(OpenLargePortableContainerPayload.ContainerKind kind,
                                                    int windowId,
                                                    net.minecraft.world.entity.player.Inventory inventory,
                                                    Player player,
                                                    ItemStack stack,
                                                    LargePortableContainerBinding binding) {
        return switch (kind) {
            case LARGE_POCKET_GENERATOR -> new LargePocketGeneratorContainer(windowId, inventory, player, stack, binding);
            case LARGE_POTION_CANISTER -> new LargePotionCanisterContainer(windowId, inventory, player, stack, binding);
            case LARGE_FUEL_CANISTER -> new LargeFuelCanisterContainer(windowId, inventory, player, stack, binding);
        };
    }

    private static Component titleFor(OpenLargePortableContainerPayload.ContainerKind kind) {
        return switch (kind) {
            case LARGE_POCKET_GENERATOR -> Component.translatable("item.jdte.large_pocket_generator")
                    .withStyle(ChatFormatting.WHITE);
            case LARGE_POTION_CANISTER -> Component.translatable("item.jdte.large_potion_canister")
                    .withStyle(ChatFormatting.WHITE);
            case LARGE_FUEL_CANISTER -> Component.translatable("item.jdte.large_fuel_canister")
                    .withStyle(ChatFormatting.WHITE);
        };
    }

    private static Predicate<ItemStack> validator(OpenLargePortableContainerPayload.ContainerKind kind) {
        return switch (kind) {
            case LARGE_POCKET_GENERATOR -> stack -> !stack.isEmpty() && stack.getItem() instanceof LargePocketGeneratorItem;
            case LARGE_POTION_CANISTER -> stack -> !stack.isEmpty() && stack.getItem() instanceof LargePotionCanisterItem;
            case LARGE_FUEL_CANISTER -> stack -> !stack.isEmpty() && stack.getItem() instanceof LargeFuelCanisterItem;
        };
    }

    private static String curiosSlot(OpenLargePortableContainerPayload.ContainerKind kind) {
        return switch (kind) {
            case LARGE_POCKET_GENERATOR -> LARGE_POCKET_GENERATOR_SLOT;
            case LARGE_POTION_CANISTER -> LARGE_POTION_CANISTER_SLOT;
            case LARGE_FUEL_CANISTER -> LARGE_FUEL_CANISTER_SLOT;
        };
    }

    static LargePortableContainerBinding createClientBinding(Player player,
                                                             ItemStack decodedStack,
                                                             OpenLargePortableContainerPayload.ContainerKind kind,
                                                             LargePortableContainerSource source) {
        return new LargePortableContainerBinding(
                decodedStack,
                () -> resolveClientSourceStack(
                        source,
                        player.getMainHandItem(),
                        ModList.get().isLoaded("curios"),
                        slotId -> CuriosApi.getCuriosInventory(player)
                                .flatMap(handler -> handler.getStacksHandler(slotId))
                                .filter(handler -> handler.getSlots() > 0)
                                .map(handler -> handler.getStacks().getStackInSlot(0))
                ).orElse(decodedStack),
                validator(kind)
        );
    }

    static Optional<ItemStack> resolveClientSourceStack(LargePortableContainerSource source,
                                                        ItemStack mainHand,
                                                        boolean curiosAvailable,
                                                        Function<String, Optional<ItemStack>> slotLookup) {
        if (source.sourceType() == LargePortableContainerSource.SourceType.MAIN_HAND) {
            return Optional.of(mainHand);
        }
        if (!curiosAvailable) {
            return Optional.empty();
        }
        return slotLookup.apply(source.curiosSlotId());
    }

    record DecodedOpenData(ItemStack stack, LargePortableContainerSource source) {
    }
}
