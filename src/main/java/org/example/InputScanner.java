package org.example;

import java.util.Scanner;

public class InputScanner {
    private static final Scanner scanner = new Scanner(System.in);

    public static Scanner getInstance() {
        return scanner;
    }
}
