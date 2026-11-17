package com.ordana.immersive_weathering.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.network.ConfigSyncPayload;
import com.ordana.immersive_weathering.platform.config.ConfigValue;
import com.ordana.immersive_weathering.util.Seasons;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.Locale;

/** /iw season ... (see IWCommand). Every change is saved to the config file and synced to all players. */
final class SeasonCommand {

    static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("season")
                .executes(SeasonCommand::status)
                .then(Commands.literal("on").executes(c -> toggle(c, true)))
                .then(Commands.literal("off").executes(c -> toggle(c, false)))
                .then(Commands.literal("set").then(Commands.argument("season", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                Arrays.stream(Seasons.Season.values()).map(s -> s.name().toLowerCase(Locale.ROOT)), b))
                        .executes(SeasonCommand::setSeason)))
                .then(Commands.literal("month").then(Commands.argument("month", IntegerArgumentType.integer(1, 12))
                        .executes(c -> jumpToMonth(c, IntegerArgumentType.getInteger(c, "month") - 1))))
                .then(Commands.literal("skip").then(Commands.argument("months", IntegerArgumentType.integer(-1200, 1200))
                        .executes(c -> skip(c, IntegerArgumentType.getInteger(c, "months")))))
                .then(Commands.literal("pause").executes(c -> pause(c, true)))
                .then(Commands.literal("resume").executes(c -> pause(c, false)))
                .then(Commands.literal("length").then(Commands.argument("days", IntegerArgumentType.integer(1, 1000))
                        .executes(SeasonCommand::length)));
    }

    private static ServerLevel overworld(CommandContext<CommandSourceStack> c) {
        return c.getSource().getServer().overworld();
    }

    private static long worldDay(ServerLevel level) {
        return Math.floorDiv(level.getOverworldClockTime(), 24000L);
    }

    private static int status(CommandContext<CommandSourceStack> c) {
        ServerLevel level = overworld(c);
        long day = Seasons.seasonDay(level);
        int len = CommonConfigs.SEASON_MONTH_DAYS.get();
        int month = Seasons.monthOf(day);
        long dayInMonth = Math.floorMod(day, (long) len) + 1;
        String state = !CommonConfigs.SEASONS.get() ? " (seasons are OFF)" : CommonConfigs.SEASON_FROZEN_DAY.get() >= 0 ? " (paused)" : "";
        String msg = "Season: " + Seasons.MONTHS[month] + " (month " + (month + 1) + "/12), day " + dayInMonth + "/" + len + state;
        c.getSource().sendSuccess(() -> Component.literal(msg), false);
        return month + 1;
    }

    /** Moves the season clock so that today becomes {@code targetDay}. */
    private static void setSeasonDay(CommandContext<CommandSourceStack> c, long targetDay) {
        ServerLevel level = overworld(c);
        if (CommonConfigs.SEASON_FROZEN_DAY.get() >= 0) {
            set(CommonConfigs.SEASON_FROZEN_DAY, Math.floorMod(targetDay, 12L * CommonConfigs.SEASON_MONTH_DAYS.get()));
        } else {
            long year = 12L * CommonConfigs.SEASON_MONTH_DAYS.get();
            set(CommonConfigs.SEASON_OFFSET, Math.floorMod(targetDay - worldDay(level), year));
        }
    }

    private static int jumpToMonth(CommandContext<CommandSourceStack> c, int month) {
        setSeasonDay(c, (long) month * CommonConfigs.SEASON_MONTH_DAYS.get());
        return done(c);
    }

    private static int setSeason(CommandContext<CommandSourceStack> c) {
        String name = StringArgumentType.getString(c, "season").toUpperCase(Locale.ROOT);
        if (name.equals("FALL")) name = "AUTUMN";
        Seasons.Season s;
        try {
            s = Seasons.Season.valueOf(name);
        } catch (IllegalArgumentException e) {
            c.getSource().sendFailure(Component.literal("Unknown season. Use spring, summer, autumn or winter"));
            return 0;
        }
        return jumpToMonth(c, s.ordinal() * 3);
    }

    private static int skip(CommandContext<CommandSourceStack> c, int months) {
        long today = Seasons.seasonDay(overworld(c));
        setSeasonDay(c, today + (long) months * CommonConfigs.SEASON_MONTH_DAYS.get());
        return done(c);
    }

    private static int pause(CommandContext<CommandSourceStack> c, boolean pause) {
        ServerLevel level = overworld(c);
        long today = Seasons.seasonDay(level);
        long year = 12L * CommonConfigs.SEASON_MONTH_DAYS.get();
        if (pause) {
            set(CommonConfigs.SEASON_FROZEN_DAY, Math.floorMod(today, year));
        } else {
            set(CommonConfigs.SEASON_FROZEN_DAY, -1);
            set(CommonConfigs.SEASON_OFFSET, Math.floorMod(today - worldDay(level), year));
        }
        return done(c);
    }

    private static int length(CommandContext<CommandSourceStack> c) {
        //keep the current month and day of month
        ServerLevel level = overworld(c);
        long today = Seasons.seasonDay(level);
        int oldLen = CommonConfigs.SEASON_MONTH_DAYS.get();
        long month = Math.floorMod(Math.floorDiv(today, (long) oldLen), 12L);
        long dayInMonth = Math.floorMod(today, (long) oldLen);
        int newLen = IntegerArgumentType.getInteger(c, "days");
        set(CommonConfigs.SEASON_MONTH_DAYS, newLen);
        setSeasonDay(c, month * newLen + Math.min(dayInMonth, newLen - 1));
        return done(c);
    }

    private static int toggle(CommandContext<CommandSourceStack> c, boolean on) {
        ((ConfigValue<?>) CommonConfigs.SEASONS).setBool(on);
        return done(c);
    }

    private static void set(java.util.function.Supplier<Integer> v, long value) {
        ((ConfigValue<?>) v).setNumber(value);
    }

    private static int done(CommandContext<CommandSourceStack> c) {
        CommonConfigs.SERVER_SPEC.save();
        Seasons.update(overworld(c));
        for (ServerPlayer p : c.getSource().getServer().getPlayerList().getPlayers()) ConfigSyncPayload.sendTo(p);
        return status(c);
    }

    private SeasonCommand() {
    }
}
