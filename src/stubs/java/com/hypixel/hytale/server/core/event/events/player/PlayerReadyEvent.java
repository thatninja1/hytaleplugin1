package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.server.core.entity.entities.Player;

public class PlayerReadyEvent {
    public Player getPlayer() {
        return new Player();
    }
}
