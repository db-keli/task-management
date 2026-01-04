package org.example.models;

import org.example.enums.Status;
import org.example.interfaces.Completable;

public class Task implements Completable {
    private String id;
    private String name;
    private Status status;
    private String assignedUserId;

    public Task(String name, Status status) {
        this.name = name;
        this.status = status;
    }

    public boolean isCompleted() {
        return status == Status.DONE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(String assignedUserId) {
        this.assignedUserId = assignedUserId;
    }
}
