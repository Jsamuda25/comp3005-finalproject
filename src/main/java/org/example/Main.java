package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;
import java.util.Properties;


public class Main {
    public static void main(String[] args) throws IOException {

        String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
        String postgresPropPath = rootPath + "postgres.properties";

        Properties postgresProps = new Properties();
        postgresProps.load(new FileInputStream(postgresPropPath));

        String user = postgresProps.getProperty("user");
        String password = postgresProps.getProperty("password");

        System.out.println("User: " + user);
        System.out.println("Password: " + password);
    }



    static Connection connect(){
        String url = "jdbc:postgresql://localhost:5432/FinalProject";
        String user = "postgres";
        String password = "root";
        Connection connection = null;
        try{
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        }
        catch(Exception e){
            System.out.println("Unsuccessful connection to the PostgreSQL server.");
        }
        return connection;
    }
}