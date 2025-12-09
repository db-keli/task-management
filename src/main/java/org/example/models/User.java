package org.example.models;

import org.example.enums.UserRoles;

public abstract class User {
    private String id;
    public String name;
    public String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public abstract boolean canManageUsers();
}