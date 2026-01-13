package com.hypixel.hytale.server.core.command.system.basecommands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public abstract class AbstractTargetPlayerCommand extends CommandBase {
    protected AbstractTargetPlayerCommand(String name, String description) {
        super(name, description);
    }

    protected AbstractTargetPlayerCommand(String name, String description, boolean visible) {
        super(name, description, visible);
    }

    @Override
    protected void executeSync(CommandContext context) {
        execute(context, null, null, null, null, null);
    }

    protected abstract void execute(CommandContext context, Ref<EntityStore> sourceRef, Ref<EntityStore> ref,
            PlayerRef playerRef, World world, Store<EntityStore> store);
}
