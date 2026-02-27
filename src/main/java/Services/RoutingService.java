package Services;

import Models.FoodListing;
import Models.NGO;
import Models.Vendor;

public class RoutingService {

    /**
     * Generates a summary string (Distance/Time) for the dashboard label.
     */
    public String getRouteSummary(FoodListing listing, NGO ngo) {
        double distance = calculateBasicDistance(listing.getVendor(), ngo);
        int estMinutes = (int) (distance * 2) + 5; // Added 5 min traffic buffer
        return String.format("📏 Distance: %.1f km\n⏱️ Est. Drive: %d mins", distance, estMinutes);
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

    public double calculateBasicDistance(Vendor vendor, NGO ngo) {
        if (vendor == null || ngo == null) return 0.0;

        double lat1 = vendor.getLatitude();
        double lon1 = vendor.getLongitude();
        double lat2 = ngo.getLatitude();
        double lon2 = ngo.getLongitude();

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