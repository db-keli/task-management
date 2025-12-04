package org.example.utils;

import java.util.Scanner;
import org.example.utils.ValidationUtils;

public class ConsoleMenu {
   private int input;
   private boolean exit = false;


   private void displayMainMenu() {
       String main_menu = """
               Main Menu:
               ___________
               1. Main Projects
               2. Manage Tasks
               3. View Status Reports
               4. Switch User
               5. Exit
               """;
       System.out.println(main_menu);
   }

   public ConsoleMenu() {

//       String projectCatalog = String.format(
//               """
//               PROJECT CATALOG
//
//               Filter Options:
//               1. View All Projects(%s)
//               2. Software Projects Only
//               3. Hardware Projects Only
//               4. Search by Budget Range
//
//               Enter filter choice:
//
//               %s
//               """
//       ); //Enter ID to view details
//
//       String projectDetails = String.format(
//               """
//               PROJECT DETAILS: %s
//
//               Project Name: %s
//               Type: %s
//               Team Size: %d
//               Budget: $%d
//
//               Associated Tasks:
//
//               %s
//
//               Completion Rate: %d%%
//               """
//       );
//       String projectTable = String.format("| %s   | %s   | %s   | %s   | %s   |");
//       String tasksTable = String.format("| %s   | %s   | %s   |");
//       String reportTable = String.format("| %s   | %s   | %s   | %s   | %s   |");
//
//       String taskMenuOptions = String.format(
//               """
//               Options:
//               1. Add New Task
//               2. Update Task Status
//               3. Remove Task
//               4. Back to Main Menu
//
//               Enter your choice:
//               """
//       );

       displayMainMenu();
       while (!exit) {
           int choice = ValidationUtils.readInt("Enter your choice: ", 1, 5);
           processMainMenuChoice(choice);
       }
       System.out.println(input);
       ValidationUtils.close();
   }

   private void processMainMenuChoice(int input) {
       switch (input) {
       case 1 -> {
           System.out.println("→ Opening Main Projects...");
           // TODO: open projects menu
       }
       case 2 -> {
           System.out.println("→ Opening Task Management...");
           // TODO: open tasks menu
       }
       case 3 -> {
           System.out.println("→ Generating Status Reports...");
           // TODO: show reports
       }
       case 4 -> {
           System.out.println("→ Switching User...");
           // TODO: logout/login
       }
       case 5 -> {
           System.out.println("Exiting program...");
           exit = true;
       }
       default -> {
           System.out.println("Invalid choice! Please select 1–5.");
       }
       }

       if (!exit) {
           System.out.println();
           displayMainMenu();
       }
   }

    private void setExit() {
        if (input == 5){
            exit = true;
        }
    }


    public int getInput() {
       return input;
   }

   public boolean isExit() {
       return exit;
   }
}
