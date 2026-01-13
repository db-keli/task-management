package org.example.utils;

import java.util.regex.Pattern;

import org.example.exceptions.ValidationException;

public class RegexValidator {
    private static final Pattern TASK_ID_PATTERN = Pattern.compile("^T\\d{3}$");
    private static final Pattern PROJECT_ID_PATTERN = Pattern.compile("^P\\d{3}$");
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^U\\d{3}$");

    private static final Pattern TASK_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9\\s_-]{1,100}$");
    private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9\\s_-]{1,100}$");
    private static final Pattern USER_NAME_PATTERN = Pattern.compile("^[A-Za-z\\s]{1,50}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern POSITIVE_NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^\\d+$");

    private RegexValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isValidTaskId(String taskId) {
        return taskId != null && TASK_ID_PATTERN.matcher(taskId).matches();
    }

    public static void validateTaskId(String taskId) {
        if (!isValidTaskId(taskId)) {
            throw new ValidationException(
                    "Invalid task ID format. Expected pattern: T\\d{3} (e.g., T001, T123). Got: "
                            + taskId);
        }
    }

    public static boolean isValidProjectId(String projectId) {
        return projectId != null && PROJECT_ID_PATTERN.matcher(projectId).matches();
    }

    public static void validateProjectId(String projectId) {
        if (!isValidProjectId(projectId)) {
            throw new ValidationException(
                    "Invalid project ID format. Expected pattern: P\\d{3} (e.g., P001, P123). Got: "
                            + projectId);
        }
    }

    public static boolean isValidUserId(String userId) {
        return userId != null && USER_ID_PATTERN.matcher(userId).matches();
    }

    public static void validateUserId(String userId) {
        if (!isValidUserId(userId)) {
            throw new ValidationException(
                    "Invalid user ID format. Expected pattern: U\\d{3} (e.g., U001, U123). Got: "
                            + userId);
        }
    }

    public static boolean isValidTaskName(String taskName) {
        if (taskName == null || taskName.trim().isEmpty()) {
            return false;
        }
        return TASK_NAME_PATTERN.matcher(taskName.trim()).matches();
    }

    public static void validateTaskName(String taskName) {
        if (!isValidTaskName(taskName)) {
            throw new ValidationException(
                    "Invalid task name. Must be 1-100 characters (alphanumeric, spaces, hyphens, underscores). Got: "
                            + taskName);
        }
    }

    public static boolean isValidProjectName(String projectName) {
        if (projectName == null || projectName.trim().isEmpty()) {
            return false;
        }
        return PROJECT_NAME_PATTERN.matcher(projectName.trim()).matches();
    }

    public static void validateProjectName(String projectName) {
        if (!isValidProjectName(projectName)) {
            throw new ValidationException(
                    "Invalid project name. Must be 1-100 characters (alphanumeric, spaces, hyphens, underscores). Got: "
                            + projectName);
        }
    }

    public static boolean isValidUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return false;
        }
        return USER_NAME_PATTERN.matcher(userName.trim()).matches();
    }

    public static void validateUserName(String userName) {
        if (!isValidUserName(userName)) {
            throw new ValidationException(
                    "Invalid user name. Must be 1-50 characters (alphabetic and spaces only). Got: "
                            + userName);
        }
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new ValidationException(
                    "Invalid email format. Expected format: user@domain.com. Got: " + email);
        }
    }

    public static boolean isValidPositiveNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return POSITIVE_NUMBER_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return INTEGER_PATTERN.matcher(value.trim()).matches();
    }

    public static void validateBudget(double budget) {
        if (budget < 0) {
            throw new ValidationException(
                    "Invalid budget. Must be a positive number. Got: " + budget);
        }
    }

    public static void validateTeamSize(int teamSize) {
        if (teamSize <= 0) {
            throw new ValidationException(
                    "Invalid team size. Must be a positive integer. Got: " + teamSize);
        }
    }

    public static boolean matchesPattern(String value, String pattern) {
        if (value == null || pattern == null) {
            return false;
        }
        try {
            return Pattern.matches(pattern, value);
        } catch (Exception e) {
            return false;
        }
    }

    public static void validatePattern(String value, String pattern, String fieldName) {
        if (!matchesPattern(value, pattern)) {
            throw new ValidationException("Invalid " + fieldName
                    + ". Does not match required pattern: " + pattern + ". Got: " + value);
        }
    }
}
