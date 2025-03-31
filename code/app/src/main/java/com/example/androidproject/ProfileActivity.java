package com.example.androidproject;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.androidproject.databinding.ActivityProfileBinding;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Creates the view profile screen, contains links to the followRequestFragment, FollowingListFragment and FollowerListFragment
 * Requires the currentUsername of the logged in user to be given in the intent.
 * Displays the following and follower caounts as well.
 */
public class ProfileActivity extends AppCompatActivity {

    private UserManager userManager;
    private String currentUsername;
    private String otherUsername;
    private User currentUser;

    /**
     * onCreate method for the ProfileActivity
     * Sets up the userManager and gets the currentUsername from the intent.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Retrieve the currentUser from the Intent
        currentUsername = (String) getIntent().getStringExtra("currentUser");

        // If it is looking at your own profile
        if (currentUsername != null) {
            Log.d("ProfileActivity", "Current user: " + currentUsername);

        } else {
            Log.e("ProfileActivity", "No currentUser data found");
        }

        userManager = new UserManager(this);

        // Retrieve the otherUser object from the database and store it into otherUser
        //userManager.fetchOtherUserData(currentUsername)
        //        .addOnSuccessListener(user ->{
        //            if (user != null){
        //                this.currentUser = user;
        //                // populate the profile
        //                populateCurrentUserProfile(currentUsername);
        //
        //            }
        //            else{
        //                Log.e("CurrentProfileActivity", "Failed to retrieve currentUser's contents");
        //            }
        //        }).addOnFailureListener(e -> {
        //            Log.e("CurrentProfileActivity", "fetchCurrentUserData error: " + e.toString());
        //        });

        this.currentUser = UserManager.getCurrentUser();
        populateCurrentUserProfile(currentUsername);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(currentUsername))
                    .commit();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        //ThemeManager.applyThemeToActivity(this);

        //int themeResId = ThemeManager.getThemeResourceId(this);
        //applyThemeToCurrentActivity(themeResId);
    }

    /**
     * Populates the user in user profile info into the activity_profile.xml file.
     * @param currentUsername the username of the logged in user.
     */
    private void populateCurrentUserProfile(String currentUsername) {
        TextView userNameTextView = findViewById(R.id.displayUsername);
        userNameTextView.setText(currentUsername);

        // Setup follower and following strings
        int followingCount = currentUser.getFollowing().size();
        int followerCount = currentUser.getFollowers().size();
        String followingString = String.format("%d following", followingCount);
        String followersString = String.format("%d followers", followerCount);

        // Populate the follower and following strings
        TextView followersTextView = findViewById(R.id.followerAmountTextView);
        TextView followingTextView = findViewById(R.id.followingAmountTextView);
        followersTextView.setText(followersString);
        followingTextView.setText(followingString);
    }

    /**
     * The onClick method for the followRequest sign.
     * Opens the Follow Request Fragment and passes currentUser into the bundle.
     * @param view
     */
    public void followRequestsOnClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("currentUser", currentUser);

        Fragment listFragment = new FollowRequestFragment();
        listFragment.setArguments(bundle);

        // The folowing code was obtained from ChatGPT on March 22, 2025.
        // Taken by Rhiyon Naderi
        // Query: how to open a list view fragment in an activities function

