package com.hypixel.hytale.server.core.universe;

import com.hypixel.hytale.server.core.Message;

import java.util.UUID;

/**
 * Stubbed player reference for CI compilation only.
 */
public class PlayerRef {
    private final UUID uuid;
    private final String displayName;

    public PlayerRef(UUID uuid, String displayName) {
        this.uuid = uuid;
        this.displayName = displayName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void sendMessage(Message message) {
        // no-op
    }

    public boolean hasPermission(String permission) {
        return false;
    }
}
