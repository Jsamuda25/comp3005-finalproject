package org.example.model;

import org.example.InputScanner;
import org.example.PostgresConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

// You can inherit from this class to create a new user type
public class User {
    public enum UserType { MEMBER, TRAINER, ADMIN }
    private String userName;  // From the database
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

    public void setUserName(String uname){
        userName = uname;
    }

}
