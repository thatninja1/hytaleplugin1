package com.example.plugin;

import com.example.plugin.commands.BalanceCommand;
import com.example.plugin.commands.EcoCommand;
import com.example.plugin.commands.PayCommand;
import com.example.plugin.economy.ConfigLoader;
import com.example.plugin.economy.EconomyConfig;
import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.storage.JsonStorageProvider;
import com.example.plugin.economy.storage.StorageProvider;
import com.example.plugin.listeners.PlayerReadyListener;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import org.checkerframework.checker.nullness.qual.NonNull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EconomyPlugin extends JavaPlugin {
    private static final Logger LOGGER = Logger.getLogger(EconomyPlugin.class.getName());
    private EconomyService economyService;
    private ScheduledExecutorService autosaveScheduler;

    public EconomyPlugin(@NonNull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        Path dataDirectory = resolveDataDirectory();
        ensureDirectory(dataDirectory);

        EconomyConfig config = new ConfigLoader(dataDirectory, LOGGER).load();
        StorageProvider storageProvider = new JsonStorageProvider(dataDirectory.resolve("balances.json"), LOGGER);
        this.economyService = new EconomyService(storageProvider, config.startingBalance(), LOGGER);
        this.economyService.loadBalances();

        registerCommands(config);
        registerEvents();
        scheduleAutosave(config);

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownSafely));
    }

    private void registerCommands(EconomyConfig config) {
        this.getCommandRegistry().registerCommand(new BalanceCommand(economyService, config));
        this.getCommandRegistry().registerCommand(new PayCommand(economyService, config));
        this.getCommandRegistry().registerCommand(new EcoCommand(economyService, config, LOGGER));
    }

    private void registerEvents() {
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class,
                event -> PlayerReadyListener.onPlayerReady(event, economyService));
    }

    private void scheduleAutosave(EconomyConfig config) {
        long autosaveInterval = config.autosaveIntervalSeconds();
        if (autosaveInterval <= 0) {
            LOGGER.info("Economy autosave is disabled by configuration.");
            return;
        }

        this.autosaveScheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "economy-autosave");
            thread.setDaemon(true);
            return thread;
        });
        this.autosaveScheduler.scheduleAtFixedRate(
                () -> economyService.saveBalances(),
                autosaveInterval,
                autosaveInterval,
                TimeUnit.SECONDS);
        LOGGER.info("Economy autosave scheduled every " + Duration.ofSeconds(autosaveInterval) + ".");
    }

    private Path resolveDataDirectory() {
        return Path.of("plugins", "economy");
    }

    private void ensureDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed to create economy data folder at " + directory + ".", exception);
        }
    }

    private void shutdownSafely() {
        if (economyService != null) {
            economyService.saveBalances();
        }
        ScheduledExecutorService scheduler = autosaveScheduler;
        if (Objects.nonNull(scheduler)) {
            scheduler.shutdown();
        }
    }
}
