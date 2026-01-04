package org.example.utils;

import org.example.exceptions.ValidationException;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Predicate;

public final class ValidationUtils {
    private static final Scanner sc = new Scanner(System.in);

    private ValidationUtils() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static int readInt(String prompt, int min, int max) {
        return readInt(prompt, min, max, "Please enter a number between " + min + " and " + max + ": ");
    }

    public static int readInt(String prompt, int min, int max, String errorMessage) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                Logger.printCriticalErrorLogMessage("Input cannot be empty!");
//                throw new ValidationException(errorMessage);
                continue;
            }

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    Logger.printSuccessLogMessage("Valid input: " + value);
                    return value;
                } else {
                    Logger.printWarningLogMessage(errorMessage);
                }
            } catch (NumberFormatException | NoSuchElementException e) {
                Logger.printCriticalErrorLogMessage("Invalid number format: '" + input + "'");
//                throw new InputMismatchException("Invalid number format: '" + input + "'");
            }
        }
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                Logger.printCriticalErrorLogMessage("Cannot be empty!");
                continue;
            }

            try {
                int value = Integer.parseInt(input);
                Logger.printSuccessLogMessage("Accepted: " + value);
                return value;
            } catch (NumberFormatException e) {
                Logger.printErrorLogMessage("Not a valid integer: '" + input + "'");
            }
        }
    }

    public static int readPositiveInt(String prompt) {
        return readInt(prompt, i ->i>0, "Enter a positive number");
    }

    public static int readPositiveInt(String prompt, int min, int max) {
        return readInt(prompt, min, max, "Please enter a number between " + min + " and " + max + ": ");
    }

    public static int readInt(String prompt, Predicate<Integer> validator, String errorMessage) {
        while (true) {
            int value = readInt(prompt + " ");
            if (validator.test(value)) {
                return value;
            }
           Logger.printWarningLogMessage(errorMessage);
        }
    }

    public static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if(!line.isEmpty()) {
                return line;
            }
            Logger.printWarningLogMessage("Please enter a non-empty string: ");
        }
    }

    public static String readStringMatching(String prompt, String regex, String errorMessage) {
        while (true) {
            String input = readNonEmptyString(prompt);
            if (input.matches(regex)) {
                return input;
            }
            Logger.printErrorLogMessage(errorMessage);
        }
    }

    public static String readEmail(String prompt) {
        return readStringMatching(prompt,
                "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",
                "Invalid email format. Example: user@example.com");
    }

    public static double readDouble(String prompt, double min, double max) {
        return readDouble(prompt, min, max, "Please enter a number between " + min + " and " + max + ": ");
    }

    public static double readDouble(String prompt, double min, double max, String errorMessage) {
        while (true) {
            System.out.print(prompt);
            if (sc.hasNextDouble()) {
                double value = sc.nextDouble();
                sc.nextLine();
                if (value >= min && value <= max) {
                    return value;
                }
            } else {
                sc.nextLine();
            }
            System.out.println(errorMessage);
        }
    }

    public static String readYesNo(String prompt) {
        while (true) {
            String input = readNonEmptyString(prompt + " (y/n): ").toLowerCase();
            if (input.equals("y") || input.equals("yes")) return "y";
            if (input.equals("n") || input.equals("no")) return "n";
            System.out.println("Please type 'y' or 'n':");
        }
    }

    public static void close() {
        if (sc != null) {
            sc.close();
        }
    }
}
