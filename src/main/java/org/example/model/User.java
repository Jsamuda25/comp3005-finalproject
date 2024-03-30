package org.example.model;

// You can inherit from this class to create a new user type
public class User {
    private final int userID;
    public UserType userType;
    private String userName;  // From the database
    public User(String userId, UserType userType, int userID) {
        this.userName = userId;
        this.userType = userType;
        this.userID = userID;
    }

    public User(User user) {
        this.userName = user.getUserName();
        this.userType = user.getUserType();
        this.userID = user.getUserID();
    }

    public UserType getUserType() {
        return userType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String uname) {
        userName = uname;
    }

    public int getUserID() {
        return userID;
    }

    public enum UserType {MEMBER, TRAINER, ADMIN}

}
