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
 * The purpose of this class is to facilitate interactions for a User object and the database
 * This class helps with the signup and login functionalities.
 * Contains the logic for following, unfollowing and followRequests.
 * Also contains a static instance of the current logged in user: currentUser.
 */
public class UserManager {


    private Database database;
    private static User currentUser;
    private static UserManager instance;
    private Context context; // The activity that is calling the UserManager

    /**
     * Constructor for UserManager, gets the database instance so that it can be used in the login/signup process
     */
    public UserManager(Context context){
        this.database = Database.getInstance();
        this.context = context;

    }

    public static UserManager getInstance(Context context){
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
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
     * @param username The username that is entered on signup. Unique among all other usernames
     * @param password The password that is entered on signup.
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
     * Handles the login verifcation in the database.
     * Ensures that the username and password entered match then fetches the user's data.
     * If they do not match then it displays a toast.
     * calls fetchCurrentUserData which populates the static currentUser object
     * @param username the username that the user entered
     * @param password the password that the user entered
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
     * Fetches the user data from the database and populates the static currentUser object.
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

    /**
     * Sends a follow request to a user in the database
     * @param senderUsername the user who is sending the follow request
     * @param receiverUsername the user who is receiving the database
     */
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

    /**
     * A function to handle accepting a follow request in firebase. Called in the FollowRequestAdapter
     * when the accept button is pressed. It adds the acceptor to requestors following list and requester to acceptors followers list
     * @param acceptorUsername the username of the current logged in user who is accepting the follow request
     * @param requesterUsername the user name who is being rejected. This is the name that is removed from rejectorUsernames followRequest list in firebase.
     */
    public void acceptFollowRequest(String acceptorUsername, String requesterUsername){
        DocumentReference acceptorDocRef = database.getUsers().document(acceptorUsername);
        DocumentReference requesterDocRef = database.getUsers().document(requesterUsername);

        // Add the acceptor to the requester's following list in firebase
        requesterDocRef.update("following", FieldValue.arrayUnion(acceptorUsername))

                // If user added to following list then we get rid of them from the acceptors followRequest list in firebase
                .addOnSuccessListener(documentReferenceUpdate -> {

                    Log.d("UserManager", "requestor now has the currentUser in their database following list");
                    // Get rid of requester in the followRequest list of acceptor
                    acceptorDocRef.update("followRequests", FieldValue.arrayRemove(requesterUsername))

                            .addOnSuccessListener(documentReferenceDelete -> {

                                Log.d("UserManager", "currentUser no longer has a follow request from " + requesterUsername);
                                // Add requester to the acceptor's follower list
                                acceptorDocRef.update("followers", FieldValue.arrayUnion(requesterUsername))

                                        .addOnSuccessListener(documentReferenceUpdate1 -> {
                                            Log.d("UserManager", "currentUser now has " + requesterUsername + " in their followers list");
                                        })
                                        .addOnFailureListener(e -> { // 3rd nested update failed
                                            Log.e("UserManager", "Could not add user to follower list error message: " + e);
                                        });

                                // Failure messaging
                            }).addOnFailureListener(e -> { // 2nd nested update failed

                                Log.e("UserManager", "Failed to get rid of" + requesterUsername +"from currentUser's follow requests");
                            });
                    // Failure messaging
                }).addOnFailureListener(failure -> { // 1st update failed

                    Log.e("UserManager", "Follow Request failed to send");
                });
    }

    /**
     * A function to unfollow a user in the database.
     * Can also be used to remove a follower by swapping the order of the arguments
     * @param currentUsername The user that wishes to unfollow another user
     * @param userToBeUnfollowed The user that is being unfollowed
     */
    public void unfollowUser(String currentUsername, String userToBeUnfollowed){
        DocumentReference currentUserDocRef = database.getUsers().document(currentUsername);
        DocumentReference userToBeUnfollowedDocRef = database.getUsers().document(userToBeUnfollowed);

        // Unfollow userToBeUnfollowed (get rid of them in currentUser's following list)
        currentUserDocRef.update("following", FieldValue.arrayRemove(userToBeUnfollowed))

                .addOnSuccessListener(currentUserDocRef1 -> {

                    userToBeUnfollowedDocRef.update("followers", FieldValue.arrayRemove(currentUsername))
                            .addOnSuccessListener(userToBeUnfollowedDocRef1 -> {

                                Log.d("User Manager", currentUsername + " Successfully unfollowed " + userToBeUnfollowed);
                            })

                            .addOnFailureListener(e -> {
                                Log.e("UserManager", "Could not remove a user from a follower list error message: " + e);
                            });

                }).addOnFailureListener(e -> {

                    Log.e("UserManager", "Could not remove a user from a following list error message: " + e);

                });

    }

    /**
     * A function to handle deleting a follow request from firebase. Called in the FollowRequestAdapter
     * when the reject button is pressed.
     * @param rejectorUsername the username of the current logged in user.
     * @param requesterUsername the user name who is being rejected. This is the name that is removed from rejectorUsernames followRequest list in firebase.
     */
    public void rejectFollowRequest(String rejectorUsername, String requesterUsername){

        DocumentReference rejectorDocRef = database.getUsers().document(rejectorUsername);

        // Removing a follow request from the database.
        rejectorDocRef.update("followRequests", FieldValue.arrayRemove(requesterUsername))
                .addOnSuccessListener(documentReferenceUpdate -> {
                    Log.d("UserManager", rejectorUsername + " successfully rejected a follow request from: " + requesterUsername);
                })

                // on Failiure write out error message.
                .addOnFailureListener(e -> {
                    Log.e("UserManager", "Failed to remove a follow request error message: " + e);
                });
    }


    /**
     * A function to handle revoking a follow request from firebase. Called in OtherProfileActivity
     * when the follow button is pressed when it says "requested".
     * @param cancelerUsername the username of the current logged in user that is revoking their follow request
     * @param receiverUsername the user who's followRequest list is being changed to not have the cancelerUsername in it.
     */
    public void cancelFollowRequest(String cancelerUsername, String receiverUsername){

        DocumentReference receiverDocRef = database.getUsers().document(receiverUsername);

        // Removing a follow request from the database.
        receiverDocRef.update("followRequests", FieldValue.arrayRemove(cancelerUsername))
                .addOnSuccessListener(documentReferenceUpdate -> {
                    Log.d("UserManager", cancelerUsername + " successfully revoked their follow request to : " + receiverUsername);
                })

                // on Failiure write out error message.
                .addOnFailureListener(e -> {
                    Log.e("UserManager", "Failed to remove a follow request error message: " + e);
                });
    }

}
