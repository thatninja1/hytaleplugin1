package com.example.plugin.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import org.checkerframework.checker.nullness.qual.NonNull;

public class EconDebugCommand extends CommandBase {
    private static final String PERMISSION_ADMIN = "economy.admin";

    public EconDebugCommand() {
        super("econdebug", "Show economy command diagnostics", false);
    }

    @Override
    protected void executeSync(@NonNull CommandContext context) {
        if (!CommandUtil.hasPermission(context, PERMISSION_ADMIN)) {
            context.sendMessage(Message.raw("You do not have permission to use this command."));
            return;
        }
        context.sendMessage(Message.raw("Economy command diagnostics logged to console."));
        CommandUtil.logContextDiagnostics(context, true);
        CommandUtil.logPermissionDiagnostics(context, PERMISSION_ADMIN, true);
    }
}
