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

    public Task createTask(String name, String status) {
        String id = idManager.getNextId(ModelType.TASK);
        Task task = new Task(name, status);
        task.setId(id);
        return task;
    }

    public boolean updateTaskStatus(Task task, String status) {
        try {
            Status newStatus = Status.valueOf(status.toUpperCase());
            task.setStatus(newStatus);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}