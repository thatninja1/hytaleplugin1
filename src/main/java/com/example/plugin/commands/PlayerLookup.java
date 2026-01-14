package com.example.plugin.commands;

import com.hypixel.hytale.server.core.universe.PlayerRef;

public interface PlayerLookup {
    PlayerRef findOnlinePlayer(String name);
}
