package org.example.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.example.enums.Status;
import org.example.exceptions.EmptyProjectException;
import org.example.exceptions.FileNotAvailableException;
import org.example.models.HardwareProject;
import org.example.models.Project;
import org.example.models.SoftwareProject;
import org.example.models.Task;

public class FileUtils {
    private static final String DATA_FILE = "projects_data.json";

    private FileUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void saveProjects(Project[] projects,
            java.util.function.Function<String, Task[]> getTasksFunction)
            throws FileNotAvailableException, EmptyProjectException {
        if (projects == null) {
            throw new EmptyProjectException("Cannot save null projects array");
        }

        Path filePath = Paths.get(DATA_FILE);

        try {
            List<String> lines = new ArrayList<>();
            lines.add("[");

            List<String> projectJsonLines = Arrays.stream(projects).map(project -> {
                Task[] tasks = getTasksFunction.apply(project.getId());
                return projectWithTasksToJson(project, tasks);
            }).collect(Collectors.toList());

            for (int i = 0; i < projectJsonLines.size(); i++) {
                String projectJson = projectJsonLines.get(i);
                String[] projectLines = projectJson.split("\n");
                for (int j = 0; j < projectLines.length; j++) {
                    String line = projectLines[j];
                    if (i < projectJsonLines.size() - 1 && j == projectLines.length - 1) {
                        line += ",";
                    }
                    lines.add("  " + line);
                }
            }

            lines.add("]");

            Files.write(filePath, lines, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new FileNotAvailableException(
                    "Failed to save projects to file: " + e.getMessage());
        } catch (Exception e) {
            throw new FileNotAvailableException(
                    "Unexpected error while saving projects: " + e.getMessage());
        }
    }

    public static boolean loadProjects(java.util.function.Consumer<Project> addProjectFunction,
            java.util.function.BiConsumer<String, Task> addTaskFunction)
            throws FileNotAvailableException {

        if (addProjectFunction == null || addTaskFunction == null) {
            throw new FileNotAvailableException("Project and task functions cannot be null");
        }

        Path filePath = Paths.get(DATA_FILE);

        if (!Files.exists(filePath)) {
            throw new FileNotAvailableException("Data file does not exist: " + DATA_FILE);
        }

        if (!Files.isReadable(filePath)) {
            throw new FileNotAvailableException(
                    "Data file is not readable (check permissions): " + DATA_FILE);
        }

        try {
            Files.readAllLines(filePath);

            try (Stream<String> stream = Files.lines(filePath)) {
                List<String> lines = stream.collect(Collectors.toList());

                if (lines.isEmpty()) {
                    System.out.println("Data file is empty. Starting with empty data.");
                    return false;
                }

                loadDataFromLines(lines, addProjectFunction, addTaskFunction);
                return true;
            }
        } catch (IOException e) {
            throw new FileNotAvailableException("Error reading data file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing data file: " + e.getMessage());
            System.out.println("Starting with empty data due to malformed data.");
            return false;
        }
    }

    private static String projectWithTasksToJson(Project project, Task[] tasks) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("    \"projectId\": \"").append(escapeJson(project.getId())).append("\",\n");
        json.append("    \"name\": \"").append(escapeJson(project.getName())).append("\",\n");
        json.append("    \"description\": \"").append(escapeJson(project.getDescription()))
                .append("\",\n");
        json.append("    \"type\": \"").append(project.getType()).append("\",\n");
        json.append("    \"budget\": ").append(project.getBudget()).append(",\n");
        json.append("    \"teamSize\": ").append(project.getTeamSize()).append(",\n");
        json.append("    \"tasks\": [\n");

        List<String> taskJsonLines =
                Arrays.stream(tasks).map(FileUtils::taskToJson).collect(Collectors.toList());

        for (int i = 0; i < taskJsonLines.size(); i++) {
            String taskLine = "      " + taskJsonLines.get(i);
            if (i < taskJsonLines.size() - 1) {
                taskLine += ",";
            }
            json.append(taskLine).append("\n");
        }

        json.append("    ]\n");
        json.append("}");

        return json.toString();
    }

