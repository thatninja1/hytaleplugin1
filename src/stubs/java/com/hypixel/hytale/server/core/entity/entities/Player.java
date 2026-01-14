package com.hypixel.hytale.server.core.entity.entities;

import java.util.UUID;

/**
 * Stubbed player entity for CI compilation only.
 */
public class Player {
    private final UUID uuid;
    private final String displayName;

    public Player(UUID uuid, String displayName) {
        this.uuid = uuid;
        this.displayName = displayName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public com.hypixel.hytale.server.core.universe.PlayerRef getReference() {
        return new com.hypixel.hytale.server.core.universe.PlayerRef(uuid, displayName);
    }
}
