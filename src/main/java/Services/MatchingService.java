package Services;

import DAO.FoodListingDAO;
import DAO.NGODAO;
import Models.FoodListing;
import Models.NGO;
import Models.Vendor;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to match surplus food listings with nearby NGOs based on 
 * location proximity and organization capacity.
 */
public class MatchingService {

    private final FoodListingDAO foodListingDAO;
    private final NGODAO ngoDAO;

    public MatchingService() {
        this.foodListingDAO = new FoodListingDAO();
        this.ngoDAO = new NGODAO();
    }

    /**
     * Given a food listing, finds all NGOs that are within their own 
     * coverage radius and have remaining capacity.
     */
    public List<NGO> findMatchingNGOs(FoodListing listing) {
        List<NGO> allNGOs = ngoDAO.getAllNGOs();
        List<NGO> matchedNGOs = new ArrayList<>();

        // Get Vendor coordinates from the User model inheritance
        Vendor vendor = listing.getVendor();
        double vendorLat = vendor.getLatitude();
        double vendorLng = vendor.getLongitude();

        for (NGO ngo : allNGOs) {
            // Get NGO coordinates (Inherited from User)
            double ngoLat = ngo.getLatitude();
            double ngoLng = ngo.getLongitude();

            double distance = calculateDistanceKm(vendorLat, vendorLng, ngoLat, ngoLng);

            // Logic: Distance must be <= NGO's radius AND NGO must have capacity
            if (distance <= ngo.getRadiusCoverage() && ngo.getCapacity() > 0) {
                matchedNGOs.add(ngo);
            }
        }
        return matchedNGOs;
    }

    /**
     * Finds all available food listings within an NGO's operational radius.
     * This is used to populate the NGO's "Surplus Feed" dashboard.
     */
    public List<FoodListing> findMatchingListingsForNGO(NGO ngo) {
        List<FoodListing> availableListings = foodListingDAO.getAvailableListings();
        List<FoodListing> matchedListings = new ArrayList<>();

        double ngoLat = ngo.getLatitude();
        double ngoLng = ngo.getLongitude();

        for (FoodListing listing : availableListings) {
            Vendor vendor = listing.getVendor();
            double distance = calculateDistanceKm(ngoLat, ngoLng, vendor.getLatitude(), vendor.getLongitude());

            if (distance <= ngo.getRadiusCoverage()) {
                matchedListings.add(listing);
            }
        }
        return matchedListings;
    }

    /**
     * Logic for an NGO to accept a listing.
     * Updates status to 'matched' to prevent others from claiming it.
     */
    public boolean acceptListing(int listingId, NGO ngo) {
        FoodListing listing = foodListingDAO.getListingById(listingId);

        if (listing == null) {
            System.out.println("Listing not found.");
            return false;
        }

        // Only 'available' listings can be accepted
        if (!listing.getStatus().equalsIgnoreCase("available")) {
            System.out.println("Listing is no longer available.");
            return false;
        }

        return foodListingDAO.updateStatus(listingId, "matched");
    }

    /**
     * Haversine formula to calculate distance between two points on Earth.
     */
    public double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
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