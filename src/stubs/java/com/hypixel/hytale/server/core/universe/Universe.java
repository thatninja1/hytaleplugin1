package com.hypixel.hytale.server.core.universe;

import com.hypixel.hytale.server.core.universe.world.World;

public class Universe {
    private static final Universe INSTANCE = new Universe();
    private final World defaultWorld = new World();

    public static Universe get() {
        return INSTANCE;
    }

    public World getDefaultWorld() {
        return this.defaultWorld;
    }
}
