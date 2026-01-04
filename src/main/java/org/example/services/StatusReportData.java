package org.example.services;

public record StatusReportData(String projectId, String projectName, int totalTasks, int completedTasks,
                               double completionPercentage) {
}

