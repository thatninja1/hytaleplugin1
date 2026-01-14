package com.example.plugin.commands;

import com.example.plugin.economy.EconomyConfig;
import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.formatter.CurrencyFormatter;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import org.checkerframework.checker.nullness.qual.NonNull;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class MoneySetCommand extends AbstractCommand {
    private static final String PERMISSION_ADMIN = "economy.money.set";

    @NonNull
    private final RequiredArg<String> playerArg;
    @NonNull
    private final RequiredArg<String> amountArg;
    private final EconomyService economyService;
    private final CurrencyFormatter formatter;
    private final PlayerLookup playerLookup;

    public MoneySetCommand(EconomyService economyService, EconomyConfig config, PlayerLookup playerLookup) {
        super("moneyset", "Set a player's balance");
        requirePermission(PERMISSION_ADMIN);
        this.economyService = economyService;
        this.formatter = new CurrencyFormatter(config);
        this.playerArg = this.withRequiredArg("player");
        this.amountArg = this.withRequiredArg("amount");
        this.playerLookup = playerLookup;
    }

    @Override
    protected CompletableFuture<Void> execute(@NonNull CommandContext context) {
        String targetName = context.getArg(this.playerArg);
        PlayerRef target = playerLookup.findOnlinePlayer(targetName);
        if (target == null) {
            context.sender().sendMessage("Player not found.");
            return CompletableFuture.completedFuture(null);
        }
        economyService.updatePlayerName(target.getUuid(), target.getDisplayName());

        BigDecimal amount = parseAmount(context.getArg(this.amountArg), context);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            context.sender().sendMessage("Amount cannot be negative.");
            return CompletableFuture.completedFuture(null);
        }
        if (amount.scale() > 2) {
            context.sender().sendMessage("Amount can only have up to 2 decimal places.");
            return CompletableFuture.completedFuture(null);
        }

        BigDecimal newBalance = economyService.setBalance(target.getUuid(), amount);
        context.sender().sendMessage("Set " + target.getDisplayName() + " to "
                + formatter.format(newBalance) + ".");
        return CompletableFuture.completedFuture(null);
    }

    private BigDecimal parseAmount(String value, CommandContext context) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            context.sender().sendMessage("Invalid amount.");
            return null;
        }
    }
}
