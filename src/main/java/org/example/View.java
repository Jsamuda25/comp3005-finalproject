package org.example;

import java.util.*;

public class View {

    private final static Scanner scanner = InputScanner.getInstance();
    private static final List<String> LOGIN_MENU_OPTIONS = Arrays.asList("Login", "Register", "Exit");
    private static final List<String> MEMBER_MENU_OPTIONS = Arrays.asList("User Registration", "Profile Management", "Dashboard Display", "Schedule Management", "Logout");
    private static final List<String> TRAINER_MENU_OPTIONS = Arrays.asList("Schedule Management", "Member Profile Viewing", "Logout");
    private static final List<String> ADMIN_MENU_OPTIONS = Arrays.asList("Room Booking Management", "Equipment Maintenance Monitoring", "Class Schedule Updating", "Billing and Payment Processing", "Logout");

    public void welcome() {
        System.out.println("Welcome to the Health and Fitness System!");
    }

    public void logoutMessage() { System.out.println("You have been logged out."); }

    /**
     * Displays the login menu and returns the user's choice
     * @return the user's choice
     */
    public int welcomeMenu() {
        menu(LOGIN_MENU_OPTIONS);
        System.out.print("Enter your choice: ");
        Optional<Integer> choice = getIntegerInput();

        while (choice.isEmpty() || choice.get() < 1 || choice.get() > LOGIN_MENU_OPTIONS.size()) {
            System.out.println("Invalid choice. Please enter a number between 1 and " + LOGIN_MENU_OPTIONS.size() + ".");
            choice = getIntegerInput();
        }

        scanner.nextLine();
        return choice.get();
    }

    /**
     * Prompts the user to enter their login information
     * @return a list containing the user's login information
     */
    public List<String> loginInput() {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        List<String> loginInfo = new ArrayList<>();
        loginInfo.add(username);
        loginInfo.add(password);
        return loginInfo;
    }

    /**
     * Prompts the user to enter their registration information
     * @return a list containing the user's registration information
     */
    public List<String> registerInput() {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();
        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine();

        List<String> registerInfo = new ArrayList<>();
        registerInfo.add(firstName);
        registerInfo.add(lastName);
        registerInfo.add(username);
        registerInfo.add(password);
        return registerInfo;
    }

    /**
     * Diplays the member menu and returns the user's choice
     * @return the user's choice
     */
    public int memberMenu() {
        menu(MEMBER_MENU_OPTIONS);
        return getChoice(MEMBER_MENU_OPTIONS);
    }

    /**
     * Displays the trainer menu and returns the user's choice
     * @return the user's choice
     */
    public int trainerMenu() {
        menu(TRAINER_MENU_OPTIONS);
        return getChoice(TRAINER_MENU_OPTIONS);
    }

    /**
     * Displays the admin menu and returns the user's choice
     * @return the user's choice
     */
    public int adminMenu() {
        menu(ADMIN_MENU_OPTIONS);
        return getChoice(ADMIN_MENU_OPTIONS);
    }

    /**
     * Displays the room booking menu and returns the user's choice
     * @return the user's choice
     */
    public int roomBookingMenu() {
        ArrayList<String> options = new ArrayList<>(Arrays.asList("View Room Bookings", "Cancel Room Booking", "Back"));
        menu(options);
        return getChoice(options);
    }

    /**
     * Helper method that prompts the user to enter a choice
     * @param options the list of options to choose from
     * @return the user's choice
     */
    private int getChoice(List<String> options) {
        System.out.print("Enter your choice: ");
        Optional<Integer> choice = getIntegerInput();

        while (choice.isEmpty() || choice.get() < 1 || choice.get() > options.size()) {
            System.out.println("Invalid choice. Please enter a number between 1 and " + options.size() + ".");
            choice = getIntegerInput();
        }

        scanner.nextLine();
        return choice.get();
    }

    /**
     * Displays a menu with the given options
     * @param options the list of options to display
     */
    private void menu(List<String> options) {
        System.out.println("Select an option:");
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
    }

    /**
     * Helper method that gets an integer input from the user
     * @return an optional containing the integer input if it is valid
     */
    public static Optional<Integer> getIntegerInput() {
        try {
            return Optional.of(scanner.nextInt());
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return Optional.empty();
        }
    }

    public void close() {
        scanner.close();
    }

    public int billingAndPaymentMenu() {
        ArrayList<String> options = new ArrayList<>(Arrays.asList("View Billing and Payment", "Process Payment", "Back"));
        menu(options);
        return getChoice(options);
    }
}
