package org.example.services;

import java.util.ArrayList;
import java.util.List;

import org.example.exceptions.EmptyProjectException;
import org.example.models.Project;
import org.example.models.Task;

public class ReportService {
    private final ProjectService projectService;

    public ReportService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public StatusReportData[] generateStatusReport() throws EmptyProjectException {
        Project[] projects = projectService.getAllProjects();
        if (projects.length == 0) {
            return new StatusReportData[0];
        }

        List<StatusReportData> reportData = new ArrayList<>();
        for (Project project : projects) {
            if (project == null) {
                continue; // Skip null projects
            }
            Task[] tasks = projectService.getTasksForProject(project.getId());
            if (tasks.length == 0) {
                throw new EmptyProjectException("Project '" + project.getName() + "' (ID: "
                        + project.getId() + ") has no tasks");
            }
            int completed = 0;
            for (Task task : tasks) {
                if (task != null && task.isCompleted()) {
                    completed++;
                }
            }
            double percent = projectService.getProjectCompletionPercentage(project.getId());
            reportData.add(new StatusReportData(project.getId(), project.getName(), tasks.length,
                    completed, percent));
        }
        return reportData.toArray(new StatusReportData[0]);
    }

    public double calculateAverageCompletion(StatusReportData[] reportData) {
        if (reportData == null || reportData.length == 0) {
            return 0.0;
        }
        double total = 0.0;
        int validCount = 0;
        for (StatusReportData data : reportData) {
            if (data != null) {
                total += data.completionPercentage();
                validCount++;
            }
        }
        return validCount > 0 ? total / validCount : 0.0;
    }
}
