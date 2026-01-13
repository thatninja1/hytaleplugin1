package com.example.plugin.economy.commands;

import com.example.plugin.economy.EconomyPermissions;
import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.PermissionUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import java.math.BigDecimal;
import javax.annotation.Nonnull;

public class GiveCommand extends CommandBase {
    private final EconomyService economyService;
    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> amountArg;

    public GiveCommand(EconomyService economyService) {
        super("give", "Give money to a player");
        this.economyService = economyService;
        this.playerArg = this.withRequiredArg("player", "com.example.plugin.commands.give.arg.player", ArgTypes.STRING);
        this.amountArg = this.withRequiredArg("amount", "com.example.plugin.commands.give.arg.amount", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        if (!PermissionUtil.hasPermission(context, EconomyPermissions.GIVE)) {
            context.sendMessage(Message.raw("You do not have permission to use /give."));
            return;
        }

        String targetName = (String) context.get(this.playerArg);
        BigDecimal amount = EconomyService.parseAmount((String) context.get(this.amountArg));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            context.sendMessage(Message.raw("Amount must be a positive number."));
            return;
        }

        this.economyService.addBalance(targetName, amount);
        this.economyService.save();

        String formattedAmount = EconomyService.formatAmount(amount);
        context.sendMessage(Message.raw("Gave $" + formattedAmount + " to " + targetName + "."));
    }
}
