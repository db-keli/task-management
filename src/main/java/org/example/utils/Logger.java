package org.example.utils;


public final class Logger {

    static class Colors {
        public static final String RESET = "\u001B[0m";

        public static final String BLACK  = "\u001B[30m";
        public static final String RED    = "\u001B[31m";
        public static final String GREEN  = "\u001B[32m";
        public static final String YELLOW = "\u001B[33m";

        public static final String BOLD_RED = "\u001B[1;31m";
        public static final String BOLD_GREEN = "\u001B[1;32m";
        public static final String BOLD_YELLOW = "\u001B[1;33m";
    }

    public static void printNormalLogMessage (String logMessage) {
        System.out.println(Colors.BLACK + logMessage + Colors.RESET);
    }

    public static void printErrorLogMessage (String logMessage) {
        System.out.println(Colors.RED + logMessage + Colors.RESET);
    }

    public static  void printSuccessLogMessage (String logMessage) {
        System.out.println(Colors.GREEN + logMessage + Colors.RESET);
    }

    public static void printCriticalSuccessLogMessage (String logMessage) {
        System.out.println(Colors.BOLD_GREEN + logMessage + Colors.RESET);
    }

    public static void printCriticalErrorLogMessage (String logMessage) {
        System.out.println(Colors.BOLD_RED + logMessage + Colors.RESET);
    }

    public static void printWarningLogMessage (String logMessage) {
        System.out.println(Colors.YELLOW + logMessage + Colors.RESET);
    }

    public static void printCriticalWarningLogMessage (String logMessage) {
        System.out.println(Colors.BOLD_YELLOW + logMessage + Colors.RESET);
    }
}


