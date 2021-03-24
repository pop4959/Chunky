package org.popcraft.chunky.platform;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.Optional;

import static org.popcraft.chunky.Chunky.translate;

public class FabricSender implements Sender {
    private CommandSource source;

    public FabricSender(CommandSource source) {
        this.source = source;
    }

    @Override
    public boolean isPlayer() {
        return getPlayer().isPresent();
    }

    @Override
    public String getName() {
        return getPlayer().map(PlayerEntity::getName).map(Text::asString).orElse("Console");
    }

    private Optional<ServerPlayerEntity> getPlayer() {
        if (!(source instanceof ServerCommandSource)) {
            return Optional.empty();
        }
        ServerCommandSource serverCommandSource = (ServerCommandSource) source;
        Entity entity = serverCommandSource.getEntity();
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of((ServerPlayerEntity) entity);
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
