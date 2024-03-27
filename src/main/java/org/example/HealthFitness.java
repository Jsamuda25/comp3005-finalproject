package org.example;

import java.sql.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import org.example.model.User;
import org.example.model.User.UserType;


public class HealthFitness {
    private User currentUser;
    public static Connection connection = null;

    /**
     * Logs in the user
     * @param loginInfo the user's login information
     * @return true if the user is successfully logged in, false otherwise
     */
    public boolean login(List<String> loginInfo) {
        connection = PostgresConnection.connect();
        String username = loginInfo.get(0);
        String password = loginInfo.get(1);
        String query = "SELECT id, username, typeofuser FROM users WHERE username=? AND password=?";

        try{
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet res = statement.executeQuery();

            if(res.next()){

                String user = res.getString("username");
                int userId = res.getInt("id");
                query = "SELECT typeofuser FROM users WHERE username = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, user);
                res = statement.executeQuery();
                res.next();

                if(res.getString("typeofuser") .equals("MEMBER")){
                    System.out.println(res.getString("typeofuser"));
                    currentUser = new User(username, UserType.MEMBER,userId);
                }
                else if(res.getString("typeofuser").equals("ADMIN")){
                    currentUser = new User(username, UserType.ADMIN, userId);
                }
                else{
                    System.out.println(res.getString("typeofuser"));
                    currentUser = new User(username, UserType.TRAINER, userId);
                }
            }
            else{
                System.out.println("Incorrect username or password!");
                return false;

            }
        }
        catch(Exception e){
            System.out.println(e);
            return false;
        }
        return true;
    }

    /**
     * Registers the user
     * @param registerInfo the user's registration information
     * @return true if the user is successfully registered, false otherwise
     */
    @SuppressWarnings("JpaQueryApiInspection")
    public boolean register(List<String> registerInfo) {
        connection = PostgresConnection.connect();
        String first_name = registerInfo.get(0);
        String last_name = registerInfo.get(1);
        String username = registerInfo.get(2);
        String password = registerInfo.get(3);
        String name = first_name + " " + last_name;


        try{
            String query = "SELECT username FROM users WHERE username=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet res = statement.executeQuery();

            if(res.next() && res.getString("username").equals(username)){
                System.out.println("This username is taken, please choose a different one.");
                return false;
            }
            else{
                query = "INSERT INTO users(username, password, name, typeofuser) " +
                        "VALUES(?,?,?,?)";
                statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, username);
                statement.setString(2, password);
                statement.setString(3, name);
                statement.setObject(4, UserType.ADMIN, Types.OTHER);
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        currentUser = new User(username, UserType.ADMIN, userId);
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void logout() {
        currentUser = null;
    }

    public UserType getUserType() {
          return currentUser.userType;


    }

    public User getCurrentUser() {
        return currentUser;
    }

}
