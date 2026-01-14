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
        return resolveArgType("PLAYER", "PLAYER_REF", "PLAYERREF", "PLAYER_REFERENCE");
    }

    static Object stringArgType() {
        return resolveArgType("STRING", "TEXT");
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

    private static Object resolveArgType(String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                return ArgTypes.class.getField(fieldName).get(null);
            } catch (NoSuchFieldException ignored) {
                // try next field
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Unable to access ArgTypes." + fieldName, exception);
            }
        }
        throw new IllegalStateException("No compatible ArgTypes field found for " + String.join(", ", fieldNames) + ".");
    }
}
