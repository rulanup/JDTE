package com.jdte.setup;

import com.jdte.JDTE;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class JDTECreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JDTE.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register(JDTE.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.jdte"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()))
            .displayItems((parameters, output) -> {
                JDTEItems.upgrades().forEach(item -> output.accept(item.get()));
                output.accept(JDTEItems.BASIC_TIME_ACCELERATOR.get());
                output.accept(JDTEItems.ADVANCED_TIME_ACCELERATOR.get());
                output.accept(JDTEItems.EXTENDED_CLICKER.get());
                output.accept(JDTEItems.EXTENDED_BLOCK_BREAKER.get());
                output.accept(JDTEItems.EXTENDED_BLOCK_PLACER.get());
                output.accept(JDTEItems.EXTENDED_BLOCK_SWAPPER.get());
                output.accept(JDTEItems.EXTENDED_DROPPER.get());
                output.accept(JDTEItems.EXTENDED_SENSOR.get());
                output.accept(JDTEItems.EXTENDED_FLUID_COLLECTOR.get());
                output.accept(JDTEItems.EXTENDED_FLUID_PLACER.get());
            })
            .build());
}
