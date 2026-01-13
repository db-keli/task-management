package org.example.services;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.example.enums.ModelType;
import org.example.enums.Status;
import org.example.exceptions.ValidationException;
import org.example.interfaces.TaskFilter;
import org.example.models.Task;
import org.example.utils.IdCounterManager;

public class TaskService {
    private static final Pattern TASK_ID_PATTERN = Pattern.compile("^T\\d{3}$");
    private static final Pattern TASK_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9\\s_-]{1,100}$");
    private final IdCounterManager idManager;

    public TaskService() {
        this.idManager = IdCounterManager.getInstance();
    }



    public boolean isValidTaskId(String taskId) {
        if (taskId == null) {
            return false;
        }
        return TASK_ID_PATTERN.matcher(taskId).matches();
    }



    public void validateTaskId(String taskId) {
        if (!isValidTaskId(taskId)) {
            throw new ValidationException(
                    "Invalid task ID format. Expected pattern: T\\d{3} (e.g., T001, T123). Got: " + taskId);
        }
    }



    public boolean isValidTaskName(String taskName) {
        if (taskName == null || taskName.trim().isEmpty()) {
            return false;
        }
        return TASK_NAME_PATTERN.matcher(taskName.trim()).matches();
    }



    public void validateTaskName(String taskName) {
        if (!isValidTaskName(taskName)) {
            throw new ValidationException(
                    "Invalid task name. Must be 1-100 characters (alphanumeric, spaces, hyphens, underscores). Got: " + taskName);
        }
    }

    public Task createTask(String name, Status status) {
        validateTaskName(name);
        if (status == null) {
            throw new ValidationException("Task status cannot be null");
        }

        try {
            String id = idManager.getNextId(ModelType.TASK);

            validateTaskId(id);
            Task task = new Task(name.trim(), status);
            task.setId(id);
            return task;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to generate task ID: " + e.getMessage(), e);
        }
    }

    public Task createTaskFromStatusString(String name, String statusString) {
        try {
            Status status = Status.valueOf(statusString.toUpperCase());
            return createTask(name, status);
        } catch (IllegalArgumentException e) {
            return createTask(name, Status.NOTSTARTED);
        }
    }

    public Status mapStatusFromChoice(int choice) {
        return switch (choice) {
            case 1 -> Status.NOTSTARTED;
            case 2 -> Status.INPROGRESS;
            case 3 -> Status.DONE;
            default -> Status.NOTSTARTED;
        };
    }



    public synchronized boolean updateTaskStatus(Task task, Status status) {
        if (task == null || status == null) {
            return false;
        }
        task.setStatus(status);
        return true;
    }

    public boolean updateTaskStatusFromString(Task task, String statusString) {
        try {
            Status status = Status.valueOf(statusString.toUpperCase());
            return updateTaskStatus(task, status);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }



    public void validateTask(Task task) {
        if (task == null) {
            throw new ValidationException("Task cannot be null");
        }
        if (task.getId() != null) {
            validateTaskId(task.getId());
        }
        if (task.getName() != null) {
            validateTaskName(task.getName());
        }
    }



    public List<Task> filterTasks(List<Task> tasks, TaskFilter filter) {
        if (tasks == null) {
            return List.of();
        }
        if (filter == null) {
            return tasks;
        }
        return tasks.stream()
                .filter(task -> task != null && filter.test(task))
                .collect(Collectors.toList());
    }



    public Task[] filterTasks(Task[] tasks, TaskFilter filter) {
        if (tasks == null) {
            return new Task[0];
        }
        if (filter == null) {
            return tasks;
        }
        return Stream.of(tasks)
                .filter(task -> task != null && filter.test(task))
                .toArray(Task[]::new);
    }



    public List<Task> filterTasksWithPredicate(List<Task> tasks, Predicate<Task> predicate) {
        if (tasks == null) {
            return List.of();
        }
        if (predicate == null) {
            return tasks;
        }
        return tasks.stream()
                .filter(task -> task != null && predicate.test(task))
                .collect(Collectors.toList());
    }



    public static class TaskFilters {


        public static final TaskFilter COMPLETED = Task::isCompleted;



        public static TaskFilter byStatus(Status status) {
            return task -> task != null && task.getStatus() == status;
        }



        public static TaskFilter byAssignedUser(String userId) {
            return task -> task != null && userId != null && userId.equals(task.getAssignedUserId());
        }



        public static TaskFilter withValidId() {
            return task -> task != null && task.getId() != null &&
                    Pattern.matches("^T\\d{3}$", task.getId());
        }



        public static TaskFilter byNamePattern(String namePattern) {
            if (namePattern == null) {
                return task -> false;
            }
            Pattern pattern = Pattern.compile(namePattern, Pattern.CASE_INSENSITIVE);
            return task -> task != null && task.getName() != null &&
                    pattern.matcher(task.getName()).find();
        }



        public static final TaskFilter NOT_COMPLETED = task -> task != null && !task.isCompleted();



        public static final TaskFilter IN_PROGRESS = byStatus(Status.INPROGRESS);



        public static final TaskFilter NOT_STARTED = byStatus(Status.NOTSTARTED);
    }
}
