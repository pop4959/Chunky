package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.util.Translator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public final class ShapeFactory {
    private static final Map<String, BiFunction<Selection, Boolean, Shape>> custom = new HashMap<>();

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
            case ShapeType.HEXAGON -> new Hexagon(selection, chunkAligned);
            case ShapeType.PENTAGON -> new Pentagon(selection, chunkAligned);
            case ShapeType.RECTANGLE -> new Rectangle(selection, chunkAligned);
            case ShapeType.STAR -> new Star(selection, chunkAligned);
            case ShapeType.TRIANGLE -> new Triangle(selection, chunkAligned);
            default -> custom.getOrDefault(selection.shape(), Square::new).apply(selection, chunkAligned);
        };
    }

    public static void registerCustom(final String name, final BiFunction<Selection, Boolean, Shape> shapeFunction) {
        custom.put(name, shapeFunction);
        Translator.addCustomTranslation("shape_%s".formatted(name), name);
    }

    public static Set<String> getCustomTypes() {
        return custom.keySet();
    }
}
