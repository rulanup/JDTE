package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.setup.JDTEFluids;
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
                output.accept(JDTEItems.LOOTING_UPGRADE.get());
                output.accept(JDTEItems.SHARPNESS_UPGRADE.get());
                output.accept(JDTEItems.EXTENDED_UPGRADE.get());
                output.accept(JDTEItems.ECLIPSEALLOY_WRENCH.get());
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
                output.accept(JDTEItems.ADVANCED_ITEM_COLLECTOR.get());
                output.accept(JDTEItems.ENTITY_SUPPRESSOR.get());
                output.accept(JDTEItems.RANGE_BLOCKER.get());

                // Glue Activators
                output.accept(JDTEItems.BASIC_GLUE_ACTIVATOR.get());
                output.accept(JDTEItems.ADVANCED_GLUE_ACTIVATOR.get());
                output.accept(JDTEItems.EXTENDED_GLUE_ACTIVATOR.get());

                // Gel Generators
                output.accept(JDTEItems.ADVANCED_GEL_GENERATOR.get());
                output.accept(JDTEItems.EXTENDED_GEL_GENERATOR.get());

                // Fluid Stabilizer
                output.accept(JDTEItems.BASIC_FLUID_STABILIZER.get());
                output.accept(JDTEItems.ADVANCED_FLUID_STABILIZER.get());
                output.accept(JDTEItems.EXTENDED_FLUID_STABILIZER.get());

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
                output.accept(JDTEItems.CRYSTAL_INCUBATOR.get());
                output.accept(JDTEItems.GREENHOUSE.get());
                output.accept(JDTEItems.BIO_FACTORY.get());

                // Fluid Receivers
                output.accept(JDTEItems.BASIC_FLUID_RECEIVER.get());
                output.accept(JDTEItems.ADVANCED_FLUID_RECEIVER.get());
                output.accept(JDTEItems.EXTENDED_FLUID_RECEIVER.get());

                // Life Extractor
                output.accept(JDTEItems.ADVANCED_LIFE_EXTRACTOR.get());
                output.accept(JDTEItems.EXTENDED_LIFE_EXTRACTOR.get());
                output.accept(JDTEItems.LIFE_APPLE.get());
                output.accept(JDTEFluids.LIFE_FLUID_BUCKET.get());

                // Infusion Machine
                output.accept(JDTEItems.ADVANCED_INFUSION_MACHINE.get());
                output.accept(JDTEItems.EXTENDED_INFUSION_MACHINE.get());
                output.accept(JDTEItems.ADVANCED_POTION_BREWER.get());

                // Bio Crusher
                output.accept(JDTEItems.ADVANCED_BIO_CRUSHER.get());
                output.accept(JDTEItems.EXTENDED_BIO_CRUSHER.get());
                output.accept(JDTEItems.LOOT_FABRICATOR.get());

                // Boss Essences
                output.accept(JDTEItems.WITHER_ESSENCE.get());
                output.accept(JDTEItems.WITHER_SPAWN_EGG.get());
                output.accept(JDTEItems.ENDER_DRAGON_SPAWN_EGG.get());
                output.accept(JDTEItems.ENDER_DRAGON_ESSENCE.get());
                output.accept(JDTEItems.ELDER_GUARDIAN_ESSENCE.get());
            })
            .build());
}
