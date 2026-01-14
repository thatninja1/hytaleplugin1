package com.example.plugin.economy.storage;

import com.example.plugin.economy.BalanceEntry;

import java.util.Map;
import java.util.UUID;

/**
 * Defines persistence operations for player balances.
 */
public interface StorageProvider {
    /**
     * Loads balances from persistent storage.
     */
    Map<UUID, BalanceEntry> loadBalances();

    /**
     * Saves balances to persistent storage.
     */
    void saveBalances(Map<UUID, BalanceEntry> balances);
}
