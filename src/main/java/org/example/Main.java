package org.example;

import java.sql.Connection;

public class Main {

    public static void main(String[] args) {
        Connection connection = PostgresConnection.connect();
        if (connection == null) {
            System.out.println("Unsuccessful connection to the PostgreSQL server.");
            System.exit(1);
        }
        System.out.println("Connected to the PostgreSQL server successfully.");

        HealthFitnessController healthFitnessController = new HealthFitnessController();
        healthFitnessController.start(); // Start the application
        PostgresConnection.closeConnection(connection);
    }
}