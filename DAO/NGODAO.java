package DAO;
import Models.NGO;
import java.util.List;

public interface NGODAO extends GenericDAO<NGO, Integer> {
    // Specific method to find NGOs within a certain radius
    List<NGO> findNGOsInRange(double lat, double lon);
}
