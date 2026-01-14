package com.example.plugin.economy.storage;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Defines persistence operations for player balances.
 */
public interface StorageProvider {
    /**
     * Loads balances from persistent storage.
     */
    Map<UUID, BigDecimal> loadBalances();

    /**
     * Saves balances to persistent storage.
     */
    void saveBalances(Map<UUID, BigDecimal> balances);
}
