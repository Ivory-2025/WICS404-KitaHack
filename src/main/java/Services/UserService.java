package Services;

import DAO.UserDAOInt;
import Models.NGO;
import Models.User;
import Models.Vendor;

public class UserService {
    private final DAO.UserDAOInt userDAO;

    public UserService() {
        // Initialize the actual DAO implementation here
        this.userDAO = new DAO.UserDAOImpl(); 
    }

    // Constructor that takes the Interface to allow flexibility
    public UserService(UserDAOInt userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Handles the login logic by checking credentials against the database.
     */
    // Inside UserService.java, around line 21
public User login(String email, String password) {
    User user = userDAO.getUserByEmail(email);
    
    // Check if user exists AND password is not null before comparing
    if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
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