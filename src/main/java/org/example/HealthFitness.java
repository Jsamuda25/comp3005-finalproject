package org.example;

import org.example.model.User;
import org.example.model.User.UserType;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.example.View.getIntegerInput;


public class HealthFitness {
    public static Connection connection = null;
    private User currentUser;

    /**
     * Logs in the user
     *
     * @param loginInfo the user's login information
     * @return true if the user is successfully logged in, false otherwise
     */
    public boolean login(List<String> loginInfo) {
        connection = PostgresConnection.connect();
        String username = loginInfo.get(0);
        String password = loginInfo.get(1);
        String query = "SELECT id, username, typeofuser FROM users WHERE username=? AND password=?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet res = statement.executeQuery();

            if (res.next()) {

                String user = res.getString("username");
                int userId = res.getInt("id");
                query = "SELECT typeofuser FROM users WHERE username = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, user);
                res = statement.executeQuery();
                res.next();

                if (res.getString("typeofuser").equals("MEMBER")) {
                    System.out.println(res.getString("typeofuser"));
                    currentUser = new User(username, UserType.MEMBER, userId);
                } else if (res.getString("typeofuser").equals("ADMIN")) {
                    currentUser = new User(username, UserType.ADMIN, userId);
                } else {
                    System.out.println(res.getString("typeofuser"));
                    currentUser = new User(username, UserType.TRAINER, userId);
                }
            } else {
                System.out.println("Incorrect username or password!");
                return false;

            }
        } catch (Exception e) {
            System.out.println("Error logging in");
            return false;
        }
        return true;
    }

    /**
     * Registers the user
     *
     * @param registerInfo the user's registration information
     * @return true if the user is successfully registered, false otherwise
     */
    @SuppressWarnings("JpaQueryApiInspection")
    public boolean register(List<String> registerInfo) {
        Scanner scanner = InputScanner.getInstance();
        UserType userType;
        int userId;
        connection = PostgresConnection.connect();

        // Extracting the user's first name, last name, username, and password
        String first_name = registerInfo.get(0);
        String last_name = registerInfo.get(1);
        String username = registerInfo.get(2);
        String password = registerInfo.get(3);
        String name = first_name + " " + last_name;

        System.out.print("Enter user type (MEMBER=0, TRAINER=1, ADMIN=2): ");
        Optional<Integer> choice = getIntegerInput();
        while (choice.isEmpty() || choice.get() < 0 || choice.get() > 2) {
            System.out.print("Invalid input. Please enter a valid user type (MEMBER=0, TRAINER=1, ADMIN=2): ");
            choice = getIntegerInput();
        }
        int type = choice.get();

        if(type==0)
            userType = UserType.MEMBER;
        else if(type==1)
            userType = UserType.TRAINER;
        else
            userType = UserType.ADMIN;

        try {
            String query = "SELECT username FROM users WHERE username=?";  // Check if username is taken
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet res = statement.executeQuery();

            if (res.next() && res.getString("username").equals(username)) {
                System.out.println("This username is taken, please choose a different one.");
                return false;
            }

            Timestamp currentTimestamp = null;
            if (type == 0) {  // If the user is a member
                System.out.println("Pay the membership fee to confirm your registration: ");
                System.out.print("Input 1 to confirm payment, otherwise your registration will be cancelled: ");
                int resPay = scanner.nextInt();
                scanner.nextLine();

                currentTimestamp = new Timestamp(System.currentTimeMillis());

                if(resPay!=1){
                    System.out.println("Your registration has been cancelled.");
                    return false;
                }
            }

            if(type>0)
                System.out.println("Welcome new staff member!");
            else
                System.out.println("Welcome new member!");

            // Insert the user into the database
            query = "INSERT INTO users(username, password, name, typeofuser) " +
                    "VALUES(?,?,?,?)";
            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, name);
            statement.setObject(4, userType, Types.OTHER);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            // Get the user's ID and create a new User object
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1);
                    currentUser = new User(username, userType, userId);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

            // Insert the user's payment into the database
            if (type == 0) {
                query = "INSERT INTO billing(member_id, fee, type_of_fee, paid, date) VALUES(?,?,?,?,?)";
                try {
                    statement = connection.prepareStatement(query);
                    statement.setInt(1, userId);
                    statement.setDouble(2, 150);
                    statement.setInt(3, 0);
                    statement.setBoolean(4, true);
                    statement.setTimestamp(5, currentTimestamp);
                    statement.executeUpdate();
                    System.out.println("Your payment has been processed. \n");
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("Error registering user");
            return false;
        }

        return true;
    }

    /**
     * Logs out the user
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Gets the current user's type
     *
     * @return the current user's type
     */
    public UserType getUserType() {
        return currentUser.userType;
    }

    /**
     * Gets the current user
     *
     * @return the current user
     */
    public User getCurrentUser() {
        return currentUser;
    }

}
