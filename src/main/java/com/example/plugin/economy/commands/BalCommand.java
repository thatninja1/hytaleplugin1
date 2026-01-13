package com.example.plugin.economy.commands;

import com.example.plugin.economy.EconomyService;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.math.BigDecimal;
import javax.annotation.Nonnull;

public class BalCommand extends CommandBase {
    private final EconomyService economyService;
    private final OptionalArg<String> playerArg;

    public BalCommand(EconomyService economyService) {
        super("bal", "Show your balance");
        this.economyService = economyService;
        this.playerArg = this.withOptionalArg("player", "com.example.plugin.commands.bal.arg.player", ArgTypes.STRING);
        this.addAliases("balance");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        String target = (String) context.get(this.playerArg);
        if (target == null) {
            Object sender = context.sender();
            if (!(sender instanceof PlayerRef)) {
                context.sendMessage(Message.raw("Console must specify a player."));
                return;
            }
            target = ((PlayerRef) sender).getUsername();
        }

        BigDecimal balance = this.economyService.getBalance(target);
        String formatted = EconomyService.formatAmount(balance);
        if (!this.economyService.hasRecordedBalance(target)) {
            context.sendMessage(Message.raw("No data for " + target + ". Balance: $" + formatted));
            return;
        }
        context.sendMessage(Message.raw("Balance for " + target + ": $" + formatted));
    }
}
