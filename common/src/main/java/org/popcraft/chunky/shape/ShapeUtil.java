package org.popcraft.chunky.shape;

import org.popcraft.chunky.platform.util.Vector2;

import java.util.Optional;

public final class ShapeUtil {
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
    public static boolean insideLine(final double ax, final double az, final double bx, final double bz, final double cx, final double cz) {
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
    public static Optional<Vector2> intersection(final double l1x1, final double l1z1, final double l1x2, final double l1z2, final double l2x1, final double l2z1, final double l2x2, final double l2z2) {
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
    public static Vector2 pointOnEllipse(final double centerX, final double centerZ, final double radiusX, final double radiusZ, final double angle) {
        final double x = centerX + radiusX * Math.cos(angle);
        final double z = centerZ + radiusZ * Math.sin(angle);
        return Vector2.of(x, z);
    }

    /**
     * Calculate the closest point on a line from a given position.
     *
     * @param posX Position x
     * @param posZ Position z
     * @param p1x Line point 1 x
     * @param p1z Line point 1 z
     * @param p2x Line point 2 x
     * @param p2z Line point 2 z
     * @return The closest point to the position on the line.
     */
    public static Vector2 closestPointOnLine(final double posX, final double posZ, final double p1x, final double p1z, final double p2x, final double p2z) {
        final double dx = p2x - p1x;
        final double dz = p2z - p1z;
        final double perpendicularSlope = -dx / dz;
        final double p3x, p3z;
        if (Double.isInfinite(perpendicularSlope)) {
            p3x = posX;
            p3z = posZ + 1;
        } else {
            p3x = posX + 1;
            p3z = posZ + perpendicularSlope;
        }
        return ShapeUtil.intersection(p1x, p1z, p2x, p2z, posX, posZ, p3x, p3z).orElseThrow(IllegalStateException::new);
    }

    /**
     * Calculate the distance between 2 points.
     *
     * @param p1x Point 1 x
     * @param p1z Point 1 z
     * @param p2x Point 2 x
     * @param p2z Point 2 z
     * @return The distance between the 2 points.
     */
    public static double distanceBetweenPoints(final double p1x, final double p1z, final double p2x, final double p2z) {
        return Math.sqrt(Math.pow(p1x - p2x, 2) + Math.pow(p1z - p2z, 2));
    }
}
