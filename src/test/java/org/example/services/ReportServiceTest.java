package org.example.services;

import static org.junit.jupiter.api.Assertions.*;

import org.example.enums.Status;
import org.example.exceptions.EmptyProjectException;
import org.example.models.Project;
import org.example.models.SoftwareProject;
import org.example.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReportServiceTest {
    private ReportService reportService;
    private ProjectService projectService;
    private TaskService taskService;

    @BeforeEach
    public void setUp() {
        projectService = new ProjectService();
        taskService = new TaskService();
        reportService = new ReportService(projectService);
    }

    @Test
    public void testGenerateStatusReport_EmptyProjects() throws Exception {
        StatusReportData[] report = reportService.generateStatusReport();
        assertNotNull(report);
        assertEquals(0, report.length);
    }

    @Test
    public void testGenerateStatusReport_ProjectWithoutTasks() throws Exception {
        Project project = new SoftwareProject("Empty Project", "No tasks", 1000.0, 5);
        project.setId("P001");
        projectService.addProject(project);
        
        assertThrows(EmptyProjectException.class, () -> reportService.generateStatusReport());
    }

    @Test
    public void testGenerateStatusReport_SingleProject() throws Exception {
        Project project = new SoftwareProject("Test Project", "Test", 5000.0, 10);
        project.setId("P001");
        projectService.addProject(project);
        
        Task task1 = taskService.createTask("Task 1", Status.DONE);
        Task task2 = taskService.createTask("Task 2", Status.INPROGRESS);
        Task task3 = taskService.createTask("Task 3", Status.DONE);
        
        projectService.addTaskToProject("P001", task1);
        projectService.addTaskToProject("P001", task2);
        projectService.addTaskToProject("P001", task3);
        
        StatusReportData[] report = reportService.generateStatusReport();
        
        assertNotNull(report);
        assertEquals(1, report.length);
        assertEquals("P001", report[0].projectId());
        assertEquals("Test Project", report[0].projectName());
        assertEquals(3, report[0].totalTasks());
        assertEquals(2, report[0].completedTasks());
    }

    @Test
    public void testGenerateStatusReport_MultipleProjects() throws Exception {
        Project p1 = new SoftwareProject("Project 1", "Test 1", 1000.0, 5);
        p1.setId("P001");
        projectService.addProject(p1);
        
        Project p2 = new SoftwareProject("Project 2", "Test 2", 2000.0, 10);
        p2.setId("P002");
        projectService.addProject(p2);
        
        Task t1 = taskService.createTask("Task 1", Status.DONE);
        Task t2 = taskService.createTask("Task 2", Status.DONE);
        projectService.addTaskToProject("P001", t1);
        projectService.addTaskToProject("P001", t2);
        
        Task t3 = taskService.createTask("Task 3", Status.NOTSTARTED);
        Task t4 = taskService.createTask("Task 4", Status.INPROGRESS);
        Task t5 = taskService.createTask("Task 5", Status.DONE);
        projectService.addTaskToProject("P002", t3);
        projectService.addTaskToProject("P002", t4);
        projectService.addTaskToProject("P002", t5);
        
        StatusReportData[] report = reportService.generateStatusReport();
        
        assertNotNull(report);
        assertEquals(2, report.length);
        
        assertEquals(2, report[0].totalTasks());
        assertEquals(2, report[0].completedTasks());
        
        assertEquals(3, report[1].totalTasks());
        assertEquals(1, report[1].completedTasks());
    }

    @Test
    public void testGenerateStatusReport_CompletionPercentage() throws Exception {
        Project project = new SoftwareProject("Test", "Test", 1000.0, 5);
        project.setId("P001");
        projectService.addProject(project);
        
        for (int i = 0; i < 10; i++) {
            Status status = (i < 7) ? Status.DONE : Status.NOTSTARTED;
            Task task = taskService.createTask("Task " + i, status);
            projectService.addTaskToProject("P001", task);
        }
        
        StatusReportData[] report = reportService.generateStatusReport();
        
        assertEquals(1, report.length);
        assertEquals(10, report[0].totalTasks());
        assertEquals(7, report[0].completedTasks());
        assertEquals(0.7, report[0].completionPercentage(), 0.01);
    }

    @Test
    public void testCalculateAverageCompletion_Empty() {
        StatusReportData[] emptyReport = new StatusReportData[0];
        double average = reportService.calculateAverageCompletion(emptyReport);
        assertEquals(0.0, average);
    }

    @Test
    public void testCalculateAverageCompletion_Null() {
        double average = reportService.calculateAverageCompletion(null);
        assertEquals(0.0, average);
    }

    @Test
    public void testCalculateAverageCompletion_SingleProject() throws Exception {
        Project project = new SoftwareProject("Test", "Test", 1000.0, 5);
        project.setId("P001");
        projectService.addProject(project);
        
        Task t1 = taskService.createTask("T1", Status.DONE);
        Task t2 = taskService.createTask("T2", Status.DONE);
        projectService.addTaskToProject("P001", t1);
        projectService.addTaskToProject("P001", t2);
        
        StatusReportData[] report = reportService.generateStatusReport();
        double average = reportService.calculateAverageCompletion(report);
        
        assertEquals(1.0, average, 0.01);
    }

    @Test
    public void testCalculateAverageCompletion_MultipleProjects() throws Exception {
        Project p1 = new SoftwareProject("P1", "Test", 1000.0, 5);
        p1.setId("P001");
        projectService.addProject(p1);
        
        Project p2 = new SoftwareProject("P2", "Test", 2000.0, 10);
        p2.setId("P002");
        projectService.addProject(p2);
        
        Task t1 = taskService.createTask("T1", Status.DONE);
        Task t2 = taskService.createTask("T2", Status.DONE);
        projectService.addTaskToProject("P001", t1);
        projectService.addTaskToProject("P001", t2);
        
        Task t3 = taskService.createTask("T3", Status.DONE);
        Task t4 = taskService.createTask("T4", Status.NOTSTARTED);
        projectService.addTaskToProject("P002", t3);
        projectService.addTaskToProject("P002", t4);
        
        StatusReportData[] report = reportService.generateStatusReport();
        double average = reportService.calculateAverageCompletion(report);
        
        assertEquals(0.75, average, 0.01);
    }

    @Test
    public void testCalculateAverageCompletion_ZeroCompletion() throws Exception {
        Project project = new SoftwareProject("Test", "Test", 1000.0, 5);
        project.setId("P001");
        projectService.addProject(project);
        
        Task t1 = taskService.createTask("T1", Status.NOTSTARTED);
        Task t2 = taskService.createTask("T2", Status.INPROGRESS);
        projectService.addTaskToProject("P001", t1);
        projectService.addTaskToProject("P001", t2);
        
        StatusReportData[] report = reportService.generateStatusReport();
        double average = reportService.calculateAverageCompletion(report);
        
        assertEquals(0.0, average, 0.01);
    }

    @Test
    public void testGenerateStatusReport_MixedCompletion() throws Exception {
        Project project = new SoftwareProject("Mixed", "Test", 1000.0, 5);
        project.setId("P001");
        projectService.addProject(project);
        
        projectService.addTaskToProject("P001", taskService.createTask("T1", Status.DONE));
        projectService.addTaskToProject("P001", taskService.createTask("T2", Status.NOTSTARTED));
        projectService.addTaskToProject("P001", taskService.createTask("T3", Status.INPROGRESS));
        projectService.addTaskToProject("P001", taskService.createTask("T4", Status.DONE));
        projectService.addTaskToProject("P001", taskService.createTask("T5", Status.DONE));
        
        StatusReportData[] report = reportService.generateStatusReport();
        
        assertEquals(1, report.length);
        assertEquals(5, report[0].totalTasks());
        assertEquals(3, report[0].completedTasks());
        assertEquals(0.6, report[0].completionPercentage(), 0.01);
    }

    @Test
    public void testGenerateStatusReport_MultipleProjectsVaryingCompletion() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Project p = new SoftwareProject("Project " + i, "Test", 1000.0 * i, i * 5);
            p.setId(String.format("P%03d", i));
            projectService.addProject(p);
            
            for (int j = 0; j < i * 2; j++) {
                Status status = (j % 2 == 0) ? Status.DONE : Status.NOTSTARTED;
                Task t = taskService.createTask("Task " + j, status);
                projectService.addTaskToProject(p.getId(), t);
            }
        }
        
        StatusReportData[] report = reportService.generateStatusReport();
        
        assertEquals(5, report.length);
        
        for (int i = 0; i < 5; i++) {
            assertEquals((i + 1) * 2, report[i].totalTasks());
            assertEquals((i + 1), report[i].completedTasks());
        }
    }

    @Test
    public void testGenerateStatusReport_FullyCompleted() throws Exception {
        Project project = new SoftwareProject("Completed", "Test", 5000.0, 10);
        project.setId("P001");
        projectService.addProject(project);
        
        for (int i = 0; i < 5; i++) {
            Task task = taskService.createTask("Task " + i, Status.DONE);
            projectService.addTaskToProject("P001", task);
        }
        
        StatusReportData[] report = reportService.generateStatusReport();
        
        assertEquals(1, report.length);
        assertEquals(5, report[0].totalTasks());
        assertEquals(5, report[0].completedTasks());
        assertEquals(1.0, report[0].completionPercentage(), 0.01);
    }

    @Test
    public void testGenerateStatusReport_NoCompletion() throws Exception {
        Project project = new SoftwareProject("Not Started", "Test", 5000.0, 10);
        project.setId("P001");
        projectService.addProject(project);
        
        for (int i = 0; i < 5; i++) {
            Task task = taskService.createTask("Task " + i, Status.NOTSTARTED);
            projectService.addTaskToProject("P001", task);
        }
        
        StatusReportData[] report = reportService.generateStatusReport();
        
        assertEquals(1, report.length);
        assertEquals(5, report[0].totalTasks());
        assertEquals(0, report[0].completedTasks());
        assertEquals(0.0, report[0].completionPercentage(), 0.01);
    }

    @Test
    public void testCalculateAverageCompletion_LargeDataset() throws Exception {
        for (int i = 1; i <= 20; i++) {
            Project p = new SoftwareProject("Project " + i, "Test", 1000.0, 5);
            p.setId(String.format("P%03d", i));
            projectService.addProject(p);
            
            int completedCount = i % 10;
            for (int j = 0; j < 10; j++) {
                Status status = (j < completedCount) ? Status.DONE : Status.NOTSTARTED;
                Task t = taskService.createTask("Task " + j, status);
                projectService.addTaskToProject(p.getId(), t);
            }
        }
        
        StatusReportData[] report = reportService.generateStatusReport();
        assertEquals(20, report.length);
        
        double average = reportService.calculateAverageCompletion(report);
        assertTrue(average >= 0.0 && average <= 1.0);
    }
}
