package Models;

public class Vendor extends User {
    private String restaurantName;
    private String address;
    private double trustScore;

    //Constructor
    //for manual vendor object creation
    public Vendor(int id, String name, String email, String password, String role, 
                  String restaurantName, String address, double trustScore, 
                  double latitude, double longitude) {
        super(id, name, email, password, "VENDOR");
        this.setLatitude(latitude); // Inherited from User
        this.setLongitude(longitude); // Inherited from User
        this.restaurantName = restaurantName;
        this.address = address;
        this.trustScore = trustScore;
    }

    //Empty constructor
    //for javafx
    public Vendor() {

    }

    // Getters & Setters
    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(double trustScore) {
        this.trustScore = trustScore;
    }
}
