package org.popcraft.chunky.platform;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.popcraft.chunky.util.Coordinate;

import static org.popcraft.chunky.util.Translator.translateKey;

public class ForgeSender implements Sender {
    private CommandSource source;

    public ForgeSender(CommandSource source) {
        this.source = source;
    }

    @Override
    public boolean isPlayer() {
        return source.getEntity() instanceof ServerPlayerEntity;
    }

    @Override
    public String getName() {
        try {
            return source.asPlayer().getName().getString();
        } catch (CommandSyntaxException e) {
            return "Console";
        }
    }

    @Override
    public Coordinate getCoordinate() {
        Vector3d pos = source.getPos();
        return new Coordinate((long) pos.getX(), (long) pos.getZ());
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        String text = translateKey(key, prefixed, args).replaceAll("&(?=[0-9a-fk-orA-FK-OR])", "§");
        ITextComponent textComponent = new StringTextComponent(text);
        source.sendFeedback(textComponent, false);
    }
}
