# Design Decisions

## IdCounterManager: Centralized ID Management

### Overview

I created the `IdCounterManager` as a centralized singleton service responsible for generating unique identifiers for all model types. It replaces the previous approach where each service managed its own ID counters independently.

### Problem

ID management was scattered across services:
- Each service had its own counter (`TaskService`, `ProjectService`)
- No centralized mechanism
- Potential for ID conflicts
- Difficult to test
- No thread-safety

### Solution

I added all ID generation into a single component.

### Design Decisions

#### 1. Singleton Pattern

I implemented it as a singleton using double-checked locking to ensure a single instance and prevent duplicate IDs.

```java
private static volatile IdCounterManager instance;

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
```

#### 2. Thread-Safety

I used `ConcurrentHashMap` and `AtomicInteger` for thread-safe operations without external 
synchronization since I'm using a single manager for all model types.

```java
private final ConcurrentHashMap<ModelType, AtomicInteger> counters;
int nextValue = counter.getAndIncrement();
```
#### 3. Enum-Based Model Types

I used a `ModelType` enum for type safety and easy extensibility.

```java
public enum ModelType {
    USER("U"),
    PROJECT("P"),
    TASK("T"),
    STATUS_REPORT("SR");
}
```

#### 4. Formatted String IDs

I generate formatted IDs like "U001", "P001" instead of plain integers for readability and easier debugging.

```java
return String.format("%s%03d", modelType.getPrefix(), nextValue);
```

#### 5. Testing Support

I included methods to reset counters and inspect state for easier testing:
- `resetCounter(ModelType)` - Reset specific counter
- `resetAllCounters()` - Reset all counters
- `getCurrentCounter(ModelType)` - Inspect current value
- `setCounter(ModelType, int)` - Set specific value
- `resetInstance()` - Reset singleton

### Benefits

1. Single point of control
2. Uniform ID format
3. Thread-safe
4. Easy to test
5. Easy to maintain
6. Extensible

### Usage

```java
IdCounterManager idManager = IdCounterManager.getInstance();
String userId = idManager.getNextId(ModelType.USER);        // "U001"
String projectId = idManager.getNextId(ModelType.PROJECT);  // "P001"
String taskId = idManager.getNextId(ModelType.TASK);        // "T001"
```

### Integration

I integrated it into `UserService`, `ProjectService`, and `TaskService`. All services initialize the manager in their constructors.

### Conclusion

The `IdCounterManager` centralizes ID generation, improving code quality, testability, and maintainability while ensuring thread-safety and consistency.
