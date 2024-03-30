package org.example;

import java.sql.Connection;

public class Main {

    public static Connection connection = null;

    public static void main(String[] args) {
        connection = PostgresConnection.connect();
        if (connection != null)
            System.out.println("Connected to the PostgreSQL server successfully.");
        else {
            System.out.println("Unsuccessful connection to the PostgreSQL server.");
            System.exit(1);
        }

        HealthFitnessController healthFitnessController = new HealthFitnessController();
        healthFitnessController.start();
    }
}