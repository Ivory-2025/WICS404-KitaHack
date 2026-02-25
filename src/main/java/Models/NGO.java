package Models;

public class NGO extends User {
    private String organizationName;
    private String address;
    private double radiusCoverage;
    private int capacity;

    // Main Constructor
    public NGO(int id, String name, String email, String password, String role,
               String organizationName, String address, double radiusCoverage,
               int capacity, double latitude, double longitude) { 
        
        super(id, name, email, password, "NGO");
        this.setLatitude(latitude); 
        this.setLongitude(longitude);
        this.organizationName = organizationName;
        this.address = address;
        this.radiusCoverage = radiusCoverage;
        this.capacity = capacity;
    }

    // Empty constructor
    public NGO() {
    }

    // Getters & Setters
    public String getOrganizationName() {
        return organizationName != null ? organizationName : getName();
    }

    public void setOrganizationName(String organizationName) { 
        this.organizationName = organizationName; 
    }

    public String getAddress() { 
        return address; 
    }

    public void setAddress(String address) { 
        this.address = address; 
    }

    public double getRadiusCoverage() { 
        return radiusCoverage; 
    }

    public void setRadiusCoverage(double radiusCoverage) { 
        this.radiusCoverage = radiusCoverage; 
    }

    public int getCapacity() { 
        return capacity; 
    }

    public void setCapacity(int capacity) { 
        this.capacity = capacity; 
    }

    private int id; // Ensure this matches your ngo_id column

    // Add this method to clear the error at line 148
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
} 