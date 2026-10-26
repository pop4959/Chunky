package org.popcraft.chunky.mixin;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ChunkyMixinPlugin implements IMixinConfigPlugin {
    private static final boolean BOOST_UNLOAD_PRIORITY = Boolean.parseBoolean(System.getProperty("chunky.boostUnloadPriority", "true"));
    private static final Set<String> UNLOAD_PRIORITY_MIXINS = Set.of(
            "org.popcraft.chunky.mixin.ChunkHolderMixin",
            "org.popcraft.chunky.mixin.ChunkMapPatchMixin"
    );
    private boolean applyUnloadPriorityMixins;

    @Override
    public void onLoad(final String mixinPackage) {
        this.applyUnloadPriorityMixins = BOOST_UNLOAD_PRIORITY && FMLLoader.getCurrent().getLoadingModList().getModFileById("moonrise") == null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        if (UNLOAD_PRIORITY_MIXINS.contains(mixinClassName)) {
            return applyUnloadPriorityMixins;
        }
        return true;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }
}
