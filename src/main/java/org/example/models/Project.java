package org.example.models;

public abstract class Project {
    private String id;
    private String name;
    private String description;
    private double budget;
    private int teamSize;
    private Task[] tasks = new Task[100];
    private int taskCount=0;

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

    public String getProjectDetails(int id) {
        return String.format("project details %s", id);
    }

    public void displayProject(int id) {
    }

    public void addTask(Task task) {
        if(taskCount < tasks.length) {
            tasks[taskCount++] = task;
        } else {
            System.err.println("Task limit reached");
        }
    }

    public Task[] getTasks() {
        Task[] activeTasks = new Task[taskCount];
        System.arraycopy(tasks, 0, activeTasks, 0, taskCount);
        return activeTasks;
    }

    public boolean removeTask(String taskId) {
        for (int i = 0; i < taskCount; i++) {
            if (tasks[i].getId().equals(taskId)) {
                // Shift remaining tasks to fill the gap
                for (int j = i; j < taskCount - 1; j++) {
                    tasks[j] = tasks[j + 1];
                }
                tasks[taskCount - 1] = null;
                taskCount--;
                return true;
            }
        }
        return false;
    }

    public double getCompletionPercentage() {
        if (taskCount == 0) return 0.0;
        int completed = 0;
        for (Task task : tasks) {
            if(task.isCompleted()) {
                completed++;
            }
        }

        return completed / (double)taskCount;
    }

    public abstract String getType();
}