package com.jdte.common.network.data;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
public record AdvancedEnergyTransmitterPayload(BlockPos blockPos, boolean showParticles) {
    public static void encode(AdvancedEnergyTransmitterPayload p, FriendlyByteBuf b) { b.writeBlockPos(p.blockPos); b.writeBoolean(p.showParticles); }
    public static AdvancedEnergyTransmitterPayload decode(FriendlyByteBuf b) { return new AdvancedEnergyTransmitterPayload(b.readBlockPos(), b.readBoolean()); }
}
