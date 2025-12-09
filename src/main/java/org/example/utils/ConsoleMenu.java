package org.example.utils;

import org.example.enums.Status;
import org.example.models.AdminUser;
import org.example.models.HardwareProject;
import org.example.models.Project;
import org.example.models.SoftwareProject;
import org.example.models.Task;
import org.example.models.User;
import org.example.services.ProjectService;
import org.example.services.ReportService;
import org.example.services.TaskService;
import org.example.services.UserService;

public class ConsoleMenu {
    private boolean exit = false;
    private final UserService userService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final ReportService reportService;

    public ConsoleMenu() {
        this.userService = new UserService();
        this.projectService = new ProjectService();
        this.taskService = new TaskService();
        this.reportService = new ReportService(projectService);
        
        displayWelcomeMessage();
        displayMainMenu();
        
        while (!exit) {
            int choice = ValidationUtils.readInt("Enter your choice: ", 1, 5);
            processMainMenuChoice(choice);
        }
        
        ValidationUtils.close();
    }

    private void displayWelcomeMessage() {
        User currentUser = userService.getCurrentUser();
        System.out.println("\n========================================");
        System.out.println("   TASK MANAGEMENT SYSTEM");
        System.out.println("========================================");
        System.out.println("Logged in as: " + currentUser.getName() + " (" + currentUser.getEmail() + ")");
        System.out.println("Role: " + (currentUser instanceof AdminUser ? "Admin" : "Regular User"));
        System.out.println("========================================\n");
    }

    private void displayMainMenu() {
        String mainMenu = """
                Main Menu:
                ___________
                1. Main Projects
                2. Manage Tasks
                3. View Status Reports
                4. Switch User
                5. Exit
                """;
        System.out.println(mainMenu);
    }

    private void processMainMenuChoice(int input) {
        switch (input) {
            case 1 -> handleProjectsMenu();
            case 2 -> handleTasksMenu();
            case 3 -> handleStatusReports();
            case 4 -> handleSwitchUser();
            case 5 -> {
                System.out.println("Exiting program...");
                exit = true;
            }
            default -> {
                System.out.println("Invalid choice! Please select 1–5.");
            }
        }

        if (!exit) {
            System.out.println();
            displayMainMenu();
        }
    }

    private void handleProjectsMenu() {
        System.out.println("\n→ Opening Main Projects...\n");
        boolean backToMain = false;

        while (!backToMain) {
            System.out.println("PROJECT CATALOG");
            System.out.println("===============\n");
            System.out.println("Filter Options:");
            System.out.println("1. View All Projects");
            System.out.println("2. Software Projects Only");
            System.out.println("3. Hardware Projects Only");
            System.out.println("4. Search by Budget Range");
            System.out.println("5. Create New Project");
            System.out.println("6. Back to Main Menu\n");

            int choice = ValidationUtils.readInt("Enter filter choice: ", 1, 6);

            Project[] projectsToDisplay = null;
            String filterType = "";

            switch (choice) {
                case 1 -> {
                    projectsToDisplay = projectService.getAllProjects();
                    filterType = "All Projects";
                }
                case 2 -> {
                    projectsToDisplay = projectService.filterByType("Software");
                    filterType = "Software Projects";
                }
                case 3 -> {
                    projectsToDisplay = projectService.filterByType("Hardware");
                    filterType = "Hardware Projects";
                }
                case 4 -> {
                    double minBudget = ValidationUtils.readInt("Enter minimum budget: ", 0, Integer.MAX_VALUE);
                    double maxBudget = ValidationUtils.readInt("Enter maximum budget: ", (int) minBudget, Integer.MAX_VALUE);
                    projectsToDisplay = projectService.filterByBudget(minBudget, maxBudget);
                    filterType = "Projects with Budget $" + minBudget + " - $" + maxBudget;
                }
                case 5 -> {
                    createNewProject();
                    continue;
                }
                case 6 -> {
                    backToMain = true;
                    continue;
                }
            }

            if (projectsToDisplay.length == 0) {
                System.out.println("\nNo projects found for: " + filterType);
                System.out.println();
                continue;
            }

            displayProjectsTable(projectsToDisplay, filterType);
            System.out.println("\nEnter Project ID to view details (or 'back' to return): ");
            String projectId = ValidationUtils.readNonEmptyString("");
            
            if (projectId.equalsIgnoreCase("back")) {
                continue;
            }

            Project project = projectService.getProjectById(projectId);
            if (project != null) {
                displayProjectDetails(project);
            } else {
                System.out.println("Project not found with ID: " + projectId);
            }
            System.out.println();
        }
    }

