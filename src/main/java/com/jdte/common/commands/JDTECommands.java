package com.jdte.common.commands;

import com.jdte.setup.JDTEConfig;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class JDTECommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("jdte")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("timeaccelerator")
                        .then(Commands.literal("fluidCostMultiplier")
                                .executes(context -> showFluidCostMultiplier(context.getSource()))
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0D, 1000.0D))
                                        .executes(context -> setFluidCostMultiplier(
                                                context.getSource(),
                                                DoubleArgumentType.getDouble(context, "value")
                                        ))))));
    }

    private static int showFluidCostMultiplier(net.minecraft.commands.CommandSourceStack source) {
        double value = JDTEConfig.COMMON.timeAcceleratorFluidCostMultiplier.get();
        source.sendSuccess(() -> Component.literal("JDTE time accelerator fluid cost multiplier: " + value), false);
        return 1;
    }

    private static int setFluidCostMultiplier(net.minecraft.commands.CommandSourceStack source, double value) {
        JDTEConfig.COMMON.timeAcceleratorFluidCostMultiplier.set(value);
        JDTEConfig.COMMON_SPEC.save();
        source.sendSuccess(() -> Component.literal("Set JDTE time accelerator fluid cost multiplier to " + value), true);
        return 1;
    }
}
