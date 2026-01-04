package org.example.services;

import org.example.models.Project;
import org.example.models.Task;

import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private final ProjectService projectService;

    public ReportService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public StatusReportData[] generateStatusReport() {
        Project[] projects = projectService.getAllProjects();
        if (projects.length == 0) {
            return new StatusReportData[0];
        }
        
        List<StatusReportData> reportData = new ArrayList<>();
        for (Project project : projects) {
            Task[] tasks = projectService.getTasksForProject(project.getId());
            int completed = 0;
            for (Task task : tasks) {
                if (task != null && task.isCompleted()) {
                    completed++;
                }
            }
            double percent = projectService.getProjectCompletionPercentage(project.getId());
            reportData.add(new StatusReportData(
                project.getId(),
                project.getName(),
                tasks.length,
                completed,
                percent
            ));
        }
        return reportData.toArray(new StatusReportData[0]);
    }

    public double calculateAverageCompletion(StatusReportData[] reportData) {
        if (reportData.length == 0) {
            return 0.0;
        }
        double total = 0.0;
        for (StatusReportData data : reportData) {
            total += data.completionPercentage();
        }
        return total / reportData.length;
    }
}