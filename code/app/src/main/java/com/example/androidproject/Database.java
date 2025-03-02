package com.example.androidproject;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Creates a database class to allow interacting with the database
 */
public class Database {
    private FirebaseFirestore database;
    private static Database dbInstance; // Used to make sure there is only a singular instance of the database throughout all classes
    private CollectionReference moods;
    private CollectionReference users;



    /**
     * Constructor called on by getInstance()
     */
    private Database(){
        database = FirebaseFirestore.getInstance();
        moods = database.collection("Moods");
        users = database.collection("Users");
    }

    /**
     * Creates an instance of the dataBase if it does not exists.
     * Otherwise returns the existing instance of it.
     * To be used in other classes to allow them to access the database
     * @return instance of the Database.
     */
    public static Database getInstance(){
        if (dbInstance == null) {
            dbInstance = new Database();
        } return dbInstance;
    }

    public void addUser(User user){
        users.document(user.getUsername()).set(user);
    }


    public AtomicBoolean searchUser(String username) {
        AtomicBoolean userAlreadyExists = new AtomicBoolean(false);
        DocumentReference docRef = users.document("username");

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            userAlreadyExists.set(documentSnapshot.exists()); // Sets the boolean to true if the user already exists

        }).addOnFailureListener(e -> {
            Log.d("Database Failiure", "Document does not exist");
        });
        return userAlreadyExists;
    }


}

