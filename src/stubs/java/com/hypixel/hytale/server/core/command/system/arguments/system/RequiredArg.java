package com.hypixel.hytale.server.core.command.system.arguments.system;

public class RequiredArg<T> {
    private final String name;

    public RequiredArg(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
