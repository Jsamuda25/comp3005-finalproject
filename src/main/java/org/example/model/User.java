package org.example.model;

// You can inherit from this class to create a new user type
public class User {
    public enum UserType { ADMIN, MEMBER, TRAINER }
    private final String userId;  // From the database
    public UserType userType;

    public User(String userId, UserType userType) {
        this.userId = userId;
        this.userType = userType;
    }

    public UserType getUserType() {
        return userType;
    }

    public String getUserId(){ return userId;}
}
