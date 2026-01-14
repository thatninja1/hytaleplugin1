package com.hypixel.hytale.server.core.commands;

import com.hypixel.hytale.server.core.commands.args.OptionalArg;
import com.hypixel.hytale.server.core.commands.args.RequiredArg;

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

    @SuppressWarnings("unchecked")
    public <T> T getArg(RequiredArg<T> arg) {
        return (T) args.get(arg);
    }

    @SuppressWarnings("unchecked")
    public <T> T getArg(OptionalArg<T> arg) {
        return (T) args.get(arg);
    }

    public boolean hasArg(OptionalArg<?> arg) {
        return args.containsKey(arg);
    }

    public void put(Object arg, Object value) {
        args.put(arg, value);
    }
}
