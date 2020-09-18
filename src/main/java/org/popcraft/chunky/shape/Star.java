package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Star extends AbstractPolygon {
    private double p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z, p5x, p5z;
    private double i1x, i1z, i2x, i2z, i3x, i3z, i4x, i4z, i5x, i5z;

    public Star(Selection selection) {
        super(selection);
        this.p1x = xCenter + radius * Math.cos(Math.toRadians(54));
        this.p1z = zCenter + radius * Math.sin(Math.toRadians(54));
        this.p2x = xCenter + radius * Math.cos(Math.toRadians(126));
        this.p2z = zCenter + radius * Math.sin(Math.toRadians(126));
        this.p3x = xCenter + radius * Math.cos(Math.toRadians(198));
        this.p3z = zCenter + radius * Math.sin(Math.toRadians(198));
        this.p4x = xCenter + radius * Math.cos(Math.toRadians(270));
        this.p4z = zCenter + radius * Math.sin(Math.toRadians(270));
        this.p5x = xCenter + radius * Math.cos(Math.toRadians(342));
        this.p5z = zCenter + radius * Math.sin(Math.toRadians(342));
        double[] i1 = intersection(p1x, p1z, p3x, p3z, p2x, p2z, p5x, p5z);
        this.i1x = i1[0];
        this.i1z = i1[1];
        double[] i2 = intersection(p1x, p1z, p3x, p3z, p2x, p2z, p4x, p4z);
        this.i2x = i2[0];
        this.i2z = i2[1];
        double[] i3 = intersection(p2x, p2z, p4x, p4z, p3x, p3z, p5x, p5z);
        this.i3x = i3[0];
        this.i3z = i3[1];
        double[] i4 = intersection(p3x, p3z, p5x, p5z, p1x, p1z, p4x, p4z);
        this.i4x = i4[0];
        this.i4z = i4[1];
        double[] i5 = intersection(p1x, p1z, p4x, p4z, p2x, p2z, p5x, p5z);
        this.i5x = i5[0];
        this.i5z = i5[1];
    }

    @Override
    public double[] pointsX() {
        return new double[]{p1x, i1x, p2x, i2x, p3x, i3x, p4x, i4x, p5x, i5x};
    }

    @Override
    public double[] pointsZ() {
        return new double[]{p1z, i1z, p2z, i2z, p3z, i3z, p4z, i4z, p5z, i5z};
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
        return "star";
    }

    private double[] intersection(double l1p1x, double l1p1z, double l1p2x, double l1p2z, double l2p1x, double l2p1z, double l2p2x, double l2p2z) {
        double l1m = (l1p2z - l1p1z) / (l1p2x - l1p1x);
        double l2m = (l2p2z - l2p1z) / (l2p2x - l2p1x);
        double l1b = l1p1z - (l1m * l1p1x);
        double l2b = l2p1z - (l2m * l2p1x);
        double a1 = -l1m, c1 = -l1b;
        double a2 = -l2m, c2 = -l2b;
        double ix = (c2 - c1) / (a1 - a2);
        double iz = (a2 * c1 - a1 * c2) / (a1 - a2);
        return new double[]{ix, iz};
    }
}
