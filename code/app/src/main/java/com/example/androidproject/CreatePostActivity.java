package com.example.androidproject;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.images.ImageManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;

/**
 * Creates the functionality to create a post
 */
public class CreatePostActivity extends AppCompatActivity {
    Button moodDropdown;
    EditText reasonText;
    Button aloneButton;
    Button pairButton;
    Button groupButton;
    Button confirmButton;
    ImageButton imageButton;
    MoodSelectionAdapter dropdownAdapter;
    ListView dropdownList;
    String chosenMood;
    String chosenSituation;
    Bitmap chosenImage;
    Boolean dropdownStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mood);
        moodDropdown = findViewById(R.id.add_select_mood_dropdown);
        reasonText = findViewById(R.id.add_reason);
        // Finds all of the views
        aloneButton = findViewById(R.id.add_mood_alone_button);
        pairButton = findViewById(R.id.add_mood_pair_button);
        groupButton = findViewById(R.id.add_mood_group_button);
        dropdownList = findViewById(R.id.add_mood_select_mood_list);
        confirmButton = findViewById(R.id.add_confirm_button);
        imageButton = findViewById(R.id.add_mood_image_button);
        //Sets drop down status to false to start
        dropdownStatus = Boolean.FALSE;

        // The following is from IJ APPS
        // Android Pick Image from Gallery | Tutorial
        // https://www.youtube.com/watch?v=H1ja8gvTtBE
        // Released: August 9, 2021
        // Taken: March 2, 2025
        Intent imagePicker = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // End of citation

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
            @Override
            public void onClick(View v) {
                // If the drop down is open close it
                if (dropdownStatus) {
                    dropdownList.setVisibility(View.GONE);
                    aloneButton.setVisibility(View.VISIBLE);
                    pairButton.setVisibility(View.VISIBLE);
                    groupButton.setVisibility(View.VISIBLE);
                    confirmButton.setVisibility(View.VISIBLE);
                } else {
                    // If the drop down is closed make it visible
                    dropdownList.setVisibility(View.VISIBLE);
                    dropdownList.bringToFront();
                    aloneButton.setVisibility(View.INVISIBLE);
                    pairButton.setVisibility(View.INVISIBLE);
                    groupButton.setVisibility(View.INVISIBLE);
                    confirmButton.setVisibility(View.INVISIBLE);
                }
                dropdownStatus = !dropdownStatus;
            }
        });
        // Sets the option to choose an item
        dropdownList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenMood = moodList.get(position);
                dropdownStatus = Boolean.FALSE;
                dropdownList.setVisibility(View.GONE);
                aloneButton.setVisibility(View.VISIBLE);
                pairButton.setVisibility(View.VISIBLE);
                groupButton.setVisibility(View.VISIBLE);
                confirmButton.setVisibility(View.VISIBLE);
                moodDropdown.setText(chosenMood);
            }
        });
        // Sets the alone option button
        aloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenSituation = "Alone";
            }
        });
        // Sets the pair option button
        pairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenSituation = "Pair";
            }
        });
        // Sets the group option button
        aloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenSituation = "Group";
            }
        });
        //Sets the image button on click listener
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePicker.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(imagePicker);

            }
        });

        // Sets the confirm on click listener
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gives an error message if trying to enter without setting mood
                if (chosenMood == null) {
                    confirmButton.setError("Cannot leave mood blank");
                } else {
                    // Creates the new mood state
                    MoodState newMood = new MoodState(chosenMood);
                    // Adds chosen situation if it was specified
                    if (chosenSituation != null) {
                        newMood.setSituation(chosenSituation);
                    }
                    // Adds reason if it was specified
                    if (reasonText.getText().length() != 0) {
                        newMood.setReason(reasonText.getText().toString());
                    }
                    if (imagePicker.getData() != null) {
                        Log.e("TEST","JOASDAD");
                        imageButton.setImageURI(imagePicker.getData());
                        chosenImage = BitmapFactory.decodeFile(imagePicker.getData().getPath());
                        newMood.setImage(chosenImage);
                    }
                    // Adds the mood to the database
                    newMood.setUser("testUser");
                    Database.getInstance().addMood(newMood);
                }
            }
        });

    }


}
