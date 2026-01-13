package com.example.plugin.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class EconomyService {
    private static final int DATA_VERSION = 1;

    private final Path dataFile;
    private final Logger logger;
    private final Gson gson;
    private final ReentrantReadWriteLock lock;
    private final Map<String, BigDecimal> balances;
    private int topCacheSize;

    public EconomyService(Path dataFile, Logger logger) {
        this.dataFile = dataFile;
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.lock = new ReentrantReadWriteLock();
        this.balances = new HashMap<>();
        this.topCacheSize = 10;
    }

    public void load() {
        if (!Files.exists(this.dataFile)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(this.dataFile)) {
            EconomyData data = this.gson.fromJson(reader, EconomyData.class);
            if (data == null) {
                return;
            }
            this.lock.writeLock().lock();
            try {
                this.balances.clear();
                if (data.balances != null) {
                    for (Map.Entry<String, BigDecimal> entry : data.balances.entrySet()) {
                        if (entry.getKey() == null || entry.getValue() == null) {
                            continue;
                        }
                        this.balances.put(entry.getKey(), normalizeAmount(entry.getValue()));
                    }
                }
                if (data.topCacheSize > 0) {
                    this.topCacheSize = data.topCacheSize;
                }
            } finally {
                this.lock.writeLock().unlock();
            }
        } catch (IOException ex) {
            this.logger.warning("Failed to load economy data: " + ex.getMessage());
        }
    }

    public void save() {
        EconomyData snapshot = snapshotData();
        try {
            Files.createDirectories(this.dataFile.getParent());
            Path tempFile = Files.createTempFile(this.dataFile.getParent(), "balances", ".tmp");
            try (Writer writer = Files.newBufferedWriter(tempFile)) {
                this.gson.toJson(snapshot, writer);
            }
            try {
                Files.move(tempFile, this.dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ex) {
                Files.move(tempFile, this.dataFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            this.logger.warning("Failed to save economy data: " + ex.getMessage());
        }
    }

    public BigDecimal getBalance(String playerId) {
        Objects.requireNonNull(playerId, "playerId");
        this.lock.readLock().lock();
        try {
            String key = resolveKey(playerId);
            BigDecimal balance = key == null ? null : this.balances.get(key);
            return balance == null ? BigDecimal.ZERO : balance;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public boolean hasRecordedBalance(String playerId) {
        Objects.requireNonNull(playerId, "playerId");
        this.lock.readLock().lock();
        try {
            String key = resolveKey(playerId);
            return key != null;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void setBalance(String playerId, BigDecimal amount) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(amount, "amount");
        this.lock.writeLock().lock();
        try {
            String key = resolveKey(playerId);
            this.balances.put(key == null ? playerId : key, normalizeAmount(amount));
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void addBalance(String playerId, BigDecimal amount) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(amount, "amount");
        this.lock.writeLock().lock();
        try {
            String key = resolveKey(playerId);
            String resolvedKey = key == null ? playerId : key;
            BigDecimal current = this.balances.getOrDefault(resolvedKey, BigDecimal.ZERO);
            this.balances.put(resolvedKey, normalizeAmount(current.add(amount)));
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public boolean transfer(String fromId, String toId, BigDecimal amount) {
        Objects.requireNonNull(fromId, "fromId");
        Objects.requireNonNull(toId, "toId");
        Objects.requireNonNull(amount, "amount");
        this.lock.writeLock().lock();
        try {
            String fromKey = Optional.ofNullable(resolveKey(fromId)).orElse(fromId);
            String toKey = Optional.ofNullable(resolveKey(toId)).orElse(toId);
            BigDecimal fromBalance = this.balances.getOrDefault(fromKey, BigDecimal.ZERO);
            if (fromBalance.compareTo(amount) < 0) {
                return false;
            }
            this.balances.put(fromKey, normalizeAmount(fromBalance.subtract(amount)));
            BigDecimal toBalance = this.balances.getOrDefault(toKey, BigDecimal.ZERO);
            this.balances.put(toKey, normalizeAmount(toBalance.add(amount)));
            return true;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public List<BalanceEntry> getTop(int limit) {
        int capped = Math.max(1, limit);
        this.lock.readLock().lock();
        try {
            List<BalanceEntry> entries = new ArrayList<>();
            for (Map.Entry<String, BigDecimal> entry : this.balances.entrySet()) {
                entries.add(new BalanceEntry(entry.getKey(), entry.getValue()));
            }
            entries.sort(Comparator.comparing(BalanceEntry::amount).reversed().thenComparing(BalanceEntry::playerId));
            return entries.subList(0, Math.min(capped, entries.size()));
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public int getTopCacheSize() {
        this.lock.readLock().lock();
        try {
            return this.topCacheSize;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void setTopCacheSize(int size) {
        this.lock.writeLock().lock();
        try {
            this.topCacheSize = size;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private String resolveKey(String playerId) {
        for (String key : this.balances.keySet()) {
            if (key.equalsIgnoreCase(playerId)) {
                return key;
            }
        }
        return null;
    }

    private EconomyData snapshotData() {
        this.lock.readLock().lock();
        try {
            EconomyData data = new EconomyData();
            data.version = DATA_VERSION;
            data.topCacheSize = this.topCacheSize;
            data.balances = new HashMap<>(this.balances);
            return data;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static BigDecimal parseAmount(String input) {
        try {
            return new BigDecimal(input.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public record BalanceEntry(String playerId, BigDecimal amount) {
    }

    private static class EconomyData {
        int version;
        Map<String, BigDecimal> balances;
        int topCacheSize;
    }
}
