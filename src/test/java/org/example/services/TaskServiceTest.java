package org.example.services;

import java.util.Arrays;
import java.util.List;

import org.example.enums.Status;
import org.example.exceptions.ValidationException;
import org.example.interfaces.TaskFilter;
import org.example.models.Task;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaskServiceTest {
    private TaskService taskService;

    @BeforeEach
    public void setUp() {
        taskService = new TaskService();
    }

    @Test
    public void testIsValidTaskId_ValidIds() {
        assertTrue(taskService.isValidTaskId("T001"));
        assertTrue(taskService.isValidTaskId("T123"));
        assertTrue(taskService.isValidTaskId("T999"));
    }

    @Test
    public void testIsValidTaskId_InvalidIds() {
        assertFalse(taskService.isValidTaskId(null));
        assertFalse(taskService.isValidTaskId(""));
        assertFalse(taskService.isValidTaskId("T1"));
        assertFalse(taskService.isValidTaskId("T0001"));
        assertFalse(taskService.isValidTaskId("P001"));
        assertFalse(taskService.isValidTaskId("001"));
        assertFalse(taskService.isValidTaskId("TABC"));
    }

    @Test
    public void testValidateTaskId_Valid() {
        assertDoesNotThrow(() -> taskService.validateTaskId("T001"));
        assertDoesNotThrow(() -> taskService.validateTaskId("T999"));
    }

    @Test
    public void testValidateTaskId_Invalid() {
        assertThrows(ValidationException.class, () -> taskService.validateTaskId(null));
        assertThrows(ValidationException.class, () -> taskService.validateTaskId("T1"));
        assertThrows(ValidationException.class, () -> taskService.validateTaskId("P001"));
    }

    @Test
    public void testIsValidTaskName_ValidNames() {
        assertTrue(taskService.isValidTaskName("Task 1"));
        assertTrue(taskService.isValidTaskName("Design-Homepage"));
        assertTrue(taskService.isValidTaskName("Fix_bug_123"));
        assertTrue(taskService.isValidTaskName("a"));
        assertTrue(taskService.isValidTaskName("Task Name With Spaces"));
    }

    @Test
    public void testIsValidTaskName_InvalidNames() {
        assertFalse(taskService.isValidTaskName(null));
        assertFalse(taskService.isValidTaskName(""));
        assertFalse(taskService.isValidTaskName("   "));
        assertFalse(taskService.isValidTaskName("a".repeat(101)));
        assertFalse(taskService.isValidTaskName("Task@Name"));
    }

    @Test
    public void testValidateTaskName_Valid() {
        assertDoesNotThrow(() -> taskService.validateTaskName("Valid Task Name"));
        assertDoesNotThrow(() -> taskService.validateTaskName("Task-123"));
    }

    @Test
    public void testValidateTaskName_Invalid() {
        assertThrows(ValidationException.class, () -> taskService.validateTaskName(null));
        assertThrows(ValidationException.class, () -> taskService.validateTaskName(""));
        assertThrows(ValidationException.class,
                () -> taskService.validateTaskName("a".repeat(101)));
    }

    @Test
    public void testCreateTask_Valid() {
        Task task = taskService.createTask("Test Task", Status.NOTSTARTED);
        assertNotNull(task);
        assertEquals("Test Task", task.getName());
        assertEquals(Status.NOTSTARTED, task.getStatus());
        assertNotNull(task.getId());
        assertTrue(taskService.isValidTaskId(task.getId()));
    }

    @Test
    public void testCreateTask_InvalidName() {
        assertThrows(ValidationException.class,
                () -> taskService.createTask("", Status.NOTSTARTED));
        assertThrows(ValidationException.class,
                () -> taskService.createTask(null, Status.NOTSTARTED));
    }

    @Test
    public void testCreateTask_InvalidStatus() {
        assertThrows(ValidationException.class, () -> taskService.createTask("Valid Name", null));
    }

    @Test
    public void testCreateTaskFromStatusString() {
        Task task1 = taskService.createTaskFromStatusString("Task 1", "DONE");
        assertEquals(Status.DONE, task1.getStatus());

        Task task2 = taskService.createTaskFromStatusString("Task 2", "INPROGRESS");
        assertEquals(Status.INPROGRESS, task2.getStatus());


        Task task3 = taskService.createTaskFromStatusString("Task 3", "INVALID");
        assertEquals(Status.NOTSTARTED, task3.getStatus());
    }

    @Test
    public void testMapStatusFromChoice() {
        assertEquals(Status.NOTSTARTED, taskService.mapStatusFromChoice(1));
        assertEquals(Status.INPROGRESS, taskService.mapStatusFromChoice(2));
        assertEquals(Status.DONE, taskService.mapStatusFromChoice(3));
        assertEquals(Status.NOTSTARTED, taskService.mapStatusFromChoice(999));
    }

    @Test
    public void testUpdateTaskStatus() {
        Task task = new Task("Test Task", Status.NOTSTARTED);
        assertTrue(taskService.updateTaskStatus(task, Status.INPROGRESS));
        assertEquals(Status.INPROGRESS, task.getStatus());

        assertTrue(taskService.updateTaskStatus(task, Status.DONE));
        assertEquals(Status.DONE, task.getStatus());
    }

    @Test
    public void testUpdateTaskStatus_Null() {
        Task task = new Task("Test Task", Status.NOTSTARTED);
        assertFalse(taskService.updateTaskStatus(null, Status.DONE));
        assertFalse(taskService.updateTaskStatus(task, null));
    }

    @Test
    public void testUpdateTaskStatusFromString() {
        Task task = new Task("Test Task", Status.NOTSTARTED);
        assertTrue(taskService.updateTaskStatusFromString(task, "DONE"));
        assertEquals(Status.DONE, task.getStatus());

        assertFalse(taskService.updateTaskStatusFromString(task, "INVALID"));
        assertEquals(Status.DONE, task.getStatus());
    }

    @Test
    public void testValidateTask_Valid() {
        Task task = taskService.createTask("Valid Task", Status.NOTSTARTED);
        assertDoesNotThrow(() -> taskService.validateTask(task));
    }

    @Test
    public void testValidateTask_Null() {
        assertThrows(ValidationException.class, () -> taskService.validateTask(null));
    }

    @Test
    public void testValidateTask_InvalidId() {
        Task task = new Task("Test Task", Status.NOTSTARTED);
        task.setId("INVALID_ID");
        assertThrows(ValidationException.class, () -> taskService.validateTask(task));
    }

    @Test
    public void testFilterTasks_List() {
        Task task1 = new Task("Task 1", Status.DONE);
        Task task2 = new Task("Task 2", Status.INPROGRESS);
        Task task3 = new Task("Task 3", Status.DONE);
        List<Task> tasks = Arrays.asList(task1, task2, task3);

        List<Task> completedTasks =
                taskService.filterTasks(tasks, TaskService.TaskFilters.COMPLETED);
        assertEquals(2, completedTasks.size());
        assertTrue(completedTasks.contains(task1));
        assertTrue(completedTasks.contains(task3));
    }

    @Test
    public void testFilterTasks_Array() {
        Task task1 = new Task("Task 1", Status.NOTSTARTED);
        Task task2 = new Task("Task 2", Status.INPROGRESS);
        Task task3 = new Task("Task 3", Status.INPROGRESS);
        Task[] tasks = {task1, task2, task3};

        Task[] inProgressTasks =
                taskService.filterTasks(tasks, TaskService.TaskFilters.IN_PROGRESS);
        assertEquals(2, inProgressTasks.length);
    }

    @Test
    public void testFilterTasks_NullHandling() {
        List<Task> result1 =
                taskService.filterTasks((List<Task>) null, TaskService.TaskFilters.COMPLETED);
        assertTrue(result1.isEmpty());

        Task[] result2 = taskService.filterTasks((Task[]) null, TaskService.TaskFilters.COMPLETED);
        assertEquals(0, result2.length);

        List<Task> tasks = Arrays.asList(new Task("Task", Status.DONE));
        List<Task> result3 = taskService.filterTasks(tasks, null);
        assertEquals(tasks, result3);
    }

    @Test
    public void testTaskFilters_ByStatus() {
        Task task1 = new Task("Task 1", Status.NOTSTARTED);
        Task task2 = new Task("Task 2", Status.INPROGRESS);

        TaskFilter notStartedFilter = TaskService.TaskFilters.byStatus(Status.NOTSTARTED);
        assertTrue(notStartedFilter.test(task1));
        assertFalse(notStartedFilter.test(task2));
    }

    @Test
    public void testTaskFilters_ByAssignedUser() {
        Task task1 = new Task("Task 1", Status.NOTSTARTED);
        task1.setAssignedUserId("U001");
        Task task2 = new Task("Task 2", Status.INPROGRESS);
        task2.setAssignedUserId("U002");

        TaskFilter userFilter = TaskService.TaskFilters.byAssignedUser("U001");
        assertTrue(userFilter.test(task1));
        assertFalse(userFilter.test(task2));
    }

    @Test
    public void testTaskFilters_WithValidId() {
        Task task1 = new Task("Task 1", Status.NOTSTARTED);
        task1.setId("T001");
        Task task2 = new Task("Task 2", Status.INPROGRESS);
        task2.setId("INVALID");

        TaskFilter validIdFilter = TaskService.TaskFilters.withValidId();
        assertTrue(validIdFilter.test(task1));
        assertFalse(validIdFilter.test(task2));
    }

    @Test
    public void testTaskFilters_ByNamePattern() {
        Task task1 = new Task("Database Migration", Status.NOTSTARTED);
        Task task2 = new Task("Frontend Design", Status.INPROGRESS);
        Task task3 = new Task("Database Backup", Status.DONE);

        TaskFilter databaseFilter = TaskService.TaskFilters.byNamePattern("(?i)database");
        assertTrue(databaseFilter.test(task1));
        assertFalse(databaseFilter.test(task2));
        assertTrue(databaseFilter.test(task3));
    }

    @Test
    public void testTaskFilters_ComplexFiltering() {
        Task task1 = new Task("Task 1", Status.DONE);
        task1.setId("T001");
        Task task2 = new Task("Task 2", Status.INPROGRESS);
        task2.setId("T002");
        Task task3 = new Task("Task 3", Status.NOTSTARTED);
        task3.setId("T003");

        List<Task> tasks = Arrays.asList(task1, task2, task3);


        List<Task> notCompleted =
                taskService.filterTasks(tasks, TaskService.TaskFilters.NOT_COMPLETED);
        assertEquals(2, notCompleted.size());


        List<Task> notStarted = taskService.filterTasks(tasks, TaskService.TaskFilters.NOT_STARTED);
        assertEquals(1, notStarted.size());
        assertEquals(task3, notStarted.get(0));
    }
}
