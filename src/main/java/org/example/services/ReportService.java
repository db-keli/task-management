package org.example.services;


import org.example.models.Project;
import org.example.models.Task;

public class ReportService {
    private ProjectService projectService;

    public ReportService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public void generateStatusReport() {
        Project[] projects = projectService.getAllProjects();
        if (projects.length == 0) {
            System.out.println("No projects available.");
            return;
        }
        double totalCompletion = 0.0;
        System.out.println("\nPROJECT STATUS REPORT");
        System.out.println("PROJECT ID | PROJECT NAME | TASKS | COMPLETED | PROGRESS (%)");
        for (Project p : projects) {
            Task[] tasks = p.getTasks();
            int completed = 0;
            for (Task t : tasks) {
                if (t.isCompleted()) {
                    completed++;
                }
            }
            double percent = p.getCompletionPercentage();
            totalCompletion += percent;
            System.out.println(
                    p.getId() + " | " + p.getName() + " | " + tasks.length + " | " + completed + " | " + percent + "%");
        }
        double average = totalCompletion / projects.length;
        System.out.println("AVERAGE COMPLETION: " + Math.round(average * 100.0) / 100.0 + "%");
    }
}