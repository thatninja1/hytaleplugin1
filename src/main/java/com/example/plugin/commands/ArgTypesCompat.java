package com.example.plugin.commands;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class ArgTypesCompat {
    private static final String ARG_TYPES_CLASS =
            "com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes";

    Object findPlayerType() {
        return findArgType("PLAYER", "PLAYER_REF", "PLAYER_REFERENCE", "PLAYERREF");
    }

    Object findStringType() {
        return findArgType("STRING", "TEXT", "WORD", "NAME", "PLAYER_NAME");
    }

    Object findDecimalType() {
        return findArgType("DECIMAL", "BIG_DECIMAL", "BIGDECIMAL", "DOUBLE", "FLOAT", "NUMBER",
                "INTEGER", "INT", "LONG");
    }

    private Object findArgType(String... fieldNames) {
        Class<?> argTypesClass = loadArgTypesClass();
        if (argTypesClass == null) {
            return null;
        }
        for (String fieldName : fieldNames) {
            try {
                Field field = argTypesClass.getField(fieldName);
                if (Modifier.isStatic(field.getModifiers())) {
                    return field.get(null);
                }
            } catch (NoSuchFieldException ignored) {
                // try next field
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }
        return null;
    }

    private Class<?> loadArgTypesClass() {
        try {
            return Class.forName(ARG_TYPES_CLASS);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
