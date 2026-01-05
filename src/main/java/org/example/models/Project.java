package org.example.models;

public abstract class Project {
    private String id;
    private String name;
    private String description;
    private double budget;
    private int teamSize;

    public Project(String name, String description, double budget, int teamSize) {
       this.name = name;
       this.description = description;
       this.budget = budget;
       this.teamSize = teamSize;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public abstract String getType();
}
