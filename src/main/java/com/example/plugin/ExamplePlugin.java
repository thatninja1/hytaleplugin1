package com.example.plugin;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.example.plugin.commands.CameraCommand;
import com.example.plugin.commands.ExampleCommand;
import com.example.plugin.commands.MsgCommand;
import com.example.plugin.commands.SendCommand;
import com.example.plugin.commands.TitleCommand;
import com.example.plugin.listeners.PlayerChatListener;
import com.example.plugin.listeners.PlayerReadyListener;

import javax.annotation.Nonnull;

public class ExamplePlugin extends JavaPlugin {
    public ExamplePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // ####### Commands ######## //
        // Move camera
        this.getCommandRegistry().registerCommand(new CameraCommand());
        // An example command
        this.getCommandRegistry().registerCommand(new ExampleCommand());
        // Send a private message to a player
        this.getCommandRegistry().registerCommand(new MsgCommand());
        // Send player to another server
        this.getCommandRegistry().registerCommand(new SendCommand());
        // Display a title to all players
        this.getCommandRegistry().registerCommand(new TitleCommand());

        // ######## Events ######## //
        // Format chat messages
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, PlayerChatListener::onPlayerChat);
        // Send welcome message
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerReadyListener::onPlayerReady);
    }
}