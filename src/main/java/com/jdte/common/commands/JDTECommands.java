package com.jdte.common.commands;

import com.jdte.setup.JDTEConfig;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.jdte.common.blockentities.TimeFreezerManager;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        event.getDispatcher().register(Commands.literal("jdte")
                .requires(source -> source.hasPermission(4))
                .then(Commands.literal("timefreezer")
                        .then(Commands.literal("list")
                                .executes(context -> listTimeFreezers(context.getSource())))));
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

    private static int listTimeFreezers(net.minecraft.commands.CommandSourceStack source) {
        List<TimeFreezerBE> freezers = new ArrayList<>(TimeFreezerManager.getRegistered());
        if (freezers.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.jdte.timefreezer.list.empty"), false);
            return 1;
        }
        freezers.sort(Comparator.comparing(freezer -> freezer.getBlockPos().asLong()));
        source.sendSuccess(() -> Component.translatable("command.jdte.timefreezer.list.header", freezers.size()), false);
        for (TimeFreezerBE freezer : freezers) {
            Level level = freezer.getLevel();
            if (level == null) {
                continue;
            }
            BlockPos pos = freezer.getBlockPos();
            boolean active = TimeFreezerManager.isActive(freezer);
            Component status = Component.translatable(active
                    ? "command.jdte.timefreezer.active" : "command.jdte.timefreezer.inactive")
                    .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.GRAY);
            source.sendSuccess(() -> Component.translatable("command.jdte.timefreezer.list.entry",
                    Component.literal(level.dimension().location().toString()),
                    Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"),
                    status, freezer.getFluidTank().getFluidAmount(), freezer.getMaxMB()), false);
        }
        return 1;
    }
}
