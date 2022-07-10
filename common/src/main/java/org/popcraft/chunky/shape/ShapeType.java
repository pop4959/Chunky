package org.popcraft.chunky.shape;

import java.util.List;

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

    public static final List<String> ALL = List.of(CIRCLE, DIAMOND, ELLIPSE, PENTAGON, RECTANGLE, SQUARE, STAR, TRIANGLE);

    private ShapeType() {
    }
}
