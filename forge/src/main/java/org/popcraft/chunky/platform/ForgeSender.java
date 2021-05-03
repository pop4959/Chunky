package org.popcraft.chunky.platform;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import static org.popcraft.chunky.Chunky.translate;

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
    public void sendMessage(String key, Object... args) {
        String text = translate(key, args).replaceAll("&(?=[0-9a-fk-orA-FK-OR])", "§");
        ITextComponent textComponent = new StringTextComponent(text);
        source.sendFeedback(textComponent, false);
    }
}
