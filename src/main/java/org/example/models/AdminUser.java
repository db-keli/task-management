package org.example.models;

import org.example.enums.UserRoles;

public class AdminUser extends User {
    public AdminUser(String name, String email, UserRoles role) {
        super(name, email, role);
    }

    @Override
    public boolean canManageUsers() {
        return false;
    }
}
