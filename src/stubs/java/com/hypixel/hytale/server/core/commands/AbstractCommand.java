package com.hypixel.hytale.server.core.commands;

import com.hypixel.hytale.server.core.commands.args.OptionalArg;
import com.hypixel.hytale.server.core.commands.args.RequiredArg;

import java.util.concurrent.CompletableFuture;

/**
 * Stubbed AbstractCommand for CI compilation only.
 */
public abstract class AbstractCommand {
    private final String name;
    private final String description;

    protected AbstractCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected abstract CompletableFuture<Void> execute(CommandContext context);

    protected void requirePermission(String permission) {
        // no-op stub
    }

    protected <T> RequiredArg<T> withRequiredArg(String name, Object type) {
        return new RequiredArg<>(name);
    }

    protected <T> OptionalArg<T> withOptionalArg(String name, Object type) {
        return new OptionalArg<>(name);
    }

    protected void addAliases(String... aliases) {
        // no-op stub
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }
}
