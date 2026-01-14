package com.hypixel.hytale.server.core.command.system;

/**
 * Stubbed command sender for CI compilation only.
 */
public interface CommandSender {
    void sendMessage(String message);

    boolean hasPermission(String permission);
}
