package com.hypixel.hytale.server.core.event;

import java.util.function.Consumer;

/**
 * Stubbed event registry for CI compilation only.
 */
public class EventRegistry {
    public <T> void registerGlobal(Class<T> eventType, Consumer<T> handler) {
        // no-op
    }
}
