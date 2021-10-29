package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.util.Vector2;

import java.util.Arrays;
import java.util.List;

import static org.popcraft.chunky.shape.ShapeUtil.insideLine;

public class Pentagon extends AbstractPolygon {
    private final double p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z, p5x, p5z;

    public Pentagon(Selection selection, boolean chunkAligned) {
        super(selection, chunkAligned);
        this.p1x = centerX + radiusX * Math.cos(Math.toRadians(54));
        this.p1z = centerZ + radiusX * Math.sin(Math.toRadians(54));
        this.p2x = centerX + radiusX * Math.cos(Math.toRadians(126));
        this.p2z = centerZ + radiusX * Math.sin(Math.toRadians(126));
        this.p3x = centerX + radiusX * Math.cos(Math.toRadians(198));
        this.p3z = centerZ + radiusX * Math.sin(Math.toRadians(198));
        this.p4x = centerX + radiusX * Math.cos(Math.toRadians(270));
        this.p4z = centerZ + radiusX * Math.sin(Math.toRadians(270));
        this.p5x = centerX + radiusX * Math.cos(Math.toRadians(342));
        this.p5z = centerZ + radiusX * Math.sin(Math.toRadians(342));
    }

    @Override
    public List<Vector2> points() {
        return Arrays.asList(
                Vector2.of(p1x, p1z),
                Vector2.of(p2x, p2z),
                Vector2.of(p3x, p3z),
                Vector2.of(p4x, p4z),
                Vector2.of(p5x, p5z)
        );
    }

    @Override
    public boolean isBounding(double x, double z) {
        boolean inside12 = insideLine(p1x, p1z, p2x, p2z, x, z);
        boolean inside23 = insideLine(p2x, p2z, p3x, p3z, x, z);
        boolean inside34 = insideLine(p3x, p3z, p4x, p4z, x, z);
        boolean inside45 = insideLine(p4x, p4z, p5x, p5z, x, z);
        boolean inside51 = insideLine(p5x, p5z, p1x, p1z, x, z);
        return inside12 && inside23 && inside34 && inside45 && inside51;
    }

    @Override
    public String name() {
        return ShapeType.PENTAGON;
    }
}
