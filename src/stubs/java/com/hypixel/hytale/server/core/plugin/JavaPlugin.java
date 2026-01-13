package com.hypixel.hytale.server.core.plugin;

import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.event.EventRegistry;

import java.io.File;
import java.util.logging.Logger;

public abstract class JavaPlugin {
    private final JavaPluginInit init;
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final CommandRegistry commandRegistry = new CommandRegistry();
    private final EventRegistry eventRegistry = new EventRegistry();
    private final File dataFolder = new File("data");

    protected JavaPlugin(JavaPluginInit init) {
        this.init = init;
    }

    protected abstract void setup();

    protected void teardown() {
    }

    public JavaPluginInit getInit() {
        return this.init;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public CommandRegistry getCommandRegistry() {
        return this.commandRegistry;
    }

    public EventRegistry getEventRegistry() {
        return this.eventRegistry;
    }

    public File getDataFolder() {
        return this.dataFolder;
    }
}
