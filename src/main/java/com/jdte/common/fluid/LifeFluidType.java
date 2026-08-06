package com.jdte.common.fluid;

import com.jdte.JDTE;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Consumer;

public class LifeFluidType extends FluidType {
    public static final ResourceLocation STILL_TEXTURE = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "block/life_fluid_still");
    public static final ResourceLocation FLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "block/life_fluid_flow");

    public LifeFluidType() {
        super(Properties.create()
                .density(1000)
                .viscosity(1000)
                .sound(SoundActions.BUCKET_FILL, net.minecraft.sounds.SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY)
        );
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING_TEXTURE;
            }

            @Override
            public int getTintColor() {
                return 0xFFDC143C;
            }
        });
    }
}
