package org.popcraft.chunky.shape;

import org.popcraft.chunky.platform.util.Vector2;

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
     * @param l1x1 Line 1 point 1 x
     * @param l1z1 Line 1 point 1 z
     * @param l1x2 Line 1 point 2 x
     * @param l1z2 Line 1 point 2 z
     * @param l2x1 Line 2 point 1 x
     * @param l2z1 Line 2 point 1 z
     * @param l2x2 Line 2 point 2 x
     * @param l2z2 Line 2 point 2 z
     * @return An optional containing the intersection point, or empty if no intersection.
     */
    public static Optional<Vector2> intersection(double l1x1, double l1z1, double l1x2, double l1z2, double l2x1, double l2z1, double l2x2, double l2z2) {
        final double a1 = l1z2 - l1z1;
        final double a2 = l2z2 - l2z1;
        final double b1 = l1x1 - l1x2;
        final double b2 = l2x1 - l2x2;
        final double determinant = a1 * b2 - a2 * b1;
        if (determinant == 0) {
            return Optional.empty();
        } else {
            final double c1 = a1 * l1x1 + b1 * l1z1;
            final double c2 = a2 * l2x1 + b2 * l2z1;
            final double x = (b2 * c1 - b1 * c2) / determinant;
            final double z = (a1 * c2 - a2 * c1) / determinant;
            return Optional.of(Vector2.of(x, z));
        }
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
    public static Vector2 pointOnEllipse(double centerX, double centerZ, double radiusX, double radiusZ, double angle) {
        final double x = centerX + radiusX * Math.cos(angle);
        final double z = centerZ + radiusZ * Math.sin(angle);
        return Vector2.of(x, z);
    }
}
