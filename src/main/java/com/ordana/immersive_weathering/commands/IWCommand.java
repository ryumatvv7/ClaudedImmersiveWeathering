package com.ordana.immersive_weathering.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.network.ConfigSyncPayload;
import com.ordana.immersive_weathering.platform.config.ConfigValue;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.Optional;

/**
 * /iw speed                      - list the speed multipliers
 * /iw speed &lt;name&gt; &lt;value&gt;       - change one (saved to the config file and sent to every player)
 * /iw reload                     - re-read the config file
 * /iw abandon &lt;days&gt;             - age the current chunk as if nobody came here for that many days
 * Needs operator permission.
 */
public final class IWCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> dispatcher.register(
                Commands.literal("iw").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("speed")
                                .executes(IWCommand::list)
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                                CommonConfigs.SERVER_SPEC.getValues().stream()
                                                        .filter(IWCommand::isSpeed).map(ConfigValue::name), b))
                                        .executes(c -> show(c, StringArgumentType.getString(c, "name")))
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0, 100))
                                                .executes(c -> set(c, StringArgumentType.getString(c, "name"),
                                                        DoubleArgumentType.getDouble(c, "value"))))))
                        .then(Commands.literal("reload").executes(IWCommand::reload))
                        .then(Commands.literal("abandon")
                                .then(Commands.argument("days", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 1000))
                                        .executes(c -> {
                                            int days = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "days");
                                            int n = com.ordana.immersive_weathering.util.Abandonment.age(c.getSource().getLevel(),
                                                    net.minecraft.core.BlockPos.containing(c.getSource().getPosition()), days);
                                            c.getSource().sendSuccess(() -> Component.literal("Aged this chunk by " + days + " days: " + n + " blocks changed"), true);
                                            return 1;
                                        })))));
    }

    private static boolean isSpeed(ConfigValue<?> v) {
        return v.isNumber() && v.path().size() == 2 && v.path().get(0).equals("speed");
    }

    private static Optional<ConfigValue<?>> find(String name) {
        return CommonConfigs.SERVER_SPEC.getValues().stream().filter(v -> isSpeed(v) && v.name().equals(name)).findFirst();
    }

    private static String fmt(Object o) {
        return o instanceof Double d ? String.format(Locale.ROOT, "%.2f", d) : String.valueOf(o);
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("Immersive Weathering speed (1 = normal, 0 = off):");
        CommonConfigs.SERVER_SPEC.getValues().stream().filter(IWCommand::isSpeed)
                .forEach(v -> sb.append("\n  ").append(v.name()).append(" = ").append(fmt(v.get())));
        c.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int show(CommandContext<CommandSourceStack> c, String name) {
        var v = find(name);
        if (v.isEmpty()) {
            c.getSource().sendFailure(Component.literal("Unknown speed setting: " + name));
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal(name + " = " + fmt(v.get().get())), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> c, String name, double value) {
        var v = find(name);
        if (v.isEmpty()) {
            c.getSource().sendFailure(Component.literal("Unknown speed setting: " + name));
            return 0;
        }
        v.get().setNumber(value);
        CommonConfigs.SERVER_SPEC.save();
        resync(c.getSource());
        c.getSource().sendSuccess(() -> Component.literal("Set " + name + " to " + fmt(v.get().get())), true);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> c) {
        CommonConfigs.SERVER_SPEC.forceLoad();
        resync(c.getSource());
        c.getSource().sendSuccess(() -> Component.literal("Immersive Weathering config reloaded"), true);
        return 1;
    }

    private static void resync(CommandSourceStack source) {
        for (ServerPlayer p : source.getServer().getPlayerList().getPlayers()) ConfigSyncPayload.sendTo(p);
    }

    private IWCommand() {
    }
}
