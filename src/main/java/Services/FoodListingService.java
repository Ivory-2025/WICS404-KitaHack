package Services;

import java.util.List;
import java.util.ArrayList;
import Models.FoodListing;

public class FoodListingService {
    private List<FoodListing> listings; // the "database"

    public FoodListingService() {
        listings = new ArrayList<>();
    }

    // Create / Add
    public void addListing(FoodListing listing) {
        listings.add(listing);
    }

    // Read / Find
    public FoodListing findById(int id) {
        for (FoodListing listing : listings) {
            if (listing.getListingId() == id) {
                return listing;
            }
        }
        return null;
    }

    // Update
    public boolean update(FoodListing updatedListing) {
        for (int i = 0; i < listings.size(); i++) {
            if (listings.get(i).getListingId() == updatedListing.getListingId()) {
                listings.set(i, updatedListing); // replace the old listing
                return true; // success
            }
        }
        return false; // not found
    }

    // Delete
    public boolean delete(int id) {
        return listings.removeIf(listing -> listing.getListingId() == id);
    }
}