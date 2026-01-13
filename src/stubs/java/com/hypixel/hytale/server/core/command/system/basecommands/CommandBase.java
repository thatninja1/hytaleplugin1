package com.hypixel.hytale.server.core.command.system.basecommands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;

public abstract class CommandBase {
    private final String name;
    private final String description;

    protected CommandBase(String name, String description) {
        this(name, description, true);
    }

    protected CommandBase(String name, String description, boolean visible) {
        this.name = name;
        this.description = description;
    }

    protected abstract void executeSync(CommandContext context);

    protected <T> RequiredArg<T> withRequiredArg(String name, String key, Object type) {
        return new RequiredArg<>(name);
    }

    protected <T> OptionalArg<T> withOptionalArg(String name, String key, Object type) {
        return new OptionalArg<>(name);
    }

    protected <T> DefaultArg<T> withDefaultArg(String name, String key, Object type, T defaultValue, String defaultKey) {
        return new DefaultArg<>(name);
    }

    protected void addAliases(String... aliases) {
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }
}
