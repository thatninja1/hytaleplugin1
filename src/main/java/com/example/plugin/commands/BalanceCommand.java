package com.example.plugin.commands;

import com.example.plugin.economy.EconomyConfig;
import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.formatter.CurrencyFormatter;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import org.checkerframework.checker.nullness.qual.NonNull;
import java.math.BigDecimal;
import java.util.UUID;

public class BalanceCommand extends CommandBase {
    private static final String PERMISSION_BALANCE_OTHER = "economy.balance.other";
    @NonNull
    private final OptionalArg<PlayerRef> playerArg;
    private final EconomyService economyService;
    private final CurrencyFormatter formatter;

    public BalanceCommand(EconomyService economyService, EconomyConfig config) {
        super("balance", "View your balance or another player's balance", false);
        this.economyService = economyService;
        this.formatter = new CurrencyFormatter(config);
        this.playerArg = CommandArgResolver.optionalArg(this, "player", "economy.command.balance.player",
                CommandArgResolver.playerArgType());
        this.addAliases("bal");
    }

    @Override
    protected void executeSync(@NonNull CommandContext context) {
        PlayerRef sender = CommandUtil.requirePlayer(context);
        if (sender == null) {
            return;
        }
        economyService.updatePlayerName(sender.getUuid(), sender.getDisplayName());

        PlayerRef target = (PlayerRef) context.get(this.playerArg);
        if (target != null && !sender.equals(target)) {
            if (!CommandUtil.hasPermission(context, PERMISSION_BALANCE_OTHER)) {
                context.sendMessage(Message.raw("You do not have permission to view other balances."));
                return;
            }
            economyService.updatePlayerName(target.getUuid(), target.getDisplayName());
        } else {
            target = sender;
        }

        UUID targetId = target.getUuid();
        economyService.ensureAccount(targetId);
        BigDecimal balance = economyService.getBalance(targetId);
        context.sendMessage(Message.raw(target.getDisplayName() + " has " + formatter.format(balance) + "."));
    }

}
