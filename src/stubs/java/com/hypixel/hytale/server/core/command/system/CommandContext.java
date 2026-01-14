package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.server.core.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Stubbed command context for CI compilation only.
 */
public class CommandContext {
    private final CommandSender sender;
    private final Map<Object, Object> args = new HashMap<>();

    public CommandContext(CommandSender sender) {
        this.sender = sender;
    }

    public CommandSender sender() {
        return sender;
    }

    public void sendMessage(Message message) {
        // no-op stub
    }


    @SuppressWarnings("unchecked")
    public <T> T get(Object arg) {
        return (T) args.get(arg);
    }

    public <T> T getArg(Object arg) {
        return get(arg);
    }

    public <T> T getArgOrNull(Object arg) {
        return get(arg);
    }

    public void put(Object arg, Object value) {
        args.put(arg, value);
    }
}
