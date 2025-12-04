package org.example.models;

import org.example.enums.UserRoles;

public abstract class User {
    private int id;
    public String name;
    public String email;
    public UserRoles role;
}