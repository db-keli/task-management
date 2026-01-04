package org.example.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.enums.ModelType;
import org.example.models.HardwareProject;
import org.example.models.Project;
import org.example.models.SoftwareProject;
import org.example.models.Task;
import org.example.utils.IdCounterManager;

public class ProjectService {
    private Project[] projects = new Project[100];
    private int projectCount = 0;
    private final Map<String, List<Task>> projectTasks = new HashMap<>();
    private final Map<String, List<String>> projectAssignedUsers = new HashMap<>();
    private final IdCounterManager idManager;

    public ProjectService() {
        this.idManager = IdCounterManager.getInstance();
    }

    public String getNextProjectId() {
        return idManager.getNextId(ModelType.PROJECT);
    }

    public Project createProject(String type, String name, String description, double budget,
            int teamSize) {
        Project project;
        if ("Software".equalsIgnoreCase(type)) {
            project = new SoftwareProject(name, description, budget, teamSize);
        } else {
            project = new HardwareProject(name, description, budget, teamSize);
        }
        project.setId(getNextProjectId());
        return project;
    }

    public boolean addProject(Project project) {
        if (project == null) {
            return false;
        }
        if (projectCount >= projects.length) {
            return false;
        }
        // Assign ID if not already set
        if (project.getId() == null || project.getId().isEmpty()) {
            project.setId(getNextProjectId());
        }
        projects[projectCount++] = project;
        return true;
    }

    public Project getProjectById(String id) {
        if (id == null)
            return null;
        for (int i = 0; i < projectCount; i++) {
            if (projects[i] != null && id.equals(projects[i].getId())) {
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
        if (type == null)
            return new Project[0];
        int count = 0;
        for (int i = 0; i < projectCount; i++) {
            if (projects[i] != null && projects[i].getType().equalsIgnoreCase(type)) {
                count++;
            }
        }
        Project[] filtered = new Project[count];
        int index = 0;
        for (int i = 0; i < projectCount; i++) {
            if (projects[i] != null && projects[i].getType().equalsIgnoreCase(type)) {
                filtered[index++] = projects[i];
            }
        }
        return filtered;
    }

    public Project[] filterByBudget(double min, double max) {
        int count = 0;
        for (int i = 0; i < projectCount; i++) {
            if (projects[i] != null && projects[i].getBudget() >= min
                    && projects[i].getBudget() <= max) {
                count++;
            }
        }
        Project[] filtered = new Project[count];
        int index = 0;
        for (int i = 0; i < projectCount; i++) {
            if (projects[i] != null && projects[i].getBudget() >= min
                    && projects[i].getBudget() <= max) {
                filtered[index++] = projects[i];
            }
        }
        return filtered;
    }

    public boolean deleteProject(String id) {
        if (id == null)
            return false;
        for (int i = 0; i < projectCount; i++) {
            if (projects[i] != null && id.equals(projects[i].getId())) {
                for (int j = i; j < projectCount - 1; j++) {
                    projects[j] = projects[j + 1];
                }
                projects[projectCount - 1] = null;
                projectCount--;
                projectTasks.remove(id);
                projectAssignedUsers.remove(id);
                return true;
            }
        }
        return false;
    }

    public boolean addTaskToProject(String projectId, Task task) {
        if (projectId == null || task == null) {
            return false;
        }
        if (getProjectById(projectId) == null) {
            return false;
        }
        projectTasks.computeIfAbsent(projectId, k -> new ArrayList<>()).add(task);
        return true;
    }

    public Task[] getTasksForProject(String projectId) {
        if (projectId == null) {
            return new Task[0];
        }
        List<Task> tasks = projectTasks.get(projectId);
        if (tasks == null) {
            return new Task[0];
        }
        return tasks.toArray(new Task[0]);
    }

    public boolean removeTaskFromProject(String projectId, String taskId) {
        if (projectId == null || taskId == null) {
            return false;
        }
        List<Task> tasks = projectTasks.get(projectId);
        if (tasks == null) {
            return false;
        }
        return tasks.removeIf(task -> task != null && taskId.equals(task.getId()));
    }

    public double getProjectCompletionPercentage(String projectId) {
        Task[] tasks = getTasksForProject(projectId);
        if (tasks.length == 0) {
            return 0.0;
        }
        int completed = 0;
        for (Task task : tasks) {
            if (task != null && task.isCompleted()) {
                completed++;
            }
        }
        return completed / (double) tasks.length;
    }

    public boolean assignUserToProject(String projectId, String userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        if (getProjectById(projectId) == null) {
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
}
