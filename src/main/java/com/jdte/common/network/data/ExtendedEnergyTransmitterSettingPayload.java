package com.jdte.common.network.data;
import net.minecraft.network.FriendlyByteBuf;
public record ExtendedEnergyTransmitterSettingPayload(boolean showParticles) {
    public static void encode(ExtendedEnergyTransmitterSettingPayload p, FriendlyByteBuf b) { b.writeBoolean(p.showParticles); }
    public static ExtendedEnergyTransmitterSettingPayload decode(FriendlyByteBuf b) { return new ExtendedEnergyTransmitterSettingPayload(b.readBoolean()); }
}
