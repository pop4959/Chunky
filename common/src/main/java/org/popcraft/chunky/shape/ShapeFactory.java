package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public final class ShapeFactory {
    private ShapeFactory() {
    }

    public static Shape getShape(final Selection selection) {
        return getShape(selection, true);
    }

    public static Shape getShape(final Selection selection, final boolean chunkAligned) {
        return switch (selection.shape()) {
            case ShapeType.CIRCLE -> new Circle(selection, chunkAligned);
            case ShapeType.DIAMOND -> new Diamond(selection, chunkAligned);
            case ShapeType.ELLIPSE, ShapeType.OVAL -> new Ellipse(selection, chunkAligned);
            case ShapeType.PENTAGON -> new Pentagon(selection, chunkAligned);
            case ShapeType.RECTANGLE -> new Rectangle(selection, chunkAligned);
            case ShapeType.STAR -> new Star(selection, chunkAligned);
            case ShapeType.TRIANGLE -> new Triangle(selection, chunkAligned);
            default -> new Square(selection, chunkAligned);
        };
    }
}
