package com.example.plugin.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nullable;

public final class CommandUtil {
    private CommandUtil() {
    }

    @Nullable
    public static PlayerRef requirePlayer(CommandContext context) {
        Object sender = context.sender();
        if (sender instanceof PlayerRef playerRef) {
            return playerRef;
        }
        context.sendMessage(Message.raw("This command can only be used by players."));
        return null;
    }

    public static boolean hasPermission(CommandContext context, String permission) {
        Object sender = context.sender();
        if (sender instanceof PlayerRef playerRef) {
            return playerRef.hasPermission(permission);
        }
        return false;
    }
}
