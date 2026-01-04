package org.example.models;

public class RegularUser extends User {
    public  RegularUser(String name, String email) {
        super(name, email);
    }

    @Override
    public boolean canManageUsers() {
        return false;
    }
}