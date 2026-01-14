package com.example.plugin.commands;

import com.example.plugin.economy.EconomyConfig;
import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.formatter.CurrencyFormatter;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import org.checkerframework.checker.nullness.qual.NonNull;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BalanceCommand extends AbstractCommand {
    private static final String PERMISSION_BALANCE_OTHER = "economy.balance.other";
    @NonNull
    private final OptionalArg<String> playerArg;
    private final EconomyService economyService;
    private final CurrencyFormatter formatter;
    private final PlayerLookup playerLookup;

    public BalanceCommand(EconomyService economyService, EconomyConfig config, PlayerLookup playerLookup) {
        super("balance", "View your balance or another player's balance");
        this.economyService = economyService;
        this.formatter = new CurrencyFormatter(config);
        this.playerArg = this.withOptionalArg("player");
        this.playerLookup = playerLookup;
        this.addAliases("bal");
    }

    @Override
    protected CompletableFuture<Void> execute(@NonNull CommandContext context) {
        PlayerRef playerRef = CommandUtil.requirePlayer(context);
        if (playerRef == null) {
            return CompletableFuture.completedFuture(null);
        }
        economyService.updatePlayerName(playerRef.getUuid(), playerRef.getDisplayName());

        String targetName = context.getArgOrNull(this.playerArg);
        PlayerRef target = null;
        if (targetName != null && !targetName.isBlank()) {
            target = playerLookup.findOnlinePlayer(targetName);
            if (target == null) {
                context.sender().sendMessage("Player not found.");
                return CompletableFuture.completedFuture(null);
            }
        }
        if (target != null && !playerRef.getUuid().equals(target.getUuid())) {
            if (!context.sender().hasPermission(PERMISSION_BALANCE_OTHER)) {
                context.sender().sendMessage("You do not have permission to view other balances.");
                return CompletableFuture.completedFuture(null);
            }
            economyService.updatePlayerName(target.getUuid(), target.getDisplayName());
        } else {
            target = playerRef;
        }

        UUID targetId = target.getUuid();
        economyService.ensureAccount(targetId);
        BigDecimal balance = economyService.getBalance(targetId);
        context.sender().sendMessage(target.getDisplayName() + " has " + formatter.format(balance) + ".");
        return CompletableFuture.completedFuture(null);
    }

}