        // Get the FragmentManager and start a transaction.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace the container with the new fragment.
        transaction.replace(R.id.fragment_container, listFragment);
        // Optionally add to back stack if you want to allow the user to navigate back.
        transaction.addToBackStack(null);
        // Commit the transaction.
        transaction.commit();
        // End of taken code.
    }

    /**
     * The on click method for the following count textview.
     * opens the FollowingListFragment which displays the user's following
     * @param view The view that this was called in (Activity_profile)
     */
    public void followingOnClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("currentUser", currentUser);

        Fragment listFragment = new FollowingListFragment();
        listFragment.setArguments(bundle);

        // The folowing code was obtained from ChatGPT on March 22, 2025.
        // Taken by Rhiyon Naderi
        // Query: how to open a list view fragment in an activities function

        // Get the FragmentManager and start a transaction.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace the container with the new fragment.
        transaction.replace(R.id.fragment_container, listFragment);
        // Optionally add to back stack if you want to allow the user to navigate back.
        transaction.addToBackStack(null);
        // Commit the transaction.
        transaction.commit();
        Log.d("ProfileActivity", "Reached");
        // End of taken code.

    }

    /**
     * The on click method for the follower count textview.
     * It opens the FollowListFragment which displays the user's followers
     * @param view The view that this was called in (Activity_profile)
     */
    public void followerOnClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("currentUser", currentUser);

        Fragment listFragment = new FollowerListFragment();
        listFragment.setArguments(bundle);

        // The folowing code was obtained from ChatGPT on March 22, 2025.
        // Taken by Rhiyon Naderi
        // Query: how to open a list view fragment in an activities function

        // Get the FragmentManager and start a transaction.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace the container with the new fragment.
        transaction.replace(R.id.fragment_container, listFragment);
        // Optionally add to back stack if you want to allow the user to navigate back.
        transaction.addToBackStack(null);
        // Commit the transaction.
        transaction.commit();
        Log.d("ProfileActivity", "Reached");
        // End of taken code.

    }

    /**
     * Takes you to a page to view mood history
     * @param view The current view
     */
    public void viewMoodHistory(View view){
        Intent i = new Intent(this, MoodHistoryActivity.class);
        i.putExtra("currentUser",
                (String) getIntent().getSerializableExtra("currentUser"));
        startActivity(i);
    }

    public void customizeTheme(View view) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_theme_selection, null);

        // we need to find the views
        RadioGroup radioGroup = dialogView.findViewById(R.id.theme_radio_group);
        RadioButton defaultTheme = dialogView.findViewById(R.id.theme_default);
        RadioButton blueTheme = dialogView.findViewById(R.id.theme_blue);
        RadioButton purpleTheme = dialogView.findViewById(R.id.theme_purple);
        RadioButton redGoldTheme = dialogView.findViewById(R.id.theme_red_gold);
        RadioButton brownOrangeTheme = dialogView.findViewById(R.id.theme_brown_orange);

        // based on what the user selected as their theme
        String currentTheme = ThemeManager.getCurrentTheme(this);
        switch (currentTheme) {
            case ThemeManager.THEME_BLUE:
                blueTheme.setChecked(true);
                break;
            case ThemeManager.THEME_PURPLE:
                purpleTheme.setChecked(true);
                break;
            case ThemeManager.THEME_RED_GOLD:
                redGoldTheme.setChecked(true);
                break;
            case ThemeManager.THEME_BROWN_ORANGE:
                brownOrangeTheme.setChecked(true);
                break;
            default:
                defaultTheme.setChecked(true);
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Set button click listeners
        Button cancelButton = dialogView.findViewById(R.id.theme_cancel_button);
        Button applyButton = dialogView.findViewById(R.id.theme_save_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        applyButton.setOnClickListener(v -> {
            // based on what the user selected as their theme
            String selectedTheme;
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == R.id.theme_blue) {
                selectedTheme = ThemeManager.THEME_BLUE;
            } else if (selectedId == R.id.theme_purple) {
                selectedTheme = ThemeManager.THEME_PURPLE;
            } else if (selectedId == R.id.theme_red_gold) {
                selectedTheme = ThemeManager.THEME_RED_GOLD;
            } else if (selectedId == R.id.theme_brown_orange) {
                selectedTheme = ThemeManager.THEME_BROWN_ORANGE;
            } else {
                selectedTheme = ThemeManager.THEME_DEFAULT;
            }

            // the theme now has to be saved
            ThemeManager.saveTheme(this, selectedTheme);

            // Database now has the users preference
            User currentUser = UserManager.getCurrentUser();
            if (currentUser != null) {
                currentUser.setThemePreference(selectedTheme);
                FirebaseFirestore.getInstance().collection("Users")
                        .document(currentUser.getUsername())
                        .update("themePreference", selectedTheme);
            }

            // Inform user and close dialog
            Toast.makeText(this, "Theme updated!", Toast.LENGTH_SHORT).show(); // confirmation message
            dialog.dismiss();
        });
    }
}