package org.example.utils;

import org.example.enums.ModelType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class IdCounterManager {
    private static volatile IdCounterManager instance;
    private final ConcurrentHashMap<ModelType, AtomicInteger> counters;

    private IdCounterManager() {
        counters = new ConcurrentHashMap<>();
        for (ModelType type : ModelType.values()) {
            counters.put(type, new AtomicInteger(1));
        }
    }

    public static IdCounterManager getInstance() {
        if (instance == null) {
            synchronized (IdCounterManager.class) {
                if (instance == null) {
                    instance = new IdCounterManager();
                }
            }
        }
        return instance;
    }

    public String getNextId(ModelType modelType) {
        if (modelType == null) {
            throw new IllegalArgumentException("ModelType cannot be null");
        }
        
        AtomicInteger counter = counters.get(modelType);
        if (counter == null) {
            throw new IllegalArgumentException("Unknown ModelType: " + modelType);
        }
        
        int nextValue = counter.getAndIncrement();
        return String.format("%s%03d", modelType.getPrefix(), nextValue);
    }

    public int getNextIntId(ModelType modelType) {
        if (modelType == null) {
            throw new IllegalArgumentException("ModelType cannot be null");
        }
        
        AtomicInteger counter = counters.get(modelType);
        if (counter == null) {
            throw new IllegalArgumentException("Unknown ModelType: " + modelType);
        }
        
        return counter.getAndIncrement();
    }

    public void resetCounter(ModelType modelType) {
        if (modelType == null) {
            throw new IllegalArgumentException("ModelType cannot be null");
        }
        
        AtomicInteger counter = counters.get(modelType);
        if (counter != null) {
            counter.set(1);
        }
    }

    public void resetAllCounters() {
        for (ModelType type : ModelType.values()) {
            AtomicInteger counter = counters.get(type);
            if (counter != null) {
                counter.set(1);
            }
        }
    }

    public int getCurrentCounter(ModelType modelType) {
        if (modelType == null) {
            throw new IllegalArgumentException("ModelType cannot be null");
        }
        
        AtomicInteger counter = counters.get(modelType);
        if (counter == null) {
            throw new IllegalArgumentException("Unknown ModelType: " + modelType);
        }
        
        return counter.get();
    }

    public void setCounter(ModelType modelType, int value) {
        if (modelType == null) {
            throw new IllegalArgumentException("ModelType cannot be null");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Counter value cannot be negative");
        }
        
        AtomicInteger counter = counters.get(modelType);
        if (counter != null) {
            counter.set(value);
        }
    }

    // only used when testing
    public static void resetInstance() {
        synchronized (IdCounterManager.class) {
            instance = null;
        }
    }
}
