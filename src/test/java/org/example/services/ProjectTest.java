package org.example.services;

import org.example.enums.Status;
import org.example.models.Project;
import org.example.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectService Progress Calculation Tests")
class ProjectTest {

    private ProjectService projectService;
    private String projectId;

    @BeforeEach
    void setUp() throws Exception {
        projectService = new ProjectService();
        
        Project project = projectService.createProject("Software", "Test Project", "Test Description", 1000.0, 5);
        projectService.addProject(project);
        projectId = project.getId();
    }

    @Test
    @DisplayName("Should return 0% completion for project with no tasks")
    void calculateCompletionPercentage_NoTasks() {
        double completion = projectService.getProjectCompletionPercentage(projectId);

        assertEquals(0.0, completion, 0.001);
    }

    @Test
    @DisplayName("Should return 0% completion when all tasks are NOTSTARTED")
    void calculateCompletionPercentage_AllNotStarted() throws Exception {
        TaskService taskService = new TaskService();
        Task task1 = taskService.createTask("Task 1", Status.NOTSTARTED);
        Task task2 = taskService.createTask("Task 2", Status.NOTSTARTED);
        
        projectService.addTaskToProject(projectId, task1);
        projectService.addTaskToProject(projectId, task2);

        double completion = projectService.getProjectCompletionPercentage(projectId);

        assertEquals(0.0, completion, 0.001);
    }

    @Test
    @DisplayName("Should return 100% completion when all tasks are DONE")
    void calculateCompletionPercentage_AllDone() throws Exception {
        TaskService taskService = new TaskService();
        Task task1 = taskService.createTask("Task 1", Status.DONE);
        Task task2 = taskService.createTask("Task 2", Status.DONE);
        Task task3 = taskService.createTask("Task 3", Status.DONE);
        
        projectService.addTaskToProject(projectId, task1);
        projectService.addTaskToProject(projectId, task2);
        projectService.addTaskToProject(projectId, task3);

        double completion = projectService.getProjectCompletionPercentage(projectId);

        assertEquals(1.0, completion, 0.001);
    }

    @Test
    @DisplayName("Should return 50% completion when half tasks are DONE")
    void calculateCompletionPercentage_HalfDone() throws Exception {
        TaskService taskService = new TaskService();
        Task task1 = taskService.createTask("Task 1", Status.DONE);
        Task task2 = taskService.createTask("Task 2", Status.DONE);
        Task task3 = taskService.createTask("Task 3", Status.NOTSTARTED);
        Task task4 = taskService.createTask("Task 4", Status.INPROGRESS);
        
        projectService.addTaskToProject(projectId, task1);
        projectService.addTaskToProject(projectId, task2);
        projectService.addTaskToProject(projectId, task3);
        projectService.addTaskToProject(projectId, task4);

        double completion = projectService.getProjectCompletionPercentage(projectId);

        assertEquals(0.5, completion, 0.001); // 2 out of 4 tasks done = 50%
    }

    @Test
    @DisplayName("Should return 33.33% completion for 1 out of 3 tasks done")
    void calculateCompletionPercentage_OneThirdDone() throws Exception {
        TaskService taskService = new TaskService();
        Task task1 = taskService.createTask("Task 1", Status.DONE);
        Task task2 = taskService.createTask("Task 2", Status.NOTSTARTED);
        Task task3 = taskService.createTask("Task 3", Status.INPROGRESS);
        
        projectService.addTaskToProject(projectId, task1);
        projectService.addTaskToProject(projectId, task2);
        projectService.addTaskToProject(projectId, task3);

        double completion = projectService.getProjectCompletionPercentage(projectId);

        assertEquals(1.0 / 3.0, completion, 0.01); // Approximately 33.33%
    }

    @Test
    @DisplayName("Should return 0% for non-existent project")
    void testGetCompletionPercentage_NonExistentProject() {
        double completion = projectService.getProjectCompletionPercentage("NONEXISTENT");

        assertEquals(0.0, completion, 0.001);
    }

    @Test
    @DisplayName("Should return 0% for null project ID")
    void testGetCompletionPercentage_NullProjectId() {
        double completion = projectService.getProjectCompletionPercentage(null);

        assertEquals(0.0, completion, 0.001);
    }

    @Test
    @DisplayName("Should calculate progress correctly after status updates")
    void testGetCompletionPercentage_AfterStatusUpdate() throws Exception {
        TaskService taskService = new TaskService();
        Task task1 = taskService.createTask("Task 1", Status.NOTSTARTED);
        Task task2 = taskService.createTask("Task 2", Status.NOTSTARTED);
        
        projectService.addTaskToProject(projectId, task1);
        projectService.addTaskToProject(projectId, task2);

        double initialCompletion = projectService.getProjectCompletionPercentage(projectId);
        assertEquals(0.0, initialCompletion, 0.001);

        taskService.updateTaskStatus(task1, Status.DONE);

        double updatedCompletion = projectService.getProjectCompletionPercentage(projectId);
        assertEquals(0.5, updatedCompletion, 0.001);
    }

    @Test
    @DisplayName("Should handle mixed task statuses correctly")
    void testGetCompletionPercentage_MixedStatuses() throws Exception {
        TaskService taskService = new TaskService();
        Task task1 = taskService.createTask("Task 1", Status.DONE);
        Task task2 = taskService.createTask("Task 2", Status.DONE);
        Task task3 = taskService.createTask("Task 3", Status.INPROGRESS);
        Task task4 = taskService.createTask("Task 4", Status.INPROGRESS);
        Task task5 = taskService.createTask("Task 5", Status.NOTSTARTED);
        
        projectService.addTaskToProject(projectId, task1);
        projectService.addTaskToProject(projectId, task2);
        projectService.addTaskToProject(projectId, task3);
        projectService.addTaskToProject(projectId, task4);
        projectService.addTaskToProject(projectId, task5);

        double completion = projectService.getProjectCompletionPercentage(projectId);

        assertEquals(0.4, completion, 0.001);
    }
}

