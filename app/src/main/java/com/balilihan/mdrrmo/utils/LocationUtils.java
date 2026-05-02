// utils/LocationUtils.java

package com.balilihan.mdrrmo.utils;

import android.location.Location;

public class LocationUtils {

    // Balilihan, Bohol coordinates — used as default map center
    public static final double BALILIHAN_LAT = 9.8833;
    public static final double BALILIHAN_LNG = 123.9833;

    // Default alert radius in meters
    public static final float DEFAULT_RADIUS_METERS = 200f;

    // Calculate distance between two coordinates in meters
    // Uses Android's built-in Haversine formula implementation
    public static float distanceBetween(
            double lat1, double lng1,
            double lat2, double lng2) {

        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0];
    }

    // Check if a hazard is within the alert radius of the user
    public static boolean isWithinRadius(
            double userLat, double userLng,
            double hazardLat, double hazardLng,
            float radiusMeters) {

        float distance = distanceBetween(
                userLat, userLng, hazardLat, hazardLng
        );
        return distance <= radiusMeters;
    }

    // Format distance for display
    // Shows meters if under 1000, kilometers if over
    public static String formatDistance(float meters) {
        if (meters < 1000) {
            return Math.round(meters) + "m away";
        } else {
            return String.format("%.1fkm away", meters / 1000);
        }
    }

    // Format coordinates for display
    public static String formatCoordinates(double lat, double lng) {
        return String.format("%.6f°N, %.6f°E", lat, lng);
    }
}