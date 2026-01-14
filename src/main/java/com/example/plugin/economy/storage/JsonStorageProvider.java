package com.example.plugin.economy.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores balances as JSON for readability and easy manual edits.
 */
public class JsonStorageProvider implements StorageProvider {
    private static final Type BALANCE_TYPE = new TypeToken<Map<String, BigDecimal>>() {
    }.getType();

    private final Path storagePath;
    private final Logger logger;
    private final Gson gson;

    public JsonStorageProvider(Path storagePath, Logger logger) {
        this.storagePath = storagePath;
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public Map<UUID, BigDecimal> loadBalances() {
        if (Files.notExists(storagePath)) {
            return new HashMap<>();
        }
        try {
            String json = Files.readString(storagePath, StandardCharsets.UTF_8);
            Map<String, BigDecimal> rawData = gson.fromJson(json, BALANCE_TYPE);
            if (rawData == null) {
                return new HashMap<>();
            }
            Map<UUID, BigDecimal> result = new HashMap<>();
            for (Map.Entry<String, BigDecimal> entry : rawData.entrySet()) {
                try {
                    UUID uuid = UUID.fromString(entry.getKey());
                    BigDecimal balance = entry.getValue();
                    if (balance != null && balance.compareTo(BigDecimal.ZERO) >= 0) {
                        result.put(uuid, balance);
                    }
                } catch (IllegalArgumentException ignored) {
                    logger.warning("Ignoring invalid UUID entry in balances.json: " + entry.getKey());
                }
            }
            return result;
        } catch (JsonParseException exception) {
            logger.log(Level.WARNING, "balances.json is corrupted. Starting with empty balances.", exception);
            return new HashMap<>();
        } catch (Exception exception) {
            logger.log(Level.WARNING, "Failed to load balances.json. Starting with empty balances.", exception);
            return new HashMap<>();
        }
    }

    @Override
    public void saveBalances(Map<UUID, BigDecimal> balances) {
        Map<String, BigDecimal> rawData = new HashMap<>();
        for (Map.Entry<UUID, BigDecimal> entry : balances.entrySet()) {
            rawData.put(entry.getKey().toString(), entry.getValue());
        }
        try {
            Files.writeString(storagePath, gson.toJson(rawData), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            logger.log(Level.WARNING, "Failed to save balances.json.", exception);
        }
    }
}
