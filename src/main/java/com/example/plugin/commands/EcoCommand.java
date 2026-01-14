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
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;

public class EcoCommand extends AbstractCommand {
    private static final String PERMISSION_ADMIN = "economy.admin";

    @NonNull
    private final RequiredArg<String> actionArg;
    @NonNull
    private final RequiredArg<String> playerArg;
    @NonNull
    private final RequiredArg<String> amountArg;
    private final EconomyService economyService;
    private final CurrencyFormatter formatter;
    private final Logger logger;
    private final PlayerLookup playerLookup;

    public EcoCommand(EconomyService economyService, EconomyConfig config, Logger logger, PlayerLookup playerLookup) {
        super("eco", "Admin economy commands");
        requirePermission(PERMISSION_ADMIN);
        this.economyService = economyService;
        this.formatter = new CurrencyFormatter(config);
        this.logger = logger;
        this.actionArg = this.withRequiredArg("action");
        this.playerArg = this.withRequiredArg("player");
        this.amountArg = this.withRequiredArg("amount");
        this.playerLookup = playerLookup;
    }

    @Override
    protected CompletableFuture<Void> execute(@NonNull CommandContext context) {
        String action = context.getArg(this.actionArg).toLowerCase(Locale.ROOT);
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

        UUID targetId = target.getUuid();
        switch (action) {
            case "give" -> {
                BigDecimal newBalance = economyService.deposit(targetId, amount);
                context.sender().sendMessage("Gave " + formatter.format(amount) + " to "
                        + target.getDisplayName() + ". New balance: " + formatter.format(newBalance) + ".");
            }
            case "take" -> {
                EconomyService.WithdrawalResult result = economyService.withdraw(targetId, amount);
                if (!result.success()) {
                    context.sender().sendMessage(target.getDisplayName() + " does not have enough funds.");
                    return CompletableFuture.completedFuture(null);
                }
                context.sender().sendMessage("Took " + formatter.format(amount) + " from "
                        + target.getDisplayName() + ". New balance: " + formatter.format(result.newBalance()) + ".");
            }
            case "set" -> {
                BigDecimal newBalance = economyService.setBalance(targetId, amount);
                context.sender().sendMessage("Set " + target.getDisplayName() + " to "
                        + formatter.format(newBalance) + ".");
            }
            default -> {
                context.sender().sendMessage("Usage: /eco <give|take|set> <player> <amount>.");
                logger.fine("Invalid eco subcommand: " + action);
            }
        }
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
