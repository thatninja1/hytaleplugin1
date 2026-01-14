package com.example.plugin.commands;

import com.example.plugin.economy.EconomyConfig;
import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.formatter.CurrencyFormatter;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BaltopCommand extends CommandBase {
    private static final int DEFAULT_LIMIT = 10;

    private final EconomyService economyService;
    private final CurrencyFormatter formatter;

    public BaltopCommand(EconomyService economyService, EconomyConfig config) {
        super("baltop", "Show the top balances", false);
        this.economyService = economyService;
        this.formatter = new CurrencyFormatter(config);
    }

    @Override
    protected void executeSync(@NonNull CommandContext context) {
        Map<UUID, com.example.plugin.economy.BalanceEntry> snapshot = economyService.getBalancesSnapshot();
        if (snapshot.isEmpty()) {
            context.sendMessage(Message.raw("No balances have been recorded yet."));
            return;
        }

        Map<UUID, com.example.plugin.economy.BalanceEntry> topBalances = snapshot.entrySet().stream()
                .sorted((left, right) -> right.getValue().balance().compareTo(left.getValue().balance()))
                .limit(DEFAULT_LIMIT)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> first,
                        LinkedHashMap::new));

        context.sendMessage(Message.raw("Top " + topBalances.size() + " balances:"));
        int index = 1;
        for (Map.Entry<UUID, com.example.plugin.economy.BalanceEntry> entry : topBalances.entrySet()) {
            String displayName = resolveDisplayName(entry.getKey(), entry.getValue());
            context.sendMessage(Message.raw(index + ". " + displayName + " - " + formatter.format(entry.getValue().balance())));
            index++;
        }
    }

    private String resolveDisplayName(UUID uuid, com.example.plugin.economy.BalanceEntry entry) {
        if (entry != null && entry.lastKnownName() != null && !entry.lastKnownName().isBlank()) {
            return entry.lastKnownName();
        }
        return uuid.toString();
    }
}
