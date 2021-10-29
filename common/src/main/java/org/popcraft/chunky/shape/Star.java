package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.util.Vector2;

import java.util.Arrays;
import java.util.List;

import static org.popcraft.chunky.shape.ShapeUtil.insideLine;
import static org.popcraft.chunky.shape.ShapeUtil.intersection;

public class Star extends AbstractPolygon {
    private final double p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z, p5x, p5z;
    private final double i1x, i1z, i2x, i2z, i3x, i3z, i4x, i4z, i5x, i5z;

    public Star(Selection selection, boolean chunkAligned) {
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
        final Vector2 i1 = intersection(p1x, p1z, p3x, p3z, p2x, p2z, p5x, p5z).orElse(Vector2.of(p1x, p1z));
        final Vector2 i2 = intersection(p1x, p1z, p3x, p3z, p2x, p2z, p4x, p4z).orElse(Vector2.of(p2x, p2z));
        final Vector2 i3 = intersection(p2x, p2z, p4x, p4z, p3x, p3z, p5x, p5z).orElse(Vector2.of(p3x, p3z));
        final Vector2 i4 = intersection(p3x, p3z, p5x, p5z, p1x, p1z, p4x, p4z).orElse(Vector2.of(p4x, p4z));
        final Vector2 i5 = intersection(p1x, p1z, p4x, p4z, p2x, p2z, p5x, p5z).orElse(Vector2.of(p5x, p5z));
        this.i1x = i1.getX();
        this.i1z = i1.getZ();
        this.i2x = i2.getX();
        this.i2z = i2.getZ();
        this.i3x = i3.getX();
        this.i3z = i3.getZ();
        this.i4x = i4.getX();
        this.i4z = i4.getZ();
        this.i5x = i5.getX();
        this.i5z = i5.getZ();
    }

    @Override
    public List<Vector2> points() {
        return Arrays.asList(
                Vector2.of(p1x, p1z),
                Vector2.of(i1x, i1z),
                Vector2.of(p2x, p2z),
                Vector2.of(i2x, i2z),
                Vector2.of(p3x, p3z),
                Vector2.of(i3x, i3z),
                Vector2.of(p4x, p4z),
                Vector2.of(i4x, i4z),
                Vector2.of(p5x, p5z),
                Vector2.of(i5x, i5z)
        );
    }

    @Override
    public boolean isBounding(double x, double z) {
        boolean inside13 = insideLine(p1x, p1z, p3x, p3z, x, z);
        boolean inside24 = insideLine(p2x, p2z, p4x, p4z, x, z);
        boolean inside35 = insideLine(p3x, p3z, p5x, p5z, x, z);
        boolean inside41 = insideLine(p4x, p4z, p1x, p1z, x, z);
        if (inside13 && inside24 && inside35 && inside41) {
            return true;
        }
        boolean inside52 = insideLine(p5x, p5z, p2x, p2z, x, z);
        if (inside24 && inside35 && inside41 && inside52) {
            return true;
        }
        if (inside35 && inside41 && inside52 && inside13) {
            return true;
        }
        if (inside41 && inside52 && inside13 && inside24) {
            return true;
        }
        return inside52 && inside13 && inside24 && inside35;
    }

    @Override
    public String name() {
        return ShapeType.STAR;
    }
}
