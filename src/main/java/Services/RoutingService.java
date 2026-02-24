package Services;

import Models.FoodListing;
import Models.NGO;
import Models.Vendor;

public class RoutingService {

    /**
     * Generates a summary string (Distance/Time) for the dashboard label.
     */
    public String getRouteSummary(FoodListing listing, NGO ngo) {
        Vendor vendor = listing.getVendor();
        // Since we are using coordinates from the database
        double distance = calculateBasicDistance(
            vendor.getLatitude(), vendor.getLongitude(),
            ngo.getLatitude(), ngo.getLongitude()
        );

        int estMinutes = (int) (distance * 2); // Rough estimate: 2 mins per km
        return String.format("📍 Pickup: %s\n📏 Distance: %.1f km\n⏱️ Est. Drive: %d mins", 
                             vendor.getRestaurantName(), distance, estMinutes);
    }

    /**
     * Creates a direct browser link for Google Maps Navigation.
     */
    public String generateGoogleMapsLink(FoodListing listing, NGO ngo) {
        Vendor v = listing.getVendor();
        // Format: https://www.google.com/maps/dir/?api=1&origin=LAT,LNG&destination=LAT,LNG
        return String.format("https://www.google.com/maps/dir/?api=1&origin=%f,%f&destination=%f,%f&travelmode=driving",
                             ngo.getLatitude(), ngo.getLongitude(),
                             v.getLatitude(), v.getLongitude());
    }

    private double calculateBasicDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula logic
        double R = 6371; 
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}