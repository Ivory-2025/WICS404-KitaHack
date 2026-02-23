package DAO;
import Models.Vendor;

public interface VendorDAO extends GenericDAO<Vendor, Integer> {
    void updateTrustScore(int vendorId, double newScore);
}
