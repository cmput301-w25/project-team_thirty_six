package com.example.androidproject;

import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

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
        // Creates the query for the username
        Task<QuerySnapshot> query = database.getUsers().whereEqualTo("username",username).get();
        // Adds a success listenet
        query.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Integer i = queryDocumentSnapshots.size();
                // If the query isn't empty user was found and new one cannot be created
                if (i > 0) {
                    Log.d("Name Error","Name already taken");
                } else {
                    // If the query is empty create new user
                    User user = new User(username, password); // Create new user
                    database.addUser(user);
                }
            }
            // Creates on failure listener to send message
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Database","Could not connect to database");
            }
        });
    }

}
