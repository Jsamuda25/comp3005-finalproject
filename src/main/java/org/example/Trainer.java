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
}
