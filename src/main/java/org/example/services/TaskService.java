package org.example.services;

import org.example.enums.ModelType;
import org.example.enums.Status;
import org.example.models.Task;
import org.example.utils.IdCounterManager;

public class TaskService {
    private final IdCounterManager idManager;

    public TaskService() {
        this.idManager = IdCounterManager.getInstance();
    }

    public Task createTask(String name, Status status) {
        String id = idManager.getNextId(ModelType.TASK);
        Task task = new Task(name, status);
        task.setId(id);
        return task;
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

    public boolean updateTaskStatus(Task task, Status status) {
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
}