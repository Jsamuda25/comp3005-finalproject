package org.example.model;

import org.example.InputScanner;
import org.example.PostgresConnection;

import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Member extends User{

    public static Connection connection = null;
    public Member(User user){super(user);}

    public void profileManagement(){
        Scanner scanner = InputScanner.getInstance();
        System.out.println("Welcome to profile management, what information would you like to modify?");
        System.out.println("1. Profile information");
        System.out.println("2. Health metrics");
        System.out.println("3. Fitness goals");
        System.out.print("Enter your choice as a number: ");
        int response = scanner.nextInt();

        if(response == 1){
            updatePersonalInfo();
        }
        else if(response ==2){
            updateHealthMetrics();
        }
        else if(response == 3){
            updateFitnessGoals();
        }
        else{
            System.out.println("Invalid selection");
            return;
        }
    }

    //helper function for profileManagement()
    public void updatePersonalInfo(){
        Scanner scanner = InputScanner.getInstance();
        System.out.println("You can change the following details:");
        System.out.println("1. Username");
        System.out.println("2. Password");
        System.out.println("3. Name");
        System.out.print("Enter your choice as a number: ");
        int response = scanner.nextInt();

        if(response == 1){
            modifyUsername();
            return;
        }
        else if(response ==2){
            modifyPassword();
            return;
        }
        else if(response == 3){
            modifyName();
            return;
        }
        else{
            System.out.println("Invalid selection");
            return;
        }

    }

    public boolean modifyUsername(){

        Scanner scanner = InputScanner.getInstance();
        System.out.print("Input new username: ");
        scanner.nextLine();
        String newUser = scanner.nextLine();
        connection = PostgresConnection.connect();

        try{
            String existQuery = "SELECT username from users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(existQuery);
            statement.setString(1, newUser);
            ResultSet res = statement.executeQuery();

            if(res.next() && res.getString("username").equals(newUser)){
                System.out.println("This username is taken, please choose a different one.");
                return false;
            }
            else {

                String query = "UPDATE users SET username = ? WHERE id = ?";
                statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, newUser);
                statement.setInt(2, getUserID());

                int result = statement.executeUpdate();
                if (result > 0) {
                    try (ResultSet gKeys = statement.getGeneratedKeys()) {
                        if (gKeys.next()) {
                            String username = gKeys.getString(2);
                            System.out.println("Your new username is: " + username);
                            setUserName(username);
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        return false;
                    }
                }
            }
        }
        catch (Exception e){
            return false;
        }

        return true;

    }

    public void modifyPassword(){

    }

    public void modifyName(){

    }

    //helper function for profileManagement()
    public void updateHealthMetrics(){
        System.out.println("Update Health Metrics");
    }

    //helper function for profileManagement()
    public void updateFitnessGoals(){
        System.out.println("Update Fitness Goals");
    }

    public void displayInfo(){

    }

    public void scheduleManagement(){

    }
}
