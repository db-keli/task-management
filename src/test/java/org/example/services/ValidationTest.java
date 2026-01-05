package org.example.services;

import org.example.enums.Status;
import org.example.exceptions.EmptyProjectException;
import org.example.exceptions.InvalidEmailException;
import org.example.exceptions.InvalidProjectDataException;
import org.example.exceptions.InvalidRoleException;
import org.example.exceptions.TaskNotFoundException;
import org.example.models.Project;
import org.example.models.Task;
import org.example.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Input Validation Tests")
class ValidationTest {

    private UserService userService;
    private ProjectService projectService;
    private TaskService taskService;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        projectService = new ProjectService();
        taskService = new TaskService();
        reportService = new ReportService(projectService);
    }


    @Test
    @DisplayName("Should throw InvalidEmailException when email is null")
    void testValidateEmail_NullEmail() {
        assertThrows(InvalidEmailException.class, () -> {
            userService.validateEmail(null);
        });
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when email is empty")
    void testValidateEmail_EmptyEmail() {
        assertThrows(InvalidEmailException.class, () -> {
            userService.validateEmail("");
        });
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when email is whitespace only")
    void testValidateEmail_WhitespaceEmail() {
        assertThrows(InvalidEmailException.class, () -> {
            userService.validateEmail("   ");
        });
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when email has no @ symbol")
    void testValidateEmail_NoAtSymbol() {
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, () -> {
            userService.validateEmail("invalidemail.com");
        });
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when email has no domain")
    void testValidateEmail_NoDomain() {
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, () -> {
            userService.validateEmail("user@");
        });
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when email has no TLD")
    void testValidateEmail_NoTLD() {
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, () -> {
            userService.validateEmail("user@example");
        });
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when email has multiple @ symbols")
    void testValidateEmail_MultipleAtSymbols() {
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, () -> {
            userService.validateEmail("user@@example.com");
        });
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    @DisplayName("Should accept valid email format")
    void testValidateEmail_ValidEmail() throws InvalidEmailException {
        assertDoesNotThrow(() -> {
            userService.validateEmail("user@example.com");
        });
    }

    @Test
    @DisplayName("Should accept valid email with subdomain")
    void testValidateEmail_ValidEmailWithSubdomain() throws InvalidEmailException {
        assertDoesNotThrow(() -> {
            userService.validateEmail("user@mail.example.com");
        });
    }


    @Test
    @DisplayName("Should throw InvalidRoleException when role is null")
    void testValidateRole_NullRole() {
        assertThrows(InvalidRoleException.class, () -> {
            userService.validateRole(null);
        });
    }

    @Test
    @DisplayName("Should throw InvalidRoleException when role is empty")
    void testValidateRole_EmptyRole() {
        assertThrows(InvalidRoleException.class, () -> {
            userService.validateRole("");
        });
    }

    @Test
    @DisplayName("Should throw InvalidRoleException when role is whitespace only")
    void testValidateRole_WhitespaceRole() {
        assertThrows(InvalidRoleException.class, () -> {
            userService.validateRole("   ");
        });
    }

    @Test
    @DisplayName("Should throw InvalidRoleException when role is invalid")
    void testValidateRole_InvalidRole() {
        InvalidRoleException exception = assertThrows(InvalidRoleException.class, () -> {
            userService.validateRole("invalid");
        });
        assertTrue(exception.getMessage().contains("Invalid role"));
    }

    @Test
    @DisplayName("Should accept valid admin role")
    void testValidateRole_ValidAdminRole() throws InvalidRoleException {
        boolean isAdmin = userService.validateRole("admin");
        assertTrue(isAdmin);
    }

    @Test
    @DisplayName("Should accept valid regular role")
    void testValidateRole_ValidRegularRole() throws InvalidRoleException {
        boolean isAdmin = userService.validateRole("regular");
        assertFalse(isAdmin);
    }

    @Test
    @DisplayName("Should accept admin role case-insensitive")
    void testValidateRole_AdminCaseInsensitive() throws InvalidRoleException {
        assertTrue(userService.validateRole("ADMIN"));
        assertTrue(userService.validateRole("Admin"));
        assertTrue(userService.validateRole("admin"));
    }

    @Test
    @DisplayName("Should accept regular role case-insensitive")
    void testValidateRole_RegularCaseInsensitive() throws InvalidRoleException {
        assertFalse(userService.validateRole("REGULAR"));
        assertFalse(userService.validateRole("Regular"));
        assertFalse(userService.validateRole("regular"));
    }


    @Test
    @DisplayName("Should throw InvalidEmailException when creating user with invalid email")
    void testCreateUser_InvalidEmail() {
        assertThrows(InvalidEmailException.class, () -> {
            userService.createUser("Test User", "invalid-email", true);
        });
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when adding user with duplicate email")
    void testAddUser_DuplicateEmail() throws Exception {
        User user1 = userService.createUser("User 1", "test@example.com", false);
        userService.addUser(user1);

        User user2 = userService.createUser("User 2", "test@example.com", false);
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, () -> {
            userService.addUser(user2);
        });
        assertTrue(exception.getMessage().contains("Email already exists"));
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when adding user with null email")
    void testAddUser_NullEmail() throws Exception {
        User user = userService.createUser("Test User", "valid@example.com", false);
        user.setEmail(null);
        
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, () -> {
            userService.addUser(user);
        });
        assertTrue(exception.getMessage().contains("null") || exception.getMessage().contains("empty"));
    }


    @Test
    @DisplayName("Should throw InvalidProjectDataException when budget is zero")
    void testCreateProject_ZeroBudget() {
        InvalidProjectDataException exception = assertThrows(InvalidProjectDataException.class, () -> {
            projectService.createProject("Software", "Test Project", "Description", 0.0, 5);
        });
        assertTrue(exception.getMessage().contains("Budget must be positive"));
    }

    @Test
    @DisplayName("Should throw InvalidProjectDataException when budget is negative")
    void testCreateProject_NegativeBudget() {
        InvalidProjectDataException exception = assertThrows(InvalidProjectDataException.class, () -> {
            projectService.createProject("Software", "Test Project", "Description", -100.0, 5);
        });
        assertTrue(exception.getMessage().contains("Budget must be positive"));
    }

    @Test
    @DisplayName("Should throw InvalidProjectDataException when adding null project")
    void testAddProject_NullProject() {
        InvalidProjectDataException exception = assertThrows(InvalidProjectDataException.class, () -> {
            projectService.addProject(null);
        });
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    @DisplayName("Should throw InvalidProjectDataException when adding project with zero budget")
    void testAddProject_ZeroBudget() throws Exception {
        Project project = projectService.createProject("Software", "Test Project", "Description", 1000.0, 5);
        project.setBudget(0.0);
        
        InvalidProjectDataException exception = assertThrows(InvalidProjectDataException.class, () -> {
            projectService.addProject(project);
        });
        assertTrue(exception.getMessage().contains("Budget must be positive"));
    }

    @Test
    @DisplayName("Should throw InvalidProjectDataException when adding project with negative budget")
    void testAddProject_NegativeBudget() throws Exception {
        Project project = projectService.createProject("Software", "Test Project", "Description", 1000.0, 5);
        project.setBudget(-100.0);
        
        InvalidProjectDataException exception = assertThrows(InvalidProjectDataException.class, () -> {
            projectService.addProject(project);
        });
        assertTrue(exception.getMessage().contains("Budget must be positive"));
    }

    @Test
    @DisplayName("Should throw InvalidProjectDataException when adding project with duplicate ID")
    void testAddProject_DuplicateId() throws Exception {
        Project project1 = projectService.createProject("Software", "Project 1", "Description", 1000.0, 5);
        projectService.addProject(project1);
        
        Project project2 = projectService.createProject("Hardware", "Project 2", "Description", 2000.0, 5);
        project2.setId(project1.getId()); // Set duplicate ID
        
        InvalidProjectDataException exception = assertThrows(InvalidProjectDataException.class, () -> {
            projectService.addProject(project2);
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }


    @Test
    @DisplayName("Should throw TaskNotFoundException when removing task from non-existent project")
    void testRemoveTaskFromProject_NonExistentProject() {
        assertThrows(TaskNotFoundException.class, () -> {
            projectService.removeTaskFromProject("NONEXISTENT", "T001");
        });
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when removing non-existent task")
    void testRemoveTaskFromProject_NonExistentTask() throws Exception {
        Project project = projectService.createProject("Software", "Test Project", "Description", 1000.0, 5);
        projectService.addProject(project);
        
        Task task = taskService.createTask("Existing Task", Status.NOTSTARTED);
        projectService.addTaskToProject(project.getId(), task);
        
        assertThrows(TaskNotFoundException.class, () -> {
            projectService.removeTaskFromProject(project.getId(), "NONEXISTENT");
        });
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when projectId is null")
    void testRemoveTaskFromProject_NullProjectId() {
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> {
            projectService.removeTaskFromProject(null, "T001");
        });
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when taskId is null")
    void testRemoveTaskFromProject_NullTaskId() throws Exception {
        Project project = projectService.createProject("Software", "Test Project", "Description", 1000.0, 5);
        projectService.addProject(project);
        
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> {
            projectService.removeTaskFromProject(project.getId(), null);
        });
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when project has no tasks")
    void testRemoveTaskFromProject_ProjectWithNoTasks() throws Exception {
        Project project = projectService.createProject("Software", "Test Project", "Description", 1000.0, 5);
        projectService.addProject(project);
        
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> {
            projectService.removeTaskFromProject(project.getId(), "T001");
        });
        assertTrue(exception.getMessage().contains("No tasks found"));
    }


    @Test
    @DisplayName("Should throw EmptyProjectException when project has no tasks")
    void testGenerateStatusReport_ProjectWithNoTasks() throws Exception {
        Project project = projectService.createProject("Software", "Test Project", "Description", 1000.0, 5);
        projectService.addProject(project);
        
        EmptyProjectException exception = assertThrows(EmptyProjectException.class, () -> {
            reportService.generateStatusReport();
        });
        assertTrue(exception.getMessage().contains("has no tasks"));
        assertTrue(exception.getMessage().contains(project.getId()));
    }

    @Test
    @DisplayName("Should throw EmptyProjectException with correct project name")
    void testGenerateStatusReport_EmptyProjectExceptionMessage() throws Exception {
        Project project = projectService.createProject("Software", "My Test Project", "Description", 1000.0, 5);
        projectService.addProject(project);
        
        EmptyProjectException exception = assertThrows(EmptyProjectException.class, () -> {
            reportService.generateStatusReport();
        });
        assertTrue(exception.getMessage().contains("My Test Project"));
        assertTrue(exception.getMessage().contains(project.getId()));
    }

    @Test
    @DisplayName("Should throw EmptyProjectException when multiple projects exist but one has no tasks")
    void testGenerateStatusReport_MultipleProjectsOneEmpty() throws Exception {
        Project project1 = projectService.createProject("Software", "Project 1", "Description", 1000.0, 5);
        projectService.addProject(project1);
        
        Project project2 = projectService.createProject("Hardware", "Project 2", "Description", 2000.0, 5);
        projectService.addProject(project2);
        
        Task task = taskService.createTask("Task 1", Status.NOTSTARTED);
        projectService.addTaskToProject(project1.getId(), task);
        
        EmptyProjectException exception = assertThrows(EmptyProjectException.class, () -> {
            reportService.generateStatusReport();
        });
        assertTrue(exception.getMessage().contains("Project 2"));
        assertTrue(exception.getMessage().contains("has no tasks"));
    }

    @Test
    @DisplayName("Should not throw exception when all projects have tasks")
    void testGenerateStatusReport_AllProjectsHaveTasks() throws Exception {
        Project project1 = projectService.createProject("Software", "Project 1", "Description", 1000.0, 5);
        projectService.addProject(project1);
        
        Project project2 = projectService.createProject("Hardware", "Project 2", "Description", 2000.0, 5);
        projectService.addProject(project2);
        
        Task task1 = taskService.createTask("Task 1", Status.NOTSTARTED);
        projectService.addTaskToProject(project1.getId(), task1);
        
        Task task2 = taskService.createTask("Task 2", Status.INPROGRESS);
        projectService.addTaskToProject(project2.getId(), task2);
        
        assertDoesNotThrow(() -> {
            reportService.generateStatusReport();
        });
    }

    @Test
    @DisplayName("Should return empty array when no projects exist")
    void testGenerateStatusReport_NoProjects() throws Exception {
        StatusReportData[] reportData = reportService.generateStatusReport();
        assertEquals(0, reportData.length);
    }
}

