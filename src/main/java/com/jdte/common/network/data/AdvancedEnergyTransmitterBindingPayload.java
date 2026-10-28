package com.jdte.common.network.data;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
public record AdvancedEnergyTransmitterBindingPayload(BlockPos blockPos) {
    public static void encode(AdvancedEnergyTransmitterBindingPayload p, FriendlyByteBuf b) { b.writeBlockPos(p.blockPos); }
    public static AdvancedEnergyTransmitterBindingPayload decode(FriendlyByteBuf b) { return new AdvancedEnergyTransmitterBindingPayload(b.readBlockPos()); }
}
