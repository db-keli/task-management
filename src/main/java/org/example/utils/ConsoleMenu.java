package org.example.utils;

import java.util.Scanner;

public class ConsoleMenu {
   private int input;
   private boolean exit = false;

   private static final Scanner sc = new Scanner(System.in);
   public ConsoleMenu() {
       String main_menu = String.format(
               """
               Main Menu:
               ___________
               1. Main Projects
               2. Manage Tasks
               3. View Status Reports
               4. Switch User
               5. Exit
               """
       );

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

       System.out.println(main_menu);

       while (!exit) {
           input = readInt();
           setExit();
           System.out.println(input);
//           sc.close();

           switch (input) {
               case 1:
                   System.out.println("Manage projects");
               case 2:
                   System.out.println("Manage tasks");
               case 3:
                   System.out.println("Manage Reports");
               case 4:
                   System.out.println("Manage user");
               default:
           }
       }
   }

   private int readInt() {
       System.out.print("Enter your choice: ");
       while (!sc.hasNextInt()) {
           System.out.println("Invalid input. try again: ");
           sc.next();
       }
       return sc.nextInt();
   }

    public static String nextLine() {
        return sc.nextLine();
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
