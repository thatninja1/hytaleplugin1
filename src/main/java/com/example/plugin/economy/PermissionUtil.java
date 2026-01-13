package com.example.plugin.economy;

import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.lang.reflect.Method;

public final class PermissionUtil {
    private PermissionUtil() {
    }

    public static boolean hasPermission(CommandContext context, String permission) {
        Object sender = context.sender();
        if (sender == null) {
            return false;
        }

        Boolean permissionResult = invokeBoolean(sender, "hasPermission", new Class<?>[] { String.class },
                new Object[] { permission });
        if (permissionResult != null) {
            return permissionResult;
        }

        permissionResult = invokeBoolean(sender, "isOp");
        if (permissionResult != null) {
            return permissionResult;
        }

        permissionResult = invokeBoolean(sender, "isAdmin");
        if (permissionResult != null) {
            return permissionResult;
        }

        permissionResult = invokeBoolean(sender, "isConsole");
        if (permissionResult != null) {
            return permissionResult;
        }

        permissionResult = invokeBoolean(sender, "isServer");
        if (permissionResult != null) {
            return permissionResult;
        }

        String className = sender.getClass().getSimpleName().toLowerCase();
        return className.contains("console");
    }

    private static Boolean invokeBoolean(Object target, String methodName) {
        return invokeBoolean(target, methodName, new Class<?>[0], new Object[0]);
    }

    private static Boolean invokeBoolean(Object target, String methodName, Class<?>[] paramTypes, Object[] args) {
        try {
            Method method = target.getClass().getMethod(methodName, paramTypes);
            Object result = method.invoke(target, args);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
        return null;
    }
}
