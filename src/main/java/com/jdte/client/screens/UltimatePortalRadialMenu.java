package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.KeyBindings;
import com.direwolf20.justdirethings.client.OurSounds;
import com.direwolf20.justdirethings.client.renderers.OurRenderTypes;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.BaseButton;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.direwolf20.justdirethings.common.items.PortalGunV2;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.NBTHelpers;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.common.network.JDTEPacketHandler;
import com.jdte.common.network.data.UltimatePortalGunPayload;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** The 12-slot-per-page radial menu used by the upstream 0.5.8 release. */
public class UltimatePortalRadialMenu extends Screen {
    private static final int SEGMENTS_PER_PAGE = 12;
    private static final int RADIUS_MIN = 26;
    private static final int RADIUS_MAX = 120;

    private final ItemStack portalGun;
    private final List<NBTHelpers.PortalDestination> destinations;
    private int timeIn;
    private int slotHovered = -1;
    private int slotSelected;
    private boolean staysOpen;

    public UltimatePortalRadialMenu(ItemStack stack) {
        super(Component.literal(""));
        portalGun = stack;
        destinations = new ArrayList<>(UltimatePortalGunItem.getDestinations(stack));
        slotSelected = Math.max(0, Math.min(destinations.size() - 1,
                UltimatePortalGunItem.getFavoritePosition(stack)));
        staysOpen = PortalGunV2.getStayOpen(stack);
    }

    private int currentPage() {
        return destinations.isEmpty() ? 0 : slotSelected / SEGMENTS_PER_PAGE;
    }

    private int pageCount() {
        return Math.max(1, (destinations.size() + SEGMENTS_PER_PAGE - 1) / SEGMENTS_PER_PAGE);
    }

    private NBTHelpers.PortalDestination getDestination(int slot) {
        if (slot < 0 || slot >= destinations.size()) {
            return null;
        }
        return destinations.get(slot);
    }

    private static float mouseAngle(int x, int y, int mouseX, int mouseY) {
        float dx = mouseX - x;
        float dy = mouseY - y;
        if (dx == 0 && dy == 0) {
            return 0;
        }
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        return angle < 0 ? angle + 360 : angle;
    }

    @Override
    protected void init() {
        GrayscaleButton addButton = new GrayscaleButton(width / 2 - 150, height / 2 - 20, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/add.png"),
                        Component.translatable("justdirethings.screen.add_favorite")).texture(),
                Component.translatable("justdirethings.screen.add_favorite"), true,
                clicked -> addFavorite());
        addRenderableWidget(addButton);

