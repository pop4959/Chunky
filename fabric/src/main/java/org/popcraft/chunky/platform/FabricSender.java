package org.popcraft.chunky.platform;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import static org.popcraft.chunky.Chunky.translate;

public class FabricSender implements Sender {
    private CommandSource source;

    public FabricSender(CommandSource source) {
        this.source = source;
    }

    @Override
    public boolean isPlayer() {
        if (!(source instanceof ServerCommandSource)) {
            return false;
        }
        ServerCommandSource serverCommandSource = (ServerCommandSource) source;
        Entity entity = serverCommandSource.getEntity();
        if (entity == null) {
            return false;
        }
        return entity instanceof ServerPlayerEntity;
    }

    @Override
    public void sendMessage(String key, Object... args) {
        final String text;
        if (isPlayer()) {
            text = translate(key, args).replaceAll("&(?=[0-9a-fk-orA-FK-OR])", "§");
        } else {
            text = translate(key, args).replaceAll("&[0-9a-fk-orA-FK-OR]", "");
        }
        if (source instanceof ServerCommandSource) {
            ((ServerCommandSource) source).sendFeedback(new LiteralText(text), false);
        }
    }
}
