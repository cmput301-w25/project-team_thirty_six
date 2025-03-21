package com.example.androidproject;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class helps with the signup and login functionalities. It facilitates interactions with the
 * database.
 */
public class UserManager {


    private Database database;
    private static User currentUser;
    private Context context; // The activity that is calling the UserManager

    /**
     * Constructor for UserManager, gets the database instance so that it can be used in the login/signup process
     */
    public UserManager(Context context){
        Log.d("myTag", "Made it to UserManager");

        this.database = Database.getInstance();
        this.context = context;
    }

    /**
     * Gets the current user
     * @return
     */
    public static User getCurrentUser(){
        return currentUser;
    }

    public interface SignUpCallback {
        void onSignUpSuccess();

    }

    /**
     * Creates new user and adds them to the database. If the user already exists in the database
     * re-prompts the user to enter a different username
     * @param username
     * @param password
     */
    public void addUser(String username, String password, SignUpCallback callback) {
        // Creates the query for the username
        Task<QuerySnapshot> query = database.getUsers().whereEqualTo("username",username).get();
        // Adds a success listener
        query.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Integer i = queryDocumentSnapshots.size();
                if (i == 1) {
                    // If the query isn't empty user was found and new one cannot be created
                    Log.d("UserManager","Name Error: Username already taken");
                    Toast userTakenToast = Toast.makeText(context, "Username already taken. Please enter a different one.", Toast.LENGTH_LONG);
                    userTakenToast.show();
                } else {
                    // If the query is empty create new user
                    User user = new User(username, password);
                    database.addUser(user);
                    // TODO Start the next activity, wherever the screen goes after signup
                    callback.onSignUpSuccess();
                }
            }
            // Creates on failure listener to send message
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("UserManager","Could not connect to database");
            }
        });
    }

    public interface LoginCallback {
        void onLoginSuccess();

    }

    /**
     *
     * @param username
     * @param password
     */
    public void loginUser(String username, String password, LoginCallback callback){
        // Creates the query for a matching username and password
        Task<QuerySnapshot> query = database.getUsers().whereEqualTo(FieldPath.documentId(), username).whereEqualTo("password", password).get();
        // Perform the query and checks
        query.addOnSuccessListener(queryDocumentSnapshots ->{
            int i = queryDocumentSnapshots.size();
            if (i == 1){ // If the user was found and there was only one of them
                Log.d("UserManager", "Username & password match, login successful!");
                callback.onLoginSuccess();
            } else if (i == 0) { // User does not exist
                Log.d("User Manager", "Username & password do not match, login unsuccessful");
                Toast userTakenToast = Toast.makeText(context, "Username and password do not match. Please try again.", Toast.LENGTH_LONG);
                userTakenToast.show();
            }else {
                Log.w("UserManager", "Something is wrong, there may be multiple users with the same username and password.");
            }
        }).addOnFailureListener(error -> {
            Log.w("UserManager", "Could not connect to database");
        });

        fetchCurrentUserData(username);

    }

    /**
     * Fetches the user data from the database and populates the currentUser object.
     * @param username The username of the user to fetch.
     */
    public void fetchCurrentUserData(String username) {
        Task<QuerySnapshot> query = database.getUsers().whereEqualTo("username", username).get();
        query.addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                // Assuming there's only one user with the given username
                currentUser = queryDocumentSnapshots.toObjects(User.class).get(0);
                Log.d("UserManager", "User data fetched and currentUser updated.");
            } else {
                Log.d("UserManager", "No user found with the given username.");
            }
        }).addOnFailureListener(e -> {
            Log.w("UserManager", "Error fetching user data: " + e.getMessage());
        });
    }

    /**
     * Fetches the other user data from the database and returns a populated user object.
     * @param username The username of the otherUser to fetch.
     */
    // The following function was taken from chatGPT by Rhiyon Naderi on Thursday March 20
    // Query: how to have this return currentUser instead of setting the classes' variable?
    // Context: This was asked in reference the fetchCurrentUserData function that was written above
    public Task<User> fetchOtherUserData(String username) {
        return database.getUsers().whereEqualTo("username", username).get()
                .continueWith(task -> {
                    if (task.isSuccessful()){
                        QuerySnapshot queryDocumentSnapshots = task.getResult();
                        // if the query was not empty
                        if (!queryDocumentSnapshots.isEmpty()){
                            return queryDocumentSnapshots.toObjects(User.class).get(0);
                        }
                    }
                    // if the query was empty
                    return null;
                });

    }


    public void sendFollowRequest(String senderUsername, String receiverUsername){
        // Gets the recievers DocumentReference
        DocumentReference docRef = database.getUsers().document(receiverUsername);
        docRef.update("followRequests", FieldValue.arrayUnion(senderUsername))
                .addOnSuccessListener(documentReferenceUpdate -> {
                    Log.d("UserManager", "Follow Request Sent");
                }).addOnFailureListener(failure -> {
                    Log.e("UserManager", "Follow Request failed to send");
                });
    }

    //public User get(String senderUsername, String receiverUsername){
    //    DocumentReference docRef = database.getUsers().document(receiverUsername);
        //ArrayList<String> reciever_follow_requests = docRef.

    //}

}
