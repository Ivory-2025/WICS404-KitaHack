package Services;

import Models.FoodListing;
import Models.NGO;

public class RoutingService {

    /**
     * Calculates the distance in km between the NGO and the vendor.
     */
    public double getDistanceKm(FoodListing listing, NGO ngo) {
        double vendorLat = getLatFromAddress(listing.getVendor().getAddress());
        double vendorLng = getLngFromAddress(listing.getVendor().getAddress());
        double ngoLat    = getLatFromAddress(ngo.getAddress());
        double ngoLng    = getLngFromAddress(ngo.getAddress());

        return calculateDistanceKm(vendorLat, vendorLng, ngoLat, ngoLng);
    }

    /**
     * Generates a Google Maps directions URL from NGO address to vendor address.
     * NGO can open this link to navigate to the pickup location.
     */
    public String generateGoogleMapsLink(FoodListing listing, NGO ngo) {
        String origin      = encodeAddress(ngo.getAddress());
        String destination = encodeAddress(listing.getVendor().getAddress());

        return "https://www.google.com/maps/dir/?api=1"
                + "&origin=" + origin
                + "&destination=" + destination
                + "&travelmode=driving";
    }

    /**
     * Estimates pickup time in minutes based on distance.
     * Assumes average urban driving speed of 40 km/h.
     */
    public int estimatePickupTimeMinutes(FoodListing listing, NGO ngo) {
        double distanceKm = getDistanceKm(listing, ngo);
        double hours = distanceKm / 40.0;
        return (int) Math.ceil(hours * 60);
    }

    /**
     * Returns a formatted route summary string.
     * Ready to display directly on the NGO dashboard label.
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

    // Haversine formula
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

    // Placeholder coordinates — update once DB stores real lat/lng
    private double getLatFromAddress(String address) {
        return 3.1390;
    }

    private double getLngFromAddress(String address) {
        return 101.6869;
    }

    private String encodeAddress(String address) {
        return address.trim().replace(" ", "+").replace(",", "%2C");
    }
}