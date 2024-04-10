package org.example.model;

import org.example.InputScanner;
import org.example.PostgresConnection;
import org.example.View;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;

/**
 * The Member class extends the User class and provides functionality for a member of a system to manage their profile, health metrics, and fitness goals.
 */
public class Member extends User {

    public static Connection connection = null;

    /**
     * Constructor for the Member class.
     *
     * @param user The User object to initialize the Member object.
     */
    public Member(User user) {
        super(user);
    }

    /**
     * Provides an interface for the member to manage their profile.
     */
    public void profileManagement() {
        Scanner scanner = InputScanner.getInstance();
        System.out.println("Welcome to profile management, what information would you like to modify?");
        System.out.println("1. Profile information");
        System.out.println("2. Health metrics");
        System.out.println("3. Fitness goals");
        System.out.println("4. Exit");
        System.out.print("Enter your choice as a number: ");
        int response = scanner.nextInt();

        if (response == 1) {
            updatePersonalInfo();
        } else if (response == 2) {
            selectHealthOption();
        } else if (response == 3) {
            selectFitnessFunction();
        } else {
            System.out.println("Invalid selection");
        }
    }

    /**
     * Provides an interface for the member to update their personal information.
     */
    public void updatePersonalInfo() {
        Scanner scanner = InputScanner.getInstance();
        System.out.println("You can change the following details:");
        System.out.println("1. Username");
        System.out.println("2. Password");
        System.out.println("3. Name");
        System.out.println("4. Exit");
        System.out.print("Enter your choice as a number: ");

        int response = scanner.nextInt();


        if (response == 1) {
            modifyUsername();
        } else if (response == 2) {
            modifyPassword();
        } else if (response == 3) {
            modifyName();
        } else if (response > 4) {
            System.out.println("Invalid selection");
        }

    }

