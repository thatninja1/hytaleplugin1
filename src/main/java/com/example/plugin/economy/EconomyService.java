package com.example.plugin.economy;

import com.example.plugin.economy.storage.StorageProvider;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe economy operations backed by persistent storage.
 */
public class EconomyService {
    private final StorageProvider storageProvider;
    private final BigDecimal startingBalance;
    private final Logger logger;
    private final Map<UUID, BalanceEntry> balances;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * BigDecimal is used here to allow very large balances with decimal precision,
     * avoiding overflow while remaining deterministic for currency operations.
     */
    public EconomyService(StorageProvider storageProvider, BigDecimal startingBalance, Logger logger) {
        this.storageProvider = storageProvider;
        this.startingBalance = startingBalance;
        this.logger = logger;
        this.balances = new ConcurrentHashMap<>();
    }

    /**
     * Loads balances from persistent storage into memory.
     */
    public void loadBalances() {
        Map<UUID, BalanceEntry> loadedBalances = storageProvider.loadBalances();
        lock.writeLock().lock();
        try {
            balances.clear();
            balances.putAll(loadedBalances);
        } finally {
            lock.writeLock().unlock();
        }
        logger.info("Loaded " + loadedBalances.size() + " economy balances.");
    }

    /**
     * Saves balances to persistent storage.
     */
    public void saveBalances() {
        lock.readLock().lock();
        try {
            storageProvider.saveBalances(Map.copyOf(balances));
        } catch (Exception exception) {
            logger.log(Level.WARNING, "Failed to save economy balances.", exception);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the balance for a player, falling back to the starting balance if missing.
     */
    public BigDecimal getBalance(UUID playerId) {
        lock.readLock().lock();
        try {
            BalanceEntry entry = balances.get(playerId);
            return entry == null ? startingBalance : entry.balance();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns a snapshot of all balances for read-only operations.
     */
    public Map<UUID, BalanceEntry> getBalancesSnapshot() {
        lock.readLock().lock();
        try {
            return Map.copyOf(balances);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Ensures a player has an account with at least the starting balance.
     */
    public void ensureAccount(UUID playerId) {
        lock.writeLock().lock();
        try {
            balances.computeIfAbsent(playerId, ignored -> new BalanceEntry(startingBalance, null));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Ensures a player has an account and stores the latest name if provided.
     */
    public void ensureAccount(UUID playerId, String lastKnownName) {
        lock.writeLock().lock();
        try {
            BalanceEntry existing = balances.get(playerId);
            if (existing == null) {
                balances.put(playerId, new BalanceEntry(startingBalance, normalizeName(lastKnownName)));
                return;
            }
            updateNameIfPresent(playerId, existing, lastKnownName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Updates the stored last known name for a player if available.
     */
    public void updatePlayerName(UUID playerId, String lastKnownName) {
        if (normalizeName(lastKnownName) == null) {
            return;
        }
        lock.writeLock().lock();
        try {
            BalanceEntry existing = balances.get(playerId);
            if (existing == null) {
                balances.put(playerId, new BalanceEntry(startingBalance, normalizeName(lastKnownName)));
                return;
            }
            updateNameIfPresent(playerId, existing, lastKnownName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Deposits currency into a player's account.
     */
    public BigDecimal deposit(UUID playerId, BigDecimal amount) {
        validateAmount(amount);
        lock.writeLock().lock();
        try {
            BalanceEntry entry = balances.getOrDefault(playerId, new BalanceEntry(startingBalance, null));
            BigDecimal updated = entry.balance().add(amount);
            balances.put(playerId, new BalanceEntry(updated, entry.lastKnownName()));
            return updated;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Attempts to withdraw currency from a player's account.
     */
    public WithdrawalResult withdraw(UUID playerId, BigDecimal amount) {
        validateAmount(amount);
        lock.writeLock().lock();
        try {
            BalanceEntry entry = balances.getOrDefault(playerId, new BalanceEntry(startingBalance, null));
            BigDecimal balance = entry.balance();
            if (balance.compareTo(amount) < 0) {
                return new WithdrawalResult(false, balance);
            }
            BigDecimal updated = balance.subtract(amount);
            balances.put(playerId, new BalanceEntry(updated, entry.lastKnownName()));
            return new WithdrawalResult(true, updated);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Sets a player's balance to a new value.
     */
    public BigDecimal setBalance(UUID playerId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative.");
        }
        lock.writeLock().lock();
        try {
            BalanceEntry entry = balances.getOrDefault(playerId, new BalanceEntry(startingBalance, null));
            balances.put(playerId, new BalanceEntry(amount, entry.lastKnownName()));
            return amount;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Transfers currency between two players.
     */
    public TransferResult transfer(UUID senderId, UUID targetId, BigDecimal amount) {
        validateAmount(amount);
        if (senderId.equals(targetId)) {
            return new TransferResult(false, "You cannot pay yourself.", null, null);
        }
        lock.writeLock().lock();
        try {
            BalanceEntry senderEntry = balances.getOrDefault(senderId, new BalanceEntry(startingBalance, null));
            BalanceEntry targetEntry = balances.getOrDefault(targetId, new BalanceEntry(startingBalance, null));
            BigDecimal senderBalance = senderEntry.balance();
            if (senderBalance.compareTo(amount) < 0) {
                return new TransferResult(false, "You do not have enough funds.", senderBalance, null);
            }
            BigDecimal targetBalance = targetEntry.balance().add(amount);
            balances.put(senderId, new BalanceEntry(senderBalance.subtract(amount), senderEntry.lastKnownName()));
            balances.put(targetId, new BalanceEntry(targetBalance, targetEntry.lastKnownName()));
            return new TransferResult(true, "Payment sent.", senderBalance.subtract(amount), targetBalance);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void updateNameIfPresent(UUID playerId, BalanceEntry existing, String lastKnownName) {
        String normalized = normalizeName(lastKnownName);
        if (normalized != null && !normalized.equals(existing.lastKnownName())) {
            balances.put(playerId, new BalanceEntry(existing.balance(), normalized));
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return name.trim();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
    }

    public record WithdrawalResult(boolean success, BigDecimal newBalance) {
    }

    public record TransferResult(boolean success, String message, BigDecimal senderBalance, BigDecimal targetBalance) {
    }
}
