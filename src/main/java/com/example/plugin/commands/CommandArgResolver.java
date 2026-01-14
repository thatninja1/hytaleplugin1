package com.example.plugin.commands;

import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

final class CommandArgResolver {
    private CommandArgResolver() {
    }

    static Object playerArgType() {
        return resolveArgType(new String[]{"PLAYER", "PLAYER_REF", "PLAYERREF", "PLAYER_REFERENCE"},
                new String[]{"player", "playerRef", "playerReference"});
    }

    static Object stringArgType() {
        return resolveArgType(new String[]{"STRING", "TEXT"},
                new String[]{"string", "text"});
    }

    @SuppressWarnings("unchecked")
    static <T> OptionalArg<T> optionalArg(CommandBase command, String name, String translationKey, Object argType) {
        return (OptionalArg<T>) invokeArgBuilder(command, "withOptionalArg", name, translationKey, argType);
    }

    @SuppressWarnings("unchecked")
    static <T> RequiredArg<T> requiredArg(CommandBase command, String name, String translationKey, Object argType) {
        return (RequiredArg<T>) invokeArgBuilder(command, "withRequiredArg", name, translationKey, argType);
    }

    private static Object invokeArgBuilder(CommandBase command, String methodName, String name, String translationKey,
            Object argType) {
        Method candidate = Arrays.stream(command.getClass().getMethods())
                .filter(method -> method.getName().equals(methodName))
                .filter(method -> method.getParameterCount() == 2 || method.getParameterCount() == 3)
                .filter(method -> method.getParameterTypes()[0].equals(String.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No compatible " + methodName + " overload found."));

        try {
            if (!Modifier.isPublic(candidate.getModifiers())) {
                candidate.setAccessible(true);
            }
            if (candidate.getParameterCount() == 2) {
                return candidate.invoke(command, name, argType);
            }
            return candidate.invoke(command, name, translationKey, argType);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke " + methodName + " for argument registration.", exception);
        }
    }

    private static Object resolveArgType(String[] fieldNames, String[] methodNames) {
        for (String methodName : methodNames) {
            try {
                Method method = ArgTypes.class.getMethod(methodName);
                if (Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 0) {
                    return method.invoke(null);
                }
            } catch (NoSuchMethodException ignored) {
                // try next method
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to invoke ArgTypes." + methodName + "()", exception);
            }
        }
        for (String fieldName : fieldNames) {
            try {
                return ArgTypes.class.getField(fieldName).get(null);
            } catch (NoSuchFieldException ignored) {
                // try next field
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Unable to access ArgTypes." + fieldName, exception);
            }
        }
        throw new IllegalStateException("No compatible ArgTypes member found for " + String.join(", ", fieldNames) + ".");
    }
}
