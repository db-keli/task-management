package org.example.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.enums.ModelType;
import org.example.exceptions.InvalidProjectDataException;
import org.example.exceptions.ProjectNotFoundException;
import org.example.exceptions.TaskNotFoundException;
import org.example.models.HardwareProject;
import org.example.models.Project;
import org.example.models.SoftwareProject;
import org.example.models.Task;
import org.example.utils.IdCounterManager;

public class ProjectService {
    private final Map<String, Project> projects = new HashMap<>();
    private final Map<String, List<Task>> projectTasks = new HashMap<>();
    private final Map<String, List<String>> projectAssignedUsers = new HashMap<>();
    private final IdCounterManager idManager;

    public ProjectService() {
        this.idManager = IdCounterManager.getInstance();
    }

    public String getNextProjectId() throws InvalidProjectDataException {
        try {
            return idManager.getNextId(ModelType.PROJECT);
        } catch (IllegalArgumentException e) {
            throw new InvalidProjectDataException(
                    "Failed to generate project ID: " + e.getMessage());
        }
    }

    public Project createProject(String type, String name, String description, double budget,
            int teamSize) throws InvalidProjectDataException {
        if (budget <= 0) {
            throw new InvalidProjectDataException("Budget must be positive. Provided: " + budget);
        }

        Project project;
        if ("Software".equalsIgnoreCase(type)) {
            project = new SoftwareProject(name, description, budget, teamSize);
        } else {
            project = new HardwareProject(name, description, budget, teamSize);
        }
        project.setId(getNextProjectId());
        return project;
    }

    public boolean addProject(Project project) throws InvalidProjectDataException {
        if (project == null) {
            throw new InvalidProjectDataException("Project cannot be null");
        }

        if (project.getBudget() <= 0) {
            throw new InvalidProjectDataException(
                    "Budget must be positive. Provided: " + project.getBudget());
        }

        if (project.getId() == null || project.getId().isEmpty()) {
            project.setId(getNextProjectId());
        } else {
            if (projectExists(project.getId())) {
                throw new InvalidProjectDataException(
                        "Project ID already exists: " + project.getId());
            }
        }

        projects.put(project.getId(), project);
        return true;
    }

    public Project getProjectById(String id) throws ProjectNotFoundException {
        if (id == null) {
            throw new ProjectNotFoundException("Project ID cannot be null");
        }

        Project project = projects.get(id);
        if (project == null) {
            throw new ProjectNotFoundException("Project ID '" + id + "' does not exist");
        }
        return project;
    }

    private boolean projectExists(String id) {
        return id != null && projects.containsKey(id);
    }

    public Project[] getAllProjects() {
        return projects.values().stream().toArray(Project[]::new);
    }

    public Project[] filterByType(String type) {
        if (type == null) {
            return new Project[0];
        }
        final String typeFilter = type;
        return projects.values().stream()
                .filter(project -> project.getType().equalsIgnoreCase(typeFilter))
                .toArray(Project[]::new);
    }

    public Project[] filterByBudget(double min, double max) {
        return projects.values().stream()
                .filter(project -> project.getBudget() >= min && project.getBudget() <= max)
                .toArray(Project[]::new);
    }

    public boolean deleteProject(String id) {
        if (id == null) {
            return false;
        }
        Project removed = projects.remove(id);
        if (removed != null) {
            projectTasks.remove(id);
            projectAssignedUsers.remove(id);
            return true;
        }
        return false;
    }



    public synchronized boolean addTaskToProject(String projectId, Task task)
            throws ProjectNotFoundException {
        if (projectId == null || task == null) {
            return false;
        }
        try {
            getProjectById(projectId);
        } catch (ProjectNotFoundException e) {
            return false;
        }
        projectTasks.computeIfAbsent(projectId, k -> new ArrayList<>()).add(task);
        return true;
    }



    public synchronized Task[] getTasksForProject(String projectId) {
        if (projectId == null) {
            return new Task[0];
        }
        List<Task> tasks = projectTasks.get(projectId);
        if (tasks == null) {
            return new Task[0];
        }
        return tasks.toArray(new Task[0]);
    }



    public synchronized boolean removeTaskFromProject(String projectId, String taskId)
            throws TaskNotFoundException, ProjectNotFoundException {
        if (projectId == null || taskId == null) {
            throw new IllegalArgumentException("Project ID and Task ID cannot be null");
        }

        getProjectById(projectId);

        List<Task> tasks = projectTasks.get(projectId);
        if (tasks == null) {
            throw new TaskNotFoundException("No tasks found for project: " + projectId);
        }

        boolean removed = tasks.removeIf(task -> task != null && taskId.equals(task.getId()));
        if (!removed) {
            throw new TaskNotFoundException(
                    "Task not found with ID: " + taskId + " in project: " + projectId);
        }

        return true;
    }

    public double getProjectCompletionPercentage(String projectId) {
        Task[] tasks = getTasksForProject(projectId);
        if (tasks.length == 0) {
            return 0.0;
        }
        long completed =
                Arrays.stream(tasks).filter(task -> task != null && task.isCompleted()).count();
        return completed / (double) tasks.length;
    }

    public boolean assignUserToProject(String projectId, String userId)
            throws ProjectNotFoundException {
        if (projectId == null || userId == null) {
            return false;
        }
        try {
            getProjectById(projectId);
        } catch (ProjectNotFoundException e) {
            return false;
        }
        List<String> assignedUsers =
                projectAssignedUsers.computeIfAbsent(projectId, k -> new ArrayList<>());
        if (!assignedUsers.contains(userId)) {
            assignedUsers.add(userId);
            return true;
        }
        return false;
    }

    public boolean removeUserFromProject(String projectId, String userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        List<String> assignedUsers = projectAssignedUsers.get(projectId);
        if (assignedUsers != null) {
            return assignedUsers.remove(userId);
        }
        return false;
    }

    public String[] getAssignedUserIdsForProject(String projectId) {
        if (projectId == null) {
            return new String[0];
        }
        List<String> assignedUsers = projectAssignedUsers.get(projectId);
        if (assignedUsers == null) {
            return new String[0];
        }
        return assignedUsers.toArray(new String[0]);
    }



    public Project[] findCompletedProjects() {
        return projects.values().stream().filter(project -> {
            double completion = getProjectCompletionPercentage(project.getId());
            return completion >= 1.0;
        }).toArray(Project[]::new);
    }



    public Project[] getAllProjectsSortedByBudget() {
        return projects.values().stream()
                .sorted(java.util.Comparator.comparingDouble(Project::getBudget))
                .toArray(Project[]::new);
    }



    public Project[] getAllProjectsSortedByBudgetDescending() {
        return projects.values().stream()
                .sorted(java.util.Comparator.comparingDouble(Project::getBudget).reversed())
                .toArray(Project[]::new);
    }



    public double getTotalBudget() {
        return projects.values().stream().mapToDouble(Project::getBudget).sum();
    }



    public double getAverageBudget() {
        return projects.values().stream().mapToDouble(Project::getBudget).average().orElse(0.0);
    }



    public long countProjectsByType(String type) {
        if (type == null) {
            return 0;
        }
        final String typeFilter = type;
        return projects.values().stream()
                .filter(project -> project.getType().equalsIgnoreCase(typeFilter)).count();
    }
}
