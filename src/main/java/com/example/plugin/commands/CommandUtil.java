package com.example.plugin.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
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
        if (sender instanceof Player player) {
            return player.getReference();
        }
        sender.sendMessage("This command can only be used by players.");
        return null;
    }

    @Nullable
    public static PlayerRef resolvePlayer(Object argValue, PlayerLookup playerLookup) {
        if (argValue == null) {
            return null;
        }
        if (argValue instanceof PlayerRef playerRef) {
            return playerRef;
        }
        if (argValue instanceof Player player) {
            return player.getReference();
        }
        if (argValue instanceof String name && !name.isBlank()) {
            return playerLookup.findOnlinePlayer(name);
        }
        return null;
    }

    public static boolean hasPermission(CommandContext context, String permission) {
        CommandSender sender = context.sender();
        return sender.hasPermission(permission);
    }
}
