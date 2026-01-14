package com.hypixel.hytale.server.core;

import com.hypixel.hytale.server.core.player.PlayerManager;

/**
 * Stubbed server for CI compilation only.
 */
public class Server {
    private final PlayerManager playerManager = new PlayerManager();

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
