package com.example.plugin.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import com.hypixel.hytale.server.core.command.system.CommandSender;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class CommandUtil {
    private CommandUtil() {
    }

    @Nullable
    public static PlayerRef requirePlayer(CommandContext context) {
        CommandSender sender = context.sender();
        if (sender instanceof PlayerRef playerRef) {
            return playerRef;
        }
        sender.sendMessage("This command can only be used by players.");
        return null;
    }

    public static boolean hasPermission(CommandContext context, String permission) {
        CommandSender sender = context.sender();
        return sender.hasPermission(permission);
    }
}
