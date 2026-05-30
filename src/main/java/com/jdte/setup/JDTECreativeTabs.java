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
                // Upgrade cards
                JDTEItems.upgrades().forEach(item -> output.accept(item.get()));
                output.accept(JDTEItems.EXTENDED_UPGRADE.get());
                output.accept(JDTEItems.TIME_FLUID_CATALYST.get());

                // Time Accelerators
                output.accept(JDTEItems.BASIC_TIME_ACCELERATOR.get());
                output.accept(JDTEItems.ADVANCED_TIME_ACCELERATOR.get());
                output.accept(JDTEItems.EXTENDED_TIME_ACCELERATOR.get());

                // Extended Machines
                output.accept(JDTEItems.EXTENDED_CLICKER.get());
                output.accept(JDTEItems.EXTENDED_BLOCK_BREAKER.get());
                output.accept(JDTEItems.EXTENDED_BLOCK_PLACER.get());
                output.accept(JDTEItems.EXTENDED_BLOCK_SWAPPER.get());
                output.accept(JDTEItems.EXTENDED_DROPPER.get());
                output.accept(JDTEItems.EXTENDED_SENSOR.get());
                output.accept(JDTEItems.EXTENDED_FLUID_COLLECTOR.get());
                output.accept(JDTEItems.EXTENDED_FLUID_PLACER.get());

                // Glue Activators
                output.accept(JDTEItems.BASIC_GLUE_ACTIVATOR.get());
                output.accept(JDTEItems.ADVANCED_GLUE_ACTIVATOR.get());
                output.accept(JDTEItems.EXTENDED_GLUE_ACTIVATOR.get());

                // Gel Generators
                output.accept(JDTEItems.ADVANCED_GEL_GENERATOR.get());
                output.accept(JDTEItems.EXTENDED_GEL_GENERATOR.get());

                // Fluid Stabilizer
                output.accept(JDTEItems.FLUID_STABILIZER.get());

                // Item Senders
                output.accept(JDTEItems.BASIC_ITEM_SENDER.get());
                output.accept(JDTEItems.ADVANCED_ITEM_SENDER.get());
                output.accept(JDTEItems.EXTENDED_ITEM_SENDER.get());

                // Fluid Senders
                output.accept(JDTEItems.BASIC_FLUID_SENDER.get());
                output.accept(JDTEItems.ADVANCED_FLUID_SENDER.get());
                output.accept(JDTEItems.EXTENDED_FLUID_SENDER.get());

                // Item Receivers
                output.accept(JDTEItems.BASIC_ITEM_RECEIVER.get());
                output.accept(JDTEItems.ADVANCED_ITEM_RECEIVER.get());
                output.accept(JDTEItems.EXTENDED_ITEM_RECEIVER.get());

                // Fluid Receivers
                output.accept(JDTEItems.BASIC_FLUID_RECEIVER.get());
                output.accept(JDTEItems.ADVANCED_FLUID_RECEIVER.get());
                output.accept(JDTEItems.EXTENDED_FLUID_RECEIVER.get());
            })
            .build());
}
