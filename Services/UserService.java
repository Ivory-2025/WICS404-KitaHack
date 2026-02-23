package Services;

import DAO.UserDAOInt;
import Models.User;
import Models.Vendor;
import Models.NGO;

public class UserService {
    private final UserDAOInt userDAO;

    // Constructor that takes the Interface to allow flexibility
    public UserService(UserDAOInt userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Handles the login logic by checking credentials against the database.
     */
    public User login(String email, String password) {
        if (email == null || password == null) return null;
        
        User user = userDAO.getUserByEmail(email);
        
        // Verify password (in a real app, use hashing!)
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Registers a new user and ensures the role is set correctly.
     */
    public boolean registerUser(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return false;
        }

        try {
            userDAO.insertUser(user);
            return true;
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a user account by ID.
     */
    public void deleteAccount(int userId) {
        userDAO.deleteUser(userId);
    }

    /**
     * Utility to check the specific type of user logged in.
     */
    public String identifyUserType(User user) {
        if (user instanceof Vendor) return "VENDOR";
        if (user instanceof NGO) return "NGO";
        return "GENERAL_USER";
    }
}