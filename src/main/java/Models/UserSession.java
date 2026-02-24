package Models;

public class UserSession {
    private static UserSession instance;
    private Vendor currentVendor;
    private NGO currentNGO;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setVendor(Vendor vendor) { this.currentVendor = vendor; }
    public Vendor getVendor() { return currentVendor; }

    public void setNGO(NGO ngo) { this.currentNGO = ngo; }
    public NGO getNGO() { return currentNGO; }

    public void cleanUserSession() {
        currentVendor = null;
        currentNGO = null;
    }
}