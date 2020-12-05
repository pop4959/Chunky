package org.popcraft.chunky.integration;

import org.popcraft.chunky.platform.Border;

public interface BorderIntegration extends Integration {
    boolean hasBorder(String world);

    Border getBorder(String world);
}
