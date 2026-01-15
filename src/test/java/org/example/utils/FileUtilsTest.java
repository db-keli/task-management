package org.example.utils;

import java.util.ArrayList;
import java.util.List;

import org.example.enums.Status;
import org.example.exceptions.EmptyProjectException;
import org.example.exceptions.FileNotAvailableException;
import org.example.models.HardwareProject;
import org.example.models.Project;
import org.example.models.SoftwareProject;
import org.example.models.Task;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileUtilsTest {
    private List<Project> loadedProjects;
    private List<Task> loadedTasks;
    private List<String> loadedProjectIds;

    @BeforeEach
    public void setUp() throws FileNotAvailableException {
        loadedProjects = new ArrayList<>();
        loadedTasks = new ArrayList<>();
        loadedProjectIds = new ArrayList<>();
        try {
            FileUtils.deleteDataFile();
        } catch (FileNotAvailableException e) {
        }
    }

    @AfterEach
    public void tearDown() throws FileNotAvailableException {
        try {
            FileUtils.deleteDataFile();
        } catch (FileNotAvailableException e) {
        }
    }

    @Test
    public void testSaveAndLoadEmptyProjects()
            throws FileNotAvailableException, EmptyProjectException {
        Project[] emptyProjects = new Project[0];

        FileUtils.saveProjects(emptyProjects, projectId -> new Task[0]);
        assertTrue(FileUtils.dataFileExists());

        boolean loaded = FileUtils.loadProjects(project -> loadedProjects.add(project),
                (projectId, task) -> {
                    loadedTasks.add(task);
                    loadedProjectIds.add(projectId);
                });

        assertTrue(loaded);
        assertEquals(0, loadedProjects.size());
        assertEquals(0, loadedTasks.size());
    }

    @Test
    public void testSaveAndLoadSingleProject()
            throws FileNotAvailableException, EmptyProjectException {
        Project project = new SoftwareProject("Test Project", "Description", 5000.0, 10);
        project.setId("P001");

        Project[] projects = {project};

        FileUtils.saveProjects(projects, projectId -> new Task[0]);
        assertTrue(FileUtils.dataFileExists());

        FileUtils.loadProjects(p -> loadedProjects.add(p), (pid, task) -> {
            loadedTasks.add(task);
            loadedProjectIds.add(pid);
        });

        assertEquals(1, loadedProjects.size());
        Project loaded = loadedProjects.get(0);
        assertEquals("P001", loaded.getId());
        assertEquals("Test Project", loaded.getName());
        assertEquals("Description", loaded.getDescription());
        assertEquals(5000.0, loaded.getBudget());
        assertEquals(10, loaded.getTeamSize());
        assertEquals("Software", loaded.getType());
    }

    @Test
    public void testSaveAndLoadMultipleProjects()
            throws FileNotAvailableException, EmptyProjectException {
        Project p1 = new SoftwareProject("Project 1", "Desc 1", 1000.0, 5);
        p1.setId("P001");

        Project p2 = new HardwareProject("Project 2", "Desc 2", 2000.0, 10);
        p2.setId("P002");

        Project p3 = new SoftwareProject("Project 3", "Desc 3", 3000.0, 15);
        p3.setId("P003");

        Project[] projects = {p1, p2, p3};

        FileUtils.saveProjects(projects, projectId -> new Task[0]);

        FileUtils.loadProjects(p -> loadedProjects.add(p), (pid, task) -> {
        });

        assertEquals(3, loadedProjects.size());
        assertEquals("P001", loadedProjects.get(0).getId());
        assertEquals("P002", loadedProjects.get(1).getId());
        assertEquals("P003", loadedProjects.get(2).getId());
    }

    @Test
    public void testSaveAndLoadProjectsWithTasks()
            throws FileNotAvailableException, EmptyProjectException {
        Project project = new SoftwareProject("Project With Tasks", "Description", 5000.0, 10);
        project.setId("P001");

        Task task1 = new Task("Task 1", Status.DONE);
        task1.setId("T001");
        task1.setAssignedUserId("U001");

        Task task2 = new Task("Task 2", Status.INPROGRESS);
        task2.setId("T002");
        task2.setAssignedUserId("U002");

        Task task3 = new Task("Task 3", Status.NOTSTARTED);
        task3.setId("T003");

        Task[] tasks = {task1, task2, task3};

        Project[] projects = {project};

        FileUtils.saveProjects(projects, projectId -> {
            if (projectId.equals("P001")) {
                return tasks;
            }
            return new Task[0];
        });

        FileUtils.loadProjects(p -> loadedProjects.add(p), (pid, task) -> {
            loadedTasks.add(task);
            loadedProjectIds.add(pid);
        });

        assertEquals(1, loadedProjects.size());
        assertEquals(3, loadedTasks.size());

        assertEquals("T001", loadedTasks.get(0).getId());
        assertEquals("Task 1", loadedTasks.get(0).getName());
        assertEquals(Status.DONE, loadedTasks.get(0).getStatus());
        assertEquals("U001", loadedTasks.get(0).getAssignedUserId());

        assertEquals("T002", loadedTasks.get(1).getId());
        assertEquals(Status.INPROGRESS, loadedTasks.get(1).getStatus());

        assertEquals("T003", loadedTasks.get(2).getId());
        assertEquals(Status.NOTSTARTED, loadedTasks.get(2).getStatus());

        assertEquals("P001", loadedProjectIds.get(0));
        assertEquals("P001", loadedProjectIds.get(1));
        assertEquals("P001", loadedProjectIds.get(2));
    }

    @Test
    public void testLoadNonExistentFile() throws FileNotAvailableException {
        FileUtils.deleteDataFile();
        assertFalse(FileUtils.dataFileExists());

        boolean loaded = FileUtils.loadProjects(p -> loadedProjects.add(p),
                (pid, task) -> loadedTasks.add(task));

        assertFalse(loaded);
        assertEquals(0, loadedProjects.size());
        assertEquals(0, loadedTasks.size());
    }

    @Test
    public void testSpecialCharactersInNames()
            throws FileNotAvailableException, EmptyProjectException {
        Project project = new SoftwareProject("Project-Name_123", "Desc with spaces", 1000.0, 5);
        project.setId("P001");

        Task task = new Task("Task-Name_456", Status.DONE);
        task.setId("T001");

        Project[] projects = {project};
        Task[] tasks = {task};

        FileUtils.saveProjects(projects, pid -> tasks);

        FileUtils.loadProjects(p -> loadedProjects.add(p), (pid, t) -> loadedTasks.add(t));

        assertEquals("Project-Name_123", loadedProjects.get(0).getName());
        assertEquals("Task-Name_456", loadedTasks.get(0).getName());
    }

    @Test
    public void testMultipleProjectsWithDifferentTaskCounts()
            throws FileNotAvailableException, EmptyProjectException {
        Project p1 = new SoftwareProject("P1", "D1", 1000.0, 5);
        p1.setId("P001");

        Project p2 = new HardwareProject("P2", "D2", 2000.0, 10);
        p2.setId("P002");

        Task t1 = new Task("T1", Status.DONE);
        t1.setId("T001");

        Task t2 = new Task("T2", Status.INPROGRESS);
        t2.setId("T002");

        Task t3 = new Task("T3", Status.NOTSTARTED);
        t3.setId("T003");

        Project[] projects = {p1, p2};

        FileUtils.saveProjects(projects, projectId -> {
            if (projectId.equals("P001")) {
                return new Task[] {t1, t2, t3};
            } else if (projectId.equals("P002")) {
                return new Task[] {};
            }
            return new Task[0];
        });

        FileUtils.loadProjects(p -> loadedProjects.add(p), (pid, task) -> {
            loadedTasks.add(task);
            loadedProjectIds.add(pid);
        });

        assertEquals(2, loadedProjects.size());
        assertEquals(3, loadedTasks.size());

        long p001Tasks = loadedProjectIds.stream().filter(id -> id.equals("P001")).count();
        long p002Tasks = loadedProjectIds.stream().filter(id -> id.equals("P002")).count();

        assertEquals(3, p001Tasks);
        assertEquals(0, p002Tasks);
    }

    @Test
    public void testDataFileExists() throws FileNotAvailableException, EmptyProjectException {
        assertFalse(FileUtils.dataFileExists());

        Project project = new SoftwareProject("Test", "Test", 1000.0, 5);
        project.setId("P001");
        FileUtils.saveProjects(new Project[] {project}, pid -> new Task[0]);

        assertTrue(FileUtils.dataFileExists());
    }

    @Test
    public void testDeleteDataFile() throws FileNotAvailableException, EmptyProjectException {
        Project project = new SoftwareProject("Test", "Test", 1000.0, 5);
        project.setId("P001");
        FileUtils.saveProjects(new Project[] {project}, pid -> new Task[0]);

        assertTrue(FileUtils.dataFileExists());

        boolean deleted = FileUtils.deleteDataFile();
        assertTrue(deleted);
        assertFalse(FileUtils.dataFileExists());

        boolean deletedAgain = FileUtils.deleteDataFile();
        assertFalse(deletedAgain);
    }

    @Test
    public void testTaskWithoutAssignedUser()
            throws FileNotAvailableException, EmptyProjectException {
        Project project = new SoftwareProject("Test", "Test", 1000.0, 5);
        project.setId("P001");

        Task task = new Task("Task", Status.DONE);
        task.setId("T001");

        FileUtils.saveProjects(new Project[] {project}, pid -> new Task[] {task});

        FileUtils.loadProjects(p -> loadedProjects.add(p), (pid, t) -> loadedTasks.add(t));

        assertEquals(1, loadedTasks.size());
        assertNull(loadedTasks.get(0).getAssignedUserId());
    }

    @Test
    public void testLargeNumberOfProjects()
            throws FileNotAvailableException, EmptyProjectException {
        List<Project> projectList = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Project p = new SoftwareProject("Project" + i, "Desc" + i, 1000.0 * i, i);
            p.setId(String.format("P%03d", i));
            projectList.add(p);
        }

        Project[] projects = projectList.toArray(new Project[0]);
        FileUtils.saveProjects(projects, pid -> new Task[0]);

        FileUtils.loadProjects(p -> loadedProjects.add(p), (pid, task) -> {
        });

        assertEquals(50, loadedProjects.size());
    }

    @Test
    public void testDifferentProjectTypes()
            throws FileNotAvailableException, EmptyProjectException {
        Project software = new SoftwareProject("Software", "SW", 1000.0, 5);
        software.setId("P001");

        Project hardware = new HardwareProject("Hardware", "HW", 2000.0, 10);
        hardware.setId("P002");

        FileUtils.saveProjects(new Project[] {software, hardware}, pid -> new Task[0]);

        FileUtils.loadProjects(p -> loadedProjects.add(p), (pid, task) -> {
        });

        assertEquals(2, loadedProjects.size());
        assertEquals("Software", loadedProjects.get(0).getType());
        assertEquals("Hardware", loadedProjects.get(1).getType());
    }

    @Test
    public void testSaveProjectsNull() {
        assertThrows(FileNotAvailableException.class,
                () -> FileUtils.saveProjects(null, pid -> new Task[0]));
    }

    @Test
    public void testLoadProjectsNullFunctions() {
        assertThrows(FileNotAvailableException.class,
                () -> FileUtils.loadProjects(null, (pid, task) -> {
                }));

        assertThrows(FileNotAvailableException.class, () -> FileUtils.loadProjects(p -> {
        }, null));
    }

    @Test
    public void testLoadNonReadableFile() throws FileNotAvailableException, EmptyProjectException {
        Project project = new SoftwareProject("Test", "Test", 1000.0, 5);
        project.setId("P001");
        FileUtils.saveProjects(new Project[] {project}, pid -> new Task[0]);

        assertTrue(FileUtils.dataFileExists());
    }
}