        GrayscaleButton removeButton = new GrayscaleButton(width / 2 + 140, height / 2 - 20, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/remove.png"),
                        Component.translatable("justdirethings.screen.remove_favorite")).texture(),
                Component.translatable("justdirethings.screen.remove_favorite"), true,
                clicked -> removeFavorite());
        addRenderableWidget(removeButton);

        GrayscaleButton editButton = new GrayscaleButton(width / 2 - 150, height / 2 + 20, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/matchnbttrue.png"),
                        Component.translatable("justdirethings.screen.edit_favorite")).texture(),
                Component.translatable("justdirethings.screen.edit_favorite"), true,
                clicked -> editFavorite());
        addRenderableWidget(editButton);

        GrayscaleButton stayOpenButton = new GrayscaleButton(width / 2 + 140, height / 2 + 20, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/area.png"),
                        Component.translatable("justdirethings.screen.stay_open")).texture(),
                Component.translatable("justdirethings.screen.stay_open"), staysOpen,
                clicked -> {
                    staysOpen = !staysOpen;
                    PortalGunV2.setStayOpen(portalGun, staysOpen);
                    sendSelect();
                    ((GrayscaleButton) clicked).toggleActive();
                });
        addRenderableWidget(stayOpenButton);

        GrayscaleButton prevButton = new GrayscaleButton(width / 2 - 150, height / 2 - 56, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/mobscanner.png"),
                        Component.translatable("screen.jdte.ultimate_portal_gun.prev_page")).texture(),
                Component.translatable("screen.jdte.ultimate_portal_gun.prev_page"), true,
                clicked -> changePage(-1));
        addRenderableWidget(prevButton);

        GrayscaleButton nextButton = new GrayscaleButton(width / 2 + 140, height / 2 - 56, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/mobscanner.png"),
                        Component.translatable("screen.jdte.ultimate_portal_gun.next_page")).texture(),
                Component.translatable("screen.jdte.ultimate_portal_gun.next_page"), true,
                clicked -> changePage(1));
        addRenderableWidget(nextButton);
    }

    private void changePage(int delta) {
        if (delta > 0 && currentPage() >= pageCount() - 1) {
            UltimatePortalGunItem.ensurePage(portalGun);
            refreshDestinations();
        } else if (delta < 0) {
            UltimatePortalGunItem.trimEmptyLastPage(portalGun);
            refreshDestinations();
        }
        slotSelected = delta > 0
                ? Math.min((currentPage() + 1) * SEGMENTS_PER_PAGE, destinations.size() - 1)
                : Math.max(0, (currentPage() - 1) * SEGMENTS_PER_PAGE);
        sendSelect();
    }

    private void refreshDestinations() {
        destinations.clear();
        destinations.addAll(UltimatePortalGunItem.getDestinations(portalGun));
        if (!destinations.isEmpty()) {
            slotSelected = Math.min(slotSelected, destinations.size() - 1);
        }
    }

    private void sendSelect() {
        UltimatePortalGunItem.setFavoritePosition(portalGun, slotSelected);
        JDTEPacketHandler.CHANNEL.sendToServer(new UltimatePortalGunPayload(
                UltimatePortalGunPayload.ACTION_SELECT, slotSelected, "", null, 0, 0, 0, staysOpen));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        slotHovered = -1;
        float fraction = Math.min(5F, timeIn + partialTicks) / 5F;
        int centerX = width / 2;
        int centerY = height / 2;
        boolean inRange = isInRange(mouseX, mouseY);

        var poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate((1 - fraction) * centerX, (1 - fraction) * centerY, 0);
        poseStack.scale(fraction, fraction, fraction);
        super.render(graphics, mouseX, mouseY, partialTicks);
        poseStack.popPose();

        float angle = mouseAngle(centerX, centerY, mouseX, mouseY);
        float totalDegrees = 0;
        float degreesPerSegment = 360F / SEGMENTS_PER_PAGE;
        int page = currentPage();

        for (int segment = 0; segment < SEGMENTS_PER_PAGE; segment++) {
            int slot = page * SEGMENTS_PER_PAGE + segment;
            NBTHelpers.PortalDestination destination = getDestination(slot);
            boolean empty = destination == null;
            String name = empty ? "Empty" : destination.name();
            String dimension = empty ? "" : destination.dimension().location().getPath();
            String coordinates = empty ? "" : String.format("(%d, %d, %d)",
                    (int) destination.position().x, (int) destination.position().y, (int) destination.position().z);

            boolean mouseInSector = isCursorInSlice(angle, totalDegrees, degreesPerSegment, inRange);
            if (mouseInSector) {
                slotHovered = slot;
            }

            float radius = Math.max(0F, Math.min(
                    (timeIn + partialTicks - segment / (float) SEGMENTS_PER_PAGE) * 25F, RADIUS_MAX));
            float shade = segment % 2 == 0 ? 0.35F : 0.25F;
            float red = shade;
            float green = shade;
            float blue = shade;
            float alpha = 0.4F;
            if (mouseInSector) {
                red = green = blue = 1F;
            }
            if (slot == slotSelected) {
                red = green = 1F;
                alpha = 0.6F;
            }

            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer buffer = bufferSource.getBuffer(OurRenderTypes.TRIANGLE_STRIP);
            for (float i = degreesPerSegment; i >= 0; i--) {
                float radians = (float) Math.toRadians(i + totalDegrees);
                float outerX = (float) (centerX + Math.cos(radians) * radius);
                float outerY = (float) (centerY + Math.sin(radians) * radius);
                Matrix4f pose = poseStack.last().pose();
                buffer.vertex(pose,
                                (float) (centerX + Math.cos(radians) * radius / 2.3F),
                                (float) (centerY + Math.sin(radians) * radius / 2.3F), 0)
                        .color(red, green, blue, alpha).endVertex();
                buffer.vertex(outerX, outerY, 0).color(red, green, blue, alpha).endVertex();
            }
            bufferSource.endBatch(OurRenderTypes.TRIANGLE_STRIP);
            totalDegrees += degreesPerSegment;

            float textAngle = (totalDegrees - degreesPerSegment / 2) * (float) Math.PI / 180F;
            float textX = centerX + (float) (Math.cos(textAngle) * (RADIUS_MAX / 1.4));
            float textY = centerY + (float) (Math.sin(textAngle) * (RADIUS_MAX / 1.4));

            poseStack.pushPose();
            poseStack.translate(textX, textY, 0);
            poseStack.scale(0.85F, 0.85F, 0.85F);
            if (textAngle > Math.PI / 2 && textAngle < 3 * Math.PI / 2) {
                poseStack.mulPose(Axis.ZP.rotation(textAngle + (float) Math.PI));
            } else {
                poseStack.mulPose(Axis.ZP.rotation(textAngle));
            }
            graphics.drawString(font, name, -font.width(name) / 2, -15, Color.WHITE.getRGB());
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(textX, textY, 0);
            poseStack.scale(0.7F, 0.7F, 0.7F);
            if (textAngle > Math.PI / 2 && textAngle < 3 * Math.PI / 2) {
                poseStack.mulPose(Axis.ZP.rotation(textAngle + (float) Math.PI));
            } else {
                poseStack.mulPose(Axis.ZP.rotation(textAngle));
            }
            graphics.drawString(font, dimension, -font.width(dimension) / 2, -5, Color.LIGHT_GRAY.getRGB());
            graphics.drawString(font, coordinates, -font.width(coordinates) / 2, 10, Color.LIGHT_GRAY.getRGB());
            poseStack.popPose();
        }

        String pageLabel = Component.translatable("screen.jdte.ultimate_portal_gun.page",
                page + 1, pageCount()).getString();
        graphics.drawString(font, pageLabel, centerX - font.width(pageLabel) / 2,
                centerY - 135, Color.WHITE.getRGB());
        renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean isCursorInSlice(float angle, float totalDegrees, float segmentDegrees, boolean inRange) {
        return inRange && angle > totalDegrees && angle < totalDegrees + segmentDegrees;
    }

    private boolean isInRange(double mouseX, double mouseY) {
        int centerX = width / 2;
        int centerY = height / 2;
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance > RADIUS_MIN && distance < RADIUS_MAX;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isInRange(mouseX, mouseY)) {
            saveFavorite();
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (staysOpen && (key == 256 || key == KeyBindings.toggleTool.getKey().getValue())) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void tick() {
        if (!staysOpen && !InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(),
                KeyBindings.toggleTool.getKey().getValue())) {
            onClose();
        }
        timeIn++;
        refreshDestinations();
    }

    private void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (Renderable renderable : renderables) {
            if (renderable instanceof BaseButton button) {
                Component localization = button.getLocalization(mouseX, mouseY);
                if (!localization.equals(Component.empty())) {
                    graphics.renderTooltip(font, localization, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void saveFavorite() {
        if (slotHovered >= 0 && slotHovered < destinations.size()) {
            slotSelected = slotHovered;
        }
        sendSelect();
        OurSounds.playSound(Registration.BEEP.get());
    }

    private void addFavorite() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        Direction facing = MiscHelpers.getFacingDirection(minecraft.player);
        if (facing == Direction.DOWN) {
            facing = Direction.NORTH;
        }
        NBTHelpers.PortalDestination destination = new NBTHelpers.PortalDestination(
                minecraft.player.level().dimension(), minecraft.player.position(), facing, "UNNAMED");
        UltimatePortalGunItem.fillDestination(portalGun, slotSelected, destination);
        refreshDestinations();
        JDTEPacketHandler.CHANNEL.sendToServer(new UltimatePortalGunPayload(
                UltimatePortalGunPayload.ACTION_ADD_POSITION, slotSelected, "UNNAMED", null,
                0, 0, 0, staysOpen));
    }

    private void removeFavorite() {
        if (destinations.isEmpty() || slotSelected >= destinations.size()) {
            return;
        }
        UltimatePortalGunItem.clearDestination(portalGun, slotSelected);
        refreshDestinations();
        JDTEPacketHandler.CHANNEL.sendToServer(new UltimatePortalGunPayload(
                UltimatePortalGunPayload.ACTION_REMOVE, slotSelected, "", null,
                0, 0, 0, staysOpen));
    }

    private void editFavorite() {
        if (destinations.isEmpty() || slotSelected >= destinations.size()
                || destinations.get(slotSelected) == null) {
            return;
        }
        Minecraft.getInstance().setScreen(new UltimatePortalEditMenu(portalGun, slotSelected, this));
    }

    @Override
    public void onClose() {
        sendSelect();
        super.onClose();
    }
}
