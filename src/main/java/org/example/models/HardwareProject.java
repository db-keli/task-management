package org.example.models;

public class HardwareProject extends Project {
    public HardwareProject(String name, String description, double budget, int teamSize) {
        super(name, description, budget, teamSize);
    }

    @Override
    public String getType() {
        return "HardwareProject";
    }
}
