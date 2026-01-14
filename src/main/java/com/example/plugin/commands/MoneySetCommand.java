package com.example.plugin.commands;

import com.example.plugin.economy.EconomyConfig;
import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.formatter.CurrencyFormatter;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import org.checkerframework.checker.nullness.qual.NonNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneySetCommand extends CommandBase {
    private static final String PERMISSION_ADMIN = "economy.admin";

    @NonNull
    private final RequiredArg<PlayerRef> playerArg;
    @NonNull
    private final RequiredArg<String> amountArg;
    private final EconomyService economyService;
    private final CurrencyFormatter formatter;

    public MoneySetCommand(EconomyService economyService, EconomyConfig config) {
        super("moneyset", "Set a player's balance", false);
        this.economyService = economyService;
        this.formatter = new CurrencyFormatter(config);
        this.playerArg = CommandArgResolver.requiredArg(this, "player", "economy.command.moneyset.player",
                CommandArgResolver.playerArgType());
        this.amountArg = CommandArgResolver.requiredArg(this, "amount", "economy.command.moneyset.amount",
                CommandArgResolver.stringArgType());
    }

    @Override
    protected void executeSync(@NonNull CommandContext context) {
        if (!CommandUtil.hasPermission(context, PERMISSION_ADMIN)) {
            context.sendMessage(Message.raw("You do not have permission to use this command."));
            return;
        }

        PlayerRef target = (PlayerRef) context.get(this.playerArg);
        if (target == null) {
            context.sendMessage(Message.raw("That player could not be found."));
            return;
        }

        BigDecimal amount = parseAmount((String) context.get(this.amountArg), context);
        if (amount == null) {
            return;
        }

        BigDecimal newBalance = economyService.setBalance(target.getUuid(), amount);
        context.sendMessage(Message.raw("Set " + target.getDisplayName() + " to "
                + formatter.format(newBalance) + "."));
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
