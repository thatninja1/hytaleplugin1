package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.server.core.Message;

public class CommandContext {
    public CommandSender sender() {
        return new CommandSender();
    }

    public Object get(Object arg) {
        return null;
    }

    public void sendMessage(Message message) {
    }
}
