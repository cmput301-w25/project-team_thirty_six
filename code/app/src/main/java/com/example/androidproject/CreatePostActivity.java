package com.example.androidproject;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
    Button pairButton;
    Button cancelButton;
    RadioButton privateButton;
    RadioButton publicButton;
    LinearLayout imageButton;
    MoodSelectionAdapter dropdownAdapter;
    ListView dropdownList;
    CardView imagePreviewCard;
    ImageView imagePreview;
    String chosenMood;
    String chosenSituation;
    Uri chosenImage;
    Boolean dropdownStatus;
    Boolean imageStatus = Boolean.FALSE;
    CreatePostActivity current;
    String user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Sets current
        current = this;
        // Gets given data
        Bundle dataGiven = getIntent().getExtras();
        // If there is data gets the user
        if (dataGiven != null) {
            user = (String) dataGiven.get("currentUser");
        } else {
            // If no data was given sets the user to a test value
            user = "testUser";
        }
        super.onCreate(savedInstanceState);
        // Sets content view
        setContentView(R.layout.activity_add_mood);
        // Gets the nav bar
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(user))
                    .commit();
        }
        // Finds all of the views
        moodDropdown = findViewById(R.id.AddMoodSelectMood);
        reasonText = findViewById(R.id.addReason);
        aloneButton = findViewById(R.id.AddradioAlone);
        crowdButton = findViewById(R.id.AddradioCrowd);
        groupButton = findViewById(R.id.AddradioGroup);
        pairButton = findViewById(R.id.AddradioPair);
        dropdownList = findViewById(R.id.add_mood_select_mood_list);
        confirmButton = findViewById(R.id.add_done_button);
        cancelButton = findViewById(R.id.add_cancel_button);
        imageButton = findViewById(R.id.AddImagebutton);
        imagePreviewCard = findViewById(R.id.imagePreviewCardView);
        imagePreview = findViewById(R.id.moodImageView);
        privateButton = findViewById(R.id.addPrivate);
        publicButton = findViewById(R.id.addPublic);
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
                        imagePreviewCard.setVisibility(View.VISIBLE);
                        imagePreview.setImageURI(chosenImage);
                        if (uri != null) {
                            findViewById(R.id.add_image_text).setVisibility(View.INVISIBLE);
                            findViewById(R.id.remove_image_text).setVisibility(View.VISIBLE);
                            imageStatus = Boolean.TRUE;
                        }
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
                } else {
                    // If the drop down is closed make it visible
                    dropdownList.setVisibility(View.VISIBLE);
                    dropdownList.bringToFront();
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
        pairButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Creates an option to choose the alone option
             * @param v The view of alone button.
             */
            @Override
            public void onClick(View v) {
                chosenSituation = "Pair";
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
                // Runs if an image hasn't already been selected
                if (!imageStatus) {
                    // Creates the flags that we need
                    launcher.launch("image/*");
                    Log.e("TEST", "adasdasd");
                    Log.e("TEST", "JOASDADadasdasd");
                } else {
                    // Sets the status of if there is an image to false
                    imageStatus = Boolean.FALSE;
                    // Stops showing the image preview
                    imagePreviewCard.setVisibility(View.INVISIBLE);
                    imagePreview.setImageDrawable(null);
                    // Removes the image
                    chosenImage = null;
                    // Displays text saying to add image
                    findViewById(R.id.add_image_text).setVisibility(View.VISIBLE);
                    findViewById(R.id.remove_image_text).setVisibility(View.INVISIBLE);
                }
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
                    CharSequence sequence = "Cannot Continue Without Mood.";
                    Toast.makeText(current,sequence, Toast.LENGTH_SHORT).show();
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
                    // Sets the post to public if public is chosen
                    if (publicButton.isChecked()) {
                        newMood.setVisibility(Boolean.TRUE);
                    } else if (privateButton.isChecked()){
                        // Sets the post to private
                        newMood.setVisibility(Boolean.FALSE);
                    }
                    // Sets the username
                    newMood.setUser(user);
                    // Adds mood to database
                    Database.getInstance().addMood(newMood);
                    // Adds images to database
                    if (chosenImage != null) {
                        Database.getInstance().addImage(chosenImage,"images/" + newMood.getId(), getContentResolver());
                        CollectionReference moodCol  = FirebaseFirestore.getInstance().collection("Moods");
                        moodCol.document(newMood.getId()).update("image",Uri.parse("images/" + newMood.getId()));
                    }
                    finish();
                }
            }
        });
        // Sets the cancel button functionality
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
