package com.example.plugin.listeners;

import com.example.plugin.chat.ChatMessageFormatter;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;

public class PlayerChatListener {
    private static final ChatMessageFormatter FORMATTER = new ChatMessageFormatter();

    public static void onPlayerChat(PlayerChatEvent event) {
        event.setFormatter(FORMATTER);
    }
}
