package org.popcraft.chunky.platform;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.serializer.TextSerializers;

import static org.popcraft.chunky.Chunky.translate;

public class SpongeSender implements Sender {
    private CommandSource commandSource;

    public SpongeSender(CommandSource commandSource) {
        this.commandSource = commandSource;
    }

    @Override
    public boolean isPlayer() {
        return commandSource instanceof Player;
    }

    @Override
    public void sendMessage(String key, Object... args) {
        commandSource.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(translate(key, args)));
    }
}