    private static String taskToJson(Task task) {
        String assignedUserId = task.getAssignedUserId() != null ? task.getAssignedUserId() : "";
        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"status\":\"%s\",\"assignedUserId\":\"%s\"}",
                escapeJson(task.getId()), escapeJson(task.getName()), task.getStatus().name(),
                escapeJson(assignedUserId));
    }

    private static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t");
    }

    private static void loadDataFromLines(List<String> lines,
            java.util.function.Consumer<Project> addProjectFunction,
            java.util.function.BiConsumer<String, Task> addTaskFunction) {

        try {
            String jsonContent = lines.stream().collect(Collectors.joining("\n"));

            Pattern arrayPattern = Pattern.compile("^\\s*\\[(.*?)\\]\\s*$", Pattern.DOTALL);
            Matcher arrayMatcher = arrayPattern.matcher(jsonContent);

            if (arrayMatcher.find()) {
                String arrayContent = arrayMatcher.group(1);
                parseProjectsWithTasks(arrayContent, addProjectFunction, addTaskFunction);
            } else {
                Pattern contentPattern = Pattern.compile("\\[(.*)\\]", Pattern.DOTALL);
                Matcher contentMatcher = contentPattern.matcher(jsonContent);
                if (contentMatcher.find()) {
                    parseProjectsWithTasks(contentMatcher.group(1), addProjectFunction,
                            addTaskFunction);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON data: " + e.getMessage());
        }
    }

    private static void parseProjectsWithTasks(String arrayContent,
            java.util.function.Consumer<Project> addProjectFunction,
            java.util.function.BiConsumer<String, Task> addTaskFunction) {

        int start = 0;
        while (start < arrayContent.length()) {
            while (start < arrayContent.length()
                    && (Character.isWhitespace(arrayContent.charAt(start))
                            || arrayContent.charAt(start) == ',')) {
                start++;
            }

            if (start >= arrayContent.length()) {
                break;
            }

            if (arrayContent.charAt(start) == '{') {
                int braceCount = 0;
                int projectStart = start;
                int projectEnd = start;

                for (int i = start; i < arrayContent.length(); i++) {
                    char c = arrayContent.charAt(i);
                    if (c == '{') {
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                        if (braceCount == 0) {
                            projectEnd = i + 1;
                            break;
                        }
                    }
                }

                if (projectEnd > projectStart) {
                    String fullProjectJson = arrayContent.substring(projectStart, projectEnd);
                    try {
                        Pattern projectIdPattern =
                                Pattern.compile("\"projectId\"\\s*:\\s*\"([^\"]+)\"");
                        Matcher projectIdMatcher = projectIdPattern.matcher(fullProjectJson);
                        if (!projectIdMatcher.find()) {
                            start = projectEnd;
                            continue;
                        }

                        Pattern tasksPattern =
                                Pattern.compile("\"tasks\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
                        Matcher tasksMatcher = tasksPattern.matcher(fullProjectJson);
                        String tasksJson = "";
                        String projectJsonWithoutTasks = fullProjectJson;
                        if (tasksMatcher.find()) {
                            tasksJson = tasksMatcher.group(1);
                            projectJsonWithoutTasks =
                                    fullProjectJson.substring(0, tasksMatcher.start())
                                            + fullProjectJson.substring(tasksMatcher.end());
                        }

                        Map<String, String> projectData = parseProjectJson(projectJsonWithoutTasks);
                        if (projectData.containsKey("projectId") || projectData.containsKey("id")) {
                            String actualProjectId = projectData.containsKey("projectId")
                                    ? projectData.get("projectId")
                                    : projectData.get("id");

                            Project project = createProject(projectData);
                            if (project != null) {
                                addProjectFunction.accept(project);

                                if (tasksJson != null && !tasksJson.trim().isEmpty()) {
                                    parseAndLoadTasks(actualProjectId, tasksJson, addTaskFunction);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing project with tasks: " + e.getMessage());
                    }

                    start = projectEnd;
                } else {
                    break;
                }
            } else {
                start++;
            }
        }
    }

    private static void parseAndLoadTasks(String projectId, String tasksJson,
            java.util.function.BiConsumer<String, Task> addTaskFunction) {

        if (tasksJson == null || tasksJson.trim().isEmpty()) {
            return;
        }

        int start = 0;
        while (start < tasksJson.length()) {
            while (start < tasksJson.length() && (Character.isWhitespace(tasksJson.charAt(start))
                    || tasksJson.charAt(start) == ',')) {
                start++;
            }

            if (start >= tasksJson.length()) {
                break;
            }

            if (tasksJson.charAt(start) == '{') {
                int braceCount = 0;
                int taskStart = start;
                int taskEnd = start;

                for (int i = start; i < tasksJson.length(); i++) {
                    char c = tasksJson.charAt(i);
                    if (c == '{') {
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                        if (braceCount == 0) {
                            taskEnd = i + 1;
                            break;
                        }
                    }
                }

                if (taskEnd > taskStart) {
                    String taskJson = tasksJson.substring(taskStart, taskEnd);
                    try {
                        Map<String, String> taskData = parseTaskJson(taskJson);
                        if (taskData.containsKey("id")) {
                            Task task = createTask(taskData);
                            if (task != null) {
                                addTaskFunction.accept(projectId, task);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing task for project " + projectId + ": "
                                + e.getMessage());
                    }

                    start = taskEnd;
                } else {
                    break;
                }
            } else {
                start++;
            }
        }
    }

    private static Map<String, String> parseProjectJson(String projectJson) {
        Map<String, String> data = new HashMap<>();

        Pattern stringPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
        Matcher stringMatcher = stringPattern.matcher(projectJson);
        while (stringMatcher.find()) {
            String key = stringMatcher.group(1);
            String value = stringMatcher.group(2);
            data.put(key, unescapeJson(value));
        }

        Pattern numPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*([\\d.]+)");
        Matcher numMatcher = numPattern.matcher(projectJson);
        while (numMatcher.find()) {
            String key = numMatcher.group(1);
            String value = numMatcher.group(2);
            data.put(key, value);
        }

        return data;
    }

    private static Map<String, String> parseTaskJson(String taskJson) {
        Map<String, String> data = new HashMap<>();

        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(taskJson);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            data.put(key, unescapeJson(value));
        }

        return data;
    }

    private static Project createProject(Map<String, String> projectData) {
        try {
            String type = projectData.get("type");
            String name = projectData.get("name");
            String description = projectData.get("description");
            double budget = Double.parseDouble(projectData.get("budget"));
            int teamSize = Integer.parseInt(projectData.get("teamSize"));

            Project project;
            if ("Software".equalsIgnoreCase(type)) {
                project = new SoftwareProject(name, description, budget, teamSize);
            } else {
                project = new HardwareProject(name, description, budget, teamSize);
            }

            String projectId = projectData.containsKey("projectId") ? projectData.get("projectId")
                    : projectData.get("id");
            project.setId(projectId);
            return project;
        } catch (NumberFormatException e) {
            System.err.println("Error parsing project numbers: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            System.err.println("Error with project data: " + e.getMessage());
            return null;
        }
    }

    private static Task createTask(Map<String, String> taskData) {
        try {
            String name = taskData.get("name");
            String statusStr = taskData.get("status");
            Status status = Status.valueOf(statusStr);

            Task task = new Task(name, status);
            task.setId(taskData.get("id"));
            String assignedUserId = taskData.get("assignedUserId");
            if (assignedUserId != null && !assignedUserId.isEmpty()) {
                task.setAssignedUserId(assignedUserId);
            }

            return task;
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing task data: " + e.getMessage());
            return null;
        } catch (NullPointerException e) {
            System.err.println("Missing task data: " + e.getMessage());
            return null;
        }
    }

    private static String unescapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n")
                .replace("\\r", "\r").replace("\\t", "\t");
    }

    public static boolean dataFileExists() {
        return Files.exists(Paths.get(DATA_FILE));
    }

    public static boolean deleteDataFile() throws FileNotAvailableException {
        try {
            Path filePath = Paths.get(DATA_FILE);

            if (Files.exists(filePath)) {
                Path parent = filePath.getParent();
                if (parent != null && !Files.isWritable(parent)) {
                    throw new FileNotAvailableException(
                            "Cannot delete file: no write permission in directory");
                }
            }

            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileNotAvailableException("Error deleting data file: " + e.getMessage());
        }
    }
}
