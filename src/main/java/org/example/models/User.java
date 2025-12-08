package org.example.models;

import org.example.enums.UserRoles;

public abstract class User {
    private String id;
    public String name;
    public String email;
    public UserRoles role;

    public User(String name, String email, UserRoles role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public UserRoles getRole() {
        return role;
    }
    public void setRole(UserRoles role) {
        this.role = role;
    }

    public abstract boolean canManageUsers();
}