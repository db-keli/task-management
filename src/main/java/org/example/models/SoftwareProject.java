package org.example.models;

public class SoftwareProject extends Project {
    public SoftwareProject(String name, String description, double budget, int teamSize) {
        super(name, description, budget, teamSize);
    }
    @Override
    public String getType() {
        return "SoftwareProject";
    }
}