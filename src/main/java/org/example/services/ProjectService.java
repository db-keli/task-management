package org.example.services;

import org.example.models.Project;

public class ProjectService {
    private Project[] projects = new Project[100];
    private int projectCount = 0;
    private static int idCounter = 1;

    public String getNextProjectId() {
        return "P" + String.format("%03d", idCounter++);
    }

    public void addProject(Project project) {
        if (projectCount < projects.length) {
            projects[projectCount++] = project;
        } else {
            System.out.println("Maximum projects reached.");
        }
    }

    public Project getProjectById(String id) {
        for (int i = 0; i < projectCount; i++) {
            if (projects[i].getId().equals(id)) {
                return projects[i];
            }
        }
        return null;
    }

    public Project[] getAllProjects() {
        Project[] all = new Project[projectCount];
        System.arraycopy(projects, 0, all, 0, projectCount);
        return all;
    }

    public Project[] filterByType(String type) {
        int count = 0;
        for (int i = 0; i < projectCount; i++) {
            if (projects[i].getType().equalsIgnoreCase(type)) {
                count++;
            }
        }
        Project[] filtered = new Project[count];
        int index = 0;
        for (int i = 0; i < projectCount; i++) {
            if (projects[i].getType().equalsIgnoreCase(type)) {
                filtered[index++] = projects[i];
            }
        }
        return filtered;
    }

    public Project[] filterByBudget(double min, double max) {
        int count = 0;
        for (int i = 0; i < projectCount; i++) {
            if (projects[i].getBudget() >= min && projects[i].getBudget() <= max) {
                count++;
            }
        }
        Project[] filtered = new Project[count];
        int index = 0;
        for (int i = 0; i < projectCount; i++) {
            if (projects[i].getBudget() >= min && projects[i].getBudget() <= max) {
                filtered[index++] = projects[i];
            }
        }
        return filtered;
    }
}