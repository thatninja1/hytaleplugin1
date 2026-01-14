package com.example.plugin.economy;

import java.math.BigDecimal;

/**
 * Represents configuration for the economy plugin.
 */
public record EconomyConfig(
        BigDecimal startingBalance,
        String currencyName,
        String currencySymbol,
        long autosaveIntervalSeconds
) {
}
