package Services;

import DAO.FoodListingDAO;
import DAO.NGODAO;
import Models.FoodListing;
import Models.NGO;

import java.util.ArrayList;
import java.util.List;

public class MatchingService {

    private final FoodListingDAO foodListingDAO;
    private final NGODAO ngoDAO;

    public MatchingService() {
        this.foodListingDAO = new FoodListingDAO();
        this.ngoDAO = new NGODAO();
    }

    /**
     * Given a food listing, find all NGOs that:
     * 1. Are within their own radius coverage of the vendor
     * 2. Have capacity > 0
     */
    public List<NGO> findMatchingNGOs(FoodListing listing) {
        List<NGO> allNGOs = ngoDAO.getAllNGOs();
        List<NGO> matchedNGOs = new ArrayList<>();

        double vendorLat = getLatFromAddress(listing.getVendor().getAddress());
        double vendorLng = getLngFromAddress(listing.getVendor().getAddress());

        for (NGO ngo : allNGOs) {
            double ngoLat = getLatFromAddress(ngo.getAddress());
            double ngoLng = getLngFromAddress(ngo.getAddress());

            double distance = calculateDistanceKm(vendorLat, vendorLng, ngoLat, ngoLng);

            boolean withinRadius = distance <= ngo.getRadiusCoverage();
            boolean hasCapacity  = ngo.getCapacity() > 0;

            if (withinRadius && hasCapacity) {
                matchedNGOs.add(ngo);
            }
        }

        return matchedNGOs;
    }

    /**
     * Given an NGO, find all AVAILABLE food listings that are within the NGO's radius.
     * This is what populates the NGO dashboard table.
     */
    public List<FoodListing> findMatchingListingsForNGO(NGO ngo) {
        List<FoodListing> availableListings = foodListingDAO.getAvailableListings();
        List<FoodListing> matchedListings = new ArrayList<>();

        double ngoLat = getLatFromAddress(ngo.getAddress());
        double ngoLng = getLngFromAddress(ngo.getAddress());

        for (FoodListing listing : availableListings) {
            double vendorLat = getLatFromAddress(listing.getVendor().getAddress());
            double vendorLng = getLngFromAddress(listing.getVendor().getAddress());

            double distance = calculateDistanceKm(ngoLat, ngoLng, vendorLat, vendorLng);

            if (distance <= ngo.getRadiusCoverage()) {
                matchedListings.add(listing);
            }
        }

        return matchedListings;
    }

    /**
     * Called when an NGO taps Accept.
     * First NGO to call this wins — listing is locked immediately.
     * Returns true if successful, false if someone else already accepted.
     */
    public boolean acceptListing(int listingId, NGO ngo) {
        FoodListing listing = foodListingDAO.getListingById(listingId);

        if (listing == null) {
            System.out.println("Listing not found.");
            return false;
        }

        if (!listing.getStatus().equalsIgnoreCase("available")) {
            System.out.println("Sorry! Another NGO already accepted this listing.");
            return false;
        }

        // Lock the listing — first-come-first-serve
        boolean updated = foodListingDAO.updateStatus(listingId, "matched");

        if (updated) {
            System.out.println("NGO " + ngo.getOrganizationName() + " accepted listing #" + listingId);
            return true;
        }

        return false;
    }

    /**
     * Haversine formula — calculates distance between two coordinates in km.
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

    // Placeholder coordinates — update once DB stores real lat/lng
    private double getLatFromAddress(String address) {
        return 3.1390;
    }

    private double getLngFromAddress(String address) {
        return 101.6869;
    }
}