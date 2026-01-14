package com.example.plugin.commands;

import com.example.plugin.economy.EconomyConfig;
import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.formatter.CurrencyFormatter;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

public class EcoCommand extends CommandBase {
    private static final String PERMISSION_ADMIN = "economy.admin";

    @Nonnull
    private final RequiredArg<String> actionArg;
    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;
    @Nonnull
    private final RequiredArg<String> amountArg;
    private final EconomyService economyService;
    private final CurrencyFormatter formatter;
    private final Logger logger;

    public EcoCommand(EconomyService economyService, EconomyConfig config, Logger logger) {
        super("eco", "Admin economy commands", false);
        this.economyService = economyService;
        this.formatter = new CurrencyFormatter(config);
        this.logger = logger;
        this.actionArg = this.withRequiredArg("action", "economy.command.eco.action", ArgTypes.STRING);
        this.playerArg = this.withRequiredArg("player", "economy.command.eco.player", ArgTypes.PLAYER_REF);
        this.amountArg = this.withRequiredArg("amount", "economy.command.eco.amount", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        if (!CommandUtil.hasPermission(context, PERMISSION_ADMIN)) {
            context.sendMessage(Message.raw("You do not have permission to use this command."));
            return;
        }

        String action = ((String) context.get(this.actionArg)).toLowerCase(Locale.ROOT);
        PlayerRef target = (PlayerRef) context.get(this.playerArg);
        if (target == null) {
            context.sendMessage(Message.raw("That player could not be found."));
            return;
        }
        BigDecimal amount = parseAmount((String) context.get(this.amountArg), context);
        if (amount == null) {
            return;
        }

        UUID targetId = target.getUuid();
        switch (action) {
            case "give" -> {
                BigDecimal newBalance = economyService.deposit(targetId, amount);
                context.sendMessage(Message.raw("Gave " + formatter.format(amount) + " to "
                        + target.getDisplayName() + ". New balance: " + formatter.format(newBalance) + "."));
            }
            case "take" -> {
                EconomyService.WithdrawalResult result = economyService.withdraw(targetId, amount);
                if (!result.success()) {
                    context.sendMessage(Message.raw(target.getDisplayName() + " does not have enough funds."));
                    return;
                }
                context.sendMessage(Message.raw("Took " + formatter.format(amount) + " from "
                        + target.getDisplayName() + ". New balance: " + formatter.format(result.newBalance()) + "."));
            }
            case "set" -> {
                BigDecimal newBalance = economyService.setBalance(targetId, amount);
                context.sendMessage(Message.raw("Set " + target.getDisplayName() + " to "
                        + formatter.format(newBalance) + "."));
            }
            default -> {
                context.sendMessage(Message.raw("Usage: /eco <give|take|set> <player> <amount>."));
                logger.fine("Invalid eco subcommand: " + action);
            }
        }
    }

    private BigDecimal parseAmount(String input, CommandContext context) {
        try {
            BigDecimal amount = new BigDecimal(input);
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                context.sendMessage(Message.raw("Amount cannot be negative."));
                return null;
            }
            if (amount.scale() > 2) {
                context.sendMessage(Message.raw("Amount can only have up to 2 decimal places."));
                return null;
            }
            return amount.setScale(2, RoundingMode.DOWN);
        } catch (NumberFormatException exception) {
            context.sendMessage(Message.raw("Invalid amount. Please enter a number."));
            return null;
        }
    }
}
