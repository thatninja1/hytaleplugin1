package com.example.plugin.economy.storage;

import com.example.plugin.economy.BalanceEntry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

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
    private static final java.lang.reflect.Type BALANCE_TYPE = new TypeToken<Map<String, BalanceEntry>>() {
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
    public Map<UUID, BalanceEntry> loadBalances() {
        if (Files.notExists(storagePath)) {
            return new HashMap<>();
        }
        try {
            String json = Files.readString(storagePath, StandardCharsets.UTF_8);
            JsonObject root = gson.fromJson(json, JsonObject.class);
            if (root == null) {
                return new HashMap<>();
            }
            Map<UUID, BalanceEntry> result = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                try {
                    UUID uuid = UUID.fromString(entry.getKey());
                    BalanceEntry balanceEntry = parseEntry(entry.getValue());
                    if (balanceEntry != null) {
                        result.put(uuid, balanceEntry);
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
    public void saveBalances(Map<UUID, BalanceEntry> balances) {
        Map<String, BalanceEntry> rawData = new HashMap<>();
        for (Map.Entry<UUID, BalanceEntry> entry : balances.entrySet()) {
            rawData.put(entry.getKey().toString(), entry.getValue());
        }
        try {
            Files.writeString(storagePath, gson.toJson(rawData), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            logger.log(Level.WARNING, "Failed to save balances.json.", exception);
        }
    }

    private BalanceEntry parseEntry(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            try {
                BigDecimal balance = element.getAsBigDecimal();
                if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                    return new BalanceEntry(balance, null);
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            BigDecimal balance = BigDecimal.ZERO;
            if (object.has("balance")) {
                try {
                    balance = object.get("balance").getAsBigDecimal();
                } catch (NumberFormatException ignored) {
                    balance = BigDecimal.ZERO;
                }
            }
            String name = null;
            if (object.has("lastKnownName") && !object.get("lastKnownName").isJsonNull()) {
                name = object.get("lastKnownName").getAsString();
            }
            if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                return new BalanceEntry(balance, name);
            }
        }
        return null;
    }
}
