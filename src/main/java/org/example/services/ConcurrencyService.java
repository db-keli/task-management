package org.example.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.example.enums.Status;
import org.example.models.Task;
import org.example.models.User;



public class ConcurrencyService {
    private final ProjectService projectService;
    private final TaskService taskService;
    private final UserService userService;
    private ExecutorService executorService;
    private final AtomicInteger completedUpdates = new AtomicInteger(0);
    private final AtomicInteger totalUpdates = new AtomicInteger(0);

    public ConcurrencyService(ProjectService projectService, TaskService taskService, UserService userService) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.userService = userService;
    }



    public void simulateMultiUserTaskUpdates(String projectId, int numThreads, int updatesPerThread) {
        try {

            Task[] tasks = projectService.getTasksForProject(projectId);
            if (tasks.length == 0) {
                System.out.println("No tasks found in project " + projectId);
                return;
            }


            completedUpdates.set(0);
            totalUpdates.set(numThreads * updatesPerThread);

            System.out.println("\n" + "=".repeat(60));
            System.out.println("SIMULATING MULTI-USER TASK UPDATES");
            System.out.println("=".repeat(60));
            System.out.println("Project ID: " + projectId);
            System.out.println("Number of concurrent users (threads): " + numThreads);
            System.out.println("Updates per user: " + updatesPerThread);
            System.out.println("Total updates: " + totalUpdates.get());
            System.out.println("Tasks available: " + tasks.length);
            System.out.println("=".repeat(60) + "\n");


            executorService = Executors.newFixedThreadPool(numThreads);


            User[] users = userService.getAllUsers();
            if (users.length == 0) {
                System.out.println("No users available for task assignment.");
                executorService.shutdown();
                return;
            }


            List<Future<?>> futures = new ArrayList<>();


            for (int i = 0; i < numThreads; i++) {
                final int userIdIndex = i % users.length;
                final String userName = users[userIdIndex].getName();
                final String userId = users[userIdIndex].getId();

                Future<?> future = executorService.submit(() -> {
                    performTaskUpdates(projectId, tasks, userName, userId, updatesPerThread);
                });
                futures.add(future);
            }


            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    System.err.println("Error in concurrent update: " + e.getMessage());
                }
            }


            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }


            displayFinalResults(projectId);

        } catch (Exception e) {
            System.err.println("Error during concurrent updates: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void performTaskUpdates(String projectId, Task[] tasks, String userName, String userId, int numUpdates) {
        for (int i = 0; i < numUpdates; i++) {
            try {

                Task task = tasks[(int) (Math.random() * tasks.length)];

                if (task == null) {
                    continue;
                }


                Thread.sleep(50 + (int) (Math.random() * 100));


                Status currentStatus = task.getStatus();
                Status newStatus = getNextStatus(currentStatus);

                taskService.updateTaskStatus(task, newStatus);


                task.setAssignedUserId(userId);


                int completed = completedUpdates.incrementAndGet();
                displayProgress(userName, task.getId(), currentStatus, newStatus, completed, totalUpdates.get());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error updating task for user " + userName + ": " + e.getMessage());
            }
        }
    }



    private Status getNextStatus(Status currentStatus) {
        return switch (currentStatus) {
            case NOTSTARTED -> Status.INPROGRESS;
            case INPROGRESS -> Status.DONE;
            case DONE -> Status.DONE;
        };
    }



    private synchronized void displayProgress(String userName, String taskId, Status oldStatus, Status newStatus,
                                              int completed, int total) {
        String statusBar = generateProgressBar(completed, total);
        System.out.printf("[%s] User: %-15s | Task: %s | %s → %s | Progress: %d/%d %s%n",
                Thread.currentThread().getName(),
                userName,
                taskId,
                oldStatus,
                newStatus,
                completed,
                total,
                statusBar);
    }



    private String generateProgressBar(int completed, int total) {
        int barLength = 20;
        int filled = (int) ((double) completed / total * barLength);
        return "[" + "=".repeat(filled) + " ".repeat(barLength - filled) + "]";
    }



    private void displayFinalResults(String projectId) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("CONCURRENT UPDATES COMPLETED");
        System.out.println("=".repeat(60));

        try {
            Task[] tasks = projectService.getTasksForProject(projectId);


            long notStarted = Arrays.stream(tasks)
                    .parallel()
                    .filter(task -> task.getStatus() == Status.NOTSTARTED)
                    .count();

            long inProgress = Arrays.stream(tasks)
                    .parallel()
                    .filter(task -> task.getStatus() == Status.INPROGRESS)
                    .count();

            long done = Arrays.stream(tasks)
                    .parallel()
                    .filter(task -> task.getStatus() == Status.DONE)
                    .count();

            System.out.println("Final Task Status Summary:");
            System.out.println("  NOTSTARTED:  " + notStarted);
            System.out.println("  INPROGRESS:  " + inProgress);
            System.out.println("  DONE:        " + done);
            System.out.println("  Total:       " + tasks.length);

            double completionPercentage = projectService.getProjectCompletionPercentage(projectId) * 100;
            System.out.printf("  Completion:  %.1f%%%n", completionPercentage);
            System.out.println("=".repeat(60) + "\n");

        } catch (Exception e) {
            System.err.println("Error displaying final results: " + e.getMessage());
        }
    }



    public void updateTasksInParallel(String projectId, Status targetStatus) {
        try {
            Task[] tasks = projectService.getTasksForProject(projectId);

            System.out.println("\nUpdating " + tasks.length + " tasks in parallel using parallel streams...");


            Arrays.stream(tasks)
                    .parallel()
                    .forEach(task -> {
                        if (task != null) {
                            taskService.updateTaskStatus(task, targetStatus);
                            System.out.println("Updated task " + task.getId() + " to " + targetStatus +
                                    " [Thread: " + Thread.currentThread().getName() + "]");
                        }
                    });

            System.out.println("\nParallel update completed!");

        } catch (Exception e) {
            System.err.println("Error in parallel update: " + e.getMessage());
        }
    }



    public void updateTasksAsync(String projectId, List<String> taskIds, Status newStatus) {
        ExecutorService executor = Executors.newFixedThreadPool(taskIds.size());
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        System.out.println("\nUpdating tasks asynchronously using CompletableFuture...");

        for (String taskId : taskIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Task[] tasks = projectService.getTasksForProject(projectId);
                    Task task = Arrays.stream(tasks)
                            .filter(t -> t != null && taskId.equals(t.getId()))
                            .findFirst()
                            .orElse(null);

                    if (task != null) {
                        taskService.updateTaskStatus(task, newStatus);
                        System.out.println("Async update: Task " + taskId + " → " + newStatus +
                                " [Thread: " + Thread.currentThread().getName() + "]");
                    }
                } catch (Exception e) {
                    System.err.println("Error updating task " + taskId + ": " + e.getMessage());
                }
            }, executor);

            futures.add(future);
        }


        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    System.out.println("\nAll asynchronous updates completed!");
                    executor.shutdown();
                })
                .join();
    }



    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
