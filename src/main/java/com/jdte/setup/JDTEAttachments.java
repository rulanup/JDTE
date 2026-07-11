package com.jdte.setup;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.JDTE;
import com.jdte.common.autoioconfig.AutoIoConfigData;
import com.jdte.common.player.LifeAppleData;
import com.jdte.common.upgrades.ExtendedUpgradeItemStackHandler;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class JDTEAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, JDTE.MODID);

    public static final Supplier<AttachmentType<UpgradeItemStackHandler>> UPGRADE_HANDLER = ATTACHMENT_TYPES.register(
            "upgrade_handler", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof BaseMachineBE machine) {
                    return new UpgradeItemStackHandler(machine);
                }
                return new UpgradeItemStackHandler(null);
            }).build());

    public static final Supplier<AttachmentType<ExtendedUpgradeItemStackHandler>> EXTENDED_UPGRADE_HANDLER = ATTACHMENT_TYPES.register(
            "extended_upgrade_handler", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof BaseMachineBE machine) {
                    return new ExtendedUpgradeItemStackHandler(machine);
                }
                return new ExtendedUpgradeItemStackHandler(null);
            }).build());

    public static final Supplier<AttachmentType<JDTEFluidTank>> CLICKER_FLUID_TANK = ATTACHMENT_TYPES.register(
            "clicker_fluid_tank", () -> AttachmentType.serializable(() -> new JDTEFluidTank(UpgradeItemStackHandler.BASE_CLICKER_FLUID_CAPACITY)).build());

    public static final Supplier<AttachmentType<AutoIoConfigData>> AUTO_IO_CONFIG = ATTACHMENT_TYPES.register(
            "auto_io_config", () -> AttachmentType.serializable(AutoIoConfigData::new).build());

    public static final Supplier<AttachmentType<LifeAppleData>> LIFE_APPLE_DATA = ATTACHMENT_TYPES.register(
            "life_apple_data", () -> AttachmentType.serializable(LifeAppleData::new).build());
}
