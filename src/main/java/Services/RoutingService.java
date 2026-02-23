package Services;

import Models.FoodListing;
import Models.NGO;
import Models.Vendor;

public class RoutingService {

    /**
     * Calculates the distance in km using real coordinates from the database.
     */
    public double getDistanceKm(FoodListing listing, NGO ngo) {
        Vendor vendor = listing.getVendor();
        
        // Use real coordinates inherited from the User model
        double vendorLat = vendor.getLatitude();
        double vendorLng = vendor.getLongitude();
        double ngoLat    = ngo.getLatitude();
        double ngoLng    = ngo.getLongitude();

        return calculateDistanceKm(vendorLat, vendorLng, ngoLat, ngoLng);
    }

    /**
     * Generates a Google Maps link using actual coordinates for higher accuracy.
     */
    public String generateGoogleMapsLink(FoodListing listing, NGO ngo) {
        Vendor vendor = listing.getVendor();
        
        // Using coordinates in the URL is more reliable than addresses for rural areas
        return String.format(
            "https://www.google.com/maps/dir/?api=1&origin=%f,%f&destination=%f,%f&travelmode=driving",
            ngo.getLatitude(), ngo.getLongitude(),
            vendor.getLatitude(), vendor.getLongitude()
        );
    }

    /**
     * Estimates pickup time in minutes based on distance.
     */
    public int estimatePickupTimeMinutes(FoodListing listing, NGO ngo) {
        double distanceKm = getDistanceKm(listing, ngo);
        double hours = distanceKm / 40.0; // Average speed 40km/h
        return (int) Math.ceil(hours * 60);
    }

    /**
     * Returns a formatted route summary for the NGO dashboard.
     */
    public String getRouteSummary(FoodListing listing, NGO ngo) {
        double distance = getDistanceKm(listing, ngo);
        int eta         = estimatePickupTimeMinutes(listing, ngo);
        String mapsLink = generateGoogleMapsLink(listing, ngo);

        return String.format(
                "Pickup from: %s\nDistance: %.2f km\nEstimated Time: ~%d min\nNavigate: %s",
                listing.getVendor().getRestaurantName(),
                distance,
                eta,
                mapsLink
        );
    }

    private double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}