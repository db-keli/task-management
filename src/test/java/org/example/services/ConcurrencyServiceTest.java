package org.example.services;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.example.enums.Status;
import org.example.models.Project;
import org.example.models.SoftwareProject;
import org.example.models.Task;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConcurrencyServiceTest {
    private ConcurrencyService concurrencyService;
    private ProjectService projectService;
    private TaskService taskService;
    private UserService userService;
    private Project testProject;

    @BeforeEach
    public void setUp() {
        projectService = new ProjectService();
        taskService = new TaskService();
        userService = new UserService();
        concurrencyService = new ConcurrencyService(projectService, taskService, userService);

        testProject = new SoftwareProject("Test Project", "Test", 10000.0, 10);
        testProject.setId("P001");
        try {
            projectService.addProject(testProject);
        } catch (Exception e) {
            fail("Failed to add test project: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        concurrencyService.shutdown();
    }

    @Test
    public void testSimulateMultiUserTaskUpdates_SingleTask() throws Exception {
        Task task = taskService.createTask("Test Task", Status.NOTSTARTED);
        projectService.addTaskToProject("P001", task);

        Task[] tasks = projectService.getTasksForProject("P001");
        assertEquals(1, tasks.length);

        concurrencyService.simulateMultiUserTaskUpdates("P001", 2, 1);

        Task updatedTask = projectService.getTasksForProject("P001")[0];
        assertNotNull(updatedTask);
    }

    @Test
    public void testSimulateMultiUserTaskUpdates_MultipleTasks() throws Exception {
        Task task1 = taskService.createTask("Task 1", Status.NOTSTARTED);
        Task task2 = taskService.createTask("Task 2", Status.NOTSTARTED);
        Task task3 = taskService.createTask("Task 3", Status.NOTSTARTED);

        projectService.addTaskToProject("P001", task1);
        projectService.addTaskToProject("P001", task2);
        projectService.addTaskToProject("P001", task3);

        Task[] tasks = projectService.getTasksForProject("P001");
        assertEquals(3, tasks.length);

        concurrencyService.simulateMultiUserTaskUpdates("P001", 3, 2);

        Task[] updatedTasks = projectService.getTasksForProject("P001");
        assertEquals(3, updatedTasks.length);
    }

    @Test
    public void testSimulateMultiUserTaskUpdates_ManyUsers() throws Exception {
        Task task = taskService.createTask("Concurrent Task", Status.NOTSTARTED);
        projectService.addTaskToProject("P001", task);

        concurrencyService.simulateMultiUserTaskUpdates("P001", 10, 3);

        Task[] updatedTasks = projectService.getTasksForProject("P001");
        assertEquals(1, updatedTasks.length);
    }

    @Test
    public void testUpdateTasksInParallel() throws Exception {
        Task task1 = taskService.createTask("Task 1", Status.NOTSTARTED);
        Task task2 = taskService.createTask("Task 2", Status.NOTSTARTED);
        Task task3 = taskService.createTask("Task 3", Status.NOTSTARTED);

        projectService.addTaskToProject("P001", task1);
        projectService.addTaskToProject("P001", task2);
        projectService.addTaskToProject("P001", task3);

        concurrencyService.updateTasksInParallel("P001", Status.DONE);

        Task[] updatedTasks = projectService.getTasksForProject("P001");
        for (Task task : updatedTasks) {
            assertEquals(Status.DONE, task.getStatus());
        }
    }

    @Test
    public void testUpdateTasksAsync() throws Exception {
        Task task1 = taskService.createTask("Task 1", Status.NOTSTARTED);
        Task task2 = taskService.createTask("Task 2", Status.NOTSTARTED);

        projectService.addTaskToProject("P001", task1);
        projectService.addTaskToProject("P001", task2);

        Task[] tasks = projectService.getTasksForProject("P001");

        List<String> taskIds = Arrays.stream(tasks)
                .map(Task::getId)
                .collect(Collectors.toList());

        concurrencyService.updateTasksAsync("P001", taskIds, Status.INPROGRESS);

        Thread.sleep(1000);

        Task[] updatedTasks = projectService.getTasksForProject("P001");
        for (Task task : updatedTasks) {
            assertEquals(Status.INPROGRESS, task.getStatus());
        }
    }

    @Test
    public void testConcurrentTaskStatusUpdates() throws Exception {
        Task task = taskService.createTask("Concurrent Task", Status.NOTSTARTED);
        projectService.addTaskToProject("P001", task);

        int threadCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        ConcurrentHashMap<Status, AtomicInteger> statusChanges = new ConcurrentHashMap<>();
        statusChanges.put(Status.DONE, new AtomicInteger(0));
        statusChanges.put(Status.INPROGRESS, new AtomicInteger(0));
        statusChanges.put(Status.NOTSTARTED, new AtomicInteger(0));

        for (int i = 0; i < threadCount; i++) {
            final Status newStatus = (i % 3 == 0) ? Status.DONE
                    : (i % 3 == 1) ? Status.INPROGRESS : Status.NOTSTARTED;

            new Thread(() -> {
                try {
                    startLatch.await();
                    Task[] tasks = projectService.getTasksForProject("P001");
                    if (tasks.length > 0) {
                        boolean updated = taskService.updateTaskStatus(tasks[0], newStatus);
                        if (updated) {
                            successCount.incrementAndGet();
                            statusChanges.get(newStatus).incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(endLatch.await(5, TimeUnit.SECONDS));

        assertEquals(threadCount, successCount.get());

        Task[] finalTasks = projectService.getTasksForProject("P001");
        assertNotNull(finalTasks[0].getStatus());

        Status finalStatus = finalTasks[0].getStatus();
        assertTrue(finalStatus == Status.DONE ||
                finalStatus == Status.INPROGRESS ||
                finalStatus == Status.NOTSTARTED);

        int totalChanges = statusChanges.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
        assertEquals(threadCount, totalChanges);
    }

    @Test
    public void testShutdown() throws Exception {
        Task task = taskService.createTask("Task", Status.NOTSTARTED);
        projectService.addTaskToProject("P001", task);

        concurrencyService.simulateMultiUserTaskUpdates("P001", 2, 1);

        concurrencyService.shutdown();

        assertDoesNotThrow(() -> concurrencyService.shutdown());
    }

    @Test
    public void testParallelStreamProcessing() throws Exception {
        for (int i = 1; i <= 10; i++) {
            Task task = taskService.createTask("Task " + i, Status.NOTSTARTED);
            projectService.addTaskToProject("P001", task);
        }

        Task[] tasks = projectService.getTasksForProject("P001");
        assertEquals(10, tasks.length);

        concurrencyService.updateTasksInParallel("P001", Status.DONE);

        Task[] updatedTasks = projectService.getTasksForProject("P001");
        long completedCount = java.util.Arrays.stream(updatedTasks)
                .filter(t -> t.getStatus() == Status.DONE).count();

        assertEquals(10, completedCount);
    }

    @Test
    public void testConcurrentProjectAccess() throws Exception {
        Project p2 = new SoftwareProject("Project 2", "Test 2", 5000.0, 5);
        p2.setId("P002");
        projectService.addProject(p2);

        Task task1 = taskService.createTask("Task P1", Status.NOTSTARTED);
        Task task2 = taskService.createTask("Task P2", Status.NOTSTARTED);

        projectService.addTaskToProject("P001", task1);
        projectService.addTaskToProject("P002", task2);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        new Thread(() -> {
            try {
                startLatch.await();
                Task[] tasks = projectService.getTasksForProject("P001");
                taskService.updateTaskStatus(tasks[0], Status.DONE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                endLatch.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                startLatch.await();
                Task[] tasks = projectService.getTasksForProject("P002");
                taskService.updateTaskStatus(tasks[0], Status.INPROGRESS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                endLatch.countDown();
            }
        }).start();

        startLatch.countDown();
        assertTrue(endLatch.await(3, TimeUnit.SECONDS));

        Thread.sleep(100);

        Task[] p1Tasks = projectService.getTasksForProject("P001");
        Task[] p2Tasks = projectService.getTasksForProject("P002");

        assertEquals(Status.DONE, p1Tasks[0].getStatus());
        assertEquals(Status.INPROGRESS, p2Tasks[0].getStatus());
    }

    @Test
    public void testHighConcurrencyLoad() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Task task = taskService.createTask("Task " + i, Status.NOTSTARTED);
            projectService.addTaskToProject("P001", task);
        }

        concurrencyService.simulateMultiUserTaskUpdates("P001", 15, 5);

        Task[] updatedTasks = projectService.getTasksForProject("P001");
        assertEquals(5, updatedTasks.length);

        for (Task task : updatedTasks) {
            assertNotNull(task.getStatus());
        }
    }

    @Test
    public void testThreadSafety_ConcurrentAddTasks() throws Exception {
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successfulAdds = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Task task = taskService.createTask("Task " + index, Status.NOTSTARTED);
                    boolean added = projectService.addTaskToProject("P001", task);
                    if (added) {
                        successfulAdds.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Error adding task: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(endLatch.await(5, TimeUnit.SECONDS));

        Task[] tasks = projectService.getTasksForProject("P001");
        assertEquals(successfulAdds.get(), tasks.length);
        assertEquals(threadCount, tasks.length);
    }

    @Test
    public void testConcurrencyWithNullInputs() {
        assertDoesNotThrow(() -> {
            concurrencyService.simulateMultiUserTaskUpdates(null, 1, 1);
        });

        assertDoesNotThrow(() -> {
            concurrencyService.updateTasksInParallel(null, Status.DONE);
        });
    }

    @Test
    public void testConcurrencyWithEmptyProject() throws Exception {
        Project emptyProject = new SoftwareProject("Empty", "No tasks", 1000.0, 5);
        emptyProject.setId("P999");
        projectService.addProject(emptyProject);

        assertDoesNotThrow(() -> {
            concurrencyService.simulateMultiUserTaskUpdates("P999", 2, 1);
        });
    }

    @Test
    public void testSingleThreadExecution() throws Exception {
        Task task = taskService.createTask("Single Task", Status.NOTSTARTED);
        projectService.addTaskToProject("P001", task);

        concurrencyService.simulateMultiUserTaskUpdates("P001", 1, 1);

        Task[] updatedTasks = projectService.getTasksForProject("P001");
        assertNotNull(updatedTasks[0].getStatus());
    }
}
