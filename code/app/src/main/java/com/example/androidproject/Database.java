package com.example.androidproject;

import android.content.ContentResolver;
import android.net.Uri;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Creates a database class to allow interacting with the database
 */
public class Database {
    private FirebaseFirestore database;
    private static Database dbInstance; // Used to make sure there is only a singular instance of the database throughout all classes
    private CollectionReference moods;
    private CollectionReference users;
    private CollectionReference comments;



    /**
     * Constructor called on by getInstance()
     */
    private Database(){
        FirebaseFirestore.getInstance();
        database = FirebaseFirestore.getInstance();
        moods = database.collection("Moods");
        users = database.collection("Users");
        comments = database.collection("Comments");
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

    /**
     * Allows a user to add moods to the database
     * @param mood
     *      mood to be added
     */
    public void addMood(MoodState mood){
        DocumentReference newDoc = moods.document();
        mood.setId(newDoc.getId());
        newDoc.set(mood);
    }

    public void addFollowRequest(String requesterUsername){
    }

    /**
     *  Adds an image to the database
     * @param uri
     *      where to find image on phone
     * @param id
     *      id to store image under
     * @param resolver
     *      content resolver to properly locate image
     */
    public void addImage(Uri uri, String id, ContentResolver resolver){
        try {
            InputStream stream = resolver.openInputStream(uri);
            int imageSize = stream.available();
            stream.close();

            if (imageSize >= 65536) {
                throw new IllegalArgumentException("Image size exceeds 64KB limit");
            }

            // Gets the firebase instance
            FirebaseStorage currentFirebase = FirebaseStorage.getInstance();
            // Gets a child for the id
            StorageReference newStorage = currentFirebase.getReference().child(id);
            InputStream newSteam = null;
            try {
                // Gets a new stream to add
                newSteam = resolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            // Adds stream to storage
            newStorage.putStream(newSteam);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  Adds a comment to the database
     * @param username
     *      the username of the commenter
     * @param moodID
     *      the mood id of the mood it is attached to
     * @param text
     *      the text of the comment
     */
    public void addComment(String username, String moodID, String text){
        // Creates the comment
        Comment comment = new Comment(username,moodID,text);
        // Adds it to the database
        DocumentReference newDoc = comments.document();
        newDoc.set(comment);
    }
    public CollectionReference getUsers() {
        return users;
    }
}

