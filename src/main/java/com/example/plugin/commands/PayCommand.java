package com.example.plugin.commands;

import com.example.plugin.economy.EconomyConfig;
import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.formatter.CurrencyFormatter;
import com.hypixel.hytale.server.core.commands.AbstractCommand;
import com.hypixel.hytale.server.core.commands.CommandContext;
import com.hypixel.hytale.server.core.commands.args.ArgTypes;
import com.hypixel.hytale.server.core.commands.args.RequiredArg;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import org.checkerframework.checker.nullness.qual.NonNull;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PayCommand extends AbstractCommand {
    @NonNull
    private final RequiredArg<PlayerRef> playerArg;
    @NonNull
    private final RequiredArg<BigDecimal> amountArg;
    private final EconomyService economyService;
    private final CurrencyFormatter formatter;

    public PayCommand(EconomyService economyService, EconomyConfig config) {
        super("pay", "Pay another player");
        this.economyService = economyService;
        this.formatter = new CurrencyFormatter(config);
        this.playerArg = this.withRequiredArg("player", ArgTypes.PLAYER_REF);
        this.amountArg = this.withRequiredArg("amount", ArgTypes.DECIMAL);
    }

    @Override
    protected CompletableFuture<Void> execute(@NonNull CommandContext context) {
        PlayerRef senderRef = CommandUtil.requirePlayer(context);
        if (senderRef == null) {
            return CompletableFuture.completedFuture(null);
        }
        economyService.updatePlayerName(senderRef.getUuid(), senderRef.getDisplayName());

        PlayerRef target = context.getArg(this.playerArg);
        if (target == null) {
            context.sender().sendMessage("Player not found.");
            return CompletableFuture.completedFuture(null);
        }
        economyService.updatePlayerName(target.getUuid(), target.getDisplayName());
        BigDecimal amount = context.getArg(this.amountArg);
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

}
