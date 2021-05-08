package org.popcraft.chunky.platform;

public interface Sender {
    boolean isPlayer();

    String getName();

    void sendMessage(String key, boolean prefixed, Object... args);

    default void sendMessage(String key, Object... args) {
        sendMessage(key, false, args);
    }

    default void sendMessagePrefixed(String key, Object... args) {
        sendMessage(key, true, args);
    }
}
