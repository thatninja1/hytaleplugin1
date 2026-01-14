package com.example.plugin.economy.formatter;

import com.example.plugin.economy.EconomyConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyFormatter {
    private final EconomyConfig config;

    public CurrencyFormatter(EconomyConfig config) {
        this.config = config;
    }

    /**
     * Formats an amount using the configured currency symbol and name.
     */
    public String format(BigDecimal amount) {
        BigDecimal normalized = amount.setScale(2, RoundingMode.DOWN);
        return config.currencySymbol() + normalized.toPlainString() + " " + config.currencyName();
    }
}
