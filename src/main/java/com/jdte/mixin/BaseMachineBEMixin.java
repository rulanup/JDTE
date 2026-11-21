package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.autoioconfig.AutoIoTransferHelper;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.common.utils.DedicatedUpgradeDropHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import java.util.function.Consumer;
import com.jdte.common.upgrades.UpgradeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseMachineBE.class)
public abstract class BaseMachineBEMixin {
    @Shadow protected int tickSpeed;
    @Shadow protected int operationTicks;

    @Inject(method = "preRemoveSideEffects", at = @At("HEAD"))
    private void jdte$dropMachineItems(BlockPos pos, BlockState state, CallbackInfo ci) {
        BaseMachineBE machine = (BaseMachineBE) (Object) this;
        var level = machine.getLevel();
        if (level == null || level.isClientSide()) return;
        Consumer<ItemStack> drop = stack -> Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        var inventory = machine.getMachineHandler();
        if (inventory != null) {
            for (int slot = 0; slot < inventory.size(); slot++) {
                var resource = inventory.getResource(slot);
                if (resource.isEmpty()) continue;
                drop.accept(resource.toStack(inventory.getAmountAsInt(slot)));
                inventory.set(slot, ItemResource.EMPTY, 0);
            }
        }
        if (machine instanceof BioCrusherBE crusher) {
            DedicatedUpgradeDropHelper.drop(crusher.getLootingHandler(), drop);
            DedicatedUpgradeDropHelper.drop(crusher.getSharpnessHandler(), drop);
        }
        var handler = machine instanceof LootFabricatorBE fabricator
                ? fabricator.getUpgradeHandler() : UpgradeHelper.getUpgradeHandler(machine);
        if (handler != null) DedicatedUpgradeDropHelper.drop(handler, drop);
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void jdte$syncUpgradeCapacities(CallbackInfo ci) {
        BaseMachineBE machine = (BaseMachineBE) (Object) this;
        UpgradeHelper.syncCapacities(machine);
        if (UpgradeHelper.mayRunWithUpgrades(machine)) {
            AutoIoTransferHelper.tick(machine);
        }
    }

    @Inject(method = "handleTicks", at = @At("HEAD"), cancellable = true)
    private void jdte$handleEffectiveTicks(CallbackInfo ci) {
        int effectiveTickSpeed = UpgradeHelper.getEffectiveTickSpeed((BaseMachineBE) (Object) this, tickSpeed);
        if (operationTicks <= 0) {
            operationTicks = effectiveTickSpeed;
        }
        operationTicks--;
        ci.cancel();
    }

    @Inject(method = "getTickSpeed", at = @At("RETURN"), cancellable = true)
    private void jdte$getEffectiveTickSpeed(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(UpgradeHelper.getEffectiveTickSpeed((BaseMachineBE) (Object) this, cir.getReturnValue()));
    }
}
