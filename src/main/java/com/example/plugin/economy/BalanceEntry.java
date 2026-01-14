package com.example.plugin.economy;

import java.math.BigDecimal;

/**
 * Snapshot of a player's balance and last known name.
 */
public record BalanceEntry(BigDecimal balance, String lastKnownName) {
}
