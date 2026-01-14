package com.hypixel.hytale.server.core.commands.args;

/**
 * Stubbed required argument for CI compilation only.
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
