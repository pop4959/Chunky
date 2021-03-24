package org.popcraft.chunky.platform;

public interface Sender {
    boolean isPlayer();

    String getName();

    void sendMessage(String key, Object... args);
}
