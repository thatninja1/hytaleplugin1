package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class PlayerChatEvent {
    public interface Formatter {
        Message format(PlayerRef playerRef, String message);
    }

    public void setFormatter(Formatter formatter) {
    }
}
