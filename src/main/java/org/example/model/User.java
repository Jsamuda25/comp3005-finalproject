package org.example.model;

// You can inherit from this class to create a new user type
public class User {
    public enum UserType { ADMIN, MEMBER, TRAINER }
    private final String userName;  // From the database
    public UserType userType;

    private final int userID;

    public User(String userId, UserType userType, int userID) {
        this.userName = userId;
        this.userType = userType;
        this.userID = userID;
    }

    public UserType getUserType() {
        return userType;
    }

    public String getUserName(){ return userName;}

    public int getUserID(){ return userID;}
}
