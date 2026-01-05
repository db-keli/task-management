package org.example.exceptions;

public class ProjectNotFoundException extends Exception {
    public ProjectNotFoundException(String message) {
        super(message);
    }
}