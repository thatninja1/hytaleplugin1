package com.hypixel.hytale.server.core.universe;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.network.PacketHandler;

public class PlayerRef extends CommandSender {
    public static Object getComponentType() {
        return new Object();
    }

    public String getUsername() {
        return "Player";
    }

    public PacketHandler getPacketHandler() {
        return new PacketHandler();
    }

    public void referToServer(String address, int port, Object token) {
    }

    @Override
    public void sendMessage(Message message) {
    }
}
