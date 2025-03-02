package com.example.androidproject;

import android.provider.ContactsContract;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

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
    public void addUser(String username, String password) throws InterruptedException {
        AtomicBoolean userAlreadyExists = database.searchUser(username);
        Thread.sleep(5000);
        if (userAlreadyExists.get()){
            Log.d("TESTERRRR", "Tis very true");
        } else{
            Log.d("TESTERRRR", "Tis not ture");

            User user = new User(username, password); // Create new user
            database.addUser(user);
        }
    }

}
