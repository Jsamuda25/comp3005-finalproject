package org.example;

import java.util.Scanner;

/**
 * This class is used to get the Scanner object for user input
 */
public class InputScanner {
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Gets the Scanner object
     *
     * @return the Scanner object
     */
    public static Scanner getInstance() {
        return scanner;
    }
}
