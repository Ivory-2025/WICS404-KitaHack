package DAO;

import Models.User;

public interface UserDAOInt {
    void insertUser(User user);
    User getUserByEmail(String email);
    void updateUser(User user); // Added for profile/coordinate updates
    void deleteUser(int id);
    User login(String email, String password); // Added for security
}