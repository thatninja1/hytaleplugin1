package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;

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

    protected AbstractCommand(String name, String description, boolean requiresConfirmation) {
        this.name = name;
        this.description = description;
    }

    protected abstract CompletableFuture<Void> execute(CommandContext context);

    protected void requirePermission(String permission) {
        // no-op stub
    }

    protected <T> RequiredArg<T> withRequiredArg(String name) {
        return new RequiredArg<>(name);
    }

    protected <T> RequiredArg<T> withRequiredArg(String name, Object type) {
        return new RequiredArg<>(name);
    }

    protected <T> OptionalArg<T> withOptionalArg(String name) {
        return new OptionalArg<>(name);
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
