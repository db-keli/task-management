package org.example.services;

import org.example.enums.ModelType;
import org.example.models.AdminUser;
import org.example.models.RegularUser;
import org.example.models.User;
import org.example.utils.IdCounterManager;

public class UserService {
    private User[] users = new User[100];
    private int userCount = 0;
    private User currentUser;
    private final IdCounterManager idManager;

    public UserService() {
        this.idManager = IdCounterManager.getInstance();
        // Initialize with default users
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        // Create default admin user
        AdminUser admin = new AdminUser("Admin", "admin@example.com");
        admin.setId(idManager.getNextId(ModelType.USER));
        users[userCount++] = admin;

        // Create default regular user
        RegularUser regular = new RegularUser("Regular User", "user@example.com");
        regular.setId(idManager.getNextId(ModelType.USER));
        users[userCount++] = regular;

        // Set admin as default current user
        currentUser = admin;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User[] getAllUsers() {
        User[] all = new User[userCount];
        System.arraycopy(users, 0, all, 0, userCount);
        return all;
    }

    public User getUserByEmail(String email) {
        if (email == null)
            return null;
        for (int i = 0; i < userCount; i++) {
            if (users[i] != null && email.equalsIgnoreCase(users[i].getEmail())) {
                return users[i];
            }
        }
        return null;
    }

    public boolean addUser(User user) {
        if (user == null) {
            return false;
        }
        if (userCount >= users.length) {
            return false;
        }
        // Check for duplicate email
        if (user.getEmail() != null && getUserByEmail(user.getEmail()) != null) {
            return false;
        }
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(idManager.getNextId(ModelType.USER));
        }
        users[userCount++] = user;
        return true;
    }

    public boolean switchUser(String email) {
        User user = getUserByEmail(email);
        if (user != null) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public boolean deleteUser(String email) {
        if (email == null)
            return false;
        // Prevent deleting the current user
        if (currentUser != null && email.equalsIgnoreCase(currentUser.getEmail())) {
            return false;
        }
        for (int i = 0; i < userCount; i++) {
            if (users[i] != null && email.equalsIgnoreCase(users[i].getEmail())) {
                // Shift remaining users to fill the gap
                for (int j = i; j < userCount - 1; j++) {
                    users[j] = users[j + 1];
                }
                users[userCount - 1] = null;
                userCount--;
                return true;
            }
        }
        return false;
    }

    public User getUserById(String userId) {
        if (userId == null)
            return null;
        for (int i = 0; i < userCount; i++) {
            if (users[i] != null && userId.equals(users[i].getId())) {
                return users[i];
            }
        }
        return null;
    }

    public User createUser(String name, String email, boolean isAdmin) {
        User user = isAdmin ? new AdminUser(name, email) : new RegularUser(name, email);
        user.setId(idManager.getNextId(ModelType.USER));
        return user;
    }
}
