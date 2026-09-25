package com.jdte.client;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.JDTE;
import com.jdte.common.containers.DynamicFilterSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;

/**
 * 隐藏彩蛋：在聊天框输入恰好 {@code 2i} 两个字符时切换（消息被拦截，不会发送到服务器）。
 * 开启后所有 JDTE 机器 GUI 的机器槽位背景替换为 {@code easter_egg_2i_slot.png}，再次输入恢复。
 * 纯客户端视觉开关：只覆盖 JDTE 机器方块实体的菜单，玩家背包格与未激活的过滤槽保持原样。
 */
@EventBusSubscriber(modid = JDTE.MODID, value = Dist.CLIENT)
public final class SlotTextureEasterEgg {
    private static final String TRIGGER = "2i";
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/easter_egg_2i_slot.png");
    private static final String JDTE_BE_PACKAGE = "com.jdte.";
    private static final int SLOT_SIZE = 18;

    private static boolean active;

    private SlotTextureEasterEgg() {
    }

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        if (!TRIGGER.equals(event.getMessage())) {
            return;
        }
        event.setCanceled(true);
        active = !active;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.translatable(active ? "jdte.easteregg.on" : "jdte.easteregg.off")
                            .withStyle(ChatFormatting.LIGHT_PURPLE),
                    false);
        }
    }

    @SubscribeEvent
    public static void onRenderContainerBackground(ContainerScreenEvent.Render.Background event) {
        if (!active) {
            return;
        }
        AbstractContainerScreen<?> screen = event.getContainerScreen();
        if (!(screen.getMenu() instanceof BaseMachineContainer container)
                || container.baseMachineBE == null
                || !container.baseMachineBE.getClass().getName().startsWith(JDTE_BE_PACKAGE)) {
            return;
        }
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();
        for (Slot slot : container.slots) {
            if (slot.container instanceof Inventory) {
                continue;
            }
            if (slot instanceof DynamicFilterSlot filterSlot && !filterSlot.isActive()) {
                continue;
            }
            guiGraphics.blit(TEXTURE, left + slot.x - 1, top + slot.y - 1, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        }
    }
}
