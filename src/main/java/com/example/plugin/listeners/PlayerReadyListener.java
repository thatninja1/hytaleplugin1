package com.example.plugin.listeners;

import com.example.plugin.economy.EconomyService;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;

import java.util.UUID;

public class PlayerReadyListener {

    public static void onPlayerReady(PlayerReadyEvent event, EconomyService economyService) {
        Player player = event.getPlayer();
        UUID playerId = player.getUuid();
        economyService.ensureAccount(playerId);
    }
}
