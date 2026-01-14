package com.example.plugin.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class CommandUtil {
    private CommandUtil() {
    }

    @Nullable
    public static PlayerRef requirePlayer(CommandContext context) {
        try {
            Object player = context.getClass().getMethod("senderAsPlayer").invoke(context);
            if (player != null) {
                Object reference = player.getClass().getMethod("getReference").invoke(player);
                if (reference instanceof PlayerRef playerRef) {
                    return playerRef;
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // fall through
        }
        context.sendMessage(Message.raw("This command can only be used by players."));
        return null;
    }

    public static boolean hasPermission(CommandContext context, String permission) {
        PlayerRef sender = requirePlayer(context);
        if (sender == null) {
            return false;
        }
        if (sender.hasPermission(permission)) {
            return true;
        }
        try {
            Object isOperator = sender.getClass().getMethod("isOperator").invoke(sender);
            if (isOperator instanceof Boolean value && value) {
                return true;
            }
        } catch (ReflectiveOperationException ignored) {
            // ignore
        }
        try {
            Object isOp = sender.getClass().getMethod("isOp").invoke(sender);
            if (isOp instanceof Boolean value && value) {
                return true;
            }
        } catch (ReflectiveOperationException ignored) {
            // ignore
        }
        return false;
    }
}
