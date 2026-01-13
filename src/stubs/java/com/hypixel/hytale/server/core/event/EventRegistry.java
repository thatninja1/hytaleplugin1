package com.hypixel.hytale.server.core.event;

import java.util.function.Consumer;

public class EventRegistry {
    public <T> void registerGlobal(Class<T> eventClass, Consumer<T> handler) {
    }
}
