package org.popcraft.chunky.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ShapeType {
    public static final String CIRCLE = "circle";
    public static final String DIAMOND = "diamond";
    public static final String ELLIPSE = "ellipse";
    public static final String OVAL = "oval";
    public static final String PENTAGON = "pentagon";
    public static final String RECTANGLE = "rectangle";
    public static final String SQUARE = "square";
    public static final String STAR = "star";
    public static final String TRIANGLE = "triangle";

    private static final List<String> DEFAULTS = List.of(CIRCLE, DIAMOND, ELLIPSE, PENTAGON, RECTANGLE, SQUARE, STAR, TRIANGLE);

    private ShapeType() {
    }

    public static List<String> all() {
        final Set<String> customTypes = ShapeFactory.getCustomTypes();
        if (customTypes.isEmpty()) {
            return DEFAULTS;
        }
        final List<String> allTypes = new ArrayList<>(DEFAULTS);
        allTypes.addAll(customTypes);
        return allTypes;
    }
}
