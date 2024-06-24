package org.besquiros.spotaffich.util;

public class GeoUtil {

    private static final double EARTH_RADIUS = 6371.0;


    /**
     * Calculates a bounding box (square) around a central point defined by latitude and longitude
     * for a given radius in kilometers.
     *
     * @param centerLatitude  Latitude of the central point (in degrees).
     * @param centerLongitude Longitude of the central point (in degrees).
     * @param radius          Radius around the central point (in kilometers).
     * @return BoundingBox object containing min/max latitude and longitude.
     */
    public static BoundingBox calculateBoundingBox(double centerLatitude, double centerLongitude, double radius) {
        // Convert radius from kilometers to radians
        double radiusRadians = radius / EARTH_RADIUS;

        // Convert center latitude and longitude from degrees to radians
        double centerLatRadians = Math.toRadians(centerLatitude);
        double centerLonRadians = Math.toRadians(centerLongitude);

        // Calculate the minimum and maximum latitude and longitude
        double minLat = centerLatRadians - radiusRadians;
        double maxLat = centerLatRadians + radiusRadians;

        // Correction for the latitude bounds
        double minLon;
        double maxLon;
        if (minLat > Math.toRadians(-90) && maxLat < Math.toRadians(90)) {
            double deltaLon = Math.asin(Math.sin(radiusRadians) / Math.cos(centerLatRadians));
            minLon = centerLonRadians - deltaLon;
            if (minLon < Math.toRadians(-180)) {
                minLon += 2 * Math.PI;
            }
            maxLon = centerLonRadians + deltaLon;
            if (maxLon > Math.toRadians(180)) {
                maxLon -= 2 * Math.PI;
            }
        } else {
            minLat = Math.max(minLat, Math.toRadians(-90));
            maxLat = Math.min(maxLat, Math.toRadians(90));
            minLon = Math.toRadians(-180);
            maxLon = Math.toRadians(180);
        }

        // Convert back to degrees
        double minLatitude = Math.toDegrees(minLat);
        double maxLatitude = Math.toDegrees(maxLat);
        double minLongitude = Math.toDegrees(minLon);
        double maxLongitude = Math.toDegrees(maxLon);

        return new BoundingBox(minLatitude, maxLatitude, minLongitude, maxLongitude);
    }

    /**
     * Checks if a point defined by latitude and longitude is within the specified bounding box.
     *
     * @param latitude    Latitude of the point.
     * @param longitude   Longitude of the point.
     * @param boundingBox BoundingBox object defining the boundaries.
     * @return true if the point is within the bounding box, false otherwise.
     */
    public static boolean isPointInRadius(double latitude, double longitude, BoundingBox boundingBox) {
        return latitude >= boundingBox.minLatitude() &&
                latitude <= boundingBox.maxLatitude() &&
                longitude >= boundingBox.minLongitude() &&
                longitude <= boundingBox.maxLongitude();
    }

    /**
     * Helper class to hold bounding box coordinates.
     */
    public record BoundingBox(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {

    }
}
