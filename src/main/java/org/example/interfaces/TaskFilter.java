package org.example.interfaces;

import java.util.function.Predicate;

import org.example.models.Task;

@FunctionalInterface
public interface TaskFilter extends Predicate<Task> {
    @Override
    boolean test(Task task);
}
