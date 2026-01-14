package com.hypixel.hytale.server.core.plugin;

import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.event.EventRegistry;

import java.util.logging.Logger;

/**
 * Stubbed JavaPlugin base for CI compilation only.
 */
public abstract class JavaPlugin {
    private final CommandRegistry commandRegistry = new CommandRegistry();
    private final EventRegistry eventRegistry = new EventRegistry();
    private final Logger logger = Logger.getLogger(getClass().getName());

    protected JavaPlugin(JavaPluginInit init) {
    }

    protected abstract void setup();

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public EventRegistry getEventRegistry() {
        return eventRegistry;
    }

    public Logger getLogger() {
        return logger;
    }
}
