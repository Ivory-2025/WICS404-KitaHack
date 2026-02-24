package Services;
import com.google.maps.GeoApiContext;
import Models.*;
import DAO.FoodListingDAO;
import DAO.NGODAO;
import java.util.*;

public class MatchingService {
    private final GeoApiContext context;
    private final NGODAO ngoDAO = new NGODAO();
    private final FoodListingDAO foodListingDAO = new FoodListingDAO();

    public MatchingService() {
        // Correctly link your Google Maps API Key
        this.context = new GeoApiContext.Builder()
                .apiKey("AIzaSyALZCuSFnvOGWw6KLaSrti3v-kb3ulX6qM") 
                .build();
    }

    public List<NGO> findMatchingNGOs(FoodListing listing) {

    List<NGO> allNGOs = ngoDAO.getAllNGOs();
    List<NGO> matchedNGOs = new ArrayList<>();

    for (NGO ngo : allNGOs) {

        double distance = calculateDistanceKm(
                ngo.getLatitude(),
                ngo.getLongitude(),
                listing.getVendor().getLatitude(),
                listing.getVendor().getLongitude()
        );

        if (distance <= ngo.getRadiusCoverage()) {
            matchedNGOs.add(ngo);
        }
    }

    return matchedNGOs;
}

    public List<FoodListing> findMatchingListingsForNGO(NGO ngo) {
        List<FoodListing> allAvailable = foodListingDAO.getAvailableListings();
        List<FoodListing> filteredMatches = new ArrayList<>();

        for (FoodListing listing : allAvailable) {
            // Fix: Implementing the missing calculateDistanceKm method
            double distance = calculateDistanceKm(
                ngo.getLatitude(), ngo.getLongitude(),
                listing.getVendor().getLatitude(), listing.getVendor().getLongitude()
            );

            // Logic: Distance must be within NGO's specific radius
            if (distance <= ngo.getRadiusCoverage()) {
                filteredMatches.add(listing);
            }
        }
        return filteredMatches;
    }

    public boolean acceptListing(int listingId, NGO ngo) {
        FoodListing listing = foodListingDAO.getListingById(listingId);

        // Security check: Only 'available' listings can be claimed
        if (listing != null && listing.getStatus().equalsIgnoreCase("available")) {
            // Sync with your DAO to update database state
            return foodListingDAO.updateStatus(listingId, "claimed", ngo.getUserId());
        }
        return false;
    }

    /**
     * Missing Method: Calculates distance in Kilometers using Haversine formula.
     * This resolves the "cannot find symbol" error in your IDE.
     */
    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}