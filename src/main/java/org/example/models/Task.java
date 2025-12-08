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
}