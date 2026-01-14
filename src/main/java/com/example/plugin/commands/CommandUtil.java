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

    public static boolean hasPermission(CommandContext context, String permission) {
        CommandSender sender = context.sender();
        Boolean contextPermission = invokePermission(context, permission);
        if (contextPermission != null) {
            return contextPermission;
        }
        Boolean senderPermission = invokePermission(sender, permission);
        if (senderPermission != null) {
            return senderPermission || isOperator(sender);
        }
        return isOperator(sender);
    }

    private static Boolean invokePermission(Object target, String permission) {
        try {
            var method = target.getClass().getMethod("hasPermission", String.class);
            Object result = method.invoke(target, permission);
            return result instanceof Boolean bool ? bool : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static boolean isOperator(Object sender) {
        return invokeBoolean(sender, "isOperator")
                || invokeBoolean(sender, "isOp")
                || invokeBoolean(sender, "isAdmin");
    }

    private static boolean invokeBoolean(Object target, String methodName) {
        try {
            var method = target.getClass().getMethod(methodName);
            Object result = method.invoke(target);
            return result instanceof Boolean bool && bool;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
