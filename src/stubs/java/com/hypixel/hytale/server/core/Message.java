package com.hypixel.hytale.server.core;

/**
 * Stubbed message API for CI compilation only.
 */
public final class Message {
    private final String text;

    private Message(String text) {
        this.text = text;
    }

    public static Message raw(String text) {
        return new Message(text);
    }

    public static Message join(Message... messages) {
        StringBuilder builder = new StringBuilder();
        if (messages != null) {
            for (Message message : messages) {
                if (message != null) {
                    builder.append(message.text);
                }
            }
        }
        return new Message(builder.toString());
    }

    public String text() {
        return text;
    }
}
