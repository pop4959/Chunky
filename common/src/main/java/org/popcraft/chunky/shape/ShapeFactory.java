package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class ShapeFactory {
    public static Shape getShape(Selection selection) {
        return getShape(selection, true);
    }

    public static Shape getShape(Selection selection, boolean chunkAligned) {
        switch (selection.shape) {
            case "circle":
                return new Circle(selection, chunkAligned);
            case "diamond":
                return new Diamond(selection, chunkAligned);
            case "oval":
            case "ellipse":
                return new Ellipse(selection, chunkAligned);
            case "pentagon":
                return new Pentagon(selection, chunkAligned);
            case "rectangle":
                return new Rectangle(selection, chunkAligned);
            case "star":
                return new Star(selection, chunkAligned);
            case "triangle":
                return new Triangle(selection, chunkAligned);
            case "square":
            default:
                return new Square(selection, chunkAligned);
        }
    }
}
