package com.hypixel.hytale.server.core.universe.world;

public class World {
    public void execute(Runnable task) {
        if (task != null) {
            task.run();
        }
    }
}
