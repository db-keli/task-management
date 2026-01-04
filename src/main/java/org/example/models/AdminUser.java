package org.example.models;

public class AdminUser extends User {
    public AdminUser(String name, String email) {
        super(name, email);
    }

    @Override
    public boolean canManageUsers() {
        return true;
    }
}
