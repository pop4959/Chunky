package org.popcraft.chunky.util;

import java.util.concurrent.TimeUnit;

public class PendingAction {
    private final Runnable action;
    private final long expiry;

    public PendingAction(Runnable action) {
        this.action = action;
        this.expiry = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1);
    }

    public Runnable getAction() {
        return action;
    }

    public boolean hasExpired() {
        return System.currentTimeMillis() > expiry;
    }
}