    private void displayProjectsTable(Project[] projects, String filterType) {
        System.out.println("\n" + filterType + ":");
        System.out.println("─────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-12s | %-20s | %-12s | %-10s | %-10s%n", 
            "PROJECT ID", "PROJECT NAME", "TYPE", "BUDGET", "TEAM SIZE");
        System.out.println("─────────────────────────────────────────────────────────────────────────");
        
        for (Project p : projects) {
            System.out.printf("%-12s | %-20s | %-12s | $%-9.2f | %-10d%n",
                p.getId(), p.getName(), p.getType(), p.getBudget(), p.getTeamSize());
        }
        System.out.println("─────────────────────────────────────────────────────────────────────────");
    }

    private void displayProjectDetails(Project project) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PROJECT DETAILS: " + project.getId());
        System.out.println("=".repeat(60));
        System.out.println("Project Name: " + project.getName());
        System.out.println("Type: " + project.getType());
        System.out.println("Description: " + project.getDescription());
        System.out.println("Team Size: " + project.getTeamSize());
        System.out.printf("Budget: $%.2f%n", project.getBudget());
        
        Task[] tasks = project.getTasks();
        System.out.println("\nAssociated Tasks:");
        if (tasks.length == 0) {
            System.out.println("  No tasks assigned.");
        } else {
            System.out.println("─────────────────────────────────────────────");
            System.out.printf("%-12s | %-20s | %-15s%n", "TASK ID", "TASK NAME", "STATUS");
            System.out.println("─────────────────────────────────────────────");
            for (Task task : tasks) {
                System.out.printf("%-12s | %-20s | %-15s%n",
                    task.getId(), task.getName(), task.getStatus());
            }
            System.out.println("─────────────────────────────────────────────");
        }
        
