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
import java.util.UUID;

public class PayCommand extends CommandBase {
    @NonNull
    private final RequiredArg<PlayerRef> playerArg;
    @NonNull
    private final RequiredArg<String> amountArg;
    private final EconomyService economyService;
    private final CurrencyFormatter formatter;

    public PayCommand(EconomyService economyService, EconomyConfig config) {
        super("pay", "Pay another player", false);
        this.economyService = economyService;
        this.formatter = new CurrencyFormatter(config);
        this.playerArg = CommandArgResolver.requiredArg(this, "player", "economy.command.pay.player",
                CommandArgResolver.playerArgType());
        this.amountArg = CommandArgResolver.requiredArg(this, "amount", "economy.command.pay.amount",
                CommandArgResolver.stringArgType());
    }

    @Override
    protected void executeSync(@NonNull CommandContext context) {
        PlayerRef sender = CommandUtil.requirePlayer(context);
        if (sender == null) {
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

        UUID senderId = sender.getUuid();
        UUID targetId = target.getUuid();
        EconomyService.TransferResult result = economyService.transfer(senderId, targetId, amount);
        if (!result.success()) {
            context.sendMessage(Message.raw(result.message()));
            return;
        }

        context.sendMessage(Message.raw("You paid " + target.getDisplayName() + " "
                + formatter.format(amount) + "."));
        target.sendMessage(Message.raw(sender.getDisplayName() + " paid you "
                + formatter.format(amount) + "."));
    }

    private BigDecimal parseAmount(String input, CommandContext context) {
        try {
            BigDecimal amount = new BigDecimal(input);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                context.sendMessage(Message.raw("Amount must be greater than zero."));
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
