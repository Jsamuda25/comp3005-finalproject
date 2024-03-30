package org.example.model;

import org.example.InputScanner;
import org.example.PostgresConnection;

import java.time.LocalDate;
import java.util.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
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
        System.out.println("4. Exit");
        System.out.print("Enter your choice as a number: ");
        int response = scanner.nextInt();

        if(response == 1){
            updatePersonalInfo();
        }
        else if(response ==2){
            selectHealthOption();
        }
        else if(response == 3){
           selectFitnessFunction();
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
        System.out.println("4. Exit");
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
        else if(response==4){
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
                            System.out.println("Your new username is: " + username + "\n");
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

    public boolean modifyPassword(){
        Scanner scanner = InputScanner.getInstance();
        System.out.print("Input new password: ");
        scanner.nextLine();
        String newPass = scanner.nextLine();
        connection = PostgresConnection.connect();

        try{
            String query = "UPDATE users SET password = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, newPass);
            statement.setInt(2, getUserID());

            int result = statement.executeUpdate();
            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next() && gKeys.getString(3).equals(newPass)) {
                        System.out.println("Your password has been updated\n");
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    return false;
                }
            }

        }
        catch(Exception e){
            System.out.println(e);
            return  false;
        }
        return  true;
    }

    public boolean modifyName(){
        Scanner scanner = InputScanner.getInstance();
        scanner.nextLine();
        System.out.print("Enter first name: ");
        String first_name = scanner.nextLine().trim();
        System.out.print("Enter last name: ");
        String last_name = scanner.nextLine().trim();

        String new_name = first_name + " " + last_name;
        connection = PostgresConnection.connect();

        try{
            String query = "UPDATE users SET name = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, new_name);
            statement.setInt(2, getUserID());

            int result = statement.executeUpdate();
            if (result > 0) {
                try (ResultSet gKeys = statement.getGeneratedKeys()) {
                    if (gKeys.next() && gKeys.getString(4).equals(new_name)) {
                        System.out.println("Your name has been updated to: " + new_name + "\n");
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    return false;
                }
            }
        }
        catch (Exception e){
            System.out.println(e);
            return false;
        }

        return true;

    }

    //helper function for profileManagement()
    public void selectHealthOption(){
        System.out.println("Health Metrics Menu");
        System.out.println("1. View Health Metrics");
        System.out.println("2. Edit Health Metrics");
        System.out.println("3. Add Health Metrics");
        System.out.println("4. Exit");
        System.out.print("Enter choice as integer: ");
        Scanner scanner = InputScanner.getInstance();
        int response = scanner.nextInt();
        if(response == 1){
            viewHealthMetrics();
        }
        else if(response == 2){
            editHealthMetrics();
        }
        else if(response == 3){
            addHealthMetrics();
        }
        else if(response == 4){
            profileManagement();
        }
        else{
            return;
        }


    }

    public void viewHealthMetrics(){
        System.out.println("Your Health Metrics");
        connection = PostgresConnection.connect();
        String query = "SELECT metric_type, value, unit, notes, date_recorded FROM healthmetrics WHERE member_id = ?";
        try{
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            ResultSet res = statement.executeQuery();

            while(res.next()){
                System.out.println("---------");
                System.out.println("Metric: " + res.getString("metric_type"));
                System.out.println("Value: " + res.getDouble("value") + "" + res.getString("unit"));
                System.out.println("Note: " + res.getString("notes"));
                System.out.println("Date recorded: " + res.getDate("date_recorded") + "\n");
            }
        }
        catch (Exception e){
            System.out.println(e);
            return;
        }

    }

    public void editHealthMetrics(){

    }

    public void addHealthMetrics(){
        Scanner scanner = InputScanner.getInstance();
        scanner.nextLine();
        System.out.print("Enter Metric Type: ");
        String metric = scanner.nextLine();
        System.out.print("Enter value: ");
        Double value = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter unit of measurement: ");
        String unit = scanner.nextLine();
        System.out.print("Enter note: ");
        String note = scanner.nextLine();

        LocalDate date = java.time.LocalDate.now();

        connection = PostgresConnection.connect();

        try{
            String query = "INSERT INTO HealthMetrics(member_id, metric_type, value, date_recorded, unit, notes) VALUES (?, ? , ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            statement.setString(2, metric);
            statement.setDouble(3, value);
            statement.setDate(4, java.sql.Date.valueOf(date));
            statement.setString(5, unit);
            statement.setString(6, note);
            statement.executeUpdate();
            System.out.println("New metric has been added!\n");
        }
        catch (Exception e){
            System.out.println(e);
        }
    }


    public void selectFitnessFunction(){
        System.out.println("Fitness Goals Menu");
        System.out.println("1. View Fitness Goals");
        System.out.println("2. Edit Fitness Goal");
        System.out.println("3. Add Fitness Goal");
        System.out.println("4. Exit");
        System.out.print("Enter choice as integer: ");
        Scanner scanner = InputScanner.getInstance();
        int response = scanner.nextInt();

        if(response == 1){
            viewFitnessGoals();
        }
        else if(response == 2){
            updateFitnessGoals();
        }
        else if(response == 3){
            addFitnessGoals();
        }
        else if(response == 4){
            profileManagement();
        }
        else{
            return;
        }

    }



    public void addFitnessGoals(){
        Scanner scanner = InputScanner.getInstance();
        scanner.nextLine();
        System.out.print("Enter title: ");
        String title = scanner.nextLine();
        System.out.print("Enter description: ");
        String description = scanner.nextLine();
        System.out.print("Enter end date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        connection = PostgresConnection.connect();

        try{
            String query = "INSERT INTO FitnessGoal(userId, title, value, enddate, completed) VALUES (?, ? , ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            statement.setString(2, title);
            statement.setString(3, description);
            statement.setDate(4, java.sql.Date.valueOf(date));
            statement.setBoolean(5, false);
            statement.executeUpdate();
            System.out.println("Your goal has been added!\n");
        }
        catch (Exception e){
            System.out.println(e);
        }

    }
    //helper function for profileManagement()
    public void updateFitnessGoals(){
        viewFitnessGoals();
        Scanner scanner = InputScanner.getInstance();
        System.out.print("Provide the ID of the goal you would like to edit, otherwise input 0: ");
        int goal_id = scanner.nextInt();
        scanner.nextLine();

        if(goal_id == 0){
            return;
        }

        System.out.println("What attributes would you like to change?");
        System.out.println("1. Title");
        System.out.println("2. Description");
        System.out.println("3. End date");
        System.out.println("4. Completion");
        System.out.println("5. Exit menu");
        System.out.print("Enter choice as integer: ");
        int response = scanner.nextInt();
        scanner.nextLine();

        if(response == 1){
            System.out.print("Enter new title: ");
            String title = scanner.nextLine();
            updateTitle(goal_id, title);
            updateFitnessGoals();
        }
        else if(response == 2){
            System.out.print("Enter new description: ");
            String des = scanner.nextLine();
            updateDescription(goal_id, des);
            updateFitnessGoals();
        }
        else if(response ==3){
            System.out.print("Enter new end date (YYYY-MM-DD):  ");
            String date =  scanner.nextLine();
            modifyDate(goal_id, date);
            updateFitnessGoals();
        }
        else if(response == 4){
            System.out.println("Enter new status (0 = Incomplete, 1 = Complete): ");
            int status = scanner.nextInt();
            boolean stat = false;

            if(status==0){
                stat = false;
            }
            else if(status ==1){
                stat = true;
            }
            else{
                System.out.println("Invalid value for status:");
            }
            updateStatus(goal_id, stat);
            updateFitnessGoals();
        }
        else if (response==5){
            profileManagement();
        }
        else{
            return;
        }
    }

    public void updateTitle(int id, String title){
        connection = PostgresConnection.connect();

        try{
            String query = "UPDATE fitnessgoal SET title = ? WHERE goalid = ? AND userid = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, title);
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if(result > 0){
                try(ResultSet gKeys = statement.getGeneratedKeys()){
                    if(gKeys.next()){
                        System.out.println("Your goal has been updated.");
                    }
                }
            }
            else{
                System.out.println("Goal was not updated.\n");
            }
        }
        catch (Exception e){
            System.out.println(e);
            return;
        }
    }

    public void updateDescription(int id, String description){
        connection = PostgresConnection.connect();

        try{
            String query = "UPDATE fitnessgoal SET value = ? WHERE goalid = ? AND userid = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, description);
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if(result > 0){
                try(ResultSet gKeys = statement.getGeneratedKeys()){
                    if(gKeys.next()){
                        System.out.println("Your goal has been updated.");
                    }
                }
            }
            else{
                System.out.println("Goal was not updated.\n");
                return;
            }
        }
        catch (Exception e){
            System.out.println(e);
        }

    }

    public void modifyDate(int id, String date){
        connection = PostgresConnection.connect();

        try{
            String query = "UPDATE fitnessgoal SET enddate = ? WHERE goalid = ? AND userid = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setDate(1, java.sql.Date.valueOf(date));
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if(result > 0){
                try(ResultSet gKeys = statement.getGeneratedKeys()){
                    if(gKeys.next()){
                        System.out.println("Your goal has been updated.");
                    }
                }
            }
            else{
                System.out.println("Goal was not updated.\n");
            }
        }
        catch (Exception e){
            System.out.println(e);
            return;
        }
    }

    public void updateStatus(int id, boolean completed){

        try{
            String query = "UPDATE fitnessgoal SET completed = ? WHERE goalid = ? AND userid = ?";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setBoolean(1, completed);
            statement.setInt(2, id);
            statement.setInt(3, getUserID());

            int result = statement.executeUpdate();

            if(result > 0){
                try(ResultSet gKeys = statement.getGeneratedKeys()){
                    if(gKeys.next()){
                        System.out.println("Your goal has been updated.");
                    }
                }
            }
            else{
                System.out.println("Goal was not updated.\n");
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }


    public void viewFitnessGoals(){
        connection = PostgresConnection.connect();
        System.out.println("Your fitness goals: ");
        try{
            String query = "SELECT goalid, title, value, enddate, completed FROM fitnessgoal WHERE userid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserID());
            ResultSet res = statement.executeQuery();
            while(res.next()){
                System.out.println("---------");
                System.out.println("Goal id: " + res.getString("goalid"));
                System.out.println("Title: " + res.getString("title"));
                System.out.println("Description: " + res.getString("value"));
                System.out.println("End date: " + res.getDate("enddate"));
                System.out.println("Completed (T/F): " + res.getBoolean("completed") + "\n");
            }
        }
        catch(Exception e){
            System.out.println(e);
            return;
        }

    }
    public void displayInfo(){

    }

    public void scheduleManagement(){

    }
}
