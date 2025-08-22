package org.popcraft.chunky;

import org.popcraft.chunky.platform.BukkitSender;
import org.popcraft.chunky.platform.Paper;
import org.popcraft.chunky.util.Version;

public final class ChunkyPaper extends AbstractChunkyBukkit {

    @Override
    protected void validateServerVersion(Version version) {
        super.validateServerVersion(version);
        if (!version.isHigherThanOrEqualTo(Version.MINECRAFT_1_21_1)) {
            getLogger().severe("This version of the Chunky plugin only support Paper versions 1.21.1 and above!");
            getLogger().severe("Please update your server or use an older version of the plugin instead.");
            setEnabled(false);
        }
    }

    @Override
    protected void postEnable() {
        Paper.registerCommand(this, chunky, BukkitSender::new, BukkitSender::new);
    }
}
