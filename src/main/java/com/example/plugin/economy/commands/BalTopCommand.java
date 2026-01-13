package com.example.plugin.economy.commands;

import com.example.plugin.economy.EconomyPermissions;
import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.PermissionUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import java.util.List;
import javax.annotation.Nonnull;

public class BalTopCommand extends CommandBase {
    private static final int MAX_LIMIT = 50;

    private final EconomyService economyService;
    private final OptionalArg<Integer> limitArg;

    public BalTopCommand(EconomyService economyService) {
        super("baltop", "Show top balances");
        this.economyService = economyService;
        this.limitArg = this.withOptionalArg("count", "com.example.plugin.commands.baltop.arg.count", ArgTypes.INTEGER);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        if (!PermissionUtil.hasPermission(context, EconomyPermissions.BALTOP)) {
            context.sendMessage(Message.raw("You do not have permission to use /baltop."));
            return;
        }

        Integer requested = (Integer) context.get(this.limitArg);
        int limit = requested == null ? this.economyService.getTopCacheSize() : requested;
        if (limit <= 0) {
            limit = this.economyService.getTopCacheSize();
        }
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        if (requested != null) {
            this.economyService.setTopCacheSize(limit);
            this.economyService.save();
        }

        List<EconomyService.BalanceEntry> topEntries = this.economyService.getTop(limit);
        context.sendMessage(Message.raw("Top " + topEntries.size() + " balances:"));
        int index = 1;
        for (EconomyService.BalanceEntry entry : topEntries) {
            String formatted = EconomyService.formatAmount(entry.amount());
            context.sendMessage(Message.raw(index + ". " + entry.playerId() + " - $" + formatted));
            index++;
        }
    }
}
