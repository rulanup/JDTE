package com.jdte.client.screens;

import com.direwolf20.justdirethings.util.NBTHelpers;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.common.network.JDTEPacketHandler;
import com.jdte.common.network.data.UltimatePortalGunPayload;
import com.jdte.setup.JDTEConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/** Dimension-aware coordinate editor for one Ultimate Portal Gun slot. */
public class UltimatePortalEditMenu extends Screen {
    private final ItemStack portalGun;
    private final int slotSelected;
    private final Screen parent;
    private final List<ResourceKey<Level>> dimensions = new ArrayList<>();
    private int selectedDimension;
    private EditBox nameField;
    private EditBox xPos;
    private EditBox yPos;
    private EditBox zPos;
    private ExtendedButton dimensionButton;
    private final Predicate<String> doubleInputValidator = this::isValidDoubleInput;

    public UltimatePortalEditMenu(ItemStack itemStack, int slot, Screen parent) {
        super(Component.literal(""));
        this.portalGun = itemStack;
        this.slotSelected = slot;
        this.parent = parent;

        Player player = Minecraft.getInstance().player;
        if (player instanceof net.minecraft.client.player.LocalPlayer localPlayer) {
            dimensions.addAll(localPlayer.connection.levels());
        }
        List<? extends String> blacklist = JDTEConfig.COMMON.ultimatePortalGun.teleportDimensionBlacklist.get();
        dimensions.removeIf(key -> blacklist.contains(key.location().toString()));
        if (dimensions.isEmpty()) {
            dimensions.add(Level.OVERWORLD);
        }
        dimensions.sort(Comparator.comparing(key -> key.location().toString()));
        NBTHelpers.PortalDestination destination = getCurrentDestination();
        selectedDimension = Math.max(0, dimensions.indexOf(destination.dimension()));
    }

    @Override
    protected void init() {
        super.init();
        int baseX = width / 2 - 115;
        int baseY = height / 2 - 50;
        nameField = new EditBox(font, baseX, baseY, 230, font.lineHeight + 3, Component.literal(""));
        xPos = new EditBox(font, baseX, baseY + 18, 70, font.lineHeight + 3, Component.literal(""));
        yPos = new EditBox(font, baseX + 80, baseY + 18, 70, font.lineHeight + 3, Component.literal(""));
        zPos = new EditBox(font, baseX + 160, baseY + 18, 70, font.lineHeight + 3, Component.literal(""));
        updateFields();

        nameField.setMaxLength(32);
        xPos.setMaxLength(16);
        yPos.setMaxLength(16);
        zPos.setMaxLength(16);
        xPos.setFilter(doubleInputValidator);
        yPos.setFilter(doubleInputValidator);
        zPos.setFilter(doubleInputValidator);
        addRenderableWidget(nameField);
        addRenderableWidget(xPos);
        addRenderableWidget(yPos);
        addRenderableWidget(zPos);

        dimensionButton = addRenderableWidget(new ExtendedButton(baseX, baseY + 38, 230, 16,
                currentDimensionLabel(), button -> {
                    selectedDimension = (selectedDimension + 1) % Math.max(1, dimensions.size());
                    button.setMessage(currentDimensionLabel());
                }));
        addRenderableWidget(new ExtendedButton(baseX, baseY + 58, 120, 16,
                Component.translatable("justdirethings.screen.save_close"), button -> save()));
        addRenderableWidget(new ExtendedButton(baseX + 130, baseY + 58, 100, 16,
                Component.translatable("justdirethings.screen.cancel"), button -> onClose()));
        setInitialFocus(nameField);
    }

    private NBTHelpers.PortalDestination getCurrentDestination() {
        List<NBTHelpers.PortalDestination> list = UltimatePortalGunItem.getDestinations(portalGun);
        if (slotSelected >= 0 && slotSelected < list.size() && list.get(slotSelected) != null) {
            return list.get(slotSelected);
        }
        Player player = Minecraft.getInstance().player;
        return new NBTHelpers.PortalDestination(player.level().dimension(), player.position(),
                Direction.NORTH, "UNNAMED");
    }

    private void updateFields() {
        NBTHelpers.PortalDestination destination = getCurrentDestination();
        nameField.setValue(destination.name());
        Vec3 coordinates = destination.position();
        xPos.setValue(String.format("%.2f", coordinates.x));
        yPos.setValue(String.format("%.2f", coordinates.y));
        zPos.setValue(String.format("%.2f", coordinates.z));
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
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private void save() {
        try {
            double x = Double.parseDouble(xPos.getValue().trim());
            double y = Double.parseDouble(yPos.getValue().trim());
            double z = Double.parseDouble(zPos.getValue().trim());
            ResourceKey<Level> dimension = dimensions.get(Math.floorMod(selectedDimension, dimensions.size()));
            String name = nameField.getValue().isEmpty() ? "UNNAMED" : nameField.getValue();
            NBTHelpers.PortalDestination current = getCurrentDestination();
            Direction facing = current.facing() == Direction.DOWN ? Direction.NORTH : current.facing();
            NBTHelpers.PortalDestination destination = new NBTHelpers.PortalDestination(
                    dimension, new Vec3(x, y, z), facing, name);
            UltimatePortalGunItem.setDestination(portalGun, slotSelected, destination);
            JDTEPacketHandler.CHANNEL.sendToServer(new UltimatePortalGunPayload(
                    UltimatePortalGunPayload.ACTION_EDIT, slotSelected, name,
                    dimension.location().toString(), x, y, z, false));
            onClose();
        } catch (NumberFormatException ignored) {
            // Leave the editor open when a coordinate is incomplete or invalid.
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int baseX = width / 2 - 115;
        int baseY = height / 2 - 50;
        graphics.drawString(font, Component.translatable("screen.jdte.ultimate_portal_gun.name"),
                baseX, baseY - 11, 0xA0A0A0, false);
        graphics.drawString(font, Component.translatable("screen.jdte.ultimate_portal_gun.coords"),
                baseX, baseY + 7, 0xA0A0A0, false);
    }
}
