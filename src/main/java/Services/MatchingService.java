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

    double vLat = listing.getVendor().getLatitude();
    double vLon = listing.getVendor().getLongitude();

    System.out.println("\n🔍 SMART MATCHING START");
    System.out.println("📍 Vendor Location: " + vLat + ", " + vLon);
    System.out.println("📂 Total NGOs in DB: " + allNGOs.size());

    for (NGO ngo : allNGOs) {
        double distance = calculateDistanceKm(vLat, vLon, ngo.getLatitude(), ngo.getLongitude());
        
        // Debugging logs to identify the "0" result
        System.out.println("🏢 Checking: " + ngo.getOrganizationName());
        System.out.println("   📏 Distance: " + String.format("%.2f", distance) + " km");
        System.out.println("   🎯 Required Radius: " + ngo.getRadiusCoverage() + " km");

        if (distance <= ngo.getRadiusCoverage()) {
            System.out.println("   ✅ MATCH FOUND!");
            matchedNGOs.add(ngo);
        } else {
            System.out.println("   ❌ TOO FAR");
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

    public boolean acceptListing(int listingId, NGO ngo) {

    FoodListing listing = foodListingDAO.findById(listingId);

    if (listing == null) return false;

    if (listing.isLocked()) {
        return false;
    }

    listing.setLocked(true);
    listing.setAcceptedByUserId(ngo.getUserId());
    listing.setStatus("accepted");

    foodListingDAO.update(listing);

    return true;
}
}