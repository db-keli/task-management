package org.example.models;

import org.example.enums.UserRoles;

public class RegularUser extends User {
    public  RegularUser(String name, String email, UserRoles role) {
        super(name, email, role);
    }

    @Override
    public boolean canManageUsers() {
        return false;
    }
}