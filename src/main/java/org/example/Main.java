package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;
import java.util.Properties;


public class Main {
    public static void main(String[] args)  {
        connect();

        System.out.println("Hello World!");
    }

    public static Properties getProperties() {
        try
        {
            String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
            String postgresPropPath = rootPath + "postgres.properties";
            Properties props = new Properties();
            props.load(new FileInputStream(postgresPropPath));
            return props;
        }
        catch (IOException e)
        {
            System.out.println("Error reading properties file");
            e.printStackTrace();
            return null;
        }
    }

    static Connection connect() {
        String url = "jdbc:postgresql://localhost:5432/FinalProject";
        Properties props = getProperties();
        assert props != null;
        String user = props.getProperty("user");
        String password = props.getProperty("password");
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