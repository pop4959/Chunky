package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class ShapeFactory {
    public static Shape getShape(Selection selection) {
        return getShape(selection, true);
    }

    public static Shape getShape(Selection selection, boolean chunkAligned) {
        switch (selection.shape()) {
            case ShapeType.CIRCLE:
                return new Circle(selection, chunkAligned);
            case ShapeType.DIAMOND:
                return new Diamond(selection, chunkAligned);
            case ShapeType.ELLIPSE:
            case ShapeType.OVAL:
                return new Ellipse(selection, chunkAligned);
            case ShapeType.PENTAGON:
                return new Pentagon(selection, chunkAligned);
            case ShapeType.RECTANGLE:
                return new Rectangle(selection, chunkAligned);
            case ShapeType.STAR:
                return new Star(selection, chunkAligned);
            case ShapeType.TRIANGLE:
                return new Triangle(selection, chunkAligned);
            case ShapeType.SQUARE:
            default:
                return new Square(selection, chunkAligned);
        }
    }
}
