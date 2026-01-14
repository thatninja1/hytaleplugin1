package com.hypixel.hytale.server.core.commands;

/**
 * Stubbed command sender for CI compilation only.
 */
public interface CommandSender {
    void sendMessage(String message);

    boolean hasPermission(String permission);
}
