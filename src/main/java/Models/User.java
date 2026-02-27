package Models;
public class User {
    private int UserId;
    private String name;
    private String email;
    private String password;
    private String role;
    // New coordinate fields for Routing and Matching Services
    private double latitude;
    private double longitude;

    // Constructor
    public User(int UserId, String name, String email, String password, String role) {
        this.UserId = UserId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Empty constructor
    // for javafx or database
    public User() {

    }

    // Getters & Setters
    public int getUserId() {
        return UserId;
    }

    public void setUserId(int Userid) {
        this.UserId = Userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

