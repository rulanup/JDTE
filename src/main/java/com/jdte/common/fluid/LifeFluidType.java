package com.jdte.common.fluid;

import com.jdte.JDTE;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;

public class LifeFluidType extends FluidType {
    public static final Identifier STILL_TEXTURE = Identifier.fromNamespaceAndPath(JDTE.MODID, "block/life_fluid_still");
    public static final Identifier FLOWING_TEXTURE = Identifier.fromNamespaceAndPath(JDTE.MODID, "block/life_fluid_flow");

    public LifeFluidType() {
        super(Properties.create()
                .density(1000)
                .viscosity(1000)
                .sound(SoundActions.BUCKET_FILL, net.minecraft.sounds.SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY)
        );
    }

}
