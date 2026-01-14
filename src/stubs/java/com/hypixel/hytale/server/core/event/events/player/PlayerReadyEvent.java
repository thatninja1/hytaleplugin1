package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.server.core.entity.entities.Player;

/**
 * Stubbed player ready event for CI compilation only.
 */
public class PlayerReadyEvent {
    private final Player player;

    public PlayerReadyEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
