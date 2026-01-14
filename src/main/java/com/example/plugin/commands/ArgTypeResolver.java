package com.example.plugin.commands;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class ArgTypeResolver {
    private static final String ARG_TYPES_CLASS = "com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes";

    ArgType resolvePlayerType() {
        Object playerType = resolveArgType("PLAYER", "PLAYER_REF", "PLAYER_REFERENCE", "PLAYERREF");
        if (playerType != null) {
            return new ArgType(playerType, false);
        }
        Object textType = resolveTextType().type();
        return new ArgType(textType, true);
    }

    ArgType resolveTextType() {
        Object textType = resolveArgType("STRING", "TEXT", "WORD", "NAME", "PLAYER_NAME");
        if (textType == null) {
            throw new IllegalStateException("No compatible ArgTypes text argument found.");
        }
        return new ArgType(textType, true);
    }

    ArgType resolveDecimalType() {
        Object numberType = resolveArgType("DECIMAL", "BIG_DECIMAL", "BIGDECIMAL", "DOUBLE", "FLOAT",
                "NUMBER", "INTEGER", "INT", "LONG");
        if (numberType != null) {
            return new ArgType(numberType, false);
        }
        return new ArgType(resolveTextType().type(), true);
    }

    private Object resolveArgType(String... fieldNames) {
        Class<?> argTypesClass;
        try {
            argTypesClass = Class.forName(ARG_TYPES_CLASS);
        } catch (ClassNotFoundException exception) {
            return null;
        }
        for (String fieldName : fieldNames) {
            try {
                Field field = argTypesClass.getField(fieldName);
                if (Modifier.isStatic(field.getModifiers())) {
                    return field.get(null);
                }
            } catch (NoSuchFieldException ignored) {
                // try next field name
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Unable to access ArgTypes." + fieldName, exception);
            }
        }
        return null;
    }

    record ArgType(Object type, boolean nameBased) {
    }
}
