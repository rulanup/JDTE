package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.common.containers.slots.FilterBasicSlot;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseMachineContainer.class)
public abstract class BaseMachineContainerFilterMixin {
    @Shadow public int FILTER_SLOTS;
    @Shadow public BaseMachineBE baseMachineBE;
    @Shadow public FilterBasicHandler filterHandler;

    @Shadow
    protected abstract int addFilterSlots(FilterBasicHandler handler, int index, int x, int y, int amount, int dx);

    @Inject(method = "addFilterSlots", at = @At("HEAD"), cancellable = true)
    private void jdte$addExtraFilterSlots(CallbackInfo ci) {
        if (baseMachineBE == null || filterHandler == null) return;

        int filterUpgrades = UpgradeHelper.countUpgrades(baseMachineBE, UpgradeType.FILTER);
        if (filterUpgrades <= 0) return;

        // 取消原方法
        ci.cancel();

        // 计算额外的过滤槽数量（每个升级增加一排，每排9个）
        int extraSlots = filterUpgrades * 9;
        int originalSlots = FILTER_SLOTS;
        int totalSlots = originalSlots + extraSlots;

        // 扩展 filterHandler 容量
        jdte$expandFilterHandler(totalSlots);

        // 更新 FILTER_SLOTS 字段
        FILTER_SLOTS = totalSlots;

        // 添加第一排（原有槽位）
        int slotsInFirstRow = Math.min(originalSlots, 9);
        addFilterSlots(filterHandler, 0, 8, 54, slotsInFirstRow, 18);

        // 添加额外的排
        int remainingSlots = totalSlots - slotsInFirstRow;
        if (remainingSlots > 0) {
            addFilterSlots(filterHandler, slotsInFirstRow, 8, 72, Math.min(remainingSlots, 9), 18);
        }
        if (remainingSlots > 9) {
            addFilterSlots(filterHandler, slotsInFirstRow + 9, 8, 90, Math.min(remainingSlots - 9, 9), 18);
        }
    }

    @Unique
    private void jdte$expandFilterHandler(int newSize) {
        // 通过反射或 accessor 扩展 filterHandler 的内部数组
        if (filterHandler instanceof FilterBasicHandlerAccessor accessor) {
            accessor.jdte$setSize(newSize);
        }
    }
}
