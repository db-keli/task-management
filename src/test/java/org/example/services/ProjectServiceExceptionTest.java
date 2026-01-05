package org.example.services;

import org.example.enums.Status;
import org.example.exceptions.TaskNotFoundException;
import org.example.models.Project;
import org.example.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectService Task Exception Handling Tests")
class ProjectServiceExceptionTest {

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
    @DisplayName("Should throw TaskNotFoundException when removing task from non-existent project")
    void testRemoveTaskFromProject_NonExistentProject() {
        String nonExistentProjectId = "NONEXISTENT";
        String taskId = "T999";

        assertThrows(TaskNotFoundException.class, () -> {
            projectService.removeTaskFromProject(nonExistentProjectId, taskId);
        });
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when removing non-existent task")
    void testRemoveTaskFromProject_NonExistentTask() throws Exception {
        TaskService taskService = new TaskService();
        Task task = taskService.createTask("Existing Task", Status.NOTSTARTED);
        projectService.addTaskToProject(projectId, task);
        
        String nonExistentTaskId = "T999";

        assertThrows(TaskNotFoundException.class, () -> {
            projectService.removeTaskFromProject(projectId, nonExistentTaskId);
        });
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when projectId is null")
    void testRemoveTaskFromProject_NullProjectId() {
        String taskId = "T001";

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> {
            projectService.removeTaskFromProject(null, taskId);
        });
        
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when taskId is null")
    void testRemoveTaskFromProject_NullTaskId() {
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> {
            projectService.removeTaskFromProject(projectId, null);
        });
        
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when project has no tasks")
    void testRemoveTaskFromProject_ProjectWithNoTasks() {
        String taskId = "T001";

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> {
            projectService.removeTaskFromProject(projectId, taskId);
        });
        
        assertTrue(exception.getMessage().contains("No tasks found"));
    }

    @Test
    @DisplayName("Should successfully remove task when task exists")
    void testRemoveTaskFromProject_Success() throws Exception {
        TaskService taskService = new TaskService();
        Task task = taskService.createTask("Test Task", Status.NOTSTARTED);
        projectService.addTaskToProject(projectId, task);
        String taskId = task.getId();

        boolean result = projectService.removeTaskFromProject(projectId, taskId);

        assertTrue(result);
        Task[] remainingTasks = projectService.getTasksForProject(projectId);
        assertEquals(0, remainingTasks.length);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException with correct message for non-existent task")
    void testRemoveTaskFromProject_ExceptionMessage() throws Exception {
        TaskService taskService = new TaskService();
        Task task = taskService.createTask("Existing Task", Status.NOTSTARTED);
        projectService.addTaskToProject(projectId, task);
        
        String nonExistentTaskId = "T999";

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> {
            projectService.removeTaskFromProject(projectId, nonExistentTaskId);
        });

        assertTrue(exception.getMessage().contains(nonExistentTaskId));
        assertTrue(exception.getMessage().contains(projectId));
    }
}

