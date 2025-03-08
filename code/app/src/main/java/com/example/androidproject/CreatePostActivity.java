package com.example.androidproject;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Creates the functionality to create a post
 */
public class CreatePostActivity extends AppCompatActivity {
    Button moodDropdown;
    EditText reasonText;
    Button aloneButton;
    Button crowdButton;
    Button groupButton;
    Button confirmButton;
    ImageButton imageButton;
    MoodSelectionAdapter dropdownAdapter;
    ListView dropdownList;
    String chosenMood;
    String chosenSituation;
    Uri chosenImage;
    Boolean dropdownStatus;
    CreatePostActivity current;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Gets given data
        Bundle dataGiven = getIntent().getExtras();
        // If there is data gets the user
        if (dataGiven != null) {
            user = (User) dataGiven.get("user");
        } else {
            // If no data was given sets the user to a test value
            user = new User("testUser","testPass");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mood);
        moodDropdown = findViewById(R.id.add_select_mood_dropdown);
        reasonText = findViewById(R.id.add_reason);
        // Finds all of the views
        aloneButton = findViewById(R.id.add_mood_alone_button);
        crowdButton = findViewById(R.id.add_mood_crowd_button);
        groupButton = findViewById(R.id.add_mood_group_button);
        dropdownList = findViewById(R.id.add_mood_select_mood_list);
        confirmButton = findViewById(R.id.add_confirm_button);
        imageButton = findViewById(R.id.add_mood_image_button);
        //Sets drop down status to false to start
        dropdownStatus = Boolean.FALSE;
        //Taken from https://developer.android.com/training/basics/intents/result
        //Authored by Google Developers
        //Taken by Dalton Low
        //Taken on March 3, 2025
        ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    /**
                     *  Creates a method that once the user has selected the image runs sets chosen image to it
                     * @param uri
                     *      image location selected
                     */
                    @Override
                    public void onActivityResult(Uri uri) {
                        // End of citation
                        chosenImage = uri;
                    }
                });
        //Adds all the moods for the dropdowns
        ArrayList<String> moodList = new ArrayList<>();
        moodList.add("Anger");
        moodList.add("Confusion");
        moodList.add("Disgust");
        moodList.add("Fear");
        moodList.add("Happiness");
        moodList.add("Sadness");
        moodList.add("Shame");
        moodList.add("Surprise");
        // Sets the adapter for the list
        dropdownAdapter = new MoodSelectionAdapter(this,moodList);
        dropdownList.setAdapter(dropdownAdapter);

        //Sets the moodDropdown button action
        moodDropdown.setOnClickListener(new View.OnClickListener() {
            /**
             * Creates a button that allows the drop down feature for the mood list
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                // If the drop down is open close it
                if (dropdownStatus) {
                    dropdownList.setVisibility(View.GONE);
                    aloneButton.setVisibility(View.VISIBLE);
                    crowdButton.setVisibility(View.VISIBLE);
                    groupButton.setVisibility(View.VISIBLE);
                    confirmButton.setVisibility(View.VISIBLE);
                } else {
                    // If the drop down is closed make it visible
                    dropdownList.setVisibility(View.VISIBLE);
                    dropdownList.bringToFront();
                    aloneButton.setVisibility(View.INVISIBLE);
                    crowdButton.setVisibility(View.INVISIBLE);
                    groupButton.setVisibility(View.INVISIBLE);
                    confirmButton.setVisibility(View.INVISIBLE);
                }
                dropdownStatus = !dropdownStatus;
            }
        });
        // Sets the option to choose an item
        dropdownList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Allows the user to interact with a single drop down list item to select it
             * @param parent The AdapterView where the click happened.
             * @param view The view within the AdapterView that was clicked (this
             *            will be a view provided by the adapter)
             * @param position The position of the view in the adapter.
             * @param id The row id of the item that was clicked.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenMood = moodList.get(position);
                dropdownStatus = Boolean.FALSE;
                dropdownList.setVisibility(View.GONE);
                aloneButton.setVisibility(View.VISIBLE);
                crowdButton.setVisibility(View.VISIBLE);
                groupButton.setVisibility(View.VISIBLE);
                confirmButton.setVisibility(View.VISIBLE);
                moodDropdown.setText(chosenMood);
            }
        });
        // Sets the alone option button
        aloneButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Creates an option to choose the alone option
             * @param v The view of alone button.
             */
            @Override
            public void onClick(View v) {
                chosenSituation = "Alone";
            }
        });
        // Sets the pair option button
        crowdButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Creates an option to chose
             * @param v The view of pair button.
             */
            @Override
            public void onClick(View v) {
                chosenSituation = "Crowd";
            }
        });
        // Sets the group option button
        groupButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Creates an option to select group feature
             * @param v The view of group button.
             */
            @Override
            public void onClick(View v) {
                chosenSituation = "Group";
            }
        });
        //Sets the image button on click listener
        imageButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Creates an option to select images
             * @param v The view of the image button.
             */
            @Override
            public void onClick(View v) {
                // Creates the flags that we need
                launcher.launch("image/*");
                Log.e("TEST","adasdasd");
                Log.e("TEST","JOASDADadasdasd");
            }
        });

        // Sets the confirm on click listener
        confirmButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Finsihes the mood while checking all requirements
             * @param v The confirm button
             */
            @Override
            public void onClick(View v) {
                // Gives an error message if trying to enter without setting mood
                if (chosenMood == null) {
                    confirmButton.setError("");
                } else {
                    // Creates the new mood state
                    MoodState newMood = new MoodState(chosenMood);
                    // Adds chosen situation if it was specified
                    if (chosenSituation != null) {
                        newMood.setSituation(chosenSituation);
                    }
                    // Adds reason if it was specified
                    if (reasonText.getText().length() != 0 ) {
                        newMood.setReason(reasonText.getText().toString());
                    }
                    if (chosenImage != null) {
                        newMood.setImage(chosenImage);
                    }
                    // Sets the username
                    newMood.setUser(user.getUsername());
                    // Adds mood to database
                    Database.getInstance().addMood(newMood);
                    // Adds images to database
                    if (chosenImage != null) {
                        Database.getInstance().addImage(chosenImage,"images/" + newMood.getId(), getContentResolver());
                        newMood.setImage(Uri.parse("images/" + newMood.getId()));
                    }
                    finish();
                }
            }
        });
    }
}
