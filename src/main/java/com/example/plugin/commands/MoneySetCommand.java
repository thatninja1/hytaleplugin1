package com.example.plugin.commands;

import com.example.plugin.economy.EconomyConfig;
import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.formatter.CurrencyFormatter;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import org.checkerframework.checker.nullness.qual.NonNull;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class MoneySetCommand extends AbstractCommand {
    private static final String PERMISSION_ADMIN = "economy.money.set";

    @NonNull
    private final RequiredArg<PlayerRef> playerArg;
    @NonNull
    private final RequiredArg<BigDecimal> amountArg;
    private final EconomyService economyService;
    private final CurrencyFormatter formatter;

    public MoneySetCommand(EconomyService economyService, EconomyConfig config) {
        super("moneyset", "Set a player's balance");
        requirePermission(PERMISSION_ADMIN);
        this.economyService = economyService;
        this.formatter = new CurrencyFormatter(config);
        this.playerArg = this.withRequiredArg("player", ArgTypes.PLAYER);
        this.amountArg = this.withRequiredArg("amount", ArgTypes.DECIMAL);
    }

    @Override
    protected CompletableFuture<Void> execute(@NonNull CommandContext context) {
        PlayerRef target = context.get(this.playerArg);
        if (target == null) {
            context.sender().sendMessage("That player could not be found.");
            return CompletableFuture.completedFuture(null);
        }
        economyService.updatePlayerName(target.getUuid(), target.getDisplayName());

        BigDecimal amount = toBigDecimal(context.get(this.amountArg));
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

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return null;
    }
}
