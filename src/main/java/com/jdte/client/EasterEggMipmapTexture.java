package com.jdte.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.Identifier;

public class EasterEggMipmapTexture extends SimpleTexture {
    public EasterEggMipmapTexture(Identifier file) {
        super(file);
    }

    @Override
    protected void doLoad(NativeImage image) {
        int levels = Math.min(5, Integer.numberOfTrailingZeros(image.getWidth() | image.getHeight()));
        NativeImage[] images = MipmapGenerator.generateMipLevels(resourceId(), new NativeImage[]{image}, levels,
                MipmapStrategy.MEAN, 0, image.computeTransparency());
        try {
            close();
            var device = RenderSystem.getDevice();
            texture = device.createTexture(resourceId()::toString, GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
                    TextureFormat.RGBA8, image.getWidth(), image.getHeight(), 1, images.length);
            textureView = device.createTextureView(texture);
            sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
                    FilterMode.LINEAR, FilterMode.LINEAR, true);
            var encoder = device.createCommandEncoder();
            for (int i = 0; i < images.length; i++) {
                var mip = images[i];
                encoder.writeToTexture(texture, mip, i, 0, 0, 0, mip.getWidth(), mip.getHeight(), 0, 0);
            }
        } finally {
            // ReloadableTexture owns the base image.
            for (int i = 1; i < images.length; i++) images[i].close();
        }
    }
}
