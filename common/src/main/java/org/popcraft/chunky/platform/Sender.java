package org.popcraft.chunky.platform;

import org.popcraft.chunky.util.Coordinate;

public interface Sender {
    boolean isPlayer();

    String getName();

    Coordinate getCoordinate();

    void sendMessage(String key, boolean prefixed, Object... args);

    default void sendMessage(String key, Object... args) {
        sendMessage(key, false, args);
    }

    default void sendMessagePrefixed(String key, Object... args) {
        sendMessage(key, true, args);
    }
}