    /**
     * Allows the member to modify their username.
     */
    public void modifyUsername() {

        Scanner scanner = InputScanner.getInstance();
        System.out.print("Input new username: ");
        scanner.nextLine();
        String newUser = scanner.nextLine();
        connection = PostgresConnection.connect();

        try {
            String existQuery = "SELECT username from users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(existQuery);
            statement.setString(1, newUser);
            ResultSet res = statement.executeQuery();

            if (res.next() && res.getString("username").equals(newUser)) {
                System.out.println("This username is taken, please choose a different one.");
            } else {

                String query = "UPDATE users SET username = ? WHERE id = ?";
                statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, newUser);
                statement.setInt(2, getUserID());

                int result = statement.executeUpdate();
                if (result > 0) {
                    try (ResultSet gKeys = statement.getGeneratedKeys()) {
                        if (gKeys.next()) {
                            String username = gKeys.getString(2);
                            System.out.println("Your new username is: " + username + "\n");
                            setUserName(username);
                        }

                    } catch (Exception e) {
                        System.out.println("Error: " + e);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

    }

    /**
     * Allows the member to modify their password.
     */
    public void modifyPassword() {
        Scanner scanner = InputScanner.getInstance();
        System.out.print("Input new password: ");
        scanner.nextLine();
        String newPass = scanner.nextLine();
        connection = PostgresConnection.connect();

        try {
            String query = "UPDATE users SET password = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, newPass);
            statement.setInt(2, getUserID());

            int result = statement.executeUpdate();
            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next() && gKeys.getString(3).equals(newPass)) {
                        System.out.println("Your password has been updated\n");
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Allows the member to modify their name.
     */
    public void modifyName() {
        Scanner scanner = InputScanner.getInstance();
        scanner.nextLine();
        System.out.print("Enter first name: ");
        String first_name = scanner.nextLine().trim();
        System.out.print("Enter last name: ");
        String last_name = scanner.nextLine().trim();

        String new_name = first_name + " " + last_name;
        connection = PostgresConnection.connect();

        try {
            String query = "UPDATE users SET name = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, new_name);
            statement.setInt(2, getUserID());

            int result = statement.executeUpdate();
            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next() && gKeys.getString(4).equals(new_name)) {
                        System.out.println("Your name has been updated to: " + new_name + "\n");
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /**
     * Provides an interface for the member to manage their health metrics.
     */
    public void selectHealthOption() {
        System.out.println("Health Metrics Menu");
        System.out.println("1. View Health Metrics");
        System.out.println("2. Edit Health Metrics");
        System.out.println("3. Add Health Metrics");
        System.out.println("4. Exit");
        System.out.print("Enter choice as integer: ");
        Scanner scanner = InputScanner.getInstance();

        int response = scanner.nextInt();

        if (response == 1) {
            viewHealthMetrics();
        } else if (response == 2) {
            editHealthMetrics();
        } else if (response == 3) {
            addHealthMetrics();
        } else if (response == 4) {
            profileManagement();
        }

    }

    /**
     * Displays the health metrics of the member.
     */
    public void viewHealthMetrics() {
        System.out.println("Your Health Metrics");
        connection = PostgresConnection.connect();
        String query = "SELECT metric_id, metric_type, value, unit, notes, date_recorded FROM healthmetrics WHERE member_id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            ResultSet res = statement.executeQuery();

            while (res.next()) {
                System.out.println("---------");
                System.out.println("ID: " + res.getInt("metric_id"));
                System.out.println("Metric Type: " + res.getString("metric_type"));
                System.out.println("Value: " + res.getDouble("value") + res.getString("unit"));
                System.out.println("Note: " + res.getString("notes"));
                System.out.println("Date recorded: " + res.getDate("date_recorded") + "\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /**
     * Allows the member to edit their health metrics.
     */
    public void editHealthMetrics() {
        viewHealthMetrics();
        Scanner scanner = InputScanner.getInstance();
        System.out.print("Provide the ID of the metric you would like to edit, otherwise input 0: ");
        int metric_id = scanner.nextInt();
        scanner.nextLine();

        if (metric_id == 0) {
            return;
        }

        System.out.println("Which field would you like to change?");
        System.out.println("1. Metric Type");
        System.out.println("2. Value");
        System.out.println("3. Unit");
        System.out.println("4. Note");
        System.out.println("5. Exit");
        System.out.print("Enter choice as integer: ");

        int response = scanner.nextInt();
        scanner.nextLine();
        if (response == 1) {
            System.out.print("Enter new metric type: ");
            String metric = scanner.nextLine();
            updateMetricType(metric_id, metric);
        } else if (response == 2) {
            System.out.print("Enter new metric value: ");
            Double val = scanner.nextDouble();
            updateMetricValue(metric_id, val);
            scanner.nextLine();

        } else if (response == 3) {
            System.out.print("Enter new metric unit: ");
            String unit = scanner.nextLine();
            updateMetricUnit(metric_id, unit);

        } else if (response == 4) {
            System.out.print("Enter new metric note: ");
            String note = scanner.nextLine();
            updateMetricNote(metric_id, note);
        }

    }

    /**
     * Allows the member to add new health metrics.
     */
    public void addHealthMetrics() {
        Scanner scanner = InputScanner.getInstance();
        scanner.nextLine();
        System.out.print("Enter Metric Type: ");
        String metric = scanner.nextLine();
        System.out.print("Enter value: ");
        double value = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter unit of measurement: ");
        String unit = scanner.nextLine();
        System.out.print("Enter note: ");
        String note = scanner.nextLine();

        LocalDate date = java.time.LocalDate.now();

        connection = PostgresConnection.connect();

        try {
            String query = "INSERT INTO HealthMetrics(member_id, metric_type, value, date_recorded, unit, notes) VALUES (?, ? , ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            statement.setString(2, metric);
            statement.setDouble(3, value);
            statement.setDate(4, java.sql.Date.valueOf(date));
            statement.setString(5, unit);
            statement.setString(6, note);
            statement.executeUpdate();
            System.out.println("New metric has been added!\n");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Updates the type of specific health metric.
     *
     * @param id          The ID of the health metric to be updated.
     * @param metric_type The new type for the health metric.
     */
    public void updateMetricType(int id, String metric_type) {
        connection = PostgresConnection.connect();

        try {
            String query = "UPDATE healthmetrics SET metric_type = ? WHERE metric_id = ? AND member_id = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, metric_type);
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        System.out.println("This metric has been updated.");
                    }
                }
            } else {
                System.out.println("Metric was not updated.\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Updates the value of a specific health metric.
     *
     * @param id    The ID of the health metric to be updated.
     * @param value The new value for the health metric.
     */
    public void updateMetricValue(int id, Double value) {

        connection = PostgresConnection.connect();

        try {
            String query = "UPDATE healthmetrics SET value = ? WHERE metric_id = ? AND member_id = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setDouble(1, value);
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        System.out.println("This metric has been updated.");
                    }
                }
            } else {
                System.out.println("Metric was not updated.\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /**
     * Updates the unit of a specific health metric.
     *
     * @param id   The ID of the health metric to be updated.
     * @param unit The new unit for the health metric.
     */
    public void updateMetricUnit(int id, String unit) {
        connection = PostgresConnection.connect();

        try {
            String query = "UPDATE healthmetrics SET unit = ? WHERE metric_id = ? AND member_id = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, unit);
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        System.out.println("This metric has been updated.");
                    }
                }
            } else {
                System.out.println("Metric was not updated.\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /**
     * Updates the note of a specific health metric.
     *
     * @param id   The ID of the health metric to be updated.
     * @param note The new note for the health metric.
     */
    public void updateMetricNote(int id, String note) {
        connection = PostgresConnection.connect();

        try {
            String query = "UPDATE healthmetrics SET notes = ? WHERE metric_id = ? AND member_id = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, note);
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        System.out.println("This metric has been updated.");
                    }
                }
            } else {
                System.out.println("Metric was not updated.\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Provides an interface for the member to manage their fitness goals.
     */
    public void selectFitnessFunction() {
        System.out.println("Fitness Goals Menu");
        System.out.println("1. View Fitness Goals");
        System.out.println("2. Edit Fitness Goal");
        System.out.println("3. Add Fitness Goal");
        System.out.println("4. Exit");
        System.out.print("Enter choice as integer: ");
        Scanner scanner = InputScanner.getInstance();
        int response = scanner.nextInt();
        scanner.nextLine();

        if (response == 1) {
            viewFitnessGoals();
        } else if (response == 2) {
            updateFitnessGoals();
        } else if (response == 3) {
            addFitnessGoals();
        } else if (response == 4) {
            profileManagement();
        }

    }

    /**
     * Allows the member to add new fitness goals.
     */
    public void addFitnessGoals() {
        Scanner scanner = InputScanner.getInstance();
        scanner.nextLine();
        System.out.print("Enter title: ");
        String title = scanner.nextLine();
        System.out.print("Enter description: ");
        String description = scanner.nextLine();
        System.out.print("Enter end date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        connection = PostgresConnection.connect();

        try {
            String query = "INSERT INTO FitnessGoal(userId, title, value, enddate, completed) VALUES (?, ? , ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            statement.setString(2, title);
            statement.setString(3, description);
            statement.setDate(4, java.sql.Date.valueOf(date));
            statement.setBoolean(5, false);
            statement.executeUpdate();
            System.out.println("Your goal has been added!\n");
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /**
     * Allows the member to update their fitness goals.
     */
    public void updateFitnessGoals() {
        viewFitnessGoals();
        Scanner scanner = InputScanner.getInstance();
        System.out.print("Provide the ID of the goal you would like to edit, otherwise input 0: ");
        int goal_id = scanner.nextInt();
        scanner.nextLine();

        if (goal_id == 0) {
            return;
        }

        // Provide options to update the fitness goal
        System.out.println("What attributes would you like to change?");
        System.out.println("1. Title");
        System.out.println("2. Description");
        System.out.println("3. End date");
        System.out.println("4. Completion");
        System.out.println("5. Exit menu");
        System.out.print("Enter choice as integer: ");
        int response = scanner.nextInt();
        scanner.nextLine();

        if (response == 1) {  // Update title
            System.out.print("Enter new title: ");
            String title = scanner.nextLine();
            updateTitle(goal_id, title);
            updateFitnessGoals();
        } else if (response == 2) {  // Update description
            System.out.print("Enter new description: ");
            String des = scanner.nextLine();
            updateDescription(goal_id, des);
            updateFitnessGoals();
        } else if (response == 3) {  // Update end date
            System.out.print("Enter new end date (YYYY-MM-DD):  ");
            String date = scanner.nextLine();
            modifyDate(goal_id, date);
            updateFitnessGoals();
        } else if (response == 4) {  // Update completion status
            System.out.println("Enter new status (0 = Incomplete, 1 = Complete): ");
            int status = scanner.nextInt();
            boolean stat = false;

            if (status < 0 || status > 1) {
                System.out.println("Invalid value for status:");
                return;
            } else if (status == 1) {
                stat = true;
            }

            updateStatus(goal_id, stat);
            updateFitnessGoals();
        } else if (response == 5) {
            profileManagement();
        } else {
            System.out.println("Invalid selection");
        }
    }

    /**
     * Updates the title of a specific fitness goal.
     *
     * @param id    The ID of the fitness goal to be updated.
     * @param title The new title for the fitness goal.
     */
    public void updateTitle(int id, String title) {
        connection = PostgresConnection.connect();

        try {
            String query = "UPDATE fitnessgoal SET title = ? WHERE goalid = ? AND userid = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, title);
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        System.out.println("Your goal has been updated.");
                    }
                }
            } else {
                System.out.println("Goal was not updated.\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Updates the description of a specific fitness goal.
     *
     * @param id          The ID of the fitness goal to be updated.
     * @param description The new description for the fitness goal.
     */
    public void updateDescription(int id, String description) {
        connection = PostgresConnection.connect();

        try {
            String query = "UPDATE fitnessgoal SET value = ? WHERE goalid = ? AND userid = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, description);
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        System.out.println("Your goal has been updated.");
                    }
                }
            } else {
                System.out.println("Goal was not updated.\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /**
     * Modifies the end date of a specific fitness goal.
     *
     * @param id   The ID of the fitness goal to be updated.
     * @param date The new end date for the fitness goal.
     */
    public void modifyDate(int id, String date) {
        connection = PostgresConnection.connect();

        try {
            String query = "UPDATE fitnessgoal SET enddate = ? WHERE goalid = ? AND userid = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setDate(1, java.sql.Date.valueOf(date));
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        System.out.println("Your goal has been updated.");
                    }
                }
            } else {
                System.out.println("Goal was not updated.\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Updates the status of a specific fitness goal.
     *
     * @param id        The ID of the fitness goal to be updated.
     * @param completed The new status for the fitness goal.
     */
    public void updateStatus(int id, boolean completed) {

        try {
            String query = "UPDATE fitnessgoal SET completed = ? WHERE goalid = ? AND userid = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setBoolean(1, completed);
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next()) {
                        System.out.println("Your goal has been updated.");
                    }
                }
            } else {
                System.out.println("Goal was not updated.\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Displays the fitness goals of the member.
     */
    public void viewFitnessGoals() {
        connection = PostgresConnection.connect();
        System.out.println("Your fitness goals: ");
        try {
            String query = "SELECT goalid, title, value, enddate, completed FROM fitnessgoal WHERE userid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            ResultSet res = statement.executeQuery();
            while (res.next()) {
                System.out.println("---------");
                System.out.println("Goal id: " + res.getString("goalid"));
                System.out.println("Title: " + res.getString("title"));
                System.out.println("Description: " + res.getString("value"));
                System.out.println("End date: " + res.getDate("enddate"));
                System.out.println("Completed (T/F): " + res.getBoolean("completed") + "\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Manages the member's schedule.
     */
    public void scheduleManagement() {

        System.out.println("--- Schedule Management Menu ---");
        System.out.println("What would you like to do?");
        System.out.println("1. Join a class");
        System.out.println("2. Schedule session with trainer");
        System.out.println("3. Reschedule session with trainer");
        System.out.println("4. Cancel session with trainer");
        System.out.println("5. View scheduling history");
        System.out.println("6. Exit");
        System.out.print("Enter choice as integer: ");
        Scanner scanner = InputScanner.getInstance();

        int response = scanner.nextInt();
        scanner.nextLine();

        if (response == 1) {
            joinClass();
        } else if (response == 2) {
            scheduleSession();
        } else if (response == 3) {
            rescheduleSession();

        } else if (response == 4) {
            cancelSession();
        } else if (response == 5) {
            viewSchedulingHistory();
        } else if (response != 6) {
            System.out.println("Invalid selection");
            scheduleManagement();
        }
    }

    /**
     * Views the available classes for the member to join.
     */
    private boolean viewAvailableClasses() {
        connection = PostgresConnection.connect();

        try {
            String query = "SELECT * FROM class WHERE start_date >= ? ORDER BY start_date";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            preparedStatement.setTimestamp(1, currentTimestamp);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(!resultSet.next()){
                return false;
            }
            else {
                do{
                    System.out.println("Class ID: " + resultSet.getInt("class_id"));
                    System.out.println("Class Name: " + resultSet.getString("class_name"));
                    System.out.println("Trainer ID: " + resultSet.getInt("trainer_id"));
                    System.out.println("Start Date: " + resultSet.getDate("start_date"));
                    System.out.println("End Date: " + resultSet.getDate("end_date"));
                    System.out.println("room ID: " + resultSet.getInt("room_id"));
                    System.out.println("Room Number " + getRoomNumber(resultSet.getInt("room_id")));
                    System.out.println();
                }
                while(resultSet.next());
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving class data.");
        }
        return true;
    }

    /**
     * Allows the member to join a class.
     */
    public void joinClass() {
        connection = PostgresConnection.connect();
        System.out.println("--- Available Classes ---");

        if(!viewAvailableClasses()){
            System.out.println("There are currently no classes available.\n");
            return;
        }

        Scanner scanner = InputScanner.getInstance();
        System.out.print("Enter the ID of the class you would like to join, otherwise input 0: ");
        int class_id = scanner.nextInt();
        scanner.nextLine();

        if (class_id == 0) {  // Exit
            scheduleManagement();
            return;
        }

        // Check if member is already registered for the class
        try {
            String query = "SELECT * FROM classmembers WHERE class_id = ? AND member_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, class_id);
            statement.setInt(2, getUserID());
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                System.out.println("You are already registered for this class. \n");
                return;
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        // Pay the fee to confirm enrollment
        System.out.println("Pay the fee to confirm enrollment");
        System.out.print("Input 1 to confirm payment, otherwise you will be removed from the class: ");
        int resPay = scanner.nextInt();
        scanner.nextLine();
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

        if (resPay == 1) {
            // Insert payment record
            String query = "INSERT INTO billing(member_id, fee, type_of_fee, paid, date) VALUES(?,?,?,?,?)";
            try {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, getUserID());
                statement.setDouble(2, 50);
                statement.setInt(3, 1);
                statement.setBoolean(4, true);
                statement.setTimestamp(5, currentTimestamp);
                statement.executeUpdate();
                System.out.println("Your payment has been processed. \n");
            } catch (Exception e) {
                System.out.println(e);
                return;
            }

            // Insert member into class
            try {
                query = "INSERT INTO classmembers(class_id, member_id) VALUES(?,?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, class_id);
                statement.setInt(2, getUserID());
                statement.executeUpdate();
                System.out.println("You have been added to this class, we look forward to seeing you!\n");
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            System.out.println("Your registration has been canceled.");
        }

    }



    public boolean viewTrainingSessions(){
        System.out.println("-- Your scheduled Training Sessions ---");
        try {
            connection = PostgresConnection.connect();
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            String query = "SELECT session_id, trainer_id, start_date, end_date FROM trainingsessions WHERE member_id = ? AND cancelled = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            statement.setBoolean(2, false);
            ResultSet res = statement.executeQuery();
            if(!res.next()){
                System.out.println("You have no scheduled training sessions to modify.\n");
                return false;
            }
            else{
                do{
                    System.out.println("Training session (ID#" + res.getInt("session_id") + ") with trainer " + res.getInt("trainer_id") + " from " + res.getTimestamp("start_date") + " to " + res.getTimestamp("end_date"));
                }
                while(res.next());
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println(e);
        }
        return true;
    }
    /**
     * Displays the member's personal sessions and classes.
     */
    public void viewSchedulingHistory() {
        System.out.println("---  Scheduling History ---");

        System.out.println("Training Sessions");
        try {
            connection = PostgresConnection.connect();
            String query = "SELECT * FROM trainingsessions WHERE member_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            ResultSet res = statement.executeQuery();
            while (res.next()) {
                String cancelled = " ";
                if(res.getBoolean("cancelled")){
                    cancelled = " (cancelled)";
                }
                System.out.println("Training session (ID#" + res.getInt("session_id") + ") with trainer " + res.getInt("trainer_id") + " from " + res.getTimestamp("start_date") + " to " + res.getTimestamp("end_date") + cancelled);
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println("Classes");
        try {
            String query = "SELECT * FROM classmembers JOIN class ON classmembers.class_id = class.class_id WHERE member_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            ResultSet res = statement.executeQuery();
            while (res.next()) {
                System.out.println("Class (ID#" + res.getInt("class_id") + ") " + res.getString("class_name") + " from " + res.getTimestamp("start_date") + " to " + res.getTimestamp("end_date"));
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /**
     * Allows the member to reschedule a session.
     */
    public void rescheduleSession() {
        viewTrainingSessions();
        if(!viewTrainerAvailability()){
            return;
        }
        Scanner scanner = InputScanner.getInstance();
        connection = PostgresConnection.connect();

        System.out.print("Enter the ID of session you would like to reschedule: ");
        int session_id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter trainer ID: ");

        int trainer_id =  scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        System.out.print("Enter start time (HH:MM:SS): ");
        String start_time = scanner.nextLine();
        System.out.print("Enter end time (HH:MM:SS): ");
        String end_time = scanner.nextLine();
        Timestamp start_timestamp;
        Timestamp end_timestamp;

        try {
            start_timestamp = Timestamp.valueOf(date + " " + start_time);
            end_timestamp = Timestamp.valueOf(date + " " + end_time);
        }
        catch (Exception e){
            System.out.println("Invalid date or time format.");
            return;
        }

        // Check if the end time is after the start time
        int span = start_timestamp.compareTo(end_timestamp);
        if (span >= 0) {
            System.out.println("ERROR: End timestamp must be greater than start timestamp.");
            return;
        }
        System.out.println();

        // Check if the trainer is available
        try {
            if (!isTrainerAvailable(trainer_id, start_timestamp, end_timestamp)) {
                System.out.println("This trainer is not available during this date and time.");
                return;
            }
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        // Update the session with the new time
        try {
            String query = "UPDATE trainingsessions SET start_date = ?, end_date = ? WHERE session_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setTimestamp(1, start_timestamp);
            statement.setTimestamp(2, end_timestamp);
            statement.setInt(3, session_id);
            statement.executeUpdate();
            Trainer trainer = new Trainer();
            trainer.deleteAvailabilitySlot(trainer_id, start_timestamp, end_timestamp);
        } catch (Exception e) {
            System.out.println("Sorry, could not reschedule");
        }
    }

    /**
     * Allows the member to cancel a session.
     */
    public void cancelSession() {
        viewTrainingSessions();
        Scanner scanner = InputScanner.getInstance();
        connection = PostgresConnection.connect();
        System.out.print("Enter the ID of session you would like to cancel: ");
        int session_id = scanner.nextInt();
        scanner.nextLine();

        try {
            String query = "UPDATE trainingsessions SET cancelled = ? WHERE session_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setBoolean(1, true);
            statement.setInt(2, session_id);
            statement.executeUpdate();
            System.out.println("This session has been cancelled\n");
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
    }

    /**
     * Allows the member to schedule a session.
     */
    public void scheduleSession() {
        if (!viewTrainerAvailability()) {
            return;  // No available trainers
        }

        // Get the trainer ID, date, and time for the session
        System.out.println("--- Book session --- ");
        System.out.print("Enter Trainer ID: ");
        Scanner scanner = InputScanner.getInstance();

        int trainer_id =  scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        System.out.print("Enter start time (HH:MM:SS): ");
        String start_time = scanner.nextLine();
        System.out.print("Enter end time (HH:MM:SS): ");
        String end_time = scanner.nextLine();

        Timestamp start_timestamp;
        Timestamp end_timestamp;
        try {
            start_timestamp = Timestamp.valueOf(date + " " + start_time);
            end_timestamp = Timestamp.valueOf(date + " " + end_time);
        } catch (Exception e) {
            System.out.println("Invalid date or time format.");
            return;
        }

        int span = start_timestamp.compareTo(end_timestamp);
        if (span >= 0) {
            System.out.println("ERROR: End timestamp must be greater than start timestamp.");
            return;
        }

        System.out.println();
        try {
            if (!isTrainerAvailable(trainer_id, start_timestamp, end_timestamp)) {
                System.out.println("This trainer is not available during this date and time.");
                return;
            }
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        // Confirm payment to book session
        System.out.println("Please pay the fee to confirm session booking.");
        System.out.print("Input 1 to confirm payment, otherwise this session will be cancelled: ");
        int resPay = scanner.nextInt();
        scanner.nextLine();

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

        if (resPay == 1) {
            // Insert the session into the database
            String query = "INSERT INTO trainingsessions (member_id, trainer_id, start_date, end_date, cancelled) VALUES (?,?,?,?,?)";
            try {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, getUserID());
                statement.setInt(2, trainer_id);
                statement.setTimestamp(3, start_timestamp);
                statement.setTimestamp(4, end_timestamp);
                statement.setBoolean(5, false);
                statement.executeUpdate();
                System.out.println("We have scheduled your training session.\n");
            } catch (Exception e) {
                System.out.println(e);
            }

            // Insert payment record
            query = "INSERT INTO billing(member_id, fee, type_of_fee, paid, date) VALUES(?,?,?,?,?)";
            try {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, getUserID());
                statement.setDouble(2, 60);
                statement.setInt(3, 2);
                statement.setBoolean(4, true);
                statement.setTimestamp(5, currentTimestamp);
                statement.executeUpdate();
                System.out.println("Your payment has been processed! \n");
                Trainer trainer = new Trainer();
                trainer.deleteAvailabilitySlot(trainer_id, start_timestamp, end_timestamp);
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            System.out.println("Your registration has been canceled.");
        }

    }

    /**
     * Checks if a trainer is available.
     * @param trainerId The ID of the trainer.
     * @param startDate The start date of the session.
     * @param endDate The end date of the session.
     * @return true if the trainer is available, false otherwise.
     */
    private boolean isTrainerAvailable(int trainerId, Timestamp startDate, Timestamp endDate) throws SQLException {
        connection = PostgresConnection.connect();
        String query = "SELECT COUNT(*) AS count FROM TrainerAvailability " + "WHERE trainer_id = ? AND ? <= end_time AND start_time <= ?";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, trainerId);
        preparedStatement.setTimestamp(2, endDate);
        preparedStatement.setTimestamp(3, startDate);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt("count");
        return count > 0;
    }

    /**
     * Displays the availability of trainers.
     * @return true if there are available trainers, false otherwise.
     */
    private boolean viewTrainerAvailability() {
        connection = PostgresConnection.connect();
        System.out.println("See trainer availability below: ");
        try {
            String query = "SELECT * FROM TrainerAvailability WHERE start_time >= ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(1, currentTimestamp);
            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
                System.out.println("Sorry, there are currently no available time slots.\n");
                return false;
            }
            else{
                do {
                    System.out.println("Trainer ID: " + resultSet.getInt("trainer_id") + ", available " + resultSet.getTimestamp("start_time") + " to " + resultSet.getTimestamp("end_time"));
                }
                while(resultSet.next());
            }
            System.out.println("\n");
        } catch (Exception e) {
            System.out.println(e);
        }
        return true;
    }

    /**
     * Displays the exercise routines of the member.
     */
    public void viewExerciseRoutines() {
        System.out.println("Exercise Routines: ");

        try {
            connection = PostgresConnection.connect();
            String query = "SELECT id, name FROM exerciseroutines";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet res = statement.executeQuery();

            while (res.next()) {
                System.out.println("Exercise " + res.getInt("id") + ": " + res.getString("name"));
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.print("Enter the ID of the exercise routine you would like to view or -1 to go back: ");
        Optional<Integer> equipmentId = View.getIntegerInput();
        while (equipmentId.isEmpty()) {
            System.out.println("Invalid input. Please enter a number.");
            equipmentId = View.getIntegerInput();
        }
        if (equipmentId.get() == -1) return;

        try {
            String query = "SELECT name, instruction FROM exerciseroutines WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, equipmentId.get());
            ResultSet res = statement.executeQuery();

            if (res.next()) {
                System.out.println("Name: " + res.getString("name"));

                // Parse the instructions into a list of strings
                String[] instructions = res.getString("instruction").split(";");
                for (String instruction : instructions) {
                    System.out.print("    ");
                    System.out.println(instruction);
                }
            } else {
                System.out.println("No exercise routine found with that ID.");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Displays the fitness achievements of the member.
     */
    public void viewFitnessAchievements() {
        // Show the fitness goals marked as completed
        System.out.println("Fitness Achievements: ");
        try {
            connection = PostgresConnection.connect();
            String query = "SELECT title, value, enddate FROM fitnessgoal WHERE userid = ? AND completed = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            statement.setBoolean(2, true);
            ResultSet res = statement.executeQuery();

            while (res.next()) {
                System.out.println("Title: " + res.getString("title"));
                System.out.println("Description: " + res.getString("value"));
                System.out.println("End date: " + res.getDate("enddate") + "\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Displays the health statistics of the member.
     */
    public void viewHealthStatistics() {
        // Get the most recent metric for each routine and display it
        System.out.println("Health Statistics: ");
        System.out.println("---Showing the most recent health metrics---");
        try {
            connection = PostgresConnection.connect();
            String query = "SELECT h.metric_type, avg(h.value), h.unit, h.date_recorded FROM healthmetrics h " + "INNER JOIN " + "(SELECT metric_type, MAX(date_recorded) as date_recorded FROM healthmetrics " + "WHERE member_id = ? GROUP BY metric_type) m " + "ON h.metric_type = m.metric_type AND h.date_recorded = m.date_recorded " + "GROUP BY h.metric_type, h.unit, h.date_recorded";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            ResultSet res = statement.executeQuery();

            while (res.next()) {
                System.out.println("Metric Type: " + res.getString("metric_type"));
                System.out.println("Value: " + res.getDouble("avg") + " " + res.getString("unit"));
                System.out.println("Last Date Recorded: " + res.getDate("date_recorded") + "\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public  void viewUpcomingEvents(){
        System.out.println("--- Your Upcoming Training Sessions ---");
        try{
            connection = PostgresConnection.connect();
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            String query = "SELECT trainer_id, start_date, end_date FROM trainingsessions WHERE member_id = ? AND start_date >= ? AND cancelled = false";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            statement.setTimestamp(2, currentTimestamp);
            ResultSet res = statement.executeQuery();

            while (res.next()) {
                System.out.println("Session with Trainer " + res.getInt("trainer_id") + " from " + res.getTimestamp("start_date") + " to " + res.getTimestamp("end_date"));
            }
            System.out.println("\n");
        }
        catch (Exception e){
            System.out.println(e);
            return;
        }

        System.out.println("--- Your Upcoming Classes ---");

        try{
            connection = PostgresConnection.connect();
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            String query = "SELECT * FROM classmembers JOIN class ON classmembers.class_id = class.class_id WHERE member_id = ? AND class.start_date >= ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            statement.setTimestamp(2, currentTimestamp);
            ResultSet res = statement.executeQuery();

            while (res.next()) {
                System.out.println(res.getString("class_name") + " from " + res.getTimestamp("start_date") + " to " + res.getTimestamp("end_date") + "in room number " +  getRoomNumber(res.getInt("room_id")));
            }
            System.out.println("\n");


        }
        catch (Exception e){
            System.out.println(e);
            return;
        }

    }

    public void viewFavouriteTrainers(){
        System.out.println("Here are the trainers you have worked with the most:");
        String query = "SELECT trainer_id, count(trainingsessions.trainer_id) AS session_count FROM Users, Trainingsessions WHERE users.id = trainingsessions.member_id AND users.id=? GROUP BY trainingsessions.trainer_id ORDER BY count(trainingsessions.trainer_id) DESC";

        try{
            connection = PostgresConnection.connect();
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.next()){
                System.out.println("You haven't worked with any trainers yet.");
            }
            else {
                do{
                    System.out.println("Trainer " + resultSet.getInt("trainer_id") + ": "  + resultSet.getInt("session_count") + " sessions");
                }
                while(resultSet.next());
            }
            System.out.println("\n");
        }
        catch (Exception e){
            System.out.println(e);
            return;
        }
    }
}
