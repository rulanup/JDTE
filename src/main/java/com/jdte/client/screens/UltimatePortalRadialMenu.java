package com.jdte.client.screens;

import com.direwolf20.justdirethings.JustDireThings;
import com.direwolf20.justdirethings.client.KeyBindings;
import com.direwolf20.justdirethings.client.OurSounds;
import com.direwolf20.justdirethings.client.renderers.OurRenderTypes;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.BaseButton;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.NBTHelpers;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.common.network.data.UltimatePortalGunPayload;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.awt.*;

/**
 * 顶级传送枪分页轮盘：每页 12 个槽位，页数无上限（数据为扁平列表）。
 * 按住 V 显示，松开关闭；左上角翻页按钮；添加/删除/编辑走服务端。
 */
public class UltimatePortalRadialMenu extends Screen {
    private static final int SEGMENTS_PER_PAGE = 12;
    private static final int radiusMin = 26;
    private static final int radiusMax = 120;

    private final ItemStack portalGun;
    private final java.util.List<NBTHelpers.PortalDestination> destinations;
    private int timeIn;
    private int slotHovered = -1;
    private int slotSelected;
    private boolean staysOpen;

    public UltimatePortalRadialMenu(ItemStack stack) {
        super(Component.literal(""));
        portalGun = stack;
        UltimatePortalGunItem.getDestinations(stack);
        destinations = UltimatePortalGunItem.getDestinations(stack);
        slotSelected = Math.min(UltimatePortalGunItem.getDestinations(stack).size() - 1, PortalGunV2Position());
        staysOpen = com.direwolf20.justdirethings.common.items.PortalGunV2.getStayOpen(stack);
    }

    private int PortalGunV2Position() {
        return com.direwolf20.justdirethings.common.items.PortalGunV2.getFavoritePosition(portalGun);
    }

    private int currentPage() {
        return destinations.isEmpty() ? 0 : slotSelected / SEGMENTS_PER_PAGE;
    }

    private int pageCount() {
        return Math.max(1, (destinations.size() + SEGMENTS_PER_PAGE - 1) / SEGMENTS_PER_PAGE);
    }

    private NBTHelpers.PortalDestination getFavorite(int slot) {
        if (slot < 0 || slot >= destinations.size()) {
            return null;
        }
        return destinations.get(slot);
    }

    private static float mouseAngle(int x, int y, int mx, int my) {
        Vector2f baseVec = new Vector2f(1F, 0F);
        Vector2f mouseVec = new Vector2f(mx - x, my - y);
        float ang = (float) (Math.acos(baseVec.dot(mouseVec) / (baseVec.length() * mouseVec.length())) * (180F / Math.PI));
        return my < y ? 360F - ang : ang;
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
    }

