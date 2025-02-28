package com.example.androidproject;

import android.provider.ContactsContract;
import android.util.Log;

public class UserManager {

    private Database database;

    /**
     * Constructor for UserManager, gets the database instance so that it can be used in the login/signup process
     */
    public UserManager(){
        Log.d("myTag", "Made it to UserManager");

        this.database = Database.getInstance();
    }

    /**
     * Creates new user and adds them to the database.
     * @param username
     * @param password
     */
    public void addUser(String username, String password){
        Log.d("myTag", "made it to addUser");
        User user = new User(username, password); // Create new user

        database.addUser(user);
    }

}
