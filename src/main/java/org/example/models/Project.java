package org.example.models;

abstract class Project {
    private int id;
    private String name;
    private String description;
    private boolean budget;
    private int teamSize;


    public String getProjectDetails(int id) {
        return String.format("project details %s", id);
    }

    public void displayProject(int id) {
    }
}