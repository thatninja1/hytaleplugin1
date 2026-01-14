package com.hypixel.hytale.server.core.command.system.arguments.system;

/**
 * Stubbed required argument type for CI compilation only.
 */
public class RequiredArg<T> {
    private final String name;

    public RequiredArg(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
