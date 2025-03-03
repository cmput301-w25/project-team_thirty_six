package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditMoodActivity extends AppCompatActivity {
    private Button doneButton, cancelButton;
    private Button aloneButton, pairButton, groupButton;
    private Button moodDropdown;
    private EditText reasonText;
    private ListView dropdownList;
    private String chosenMood, chosenSituation, moodId, location, image;
    private FirebaseFirestore db;
    private MoodSelectionAdapter dropdownAdapter;
    private Boolean dropdownStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mood);

        db = FirebaseFirestore.getInstance();
        // Find UI elements
        doneButton = findViewById(R.id.done_button);
        cancelButton = findViewById(R.id.cancel_button);
        moodDropdown = findViewById(R.id.btnEditMoodSelectMood);
        reasonText = findViewById(R.id.editReason);
        aloneButton = findViewById(R.id.add_mood_alone_button);
        pairButton = findViewById(R.id.add_mood_pair_button);
        groupButton = findViewById(R.id.add_mood_group_button);
        dropdownList = findViewById(R.id.edit_mood_select_mood_list);

        // Get existing mood data from Intent
        Intent intent = getIntent();
        moodId = intent.getStringExtra("moodId");
        chosenMood = intent.getStringExtra("mood");
        chosenSituation = intent.getStringExtra("situation");
        String reason = intent.getStringExtra("reason");

        // Pre-fill fields with existing data
        if (chosenMood != null) moodDropdown.setText(chosenMood);
        if (reason != null) reasonText.setText(reason);

        // Setup dropdown menu
        ArrayList<String> moodList = new ArrayList<>();
        moodList.add("Anger");
        moodList.add("Confusion");
        moodList.add("Disgust");
        moodList.add("Fear");
        moodList.add("Happiness");
        moodList.add("Sadness");
        moodList.add("Shame");
        moodList.add("Surprise");

        dropdownAdapter = new MoodSelectionAdapter(this, moodList);
        dropdownList.setAdapter(dropdownAdapter);

        // Dropdown button click listener
        moodDropdown.setOnClickListener(v -> {
            if (dropdownStatus) {
                dropdownList.setVisibility(View.GONE);
                aloneButton.setVisibility(View.VISIBLE);
                pairButton.setVisibility(View.VISIBLE);
                groupButton.setVisibility(View.VISIBLE);
                doneButton.setVisibility(View.VISIBLE);
            } else {
                dropdownList.setVisibility(View.VISIBLE);
                dropdownList.bringToFront();
                aloneButton.setVisibility(View.INVISIBLE);
                pairButton.setVisibility(View.INVISIBLE);
                groupButton.setVisibility(View.INVISIBLE);
                doneButton.setVisibility(View.INVISIBLE);
            }
            dropdownStatus = !dropdownStatus;
        });

        // Handle mood selection
        dropdownList.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            chosenMood = moodList.get(position);
            dropdownStatus = false;
            dropdownList.setVisibility(View.GONE);
            aloneButton.setVisibility(View.VISIBLE);
            pairButton.setVisibility(View.VISIBLE);
            groupButton.setVisibility(View.VISIBLE);
            doneButton.setVisibility(View.VISIBLE);
            moodDropdown.setText(chosenMood);
        });

        // Social situation button click listeners
        aloneButton.setOnClickListener(v -> {
            chosenSituation = "Alone";
            highlightSelectedSituation("Alone");
        });
        pairButton.setOnClickListener(v -> {
            chosenSituation = "Pair";
            highlightSelectedSituation("Pair");
        });
        groupButton.setOnClickListener(v -> {
            chosenSituation = "Group";
            highlightSelectedSituation("Group");
        });

        // Save changes to Firestore when clicking "Done"
        doneButton.setOnClickListener(v -> updateMoodInFirestore());
        // Close activity without saving when clicking "Cancel"
        cancelButton.setOnClickListener(v -> finish());
    }
    /**
     * Highlights the selected social situation button and resets others.
     */
    private void highlightSelectedSituation(String situation) {
        aloneButton.setSelected("Alone".equals(situation));
        pairButton.setSelected("Pair".equals(situation));
        groupButton.setSelected("Group".equals(situation));
    }
    /**
     * Updates the mood post in Firestore.
     */
    private void updateMoodInFirestore() {
        // Prevent saving if the mood is not selected
        if (chosenMood == null || chosenMood.isEmpty()) {
            Toast.makeText(this, "Please select a mood before saving.", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("mood", chosenMood);

        // Only add fields to Firestore if they are not empty
        if (chosenSituation != null && !chosenSituation.isEmpty()) {
            updatedData.put("situation", chosenSituation);
        }
        if (reasonText.getText() != null && !reasonText.getText().toString().trim().isEmpty()) {
            updatedData.put("reason", reasonText.getText().toString().trim());
        }
        if (location != null && !location.isEmpty()) {
            updatedData.put("location", location);
        }
        if (image != null && !image.isEmpty()) {
            updatedData.put("imageUrl", image);
        }

        // Update Firestore
        db.collection("moods").document(moodId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Mood updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update mood. Please try again.", Toast.LENGTH_SHORT).show()
                );
    }
}
