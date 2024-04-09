package org.example.model;

import org.example.PostgresConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    /**
     *  This method takes a room id and gets the room number from the database
     *
     * @param roomId the room id
     * @return the room number
     */
    public String getRoomNumber(int roomId) {
        String roomNumber = "";
        Connection connection = PostgresConnection.connect();
        try {
            String query = "SELECT room_number FROM Room WHERE room_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, roomId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                roomNumber = resultSet.getString("room_number");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving room number.");
        }
        PostgresConnection.closeConnection(connection);
        return roomNumber;
    }

    public enum UserType {MEMBER, TRAINER, ADMIN}

}
