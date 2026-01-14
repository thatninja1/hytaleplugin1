package com.example.plugin.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class CommandUtil {
    private static final Logger LOGGER = Logger.getLogger(CommandUtil.class.getName());
    private static final AtomicBoolean SENDER_DIAGNOSTICS_LOGGED = new AtomicBoolean(false);
    private static final AtomicBoolean PERMISSION_DIAGNOSTICS_LOGGED = new AtomicBoolean(false);

    private CommandUtil() {
    }

    @Nullable
    public static PlayerRef requirePlayer(CommandContext context) {
        PlayerRef resolved = resolvePlayerRef(context);
        if (resolved != null) {
            return resolved;
        }
        if (SENDER_DIAGNOSTICS_LOGGED.compareAndSet(false, true)) {
            logContextDiagnostics(context, false);
        }
        context.sendMessage(Message.raw("This command can only be used by players."));
        return null;
    }

    public static boolean hasPermission(CommandContext context, String permission) {
        Boolean contextPermission = resolveContextPermission(context, permission);
        if (contextPermission != null) {
            if (contextPermission) {
                return true;
            }
        }

        PlayerRef sender = resolvePlayerRef(context);
        if (sender == null) {
            if (PERMISSION_DIAGNOSTICS_LOGGED.compareAndSet(false, true)) {
                logPermissionDiagnostics(context, permission, false);
            }
            return false;
        }

        Boolean playerPermission = resolvePlayerPermission(sender, permission);
        boolean isOp = resolveOpStatus(sender);
        if (playerPermission != null && playerPermission) {
            return true;
        }
        if (isOp) {
            return true;
        }

        if (PERMISSION_DIAGNOSTICS_LOGGED.compareAndSet(false, true)) {
            logPermissionDiagnostics(context, permission, false);
        }
        return false;
    }

    static void logContextDiagnostics(CommandContext context, boolean force) {
        if (!force && SENDER_DIAGNOSTICS_LOGGED.get()) {
            return;
        }
        LOGGER.warning("EconomyPlugin sender diagnostics for CommandContext: " + context.getClass().getName());
        for (Method method : context.getClass().getMethods()) {
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (name.contains("sender") || name.contains("player") || name.contains("source")
                    || name.contains("caller") || name.contains("permission") || name.contains("op")) {
                LOGGER.warning(" - " + method.getName() + "(): " + method.getReturnType().getName());
            }
        }
    }

    static void logPermissionDiagnostics(CommandContext context, String permission, boolean force) {
        if (!force && PERMISSION_DIAGNOSTICS_LOGGED.get()) {
            return;
        }
        LOGGER.warning("EconomyPlugin permission diagnostics for node: " + permission);
        Boolean contextPermission = resolveContextPermission(context, permission);
        LOGGER.warning(" - CommandContext.hasPermission available: " + (contextPermission != null)
                + ", value: " + contextPermission);
        PlayerRef sender = resolvePlayerRef(context);
        if (sender == null) {
            LOGGER.warning(" - PlayerRef not resolved from CommandContext.");
            return;
        }
        LOGGER.warning(" - PlayerRef type: " + sender.getClass().getName());
        Boolean playerPermission = resolvePlayerPermission(sender, permission);
        LOGGER.warning(" - PlayerRef.hasPermission available: " + (playerPermission != null)
                + ", value: " + playerPermission);
        LOGGER.warning(" - PlayerRef op status: " + resolveOpStatus(sender));
        logPlayerRefMethods(sender);
    }

    private static void logPlayerRefMethods(PlayerRef sender) {
        Arrays.stream(sender.getClass().getMethods())
                .filter(method -> method.getName().toLowerCase(Locale.ROOT).contains("op")
                        || method.getName().toLowerCase(Locale.ROOT).contains("permission"))
                .forEach(method -> LOGGER.warning(" - PlayerRef method: " + method.getName()
                        + "(): " + method.getReturnType().getName()));
    }

    private static PlayerRef resolvePlayerRef(CommandContext context) {
        PlayerRef ref = resolveDirectPlayerRef(context);
        if (ref != null) {
            return ref;
        }
        Object player = invokeSenderMethod(context, "senderAsPlayer");
        if (player instanceof PlayerRef playerRef) {
            return playerRef;
        }
        if (player instanceof Optional<?> optional) {
            if (optional.isPresent()) {
                player = optional.get();
            } else {
                player = null;
            }
        }
        if (player != null) {
            Object reference = invokePlayerReference(player);
            if (reference instanceof PlayerRef playerRef) {
                return playerRef;
            }
        }
        return null;
    }

    private static PlayerRef resolveDirectPlayerRef(CommandContext context) {
        Object directRef = invokeSenderMethod(context, "senderAsPlayerRef");
        if (directRef instanceof Optional<?> optional) {
            if (optional.isPresent() && optional.get() instanceof PlayerRef playerRef) {
                return playerRef;
            }
            return null;
        }
        if (directRef instanceof PlayerRef playerRef) {
            return playerRef;
        }
        return null;
    }

    private static Object invokeSenderMethod(CommandContext context, String methodName) {
        try {
            Method method = context.getClass().getMethod(methodName);
            return method.invoke(context);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object invokePlayerReference(Object player) {
        for (String methodName : new String[]{"getReference", "reference", "getRef"}) {
            try {
                Method method = player.getClass().getMethod(methodName);
                return method.invoke(player);
            } catch (ReflectiveOperationException ignored) {
                // try next
            }
        }
        return null;
    }

    private static Boolean resolveContextPermission(CommandContext context, String permission) {
        try {
            Method method = context.getClass().getMethod("hasPermission", String.class);
            Object result = method.invoke(context, permission);
            if (result instanceof Boolean value) {
                return value;
            }
        } catch (ReflectiveOperationException ignored) {
            // ignore
        }
        return null;
    }

    private static Boolean resolvePlayerPermission(PlayerRef sender, String permission) {
        try {
            Method method = sender.getClass().getMethod("hasPermission", String.class);
            Object result = method.invoke(sender, permission);
            if (result instanceof Boolean value) {
                return value;
            }
        } catch (ReflectiveOperationException ignored) {
            // ignore
        }
        return null;
    }

    private static boolean resolveOpStatus(PlayerRef sender) {
        for (String methodName : new String[]{"isOperator", "isOp", "isAdmin"}) {
            try {
                Method method = sender.getClass().getMethod(methodName);
                Object result = method.invoke(sender);
                if (result instanceof Boolean value && value) {
                    return true;
                }
            } catch (ReflectiveOperationException ignored) {
                // try next
            }
        }
        return false;
    }
}
