package org.example.model;

import org.example.InputScanner;
import org.example.PostgresConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class Member extends User{

    public static Connection connection = null;
    public Member(User user){super(user);}

    public void profileManagement(){
        Scanner scanner = InputScanner.getInstance();
        System.out.println("Welcome to profile management, what information would you like to modify?");
        System.out.println("Profile information - 1");
        System.out.println("Health metrics - 2");
        System.out.println("Fitness goals - 3");
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
        System.out.println("What would profile details would you like to update?");
        System.out.println("Username - 1");
        System.out.println("Password - 2");
        System.out.print("Name - 3");
        int response = scanner.nextInt();

        if(response == 1){
            modifyUsername();
        }
        else if(response ==2){
            modifyPassword();
        }
        else if(response == 3){
            modifyName();
        }
        else{
            System.out.println("Invalid selection");
            return;
        }

    }

    public boolean modifyUsername(){

        Scanner scanner = InputScanner.getInstance();
        System.out.print("Input new username: ");
        String newUser = scanner.nextLine();
        connection = PostgresConnection.connect();


        try{
            String query = "UPDATE users SET username = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, newUser);
            statement.setInt(2, getUserID());

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
