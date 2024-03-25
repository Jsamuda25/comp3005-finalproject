package org.example;

import java.util.List;

import org.example.model.User;
import org.example.model.User.UserType;


public class HealthFitness {
    private User currentUser;

    /**
     * Logs in the user
     * @param loginInfo the user's login information
     * @return true if the user is successfully logged in, false otherwise
     */
    public boolean login(List<String> loginInfo) {
        // TODO: implement login logic
        currentUser = new User("123", UserType.ADMIN);

        return true;
    }

    /**
     * Registers the user
     * @param registerInfo the user's registration information
     * @return true if the user is successfully registered, false otherwise
     */
    public boolean register(List<String> registerInfo) {
        // TODO: implement register logic
        currentUser = new User("123", UserType.ADMIN);

        return true;
    }

    public UserType getUserType() {
        return currentUser.userType;
    }


}