        double completionRate = project.getCompletionPercentage();
        System.out.printf("%nCompletion Rate: %.1f%%%n", completionRate * 100);
        System.out.println("=".repeat(60));
    }

    private void createNewProject() {
        System.out.println("\nCreate New Project");
        System.out.println("──────────────────");
        
        String name = ValidationUtils.readNonEmptyString("Project Name: ");
        String description = ValidationUtils.readNonEmptyString("Description: ");
        double budget = ValidationUtils.readInt("Budget: $", 0, Integer.MAX_VALUE);
        int teamSize = ValidationUtils.readInt("Team Size: ", 1, 1000);
        
        System.out.println("Project Type:");
        System.out.println("1. Software");
        System.out.println("2. Hardware");
        int typeChoice = ValidationUtils.readInt("Enter choice: ", 1, 2);
        
        Project project;
        if (typeChoice == 1) {
            project = new SoftwareProject(name, description, budget, teamSize);
        } else {
            project = new HardwareProject(name, description, budget, teamSize);
        }
        
        projectService.addProject(project);
        System.out.println("\n✓ Project created successfully! ID: " + project.getId() + "\n");
    }

    private void handleTasksMenu() {
        System.out.println("\n→ Opening Task Management...\n");
        boolean backToMain = false;

        while (!backToMain) {
            Project[] projects = projectService.getAllProjects();
            if (projects.length == 0) {
                System.out.println("No projects available. Please create a project first.\n");
                backToMain = true;
                continue;
            }

            System.out.println("MANAGE TASKS");
            System.out.println("============\n");
            System.out.println("Options:");
            System.out.println("1. Add New Task");
            System.out.println("2. Update Task Status");
            System.out.println("3. Remove Task");
            System.out.println("4. Back to Main Menu\n");

            int choice = ValidationUtils.readInt("Enter your choice: ", 1, 4);

            switch (choice) {
                case 1 -> addNewTask(projects);
                case 2 -> updateTaskStatus(projects);
                case 3 -> removeTask(projects);
                case 4 -> backToMain = true;
            }
            
            if (!backToMain) {
                System.out.println();
            }
        }
    }

    private void addNewTask(Project[] projects) {
        System.out.println("\nAdd New Task");
        System.out.println("────────────");
        
        displayProjectsTable(projects, "Available Projects");
        String projectId = ValidationUtils.readNonEmptyString("\nEnter Project ID: ");
        Project project = projectService.getProjectById(projectId);
        
        if (project == null) {
            System.out.println("Project not found!");
            return;
        }

        String taskName = ValidationUtils.readNonEmptyString("Task Name: ");
        
        System.out.println("Task Status:");
        System.out.println("1. NOTSTARTED");
        System.out.println("2. INPROGRESS");
        System.out.println("3. DONE");
        int statusChoice = ValidationUtils.readInt("Enter status: ", 1, 3);
        
        String status = switch (statusChoice) {
            case 1 -> "NOTSTARTED";
            case 2 -> "INPROGRESS";
            case 3 -> "DONE";
            default -> "NOTSTARTED";
        };

        Task task = taskService.createTask(taskName, status);
        project.addTask(task);
        System.out.println("\n✓ Task added successfully! ID: " + task.getId());
    }

    private void updateTaskStatus(Project[] projects) {
        System.out.println("\nUpdate Task Status");
        System.out.println("──────────────────");
        
        displayProjectsTable(projects, "Available Projects");
        String projectId = ValidationUtils.readNonEmptyString("\nEnter Project ID: ");
        Project project = projectService.getProjectById(projectId);
        
        if (project == null) {
            System.out.println("Project not found!");
            return;
        }

        Task[] tasks = project.getTasks();
        if (tasks.length == 0) {
            System.out.println("No tasks found in this project!");
            return;
        }

        System.out.println("\nTasks in Project:");
        System.out.println("─────────────────────────────────────────────");
        System.out.printf("%-12s | %-20s | %-15s%n", "TASK ID", "TASK NAME", "STATUS");
        System.out.println("─────────────────────────────────────────────");
        for (Task task : tasks) {
            System.out.printf("%-12s | %-20s | %-15s%n",
                task.getId(), task.getName(), task.getStatus());
        }
        System.out.println("─────────────────────────────────────────────");

        String taskId = ValidationUtils.readNonEmptyString("\nEnter Task ID to update: ");
        Task taskToUpdate = null;
        for (Task task : tasks) {
            if (task.getId().equals(taskId)) {
                taskToUpdate = task;
                break;
            }
        }

        if (taskToUpdate == null) {
            System.out.println("Task not found!");
            return;
        }

        System.out.println("\nNew Status:");
        System.out.println("1. NOTSTARTED");
        System.out.println("2. INPROGRESS");
        System.out.println("3. DONE");
        int statusChoice = ValidationUtils.readInt("Enter status: ", 1, 3);
        
        String newStatus = switch (statusChoice) {
            case 1 -> "NOTSTARTED";
            case 2 -> "INPROGRESS";
            case 3 -> "DONE";
            default -> "NOTSTARTED";
        };

        if (taskService.updateTaskStatus(taskToUpdate, newStatus)) {
            System.out.println("\n✓ Task status updated successfully!");
        } else {
            System.out.println("\n✗ Failed to update task status!");
        }
    }

    private void removeTask(Project[] projects) {
        System.out.println("\nRemove Task");
        System.out.println("───────────");
        
        displayProjectsTable(projects, "Available Projects");
        String projectId = ValidationUtils.readNonEmptyString("\nEnter Project ID: ");
        Project project = projectService.getProjectById(projectId);
        
        if (project == null) {
            System.out.println("Project not found!");
            return;
        }

        Task[] tasks = project.getTasks();
        if (tasks.length == 0) {
            System.out.println("No tasks found in this project!");
            return;
        }

        System.out.println("\nTasks in Project:");
        System.out.println("─────────────────────────────────────────────");
        System.out.printf("%-12s | %-20s | %-15s%n", "TASK ID", "TASK NAME", "STATUS");
        System.out.println("─────────────────────────────────────────────");
        for (Task task : tasks) {
            System.out.printf("%-12s | %-20s | %-15s%n",
                task.getId(), task.getName(), task.getStatus());
        }
        System.out.println("─────────────────────────────────────────────");

        String taskId = ValidationUtils.readNonEmptyString("\nEnter Task ID to remove: ");
        
        if (project.removeTask(taskId)) {
            System.out.println("\n✓ Task removed successfully!");
        } else {
            System.out.println("\n✗ Task not found with ID: " + taskId);
        }
    }

    private void handleStatusReports() {
        System.out.println("\n→ Generating Status Reports...\n");
        reportService.generateStatusReport();
        System.out.println();
    }

    private void handleSwitchUser() {
        System.out.println("\n→ Switching User...\n");
        userService.displayUsers();
        
        String email = ValidationUtils.readEmail("\nEnter user email to switch to: ");
        
        if (userService.switchUser(email)) {
            User newUser = userService.getCurrentUser();
            System.out.println("\n✓ Switched to: " + newUser.getName() + " (" + newUser.getEmail() + ")");
            System.out.println("Role: " + (newUser instanceof AdminUser ? "Admin" : "Regular User"));
        } else {
            System.out.println("\n✗ User not found with email: " + email);
        }
        System.out.println();
    }

    public boolean isExit() {
        return exit;
    }
}
