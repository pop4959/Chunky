package org.popcraft.chunky.integration;

import java.awt.Color;

public abstract class AbstractMapIntegration implements MapIntegration {
    protected String label = "World Border";
    protected Color color = Color.RED;

    @Override
    public void setOptions(String label, String color, boolean hideByDefault, int priority, int weight) {
        if (label != null && !label.isEmpty()) {
            this.label = label;
        }
        if (color.length() != 6) {
            return;
        }
        try {
            int r = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int b = Integer.parseInt(color.substring(4, 6), 16);
            this.color = new Color(r, g, b);
        } catch (NumberFormatException ignored) {
        }
    }
}
