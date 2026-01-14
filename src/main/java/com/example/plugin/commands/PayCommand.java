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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PayCommand extends AbstractCommand {
    @NonNull
    private final RequiredArg<Object> playerArg;
    @NonNull
    private final RequiredArg<Object> amountArg;
    private final EconomyService economyService;
    private final CurrencyFormatter formatter;
    private final PlayerLookup playerLookup;
    private final boolean amountArgIsText;

    public PayCommand(EconomyService economyService, EconomyConfig config, PlayerLookup playerLookup) {
        super("pay", "Pay another player");
        this.economyService = economyService;
        this.formatter = new CurrencyFormatter(config);
        ArgTypeResolver resolver = new ArgTypeResolver();
        this.playerArg = this.withRequiredArg("player", resolver.resolvePlayerType().type());
        ArgTypeResolver.ArgType amountType = resolver.resolveDecimalType();
        this.amountArg = this.withRequiredArg("amount", amountType.type());
        this.playerLookup = playerLookup;
        this.amountArgIsText = amountType.nameBased();
    }

    @Override
    protected CompletableFuture<Void> execute(@NonNull CommandContext context) {
        PlayerRef senderRef = CommandUtil.requirePlayer(context);
        if (senderRef == null) {
            return CompletableFuture.completedFuture(null);
        }
        economyService.updatePlayerName(senderRef.getUuid(), senderRef.getDisplayName());

        Object targetValue = context.getArg(this.playerArg);
        PlayerRef target = CommandUtil.resolvePlayer(targetValue, playerLookup);
        if (target == null) {
            context.sender().sendMessage("That player could not be found.");
            return CompletableFuture.completedFuture(null);
        }
        economyService.updatePlayerName(target.getUuid(), target.getDisplayName());
        BigDecimal amount = toBigDecimal(context.getArg(this.amountArg));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            context.sender().sendMessage("Amount must be greater than zero.");
            return CompletableFuture.completedFuture(null);
        }
        if (amount.scale() > 2) {
            context.sender().sendMessage("Amount can only have up to 2 decimal places.");
            return CompletableFuture.completedFuture(null);
        }

        UUID senderId = senderRef.getUuid();
        UUID targetId = target.getUuid();
        EconomyService.TransferResult result = economyService.transfer(senderId, targetId, amount);
        if (!result.success()) {
            context.sender().sendMessage(result.message());
            return CompletableFuture.completedFuture(null);
        }

        context.sender().sendMessage("You paid " + target.getDisplayName() + " "
                + formatter.format(amount) + ".");
        target.sendMessage("You received " + formatter.format(amount)
                + " from " + senderRef.getDisplayName() + ".");
        return CompletableFuture.completedFuture(null);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (amountArgIsText && value instanceof String text) {
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
