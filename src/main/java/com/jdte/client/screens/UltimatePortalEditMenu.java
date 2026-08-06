package com.jdte.client.screens;

import com.direwolf20.justdirethings.util.NBTHelpers;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.common.network.data.UltimatePortalGunPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * 顶级传送枪槽位编辑界面：名称、可编辑坐标、以及注册表内全部维度选择。
 */
public class UltimatePortalEditMenu extends Screen {
    private final ItemStack portalGun;
    private final int slotSelected;
    private final List<ResourceKey<Level>> dimensions = new ArrayList<>();
    private int selectedDimension;
    private EditBox nameField;
    private EditBox xPos;
    private EditBox yPos;
    private EditBox zPos;
    private ExtendedButton dimensionButton;
    private final Predicate<String> doubleInputValidator = this::isValidDoubleInput;

    public UltimatePortalEditMenu(ItemStack itemStack, int slot) {
        super(Component.literal(""));
        this.portalGun = itemStack;
        this.slotSelected = slot;
        Player player = Minecraft.getInstance().player;
        if (player instanceof net.minecraft.client.player.LocalPlayer localPlayer) {
            this.dimensions.addAll(localPlayer.connection.levels());
        }
        // 过滤配置中的传送黑名单维度
        List<? extends String> blacklist = com.jdte.setup.JDTEConfig.COMMON.ultimatePortalGun.teleportDimensionBlacklist.get();
        if (!blacklist.isEmpty()) {
            this.dimensions.removeIf(key -> blacklist.contains(key.location().toString()));
        }
        if (dimensions.isEmpty()) {
            dimensions.add(Level.OVERWORLD);
        }
        this.dimensions.sort(Comparator.comparing(key -> key.location().toString()));
        NBTHelpers.PortalDestination destination = getCurrentDestination();
        int index = Math.max(0, dimensions.indexOf(destination.globalVec3().dimension()));
        this.selectedDimension = index;
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
    }

    @Override
    public void init() {
        super.init();
        int baseX = width / 2 - 115;
        int baseY = height / 2 - 50;
        this.nameField = new EditBox(this.font, baseX, baseY, 230, this.font.lineHeight + 3, Component.literal(""));
        this.xPos = new EditBox(this.font, baseX, baseY + 18, 70, this.font.lineHeight + 3, Component.literal(""));
        this.yPos = new EditBox(this.font, baseX + 80, baseY + 18, 70, this.font.lineHeight + 3, Component.literal(""));
        this.zPos = new EditBox(this.font, baseX + 160, baseY + 18, 70, this.font.lineHeight + 3, Component.literal(""));
        updateFields();

        this.nameField.setMaxLength(32);
        this.xPos.setMaxLength(16);
        this.yPos.setMaxLength(16);
        this.zPos.setMaxLength(16);
        this.xPos.setFilter(doubleInputValidator);
        this.yPos.setFilter(doubleInputValidator);
        this.zPos.setFilter(doubleInputValidator);
        addRenderableWidget(nameField);
        addRenderableWidget(xPos);
        addRenderableWidget(yPos);
        addRenderableWidget(zPos);

        dimensionButton = addRenderableWidget(new ExtendedButton(baseX, baseY + 38, 230, 16, currentDimensionLabel(), button -> {
            selectedDimension = (selectedDimension + 1) % Math.max(1, dimensions.size());
            button.setMessage(currentDimensionLabel());
        }));

        ExtendedButton buttonSave = new ExtendedButton(baseX, baseY + 58, 120, 16,
                Component.translatable("justdirethings.screen.save_close"), (button) -> save());
        addRenderableWidget(buttonSave);

        ExtendedButton buttonCancel = new ExtendedButton(baseX + 130, baseY + 58, 100, 16,
                Component.translatable("justdirethings.screen.cancel"), (button) -> onClose());
        addRenderableWidget(buttonCancel);
    }

    private NBTHelpers.PortalDestination getCurrentDestination() {
        List<NBTHelpers.PortalDestination> list = UltimatePortalGunItem.getDestinations(portalGun);
        if (slotSelected >= 0 && slotSelected < list.size()) {
            return list.get(slotSelected);
        }
        Player player = Minecraft.getInstance().player;
        return new NBTHelpers.PortalDestination(
                new NBTHelpers.GlobalVec3(player.level().dimension(), player.position()),
                net.minecraft.core.Direction.NORTH, "UNNAMED");
    }

    private void updateFields() {
        NBTHelpers.PortalDestination destination = getCurrentDestination();
        this.nameField.setValue(destination.name());
        Vec3 coords = destination.globalVec3().position();
        this.xPos.setValue(String.format("%.2f", coords.x));
        this.yPos.setValue(String.format("%.2f", coords.y));
        this.zPos.setValue(String.format("%.2f", coords.z));
    }

    private Component currentDimensionLabel() {
        if (dimensions.isEmpty()) {
            return Component.translatable("screen.jdte.ultimate_portal_gun.no_dimensions");
        }
        ResourceKey<Level> key = dimensions.get(Math.floorMod(selectedDimension, dimensions.size()));
        return Component.translatable("screen.jdte.ultimate_portal_gun.dimension", key.location().toString());
    }

    private boolean isValidDoubleInput(String input) {
        if (input.isEmpty() || input.equals("-") || input.equals(".") || input.equals("-.")) {
            return true;
        }
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void save() {
        try {
            double x = Double.parseDouble(xPos.getValue().trim());
            double y = Double.parseDouble(yPos.getValue().trim());
            double z = Double.parseDouble(zPos.getValue().trim());
            ResourceKey<Level> key = dimensions.isEmpty()
                    ? Level.OVERWORLD : dimensions.get(Math.floorMod(selectedDimension, dimensions.size()));
            // 客户端本地立即写入并刷新，服务端同步处理
            NBTHelpers.PortalDestination destination = new NBTHelpers.PortalDestination(
                    new NBTHelpers.GlobalVec3(key, new Vec3(x, y, z)),
                    net.minecraft.core.Direction.NORTH,
                    nameField.getValue().isEmpty() ? "UNNAMED" : nameField.getValue());
            UltimatePortalGunItem.setDestination(portalGun, slotSelected, destination);
            PacketDistributor.sendToServer(new UltimatePortalGunPayload(
                    UltimatePortalGunPayload.ACTION_EDIT, slotSelected, nameField.getValue(), key, x, y, z, false));
            this.onClose();
        } catch (NumberFormatException ignored) {
            // 输入非法时忽略
        }
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.nameField);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int baseX = width / 2 - 115;
        int baseY = height / 2 - 50;
        guiGraphics.drawString(font, Component.translatable("screen.jdte.ultimate_portal_gun.name"),
                baseX, baseY - 11, 0xA0A0A0, false);
        guiGraphics.drawString(font, Component.translatable("screen.jdte.ultimate_portal_gun.coords"),
                baseX, baseY + 7, 0xA0A0A0, false);
    }
}
