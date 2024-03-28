package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class PostgresConnection {

    /**
     * Reads the postgres.properties file in resources and gets your username and password
     * @return the properties
     */
    public static Properties getProperties() {
        try
        {
            String rootPath = System.getProperty("user.dir") + "/src/main/resources/";
            String postgresPropPath = rootPath + "postgres.properties";
            Properties props = new Properties();
            props.load(new FileInputStream(postgresPropPath));
            return props;
        }
        catch (IOException e)
        {
            System.out.println("Error reading properties file");
            return null;
        }
    }

    /**
     * Connects to the PostgreSQL database
     * @return the connection
     */
    public static Connection connect() {
        String url = "jdbc:postgresql://localhost:5432/FinalProject";
        Properties props = getProperties();
        assert props != null;
        String user = props.getProperty("user");
        String password = props.getProperty("password");
        Connection connection = null;
        try{
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);
            // System.out.println("Connected to the PostgreSQL server successfully.");
        }
        catch(Exception e){
            System.out.println("Unsuccessful connection to the PostgreSQL server.");
        }
        return connection;
    }

    /**
     * Closes the connection
     * @param connection the connection to close
     */
    public static void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (Exception e) {
            System.out.println("Error closing connection");
        }
    }
}
