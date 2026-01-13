package com.hypixel.hytale.server.core;

import java.awt.Color;

public class Message {
    public static Message raw(String text) {
        return new Message();
    }

    public static Message join(Message... parts) {
        return new Message();
    }

    public Message color(Color color) {
        return this;
    }
}
