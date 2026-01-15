package org.example.services;

import org.example.exceptions.EmptyProjectException;
import org.example.exceptions.FileNotAvailableException;
import org.example.models.Project;
import org.example.utils.FileUtils;

public class DataPersistenceService {
    private final ProjectService projectService;

    public DataPersistenceService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public void saveProjectsData() throws FileNotAvailableException, EmptyProjectException {
        try {
            Project[] projects = projectService.getAllProjects();

            if (projects == null || projects.length == 0) {
                System.out.println("No projects to save.");
                return;
            }

            FileUtils.saveProjects(projects, projectService::getTasksForProject);
            System.out.println("Successfully saved " + projects.length + " project(s).");
        } catch (FileNotAvailableException | EmptyProjectException e) {
            System.err.println("Failed to save projects: " + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            throw new FileNotAvailableException("Runtime error during save: " + e.getMessage());
        }
    }

    public void loadProjectsData() throws FileNotAvailableException {
        try {
            boolean loaded = FileUtils.loadProjects(project -> {
                try {
                    projectService.addProject(project);
                } catch (Exception e) {
                    System.err.println(
                            "Error adding project " + project.getId() + ": " + e.getMessage());
                }
            }, (projectId, task) -> {
                try {
                    boolean added = projectService.addTaskToProject(projectId, task);
                    if (!added) {
                        System.err.println(
                                "Failed to add task " + task.getId() + " to project " + projectId);
                    }
                } catch (Exception e) {
                    System.err.println(
                            "Error adding task to project " + projectId + ": " + e.getMessage());
                }
            });

            if (loaded) {
                Project[] projects = projectService.getAllProjects();
                System.out.println("Successfully loaded " + projects.length + " project(s).");
            }
        } catch (FileNotAvailableException e) {
            System.err.println("Failed to load projects: " + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            throw new FileNotAvailableException("Runtime error during load: " + e.getMessage());
        }
    }
}
