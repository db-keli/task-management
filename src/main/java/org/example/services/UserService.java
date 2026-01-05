package org.example.services;

import org.example.enums.ModelType;
import org.example.exceptions.InvalidEmailException;
import org.example.exceptions.InvalidRoleException;
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
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        try {
            AdminUser admin = new AdminUser("Admin", "admin@example.com");
            admin.setId(idManager.getNextId(ModelType.USER));
            users[userCount++] = admin;

            RegularUser regular = new RegularUser("Regular User", "user@example.com");
            regular.setId(idManager.getNextId(ModelType.USER));
            users[userCount++] = regular;

            currentUser = admin;
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Failed to initialize default users: " + e.getMessage());
        }
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

    public boolean addUser(User user) throws InvalidEmailException {
        if (user == null) {
            return false;
        }
        if (userCount >= users.length) {
            return false;
        }
        
        // Validate email
        validateEmail(user.getEmail());
        
        // Check for duplicate email
        if (getUserByEmail(user.getEmail()) != null) {
            throw new InvalidEmailException("Email already exists: " + user.getEmail());
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

    public User createUser(String name, String email, boolean isAdmin) throws InvalidEmailException {
        validateEmail(email);
        
        User user = isAdmin ? new AdminUser(name, email) : new RegularUser(name, email);
        user.setId(idManager.getNextId(ModelType.USER));
        return user;
    }

    public void validateEmail(String email) throws InvalidEmailException {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidEmailException("Email cannot be null or empty");
        }
        
        String emailPattern = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
        if (!email.matches(emailPattern)) {
            throw new InvalidEmailException("Invalid email format. Expected format: user@example.com");
        }
    }

    public boolean validateRole(String role) throws InvalidRoleException {
        if (role == null || role.trim().isEmpty()) {
            throw new InvalidRoleException("Role cannot be null or empty");
        }
        
        String normalizedRole = role.trim().toLowerCase();
        if (!normalizedRole.equals("admin") && !normalizedRole.equals("regular") && 
            !normalizedRole.equals("adminuser") && !normalizedRole.equals("regularuser")) {
            throw new InvalidRoleException("Invalid role. Role must be 'admin' or 'regular'");
        }
        
        return normalizedRole.equals("admin") || normalizedRole.equals("adminuser");
    }
}
