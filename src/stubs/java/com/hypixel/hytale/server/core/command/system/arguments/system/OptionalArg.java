package com.hypixel.hytale.server.core.command.system.arguments.system;

/**
 * Stubbed optional argument type for CI compilation only.
 */
public class OptionalArg<T> {
    private final String name;

    public OptionalArg(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
