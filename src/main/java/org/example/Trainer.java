package org.example;
import java.sql.*;
import java.util.Scanner;

import org.example.model.User;

public class Trainer {
    public static Connection connection = PostgresConnection.connect();
    private static PreparedStatement preparedStatement; 

    public static void scheduleManagement(User user){
        Scanner scanner = InputScanner.getInstance();
        System.out.println("Enter start time (format: YYYY-MM-DD HH:MM:SS):");
        String startTime = scanner.nextLine().trim();

        System.out.println("Enter end time (format: YYYY-MM-DD HH:MM:SS):");
        String endTime = scanner.nextLine().trim();
        String query = "INSERT INTO TrainerAvailability (trainer_id, start_time, end_time) VALUES (?, ?, ?)";
        try {
            // Prepare SQL statement
            preparedStatement = connection.prepareStatement(query);
            
            // Assuming trainerId is retrieved or stored elsewhere
            int trainerId = user.getUserID(); // Example trainer ID
            
            // Set parameters
            preparedStatement.setInt(1, trainerId);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(startTime));
            preparedStatement.setTimestamp(3, Timestamp.valueOf(endTime));

            // Execute the statement
            preparedStatement.executeUpdate();
            
            System.out.println("Trainer availability set successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // Close resources
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

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

                fetchAndDisplayFitnessGoals(memberId);
                fetchAndDisplayHealthMetrics(memberId);
                fetchAndDisplayTrainingSessions(memberId);
                fetchAndDisplayClasses(memberId);
            } else {
                System.out.println("No member found with the username: " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


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
                int status = resultSet.getInt("status");

                System.out.println("Goal ID: " + goalId);
                System.out.println("Title: " + title);
                System.out.println("Value: " + value);
                System.out.println("End Date: " + endDate);
                System.out.println("Status: " + status);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    private static void fetchAndDisplayClasses(int memberId) {
        try {
            String query = "SELECT C.class_name FROM ClassMembers CM " +
                    "JOIN Class C ON CM.class_id = C.class_id " +
                    "WHERE CM.member_id = ?";
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
}
