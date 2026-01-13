package com.hypixel.hytale.server.core.command.system.arguments.system;

public class DefaultArg<T> {
    private final String name;

    public DefaultArg(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
