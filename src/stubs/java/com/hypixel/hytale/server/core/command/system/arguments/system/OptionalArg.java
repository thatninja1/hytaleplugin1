package com.hypixel.hytale.server.core.command.system.arguments.system;

public class OptionalArg<T> {
    private final String name;

    public OptionalArg(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
