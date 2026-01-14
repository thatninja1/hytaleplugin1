package com.hypixel.hytale.server.core.command.system.basecommands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;

/**
 * Stubbed command base for CI compilation only.
 */
public abstract class CommandBase {
    private final String name;
    private final String description;
    private final boolean requiresPermission;

    protected CommandBase(String name, String description) {
        this(name, description, true);
    }

    protected CommandBase(String name, String description, boolean requiresPermission) {
        this.name = name;
        this.description = description;
        this.requiresPermission = requiresPermission;
    }

    protected <T> RequiredArg<T> withRequiredArg(String name, String key, Object type) {
        return new RequiredArg<>(name);
    }

    protected <T> OptionalArg<T> withOptionalArg(String name, String key, Object type) {
        return new OptionalArg<>(name);
    }

    protected void addAliases(String... aliases) {
        // no-op
    }

    protected abstract void executeSync(CommandContext context);

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public boolean requiresPermission() {
        return requiresPermission;
    }
}
