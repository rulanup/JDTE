package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.ChargedLightningRodBE;
import com.jdte.common.blockentities.ChargedLightningRodMode;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;

public class ChargedLightningRodContainer extends BaseMachineContainer {
    private final SimpleContainerData settings = new SimpleContainerData(4);

    public ChargedLightningRodContainer(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos());
    }

    public ChargedLightningRodContainer(int id, Inventory inventory, BlockPos pos) {
        super(JDTEMenus.CHARGED_LIGHTNING_ROD.get(), id, inventory, pos);
        addPlayerSlots(inventory);
        updateSettings();
        addDataSlots(settings);
    }

    private void updateSettings() {
        if (baseMachineBE instanceof ChargedLightningRodBE rod) {
            settings.set(0, rod.isLightningRenderEnabled() ? 1 : 0);
            settings.set(1, rod.isLightningSoundEnabled() ? 1 : 0);
            settings.set(2, rod.getStrikeMode().ordinal());
            settings.set(3, rod.canOperateInWeather() ? 1 : 0);
        }
    }

    @Override
    public void broadcastChanges() {
        if (!player.level().isClientSide) updateSettings();
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!stillValid(player) || !(baseMachineBE instanceof ChargedLightningRodBE rod)
                || button < 0 || button > 1) return false;
        rod.setLightningEffects(button == 0 ? !rod.isLightningRenderEnabled() : rod.isLightningRenderEnabled(),
                button == 1 ? !rod.isLightningSoundEnabled() : rod.isLightningSoundEnabled());
        broadcastChanges();
        return true;
    }

    public boolean isRenderEnabled() { return settings.get(0) != 0; }
    public boolean isSoundEnabled() { return settings.get(1) != 0; }
    public boolean isWeatherAllowed() { return settings.get(3) != 0; }
    public ChargedLightningRodMode getMode() {
        return ChargedLightningRodMode.values()[Math.clamp(settings.get(2), 0, ChargedLightningRodMode.values().length - 1)];
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.CHARGED_LIGHTNING_ROD.get());
    }
}
