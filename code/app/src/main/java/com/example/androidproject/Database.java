package com.example.androidproject;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

}
