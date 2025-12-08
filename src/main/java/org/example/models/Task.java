package org.example.models;

import org.example.enums.Status;
import org.example.interfaces.Completable;

public class Task implements Completable {
    private int id;
    private String name;
    private Status status;

    public boolean isCompleted() {
        return status == Status.DONE;
    }

    public int getId() {
        return id;
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
}