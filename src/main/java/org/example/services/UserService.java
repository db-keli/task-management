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
        for (int i = 0; i < userCount; i++) {
            if (users[i].getEmail().equalsIgnoreCase(email)) {
                return users[i];
            }
        }
        return null;
    }

    public void addUser(User user) {
        if (userCount < users.length) {
            if (user.getId() == null || user.getId().isEmpty()) {
                user.setId(idManager.getNextId(ModelType.USER));
            }
            users[userCount++] = user;
        } else {
            System.out.println("Maximum users reached.");
        }
    }

    public boolean switchUser(String email) {
        User user = getUserByEmail(email);
        if (user != null) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public void displayUsers() {
        System.out.println("\nAvailable Users:");
        System.out.println("ID | Name | Email | Role");
        System.out.println("------------------------");
        for (int i = 0; i < userCount; i++) {
            User u = users[i];
            String role = u instanceof AdminUser ? "Admin" : "Regular";
            System.out.println(u.getId() + " | " + u.getName() + " | " + u.getEmail() + " | " + role);
        }
    }
}
