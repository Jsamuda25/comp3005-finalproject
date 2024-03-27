package org.example.model;

import org.example.InputScanner;

import java.util.Scanner;

// You can inherit from this class to create a new user type
public class User {
    public enum UserType { MEMBER, TRAINER, ADMIN }
    private final String userName;  // From the database
    public UserType userType;

    private final int userID;

    public User(String userId, UserType userType, int userID) {
        this.userName = userId;
        this.userType = userType;
        this.userID = userID;
    }

    public User(User user){
        this.userName = user.getUserName();
        this.userType = user.getUserType();
        this.userID = user.getUserID();
    }

    public UserType getUserType() {
        return userType;
    }

    public String getUserName(){ return userName;}

    public int getUserID(){ return userID;}

    public static void profileManagement(){
        Scanner scanner = InputScanner.getInstance();
        System.out.println("Welcome to profile management, what personal information would you like to modify?");
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
    public static void updatePersonalInfo(){
        System.out.println("Update Personal Info");
    }

    //helper function for profileManagement()
    public static void updateHealthMetrics(){
        System.out.println("Update Health Metrics");
    }

    //helper function for profileManagement()
    public static void updateFitnessGoals(){
        System.out.println("Update Fitness Goals");
    }

    public static void displayInfo(){

    }

    public static void scheduleManagement(){

    }

}
