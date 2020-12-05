package org.popcraft.chunky.platform;

public interface Sender {
    boolean isPlayer();

    void sendMessage(String key, Object... args);
}
