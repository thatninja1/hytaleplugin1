package com.hypixel.hytale.server.core.commands.args;

/**
 * Stubbed optional argument for CI compilation only.
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
