package com.example.plugin.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads and saves the economy configuration.
 */
public class ConfigLoader {
    private static final EconomyConfig DEFAULT_CONFIG = new EconomyConfig(
            new BigDecimal("100.00"),
            "Coins",
            "$",
            300
    );

    private final Path configPath;
    private final Logger logger;
    private final Gson gson;

    public ConfigLoader(Path dataDirectory, Logger logger) {
        this.configPath = dataDirectory.resolve("config.json");
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Loads the economy configuration from disk, creating defaults if missing.
     */
    public EconomyConfig load() {
        if (Files.notExists(configPath)) {
            save(DEFAULT_CONFIG);
            return DEFAULT_CONFIG;
        }

        try {
            String json = Files.readString(configPath, StandardCharsets.UTF_8);
            EconomyConfig config = gson.fromJson(json, EconomyConfig.class);
            return normalize(config);
        } catch (JsonParseException | IllegalStateException exception) {
            logger.log(Level.WARNING, "Economy config is corrupted. Falling back to defaults.", exception);
            return DEFAULT_CONFIG;
        } catch (Exception exception) {
            logger.log(Level.WARNING, "Failed to read economy config. Falling back to defaults.", exception);
            return DEFAULT_CONFIG;
        }
    }

    private EconomyConfig normalize(EconomyConfig config) {
        if (config == null) {
            return DEFAULT_CONFIG;
        }
        BigDecimal startingBalance = config.startingBalance() == null
                ? DEFAULT_CONFIG.startingBalance()
                : config.startingBalance();
        if (startingBalance.compareTo(BigDecimal.ZERO) < 0) {
            startingBalance = DEFAULT_CONFIG.startingBalance();
        }
        String currencyName = isBlank(config.currencyName())
                ? DEFAULT_CONFIG.currencyName()
                : config.currencyName();
        String currencySymbol = isBlank(config.currencySymbol())
                ? DEFAULT_CONFIG.currencySymbol()
                : config.currencySymbol();
        long autosaveInterval = config.autosaveIntervalSeconds() <= 0
                ? DEFAULT_CONFIG.autosaveIntervalSeconds()
                : config.autosaveIntervalSeconds();
        EconomyConfig normalized = new EconomyConfig(startingBalance, currencyName, currencySymbol, autosaveInterval);
        save(normalized);
        return normalized;
    }

    private void save(EconomyConfig config) {
        try {
            Files.writeString(configPath, gson.toJson(config), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            logger.log(Level.WARNING, "Failed to write economy config to disk.", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
