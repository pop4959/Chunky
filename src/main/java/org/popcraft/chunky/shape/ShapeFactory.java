package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class ShapeFactory {
    public static Shape getShape(Selection selection) {
        switch (selection.shape) {
            case "circle":
                return new Circle(selection);
            case "diamond":
                return new Diamond(selection);
            case "oval":
                return new Oval(selection);
            case "pentagon":
                return new Pentagon(selection);
            case "star":
                return new Star(selection);
            case "triangle":
                return new Triangle(selection);
            case "rectangle":
                return new Rectangle();
            case "square":
            default:
                return new Square();
        }
    }
}
