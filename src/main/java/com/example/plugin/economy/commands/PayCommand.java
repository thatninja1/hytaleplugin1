package com.example.plugin.economy.commands;

import com.example.plugin.economy.EconomyService;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.math.BigDecimal;
import javax.annotation.Nonnull;

public class PayCommand extends CommandBase {
    private final EconomyService economyService;
    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> amountArg;

    public PayCommand(EconomyService economyService) {
        super("pay", "Pay another player");
        this.economyService = economyService;
        this.playerArg = this.withRequiredArg("player", "com.example.plugin.commands.pay.arg.player", ArgTypes.STRING);
        this.amountArg = this.withRequiredArg("amount", "com.example.plugin.commands.pay.arg.amount", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        Object sender = context.sender();
        if (!(sender instanceof PlayerRef)) {
            context.sendMessage(Message.raw("Only players can use /pay."));
            return;
        }

        String targetName = (String) context.get(this.playerArg);
        String senderName = ((PlayerRef) sender).getUsername();
        if (targetName.equalsIgnoreCase(senderName)) {
            context.sendMessage(Message.raw("You cannot pay yourself."));
            return;
        }

        BigDecimal amount = EconomyService.parseAmount((String) context.get(this.amountArg));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            context.sendMessage(Message.raw("Amount must be a positive number."));
            return;
        }

        BigDecimal senderBalance = this.economyService.getBalance(senderName);
        if (senderBalance.compareTo(amount) < 0) {
            context.sendMessage(Message.raw("You do not have enough funds."));
            return;
        }

        boolean success = this.economyService.transfer(senderName, targetName, amount);
        if (!success) {
            context.sendMessage(Message.raw("Transfer failed."));
            return;
        }
        this.economyService.save();

        String formattedAmount = EconomyService.formatAmount(amount);
        context.sendMessage(Message.raw("You paid " + targetName + " $" + formattedAmount + "."));
    }
}
