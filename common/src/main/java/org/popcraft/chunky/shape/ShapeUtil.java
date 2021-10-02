package org.popcraft.chunky.shape;

import java.util.Optional;

public class ShapeUtil {
    private ShapeUtil() {
    }

    /**
     * Checks if a point C is inside (to the left of) the line defined by point A and B.
     *
     * @param ax Point A x
     * @param az Point A z
     * @param bx Point B x
     * @param bz Point B z
     * @param cx Point C x
     * @param cz Point C z
     * @return Whether point C can be considered inside of line AB.
     */
    public static boolean insideLine(double ax, double az, double bx, double bz, double cx, double cz) {
        // Compute whether the point is inside the line using a cross product
        return (bx - ax) * (cz - az) > (bz - az) * (cx - ax);
    }

    /**
     * Given two lines defined by two points each respectively, return the intersection point, if any.
     *
     * @param l1p1x Line 1 point 1 x
     * @param l1p1z Line 1 point 1 z
     * @param l1p2x Line 1 point 2 x
     * @param l1p2z Line 1 point 2 z
     * @param l2p1x Line 2 point 1 x
     * @param l2p1z Line 2 point 1 z
     * @param l2p2x Line 2 point 2 x
     * @param l2p2z Line 2 point 2 z
     * @return An optional containing the intersection point, or empty if no intersection.
     */
    public static Optional<double[]> intersection(double l1p1x, double l1p1z, double l1p2x, double l1p2z, double l2p1x, double l2p1z, double l2p2x, double l2p2z) {
        // Compute delta x for each line
        double l1dx = l1p2x - l1p1x, l2dx = l2p2x - l2p1x;
        // If both are equal to zero, the lines do not intersect at a point
        if (l1dx == 0 && l2dx == 0) {
            return Optional.empty();
        }
        // Compute delta z for each line
        double l1dz = l1p2z - l1p1z, l2dz = l2p2z - l2p1z;
        // Compute the slope and intercept for line 2
        double l2m = l2dz / l2dx;
        double l2b = l2p1z - (l2m * l2p1x);
        // If the slope of line 1 is zero, then the intersection can be computed directly from its x value
        if (l1dx == 0) {
            return Optional.of(new double[]{l1p1x, l2m * l1p1x + l2b});
        }
        // Compute the slope and intercept for line 1
        double l1m = l1dz / l1dx;
        double l1b = l1p1z - (l1m * l1p1x);
        // If the slope of line 2 is zero, then the intersection can be computed directly from its x value
        if (l2dx == 0) {
            return Optional.of(new double[]{l2p1x, l1m * l2p1x + l1b});
        }
        // If the slope of both lines are equal, then they do not intersect, or are the same line
        if (l1m == l2m) {
            return Optional.empty();
        }
        // Solve the system of equations to get the intersection point
        double a1 = -l1m, c1 = -l1b;
        double a2 = -l2m, c2 = -l2b;
        double ix = (c2 - c1) / (a1 - a2);
        double iz = (a2 * c1 - a1 * c2) / (a1 - a2);
        return Optional.of(new double[]{ix, iz});
    }

    /**
     * Given an ellipse defined by the center and radii, find a point on the perimeter corresponding to a specific
     * angle.
     *
     * @param centerX Ellipse center x
     * @param centerZ Ellipse center z
     * @param radiusX Ellipse radius x
     * @param radiusZ Ellipse radius z
     * @param angle   Angle in radians
     * @return The point on the ellipse.
     */
    public static double[] pointOnEllipse(double centerX, double centerZ, double radiusX, double radiusZ, double angle) {
        double pointX = centerX + radiusX * Math.cos(angle);
        double pointZ = centerZ + radiusZ * Math.sin(angle);
        return new double[]{pointX, pointZ};
    }
}
