package org.popcraft.chunky.platform;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.popcraft.chunky.util.Coordinate;

import static org.popcraft.chunky.util.Translator.translateKey;

public class FabricSender implements Sender {
    private ServerCommandSource source;

    public FabricSender(ServerCommandSource source) {
        this.source = source;
    }

    @Override
    public boolean isPlayer() {
        return source.getEntity() instanceof ServerPlayerEntity;
    }

    @Override
    public String getName() {
        try {
            return source.getPlayer().getName().getString();
        } catch (CommandSyntaxException e) {
            return "Console";
        }
    }

    @Override
    public Coordinate getCoordinate() {
        Vec3d pos = source.getPosition();
        return new Coordinate((long) pos.getX(), (long) pos.getZ());
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        final String text;
        if (isPlayer()) {
            text = translateKey(key, prefixed, args).replaceAll("&(?=[0-9a-fk-orA-FK-OR])", "§");
        } else {
            text = translateKey(key, prefixed, args).replaceAll("&[0-9a-fk-orA-FK-OR]", "");
        }
        Text textComponent = new LiteralText(text);
        source.sendFeedback(textComponent, false);
    }
}
