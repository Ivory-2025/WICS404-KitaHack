package DAO;
import Models.User;

public interface UserDAO extends GenericDAO<User, Integer> {
    User login(String email, String password);
}
