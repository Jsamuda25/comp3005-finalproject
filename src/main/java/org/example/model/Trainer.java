package org.example.model;

import org.example.InputScanner;
import org.example.PostgresConnection;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * The Trainer class provides functionality for a trainer of a system to manage their schedule and view member profiles.
 */
public class Trainer {
    private static final Connection connection = PostgresConnection.connect();
    private static PreparedStatement preparedStatement;

    /**
     * Allows the trainer to manage their schedule.
     * @param user The User object representing the trainer.
     */
    public static void scheduleManagement(User user) {
        Scanner scanner = InputScanner.getInstance();
        Timestamp startTime;
        Timestamp endTime;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);

        // Get the start and end time for the availability slot
        System.out.println("Enter start time (format: YYYY-MM-DD HH:MM:SS):");
        String startInput = scanner.nextLine().trim();
        System.out.println("Enter end time (format: YYYY-MM-DD HH:MM:SS):");
        String endInput = scanner.nextLine().trim();

        try {
            startTime = new Timestamp(dateFormat.parse(startInput).getTime());
            endTime = new Timestamp(dateFormat.parse(endInput).getTime());
        } catch (ParseException e) {
            System.out.println("Invalid timestamp format.");
            return;
        }

        // Check if the start time is before the end time
        if (startTime.after(endTime)) {
            System.out.println("Start time cannot be after end time.");
            return;
        }

        // Insert the availability slot into the database
        String query = "INSERT INTO TrainerAvailability (trainer_id, start_time, end_time) VALUES (?, ?, ?)";
        try {
            preparedStatement = connection.prepareStatement(query);

            int trainerId = user.getUserID();

            preparedStatement.setInt(1, trainerId);
            preparedStatement.setTimestamp(2, startTime);
            preparedStatement.setTimestamp(3, endTime);

            preparedStatement.executeUpdate();

            System.out.println("Trainer availability set successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Allows the trainer to view a member's profile.
     */
    public static void viewMemberProfile() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the member's username: ");
        String username = scanner.nextLine().trim();

        try {
            String query = "SELECT * FROM Users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int memberId = resultSet.getInt("id");
                String memberName = resultSet.getString("name");
                String typeOfUser = resultSet.getString("typeOfUser");

                System.out.println("Member ID: " + memberId);
                System.out.println("Username: " + username);
                System.out.println("Name: " + memberName);
                System.out.println("User Type: " + typeOfUser);
                System.out.println();

                fetchAndDisplayFitnessGoals(memberId);
                System.out.println();
                fetchAndDisplayHealthMetrics(memberId);
                System.out.println();
                fetchAndDisplayTrainingSessions(memberId);
                System.out.println();
                fetchAndDisplayClasses(memberId);
                System.out.println();
            } else {
                System.out.println("No member found with the username: " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches and displays the fitness goals of a member.
     * @param memberId The ID of the member.
     */
    private static void fetchAndDisplayFitnessGoals(int memberId) {
        try {
            String query = "SELECT * FROM FitnessGoal WHERE userId = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, memberId);
            ResultSet resultSet = statement.executeQuery();

            System.out.println("Fitness Goals:");
            while (resultSet.next()) {
                int goalId = resultSet.getInt("goalId");
                String title = resultSet.getString("title");
                String value = resultSet.getString("value");
                Date endDate = resultSet.getDate("endDate");
                boolean completed = resultSet.getBoolean("completed");

                System.out.println("Goal ID: " + goalId);
                System.out.println("Title: " + title);
                System.out.println("Value: " + value);
                System.out.println("End Date: " + endDate);
                System.out.println("Completed: " + completed);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches and displays the health metrics of a member.
     * @param memberId The ID of the member.
     */
    private static void fetchAndDisplayHealthMetrics(int memberId) {
        try {
            String query = "SELECT * FROM HealthMetrics WHERE Member_ID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, memberId);
            ResultSet resultSet = statement.executeQuery();

            System.out.println("Health Metrics:");
            while (resultSet.next()) {
                int metricId = resultSet.getInt("Metric_ID");
                String metricType = resultSet.getString("Metric_Type");
                float value = resultSet.getFloat("Value");
                Date dateRecorded = resultSet.getDate("Date_Recorded");
                String unit = resultSet.getString("Unit");
                String notes = resultSet.getString("Notes");

                System.out.println("Metric ID: " + metricId);
                System.out.println("Type: " + metricType);
                System.out.println("Value: " + value);
                System.out.println("Date Recorded: " + dateRecorded);
                System.out.println("Unit: " + unit);
                System.out.println("Notes: " + notes);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches and displays the training sessions of a member.
     * @param memberId The ID of the member.
     */
    private static void fetchAndDisplayTrainingSessions(int memberId) {
        try {
            String query = "SELECT * FROM TrainingSessions WHERE member_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, memberId);
            ResultSet resultSet = statement.executeQuery();

            System.out.println("Training Sessions:");
            while (resultSet.next()) {
                int sessionId = resultSet.getInt("session_id");
                int trainerId = resultSet.getInt("trainer_id");
                Timestamp startDate = resultSet.getTimestamp("start_date");
                Timestamp endDate = resultSet.getTimestamp("end_date");

                System.out.println("Session ID: " + sessionId);
                System.out.println("Trainer ID: " + trainerId);
                System.out.println("Start Date: " + startDate);
                System.out.println("End Date: " + endDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches and displays the classes of a member.
     * @param memberId The ID of the member.
     */
    private static void fetchAndDisplayClasses(int memberId) {
        try {
            String query = "SELECT C.class_name FROM ClassMembers CM " + "JOIN Class C ON CM.class_id = C.class_id " + "WHERE CM.member_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, memberId);
            ResultSet resultSet = statement.executeQuery();

            System.out.println("Classes:");
            while (resultSet.next()) {
                String className = resultSet.getString("class_name");
                System.out.println("Class Name: " + className);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Allows the trainer to delete an availability slot.
     * @param trainer_id The ID of the trainer.
     * @param start_date The start date of the availability slot.
     * @param end_date The end date of the availability slot.
     */
    public void deleteAvailabilitySlot(int trainer_id, Timestamp start_date, Timestamp end_date) {
        try {
            String delQuery = "DELETE FROM TrainerAvailability WHERE trainer_id = ? AND ? <= end_time AND start_time <= ?";
            PreparedStatement statement = connection.prepareStatement(delQuery);
            statement.setInt(1, trainer_id);
            statement.setTimestamp(2, start_date);
            statement.setTimestamp(3, end_date);
            statement.executeUpdate();
            System.out.println("Trainer's schedule has been updated accordingly.\n");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


}

