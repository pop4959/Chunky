package org.popcraft.chunky.shape;

import org.popcraft.chunky.iterator.ChunkIterator;

public class ShapeFactory {
    public static Shape getShape(String shape, ChunkIterator iterator) {
        switch (shape) {
            case "circle":
                return new Circle(iterator);
            case "triangle":
                return new Triangle(iterator);
            case "diamond":
                return new Diamond(iterator);
            case "pentagon":
                return new Pentagon(iterator);
            case "star":
                return new Star(iterator);
            case "square":
            default:
                return new Square();
        }
    }
}
