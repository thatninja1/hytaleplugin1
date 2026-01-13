package com.example.plugin;

import com.example.plugin.economy.EconomyService;
import com.example.plugin.economy.commands.BalCommand;
import com.example.plugin.economy.commands.BalTopCommand;
import com.example.plugin.economy.commands.GiveCommand;
import com.example.plugin.economy.commands.PayCommand;
import com.example.plugin.economy.commands.SetCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.nio.file.Path;
import javax.annotation.Nonnull;

public class EconomyPlugin extends JavaPlugin {
    private EconomyService economyService;

    public EconomyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        Path dataFile = this.getDataFolder().toPath().resolve("balances.json");
        this.economyService = new EconomyService(dataFile, this.getLogger());
        this.economyService.load();

        this.getLogger().info("EconomyPlugin loaded. Data file: " + dataFile);

        this.getCommandRegistry().registerCommand(new BalCommand(this.economyService));
        this.getCommandRegistry().registerCommand(new PayCommand(this.economyService));
        this.getCommandRegistry().registerCommand(new GiveCommand(this.economyService));
        this.getCommandRegistry().registerCommand(new SetCommand(this.economyService));
        this.getCommandRegistry().registerCommand(new BalTopCommand(this.economyService));
    }

    @Override
    protected void teardown() {
        if (this.economyService != null) {
            this.economyService.save();
        }
    }
}
