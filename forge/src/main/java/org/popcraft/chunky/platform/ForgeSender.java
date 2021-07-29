package org.popcraft.chunky.platform;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.popcraft.chunky.util.Coordinate;

import static org.popcraft.chunky.util.Translator.translateKey;

public class ForgeSender implements Sender {
    private CommandSourceStack source;

    public ForgeSender(CommandSourceStack source) {
        this.source = source;
    }

    @Override
    public boolean isPlayer() {
        return source.getEntity() instanceof ServerPlayer;
    }

    @Override
    public String getName() {
        try {
            return source.getPlayerOrException().getName().getString();
        } catch (CommandSyntaxException e) {
            return "Console";
        }
    }

    @Override
    public Coordinate getCoordinate() {
        Vec3 pos = source.getPosition();
        return new Coordinate((long) pos.x(), (long) pos.z());
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        String text = translateKey(key, prefixed, args).replaceAll("&(?=[0-9a-fk-orA-FK-OR])", "§");
        Component textComponent = new TextComponent(text);
        source.sendSuccess(textComponent, false);
    }
}
