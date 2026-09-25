package com.jdte.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.IOException;
import java.io.InputStream;

/**
 * 彩蛋用高分辨率贴图：加载 512x512 PNG，CPU 生成完整 mip 链（512→16）并启用三线性过滤，
 * 使整张图缩进 16x16 GUI 槽位时按 mip 级别平滑采样，避免最近邻抽样产生的雪花噪点。
 * 加载流程与 vanilla {@code SimpleTexture#doLoad} 一致；资源重载时由 TextureManager 重新调用 load()。
 */
public class EasterEggMipmapTexture extends AbstractTexture {
    private static final int MIP_LEVELS = 5; // 512 -> 256 -> 128 -> 64 -> 32 -> 16

    private final ResourceLocation file;

    public EasterEggMipmapTexture(ResourceLocation file) {
        this.file = file;
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        NativeImage image;
        try (InputStream in = resourceManager.open(this.file)) {
            image = NativeImage.read(in);
        }
        try {
            TextureUtil.prepareImage(this.getId(), MIP_LEVELS, image.getWidth(), image.getHeight());
            NativeImage[] byMipLevel = MipmapGenerator.generateMipLevels(new NativeImage[]{image}, MIP_LEVELS);
            this.bind();
            for (int level = 0; level < byMipLevel.length; level++) {
                NativeImage mip = byMipLevel[level];
                mip.upload(level, 0, 0, 0, 0, mip.getWidth(), mip.getHeight(), false, false);
            }
            this.setFilter(true, true); // blur + mipmap -> GL_LINEAR_MIPMAP_LINEAR / GL_LINEAR
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        } finally {
            image.close();
        }
    }
}
