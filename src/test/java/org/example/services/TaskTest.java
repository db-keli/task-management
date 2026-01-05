package org.example.services;

import org.example.enums.Status;
import org.example.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaskService Tests")
class TaskTest {

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
    }

    @Test
    @DisplayName("Should create task with valid name and status")
    void testCreateTask_ValidInput() {
        String taskName = "Test Task";
        Status status = Status.NOTSTARTED;

        Task task = taskService.createTask(taskName, status);

        assertNotNull(task);
        assertEquals(taskName, task.getName());
        assertEquals(status, task.getStatus());
        assertNotNull(task.getId());
        assertTrue(task.getId().startsWith("T"));
    }

    @Test
    @DisplayName("Should update task status successfully")
    void testUpdateTaskStatus_Success() {
        Task task = taskService.createTask("Test Task", Status.NOTSTARTED);
        Status newStatus = Status.INPROGRESS;

        boolean result = taskService.updateTaskStatus(task, newStatus);

        assertTrue(result);
        assertEquals(newStatus, task.getStatus());
    }

    @Test
    @DisplayName("Should return false when updating null task")
    void testUpdateTaskStatus_NullTask() {
        Status newStatus = Status.DONE;

        boolean result = taskService.updateTaskStatus(null, newStatus);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when updating task with null status")
    void testUpdateTaskStatus_NullStatus() {
        Task task = taskService.createTask("Test Task", Status.NOTSTARTED);

        boolean result = taskService.updateTaskStatus(task, null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should map status choice correctly")
    void testMapStatusFromChoice() {
        assertEquals(Status.NOTSTARTED, taskService.mapStatusFromChoice(1));
        assertEquals(Status.INPROGRESS, taskService.mapStatusFromChoice(2));
        assertEquals(Status.DONE, taskService.mapStatusFromChoice(3));
        assertEquals(Status.NOTSTARTED, taskService.mapStatusFromChoice(999)); // Default case
    }

    @Test
    @DisplayName("Should create task from status string")
    void testCreateTaskFromStatusString_ValidStatus() {
        String taskName = "Test Task";
        String statusString = "INPROGRESS";

        Task task = taskService.createTaskFromStatusString(taskName, statusString);

        assertNotNull(task);
        assertEquals(taskName, task.getName());
        assertEquals(Status.INPROGRESS, task.getStatus());
    }

    @Test
    @DisplayName("Should default to NOTSTARTED for invalid status string")
    void testCreateTaskFromStatusString_InvalidStatus() {
        String taskName = "Test Task";
        String invalidStatus = "INVALID_STATUS";

        Task task = taskService.createTaskFromStatusString(taskName, invalidStatus);

        assertNotNull(task);
        assertEquals(Status.NOTSTARTED, task.getStatus());
    }

    @Test
    @DisplayName("Should update task status from string")
    void testUpdateTaskStatusFromString_Success() {
        Task task = taskService.createTask("Test Task", Status.NOTSTARTED);
        String statusString = "DONE";

        boolean result = taskService.updateTaskStatusFromString(task, statusString);

        assertTrue(result);
        assertEquals(Status.DONE, task.getStatus());
    }

    @Test
    @DisplayName("Should return false for invalid status string")
    void testUpdateTaskStatusFromString_InvalidStatus() {
        Task task = taskService.createTask("Test Task", Status.NOTSTARTED);
        String invalidStatus = "INVALID";

        boolean result = taskService.updateTaskStatusFromString(task, invalidStatus);

        assertFalse(result);
        assertEquals(Status.NOTSTARTED, task.getStatus());
    }
}




