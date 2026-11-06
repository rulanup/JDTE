package com.jdte.common.network.data;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
/** Source target-level control. Boolean toggles use the vanilla menu protocol to avoid stale-client overwrites. */
public record ExtendedExperienceHolderSettingsPayload(BlockPos blockPos,int targetExp) {
    public static void encode(ExtendedExperienceHolderSettingsPayload p,FriendlyByteBuf b){b.writeBlockPos(p.blockPos);b.writeInt(p.targetExp);}
    public static ExtendedExperienceHolderSettingsPayload decode(FriendlyByteBuf b){return new ExtendedExperienceHolderSettingsPayload(b.readBlockPos(),b.readInt());}
}