    @Override
    public void init() {
        GrayscaleButton addButton = new GrayscaleButton(width / 2 - 150, height / 2 - 20, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "textures/gui/buttons/add.png"),
                        Component.translatable("justdirethings.screen.add_favorite")).texture(),
                Component.translatable("justdirethings.screen.add_favorite"), true, (clicked) -> addFavorite());
        addRenderableWidget(addButton);

        GrayscaleButton removeButton = new GrayscaleButton(width / 2 + 140, height / 2 - 20, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "textures/gui/buttons/remove.png"),
                        Component.translatable("justdirethings.screen.remove_favorite")).texture(),
                Component.translatable("justdirethings.screen.remove_favorite"), true, (clicked) -> removeFavorite());
        addRenderableWidget(removeButton);

        GrayscaleButton editButton = new GrayscaleButton(width / 2 - 150, height / 2 + 20, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "textures/gui/buttons/matchnbttrue.png"),
                        Component.translatable("justdirethings.screen.edit_favorite")).texture(),
                Component.translatable("justdirethings.screen.edit_favorite"), true, (clicked) -> editFavorite());
        addRenderableWidget(editButton);

        GrayscaleButton stayOpenButton = new GrayscaleButton(width / 2 + 140, height / 2 + 20, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "textures/gui/buttons/area.png"),
                        Component.translatable("justdirethings.screen.stay_open")).texture(),
                Component.translatable("justdirethings.screen.stay_open"), staysOpen, (clicked) -> {
            staysOpen = !staysOpen;
            saveFavorite();
            ((GrayscaleButton) clicked).toggleActive();
        });
        addRenderableWidget(stayOpenButton);

        // 翻页按钮
        GrayscaleButton prevButton = new GrayscaleButton(width / 2 - 150, height / 2 - 56, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "textures/gui/buttons/mobscanner.png"),
                        Component.translatable("screen.jdte.ultimate_portal_gun.prev_page")).texture(),
                Component.translatable("screen.jdte.ultimate_portal_gun.prev_page"), true, (clicked) -> changePage(-1));
        addRenderableWidget(prevButton);

        GrayscaleButton nextButton = new GrayscaleButton(width / 2 + 140, height / 2 - 56, 16, 16,
                new ToggleButtonFactory.TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "textures/gui/buttons/mobscanner.png"),
                        Component.translatable("screen.jdte.ultimate_portal_gun.next_page")).texture(),
                Component.translatable("screen.jdte.ultimate_portal_gun.next_page"), true, (clicked) -> changePage(1));
        addRenderableWidget(nextButton);
    }

    private void changePage(int delta) {
        if (delta > 0) {
            // 下一页：若已在最后一页则先创建新页
            if (currentPage() >= pageCount() - 1) {
                UltimatePortalGunItem.ensurePage(portalGun);
                refreshDestinations();
            }
            slotSelected = Math.min((currentPage() + 1) * SEGMENTS_PER_PAGE, destinations.size() - 1);
        } else {
            // 上一页：若当前页是最后一页且整页为空，回收该页
            UltimatePortalGunItem.trimEmptyLastPage(portalGun);
            refreshDestinations();
            slotSelected = Math.max(0, (currentPage() - 1) * SEGMENTS_PER_PAGE);
        }
        sendSelect();
    }

    private void refreshDestinations() {
        destinations.clear();
        destinations.addAll(UltimatePortalGunItem.getDestinations(portalGun));
    }

    private void sendSelect() {
        PacketDistributor.sendToServer(new UltimatePortalGunPayload(
                UltimatePortalGunPayload.ACTION_SELECT, slotSelected, "", null, 0, 0, 0, staysOpen));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mx, int my, float partialTicks) {
        renderTooltip(guiGraphics, mx, my);
        float speedOfButtonGrowth = 5f;
        float fract = Math.min(speedOfButtonGrowth, this.timeIn + partialTicks) / speedOfButtonGrowth;
        int x = this.width / 2;
        int y = this.height / 2;

        boolean inRange = isInRange(mx, my);

        com.mojang.blaze3d.vertex.PoseStack matrices = guiGraphics.pose();
        matrices.pushPose();
        matrices.translate((1 - fract) * x, (1 - fract) * y, 0);
        matrices.scale(fract, fract, fract);
        super.render(guiGraphics, mx, my, partialTicks);
        matrices.popPose();

        float angle = mouseAngle(x, y, mx, my);
        float totalDeg = 0;
        float degPer = 360F / SEGMENTS_PER_PAGE;
        int page = currentPage();

        for (int seg = 0; seg < SEGMENTS_PER_PAGE; seg++) {
            int slot = page * SEGMENTS_PER_PAGE + seg;
            NBTHelpers.PortalDestination favorite = getFavorite(slot);
            boolean isEmpty = favorite == null || favorite.equals(NBTHelpers.PortalDestination.EMPTY);
            String favoriteName = isEmpty ? "Empty" : favorite.name();
            String dimension = favorite != null && !favorite.equals(NBTHelpers.PortalDestination.EMPTY)
                    ? favorite.globalVec3().dimension().location().getPath() : "";
            String coordinates = favorite != null && !favorite.equals(NBTHelpers.PortalDestination.EMPTY)
                    ? String.format("(%d, %d, %d)",
                    (int) favorite.globalVec3().position().x(),
                    (int) favorite.globalVec3().position().y(),
                    (int) favorite.globalVec3().position().z()) : "";
            boolean mouseInSector = this.isCursorInSlice(angle, totalDeg, degPer, inRange);
            float delayBetweenSegments = 1f;
            float speedOfSegmentGrowth = 25f;
            float radius = Math.max(0F, Math.min((this.timeIn + partialTicks - seg * delayBetweenSegments / SEGMENTS_PER_PAGE) * speedOfSegmentGrowth, radiusMax));
            float gs = 0.25F;
            if (seg % 2 == 0) {
                gs += 0.1F;
            }

            float r = gs;
            float g = gs;
            float b = gs;
            float a = 0.4F;
            if (mouseInSector) {
                this.slotHovered = slot;
                r = g = b = 1F;
            }
            if (slot == slotSelected) {
                r = g = 1F;
                a = 0.6f;
            }

            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer buffer = bufferSource.getBuffer(OurRenderTypes.TRIANGLE_STRIP);

            for (float i = degPer; i >= 0; i--) {
                float rad = (float) ((i + totalDeg) / 180F * Math.PI);
                float xp = (float) (x + Math.cos(rad) * radius);
                float yp = (float) (y + Math.sin(rad) * radius);

                Matrix4f pose = matrices.last().pose();
                buffer.addVertex(pose, (float) (x + Math.cos(rad) * radius / 2.3F), (float) (y + Math.sin(rad) * radius / 2.3F), 0).setColor(r, g, b, a);
                buffer.addVertex(xp, yp, 0).setColor(r, g, b, a);
            }

            bufferSource.endBatch(OurRenderTypes.TRIANGLE_STRIP);
            totalDeg += degPer;

            float nameAngle = (totalDeg - degPer / 2) * (float) Math.PI / 180F;
            float nameX = x + (float) (Math.cos(nameAngle) * (radiusMax / 1.4));
            float nameY = y + (float) (Math.sin(nameAngle) * (radiusMax / 1.4));
            int textWidth = this.font.width(favoriteName);
            int dimensionWidth = this.font.width(dimension);
            int coordinatesWidth = this.font.width(coordinates);

            matrices.pushPose();
            matrices.translate(nameX, nameY, 0);
            matrices.scale(0.85f, 0.85f, 0.85f);
            if (nameAngle > Math.PI / 2 && nameAngle < 3 * Math.PI / 2) {
                matrices.mulPose(Axis.ZP.rotation(nameAngle + (float) Math.PI));
            } else {
                matrices.mulPose(Axis.ZP.rotation(nameAngle));
            }
            guiGraphics.drawString(this.font, favoriteName, -textWidth / 2, -15, Color.WHITE.getRGB());
            matrices.popPose();

            matrices.pushPose();
            matrices.translate(nameX, nameY, 0);
            matrices.scale(0.7f, 0.7f, 0.7f);
            if (nameAngle > Math.PI / 2 && nameAngle < 3 * Math.PI / 2) {
                matrices.mulPose(Axis.ZP.rotation(nameAngle + (float) Math.PI));
            } else {
                matrices.mulPose(Axis.ZP.rotation(nameAngle));
            }
            guiGraphics.drawString(this.font, dimension, -dimensionWidth / 2, -5, Color.LIGHT_GRAY.getRGB());
            guiGraphics.drawString(this.font, coordinates, -coordinatesWidth / 2, 10, Color.LIGHT_GRAY.getRGB());
            matrices.popPose();
        }

        // 页码
        String pageLabel = Component.translatable("screen.jdte.ultimate_portal_gun.page", page + 1, pageCount()).getString();
        guiGraphics.drawString(this.font, pageLabel, x - this.font.width(pageLabel) / 2, y - 135, Color.WHITE.getRGB());
    }

    private boolean isCursorInSlice(float angle, float totalDeg, float degPer, boolean inRange) {
        return inRange && angle > totalDeg && angle < totalDeg + degPer;
    }

    public boolean isInRange(double mouseX, double mouseY) {
        int x = this.width / 2;
        int y = this.height / 2;
        double dist = new Vec3(x, y, 0).distanceTo(new Vec3(mouseX, mouseY, 0));
        return dist > radiusMin && dist < radiusMax;
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
        if (!staysOpen && !InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), KeyBindings.toggleTool.getKey().getValue())) {
            onClose();
        }
        this.timeIn++;
        destinations.clear();
        destinations.addAll(UltimatePortalGunItem.getDestinations(portalGun));
        if (!destinations.isEmpty() && slotSelected >= destinations.size()) {
            slotSelected = destinations.size() - 1;
        }
    }
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        for (Renderable renderable : this.renderables) {
            if (renderable instanceof BaseButton button && !button.getLocalization(pX, pY).equals(Component.empty())) {
                pGuiGraphics.renderTooltip(font, button.getLocalization(), pX, pY);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void saveFavorite() {
        if (slotHovered >= 0 && slotHovered < destinations.size()) {
            slotSelected = slotHovered;
        }
        sendSelect();
        OurSounds.playSound(Registration.BEEP.get());
    }

    public void addFavorite() {
        // 客户端本地立即写入并刷新，服务端同步处理
        net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            NBTHelpers.PortalDestination destination = new NBTHelpers.PortalDestination(
                    new NBTHelpers.GlobalVec3(player.level().dimension(), player.position()),
                    com.direwolf20.justdirethings.util.MiscHelpers.getFacingDirection(player), "UNNAMED");
            UltimatePortalGunItem.fillDestination(portalGun, slotSelected, destination);
            refreshDestinations();
        }
        PacketDistributor.sendToServer(new UltimatePortalGunPayload(
                UltimatePortalGunPayload.ACTION_ADD_POSITION, slotSelected, "UNNAMED", null, 0, 0, 0, staysOpen));
    }

    public void removeFavorite() {
        if (destinations.isEmpty() || slotSelected >= destinations.size()) {
            return;
        }
        UltimatePortalGunItem.clearDestination(portalGun, slotSelected);
        refreshDestinations();
        PacketDistributor.sendToServer(new UltimatePortalGunPayload(
                UltimatePortalGunPayload.ACTION_REMOVE, slotSelected, "", null, 0, 0, 0, staysOpen));
    }

    public void editFavorite() {
        if (destinations.isEmpty() || slotSelected >= destinations.size()) {
            return;
        }
        Minecraft.getInstance().setScreen(new UltimatePortalEditMenu(portalGun, slotSelected));
    }

    private static class Vector2f {
        public float x;
        public float y;

        public Vector2f(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float dot(Vector2f other) {
            return this.x * other.x + this.y * other.y;
        }

        public float length() {
            return (float) Math.sqrt(x * x + y * y);
        }
    }
}
