package org.example.utils;

import org.example.exceptions.InvalidProjectDataException;
import org.example.models.AdminUser;
import org.example.models.Project;
import org.example.models.Task;
import org.example.models.User;
import org.example.services.ProjectService;
import org.example.services.ReportService;
import org.example.services.StatusReportData;
import org.example.services.TaskService;
import org.example.services.UserService;

public class ConsoleMenu {
    private boolean exit = false;
    private final UserService userService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final ReportService reportService;

    public ConsoleMenu() {
        this(new UserService(), new ProjectService(), new TaskService(), null);
    }

    public ConsoleMenu(UserService userService, ProjectService projectService,
            TaskService taskService, ReportService reportService) {
        this.userService = userService;
        this.projectService = projectService;
        this.taskService = taskService;
        this.reportService =
                (reportService != null) ? reportService : new ReportService(this.projectService);

        displayWelcomeMessage();
        displayMainMenu();

        while (!exit) {
            int choice = ValidationUtils.readInt("Enter your choice: ", 1, 6);
            processMainMenuChoice(choice);
        }

        ValidationUtils.close();
    }

    private void displayWelcomeMessage() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            System.out.println("\n========================================");
            System.out.println("   TASK MANAGEMENT SYSTEM");
            System.out.println("========================================");
            System.out.println("Error: No current user set. Please restart the application.");
            System.out.println("========================================\n");
            return;
        }
        System.out.println("\n========================================");
        System.out.println("   TASK MANAGEMENT SYSTEM");
        System.out.println("========================================");
        System.out.println(
                "Logged in as: " + currentUser.getName() + " (" + currentUser.getEmail() + ")");
        System.out
                .println("Role: " + (currentUser instanceof AdminUser ? "Admin" : "Regular User"));
        System.out.println("========================================\n");
    }

    private void displayMainMenu() {
        String mainMenu = """
                Main Menu:
                ___________
                1. Manage Projects
                2. Manage Tasks
                3. View Status Reports
                4. User Management
                5. Switch User
                6. Exit
                """;
        System.out.println(mainMenu);
    }

    private void processMainMenuChoice(int input) {
        switch (input) {
            case 1 -> handleProjectsMenu();
            case 2 -> handleTasksMenu();
            case 3 -> handleStatusReports();
            case 4 -> handleUserManagement();
            case 5 -> handleSwitchUser();
            case 6 -> {
                System.out.println("Exiting program...");
                exit = true;
            }
            default -> {
                System.out.println("Invalid choice! Please select 1–6.");
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

            Project[] projectsToDisplay = new Project[0];
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
                    double minBudget = ValidationUtils.readDouble("Enter minimum budget: $", 0.0,
                            Double.MAX_VALUE);
                    double maxBudget = ValidationUtils.readDouble("Enter maximum budget: $",
                            minBudget, Double.MAX_VALUE);
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

            if (projectId != null && projectId.equalsIgnoreCase("back")) {
                continue;
            }

            try {
                Project project = projectService.getProjectById(projectId);
                if (project == null) {
                    throw new org.example.exceptions.ProjectNotFoundException(
                            "Project ID '" + projectId + "' does not exist");
                }
                displayProjectDetails(project);
            } catch (org.example.exceptions.ProjectNotFoundException e) {
                System.out.println("Error: ProjectNotFoundException - Project ID '" + projectId
                        + "' does not exist");
                System.out.println("Please try again");
            }
            System.out.println();
        }
    }

    private void displayProjectsTable(Project[] projects, String filterType) {
        System.out.println("\n" + filterType + ":");
        System.out.println(
                "─────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-12s | %-20s | %-12s | %-10s | %-10s%n", "PROJECT ID", "PROJECT NAME",
                "TYPE", "BUDGET", "TEAM SIZE");
        System.out.println(
                "─────────────────────────────────────────────────────────────────────────");

        for (Project p : projects) {
            if (p == null) {
                continue; // Skip null projects
            }
            System.out.printf("%-12s | %-20s | %-12s | $%-9.2f | %-10d%n", p.getId(), p.getName(),
                    p.getType(), p.getBudget(), p.getTeamSize());
        }
        System.out.println(
                "─────────────────────────────────────────────────────────────────────────");
    }

    private void displayProjectDetails(Project project) {
        if (project == null) {
            System.out.println("\n✗ Error: Project details cannot be displayed - project is null.");
            return;
        }
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PROJECT DETAILS: " + project.getId());
        System.out.println("=".repeat(60));
        System.out.println("Project Name: " + project.getName());
        System.out.println("Type: " + project.getType());
        System.out.println("Description: " + project.getDescription());
        System.out.println("Team Size: " + project.getTeamSize());
        System.out.printf("Budget: $%.2f%n", project.getBudget());

        Task[] tasks = projectService.getTasksForProject(project.getId());
        System.out.println("\nAssociated Tasks:");
        if (tasks.length == 0) {
            System.out.println("  No tasks assigned.");
        } else {
            System.out.println("─────────────────────────────────────────────────────────────");
            System.out.printf("%-12s | %-20s | %-15s | %-20s%n", "TASK ID", "TASK NAME", "STATUS",
                    "ASSIGNED USER");
            System.out.println("─────────────────────────────────────────────────────────────");
            for (Task task : tasks) {
                if (task == null) {
                    continue; // Skip null tasks
                }
                String assignedUser = "Unassigned";
                if (task.getAssignedUserId() != null) {
                    User assignedUserObj = userService.getUserById(task.getAssignedUserId());
                    assignedUser = assignedUserObj != null ? assignedUserObj.getName() : "Unknown";
                }
                System.out.printf("%-12s | %-20s | %-15s | %-20s%n", task.getId(), task.getName(),
                        task.getStatus(), assignedUser);
            }
            System.out.println("─────────────────────────────────────────────────────────────");
        }

        String[] assignedUserIds = projectService.getAssignedUserIdsForProject(project.getId());
        System.out.println("\nAssigned Users:");
        if (assignedUserIds.length == 0) {
            System.out.println("  No users assigned.");
        } else {
            for (String userId : assignedUserIds) {
                User user = userService.getUserById(userId);
                if (user != null) {
                    System.out.println("  - " + user.getName() + " (" + user.getEmail() + ")");
                }
            }
        }

        double completionRate = projectService.getProjectCompletionPercentage(project.getId());
        System.out.printf("%nCompletion Rate: %.1f%%%n", completionRate * 100);
        System.out.println("=".repeat(60));
    }

    private void createNewProject() {
        System.out.println("\nCreate New Project");
        System.out.println("──────────────────");

        String name = ValidationUtils.readNonEmptyString("Project Name: ");
        String description = ValidationUtils.readNonEmptyString("Description: ");
        double budget = ValidationUtils.readDouble("Budget: $", 0.0, Double.MAX_VALUE);
        int teamSize = ValidationUtils.readInt("Team Size: ", 1, 1000);

        System.out.println("Project Type:");
        System.out.println("1. Software");
        System.out.println("2. Hardware");
        int typeChoice = ValidationUtils.readInt("Enter choice: ", 1, 2);

        String type = (typeChoice == 1) ? "Software" : "Hardware";
        try {
            Project project =
                    projectService.createProject(type, name, description, budget, teamSize);
            projectService.addProject(project);
            System.out.println("\n✓ Project created successfully! ID: " + project.getId() + "\n");
        } catch (InvalidProjectDataException e) {
            System.out.println("\n✗ Failed to create project: " + e.getMessage() + "\n");
        }
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
        try {
            Project project = projectService.getProjectById(projectId);
            if (project == null) {
                throw new org.example.exceptions.ProjectNotFoundException(
                        "Project ID '" + projectId + "' does not exist");
            }

            String taskName = ValidationUtils.readNonEmptyString("Task Name: ");

            System.out.println("Task Status:");
            System.out.println("1. NOTSTARTED");
            System.out.println("2. INPROGRESS");
            System.out.println("3. DONE");
            int statusChoice = ValidationUtils.readInt("Enter status: ", 1, 3);

            org.example.enums.Status status = taskService.mapStatusFromChoice(statusChoice);
            Task task = taskService.createTask(taskName, status);

            if (projectService.addTaskToProject(projectId, task)) {
                System.out.println("\n✓ Task added successfully! ID: " + task.getId());
            } else {
                System.out.println(
                        "\n✗ Failed to add task. Project may not exist or task is invalid.");
            }
        } catch (org.example.exceptions.ProjectNotFoundException e) {
            System.out.println("Error: ProjectNotFoundException - Project ID '" + projectId
                    + "' does not exist");
            System.out.println("Please try again");
        }
    }

    private void updateTaskStatus(Project[] projects) {
        System.out.println("\nUpdate Task Status");
        System.out.println("──────────────────");

        displayProjectsTable(projects, "Available Projects");
        String projectId = ValidationUtils.readNonEmptyString("\nEnter Project ID: ");
        try {
            Project project = projectService.getProjectById(projectId);
            if (project == null) {
                throw new org.example.exceptions.ProjectNotFoundException(
                        "Project ID '" + projectId + "' does not exist");
            }

            Task[] tasks = projectService.getTasksForProject(projectId);
            if (tasks.length == 0) {
                System.out.println("No tasks found in this project!");
                return;
            }

            System.out.println("\nTasks in Project:");
            System.out.println("─────────────────────────────────────────────");
            System.out.printf("%-12s | %-20s | %-15s%n", "TASK ID", "TASK NAME", "STATUS");
            System.out.println("─────────────────────────────────────────────");
            for (Task task : tasks) {
                if (task == null) {
                    continue; // Skip null tasks
                }
                System.out.printf("%-12s | %-20s | %-15s%n", task.getId(), task.getName(),
                        task.getStatus());
            }
            System.out.println("─────────────────────────────────────────────");

            String taskId = ValidationUtils.readNonEmptyString("\nEnter Task ID to update: ");
            Task taskToUpdate = null;
            for (Task task : tasks) {
                if (task.getId() != null && task.getId().equals(taskId)) {
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

            org.example.enums.Status newStatus = taskService.mapStatusFromChoice(statusChoice);

            if (taskService.updateTaskStatus(taskToUpdate, newStatus)) {
                System.out.println("\n✓ Task status updated successfully!");
            } else {
                System.out.println(
                        "\n✗ Failed to update task status. Task or status may be invalid.");
            }
        } catch (org.example.exceptions.ProjectNotFoundException e) {
            System.out.println("Error: ProjectNotFoundException - Project ID '" + projectId
                    + "' does not exist");
            System.out.println("Please try again");
        }
    }

    private void removeTask(Project[] projects) {
        System.out.println("\nRemove Task");
        System.out.println("───────────");

        displayProjectsTable(projects, "Available Projects");
        String projectId = ValidationUtils.readNonEmptyString("\nEnter Project ID: ");
        try {
            Project project = projectService.getProjectById(projectId);
            if (project == null) {
                throw new org.example.exceptions.ProjectNotFoundException(
                        "Project ID '" + projectId + "' does not exist");
            }

            Task[] tasks = projectService.getTasksForProject(projectId);
            if (tasks.length == 0) {
                System.out.println("No tasks found in this project!");
                return;
            }

            System.out.println("\nTasks in Project:");
            System.out.println("─────────────────────────────────────────────");
            System.out.printf("%-12s | %-20s | %-15s%n", "TASK ID", "TASK NAME", "STATUS");
            System.out.println("─────────────────────────────────────────────");
            for (Task task : tasks) {
                if (task == null) {
                    continue; // Skip null tasks
                }
                System.out.printf("%-12s | %-20s | %-15s%n", task.getId(), task.getName(),
                        task.getStatus());
            }
            System.out.println("─────────────────────────────────────────────");

            String taskId = ValidationUtils.readNonEmptyString("\nEnter Task ID to remove: ");

            try {
                projectService.removeTaskFromProject(projectId, taskId);
                System.out.println("\n✓ Task removed successfully!");
            } catch (org.example.exceptions.TaskNotFoundException e) {
                System.out.println("Error TaskNotFoundException - Task \"" + taskId
                        + "\" was not found in the project");
            }
        } catch (org.example.exceptions.ProjectNotFoundException e) {
            System.out.println("Error: ProjectNotFoundException - Project ID '" + projectId
                    + "' does not exist");
            System.out.println("Please try again");
        }
    }

    private void handleStatusReports() {
        System.out.println("\n→ Generating Status Reports...\n");

        try {
            StatusReportData[] reportData = reportService.generateStatusReport();
            if (reportData.length == 0) {
                System.out.println("No projects available.");
                System.out.println();
                return;
            }

            System.out.println("PROJECT STATUS REPORT");
            System.out.println("PROJECT ID | PROJECT NAME | TASKS | COMPLETED | PROGRESS (%)");
            for (StatusReportData data : reportData) {
                System.out.println(data.projectId() + " | " + data.projectName() + " | "
                        + data.totalTasks() + " | " + data.completedTasks() + " | "
                        + (data.completionPercentage() * 100) + "%");
            }
            double average = reportService.calculateAverageCompletion(reportData);
            System.out.println("AVERAGE COMPLETION: " + Math.round(average * 100.0) / 100.0 + "%");
            System.out.println();
        } catch (org.example.exceptions.EmptyProjectException e) {
            System.out.println("\n✗ " + e.getMessage());
            System.out.println("Please add tasks to projects before generating status reports.\n");
        }
    }

    private void handleUserManagement() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Error: No current user set.");
            return;
        }
        boolean isAdmin = currentUser instanceof AdminUser;

        System.out.println("\n→ User Management...\n");
        boolean backToMain = false;

        while (!backToMain) {
            System.out.println("USER MANAGEMENT");
            System.out.println("===============\n");
            System.out.println("Options:");
            System.out.println("1. View All Users");
            if (isAdmin) {
                System.out.println("2. Create New User");
                System.out.println("3. Delete User");
                System.out.println("4. Assign User to Project");
                System.out.println("5. Assign User to Task");
                System.out.println("6. Back to Main Menu\n");
            } else {
                System.out.println("2. Assign User to Project");
                System.out.println("3. Assign User to Task");
                System.out.println("4. Back to Main Menu\n");
            }

            int maxChoice = isAdmin ? 6 : 4;
            int choice = ValidationUtils.readInt("Enter your choice: ", 1, maxChoice);

            switch (choice) {
                case 1 -> displayUsers();
                case 2 -> {
                    if (isAdmin) {
                        createNewUser();
                    } else {
                        assignUserToProject();
                    }
                }
                case 3 -> {
                    if (isAdmin) {
                        deleteUser();
                    } else {
                        assignUserToTask();
                    }
                }
                case 4 -> {
                    if (isAdmin) {
                        assignUserToProject();
                    } else {
                        backToMain = true;
                    }
                }
                case 5 -> {
                    if (isAdmin) {
                        assignUserToTask();
                    }
                }
                case 6 -> {
                    if (isAdmin) {
                        backToMain = true;
                    }
                }
            }

            if (!backToMain) {
                System.out.println();
            }
        }
    }

    private void createNewUser() {
        User currentUser = userService.getCurrentUser();
        if (!(currentUser instanceof AdminUser)) {
            System.out.println("\n✗ Access denied. Only administrators can create users.");
            return;
        }

        System.out.println("\nCreate New User");
        System.out.println("───────────────");

        String name = ValidationUtils.readNonEmptyString("User Name: ");

        String email = null;
        boolean validEmail = false;
        while (!validEmail) {
            try {
                email = ValidationUtils.readNonEmptyString("Email: ");
                userService.validateEmail(email);
                validEmail = true;
            } catch (org.example.exceptions.InvalidEmailException e) {
                System.out.println("\n✗ " + e.getMessage() + " Please try again.\n");
            }
        }

        boolean isAdmin = false;
        boolean validRole = false;
        while (!validRole) {
            System.out.println("User Role:");
            System.out.println("1. Admin");
            System.out.println("2. Regular User");
            int roleChoice = ValidationUtils.readInt("Enter role: ", 1, 2);

            try {
                String roleString = (roleChoice == 1) ? "admin" : "regular";
                isAdmin = userService.validateRole(roleString);
                validRole = true;
            } catch (org.example.exceptions.InvalidRoleException e) {
                System.out.println("\n✗ " + e.getMessage() + " Please try again.\n");
            }
        }

        try {
            User newUser = userService.createUser(name, email, isAdmin);
            userService.addUser(newUser);
            System.out.println("\n✓ User created successfully! ID: " + newUser.getId());
        } catch (org.example.exceptions.InvalidEmailException e) {
            System.out.println("\n✗ Failed to create user: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n✗ Failed to create user: " + e.getClass().getSimpleName() + " - "
                    + e.getMessage());
        }
    }

    private void deleteUser() {
        User currentUser = userService.getCurrentUser();
        if (!(currentUser instanceof AdminUser)) {
            System.out.println("\n✗ Access denied. Only administrators can delete users.");
            return;
        }

        System.out.println("\nDelete User");
        System.out.println("───────────");

        displayUsers();

        String email = null;
        boolean validEmail = false;
        while (!validEmail) {
            try {
                email = ValidationUtils.readNonEmptyString("\nEnter user email to delete: ");
                userService.validateEmail(email);
                validEmail = true;
            } catch (org.example.exceptions.InvalidEmailException e) {
                System.out.println("\n✗ " + e.getMessage() + " Please try again.");
            }
        }

        if (userService.deleteUser(email)) {
            System.out.println("\n✓ User deleted successfully!");
        } else {
            System.out.println(
                    "\n✗ Failed to delete user. User not found or cannot delete current user.");
        }
    }

    private void assignUserToProject() {
        System.out.println("\nAssign User to Project");
        System.out.println("──────────────────────");

        Project[] projects = projectService.getAllProjects();
        if (projects.length == 0) {
            System.out.println("No projects available.");
            return;
        }

        displayProjectsTable(projects, "Available Projects");
        String projectId = ValidationUtils.readNonEmptyString("\nEnter Project ID: ");
        try {
            Project project = projectService.getProjectById(projectId);
            if (project == null) {
                throw new org.example.exceptions.ProjectNotFoundException(
                        "Project ID '" + projectId + "' does not exist");
            }

            displayUsers();

            String userEmail = null;
            boolean validEmail = false;
            while (!validEmail) {
                try {
                    userEmail =
                            ValidationUtils.readNonEmptyString("\nEnter user email to assign: ");
                    userService.validateEmail(userEmail);
                    validEmail = true;
                } catch (org.example.exceptions.InvalidEmailException e) {
                    System.out.println("\n✗ " + e.getMessage() + " Please try again.");
                }
            }

            User user = userService.getUserByEmail(userEmail);
            if (user == null) {
                System.out.println("User not found!");
                return;
            }

            if (projectService.assignUserToProject(projectId, user.getId())) {
                System.out.println("\n✓ User assigned to project successfully!");
            } else {
                System.out.println("\n✗ User is already assigned to this project.");
            }
        } catch (org.example.exceptions.ProjectNotFoundException e) {
            System.out.println("Error: ProjectNotFoundException - Project ID '" + projectId
                    + "' does not exist");
            System.out.println("Please try again");
        }
    }

    private void assignUserToTask() {
        System.out.println("\nAssign User to Task");
        System.out.println("───────────────────");

        Project[] projects = projectService.getAllProjects();
        if (projects.length == 0) {
            System.out.println("No projects available.");
            return;
        }

        displayProjectsTable(projects, "Available Projects");
        String projectId = ValidationUtils.readNonEmptyString("\nEnter Project ID: ");
        try {
            Project project = projectService.getProjectById(projectId);
            if (project == null) {
                throw new org.example.exceptions.ProjectNotFoundException(
                        "Project ID '" + projectId + "' does not exist");
            }

            Task[] tasks = projectService.getTasksForProject(projectId);
            if (tasks.length == 0) {
                System.out.println("No tasks found in this project!");
                return;
            }

            System.out.println("\nTasks in Project:");
            System.out.println("─────────────────────────────────────────────");
            System.out.printf("%-12s | %-20s | %-15s%n", "TASK ID", "TASK NAME", "STATUS");
            System.out.println("─────────────────────────────────────────────");
            for (Task task : tasks) {
                if (task == null) {
                    continue; // Skip null tasks
                }
                System.out.printf("%-12s | %-20s | %-15s%n", task.getId(), task.getName(),
                        task.getStatus());
            }
            System.out.println("─────────────────────────────────────────────");

            String taskId = ValidationUtils.readNonEmptyString("\nEnter Task ID: ");
            Task taskToAssign = null;
            for (Task task : tasks) {
                if (task.getId() != null && task.getId().equals(taskId)) {
                    taskToAssign = task;
                    break;
                }
            }

            if (taskToAssign == null) {
                System.out.println("Task not found!");
                return;
            }

            displayUsers();

            String userEmail = null;
            boolean validEmail = false;
            while (!validEmail) {
                try {
                    userEmail =
                            ValidationUtils.readNonEmptyString("\nEnter user email to assign: ");
                    userService.validateEmail(userEmail);
                    validEmail = true;
                } catch (org.example.exceptions.InvalidEmailException e) {
                    System.out.println("\n✗ " + e.getMessage() + " Please try again.");
                }
            }

            User user = userService.getUserByEmail(userEmail);
            if (user == null) {
                System.out.println("User not found!");
                return;
            }

            taskToAssign.setAssignedUserId(user.getId());
            System.out.println("\n✓ User assigned to task successfully!");
        } catch (org.example.exceptions.ProjectNotFoundException e) {
            System.out.println("Error: ProjectNotFoundException - Project ID '" + projectId
                    + "' does not exist");
            System.out.println("Please try again");
        }
    }

    private void handleSwitchUser() {
        System.out.println("\n→ Switching User...\n");
        displayUsers();

        String email = null;
        boolean validEmail = false;
        while (!validEmail) {
            try {
                email = ValidationUtils.readNonEmptyString("\nEnter user email to switch to: ");
                userService.validateEmail(email);
                validEmail = true;
            } catch (org.example.exceptions.InvalidEmailException e) {
                System.out.println("\n✗ " + e.getMessage() + " Please try again.");
            }
        }

        if (email != null && userService.switchUser(email)) {
            User newUser = userService.getCurrentUser();
            if (newUser != null) {
                System.out.println(
                        "\n✓ Switched to: " + newUser.getName() + " (" + newUser.getEmail() + ")");
                System.out.println(
                        "Role: " + (newUser instanceof AdminUser ? "Admin" : "Regular User"));
            }
        } else if (email != null) {
            System.out.println("\n✗ User not found with email: " + email);
        }
        System.out.println();
    }

    private void displayUsers() {
        User[] users = userService.getAllUsers();
        System.out.println("\nAvailable Users:");
        System.out.println("ID | Name | Email | Role");
        System.out.println("------------------------");
        for (User u : users) {
            if (u == null) {
                continue; // Skip null users
            }
            String role = u instanceof AdminUser ? "Admin" : "Regular";
            System.out
                    .println(u.getId() + " | " + u.getName() + " | " + u.getEmail() + " | " + role);
        }
    }

    public boolean isExit() {
        return exit;
    }
}
