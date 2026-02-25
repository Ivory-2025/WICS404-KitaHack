package Models;

public class OrderResponse {
    private FoodListing listing;
    private NGO ngo;
    private String status; // Accepted / Rejected

    public OrderResponse(FoodListing listing, NGO ngo, String status) {
        this.listing = listing;
        this.ngo = ngo;
        this.status = status;
    }

    public FoodListing getListing() { return listing; }
    public NGO getNgo() { return ngo; }
    public String getStatus() { return status; }

    public void setListing(FoodListing listing) { this.listing = listing; }
    public void setNgo(NGO ngo) { this.ngo = ngo; }
    public void setStatus(String status) { this.status = status; }
}