package com.example.androidproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;

/**
 * Creates an array adapter for the add mood that display the moods
 */
public class MoodArrayAdapter extends ArrayAdapter<MoodState> {
    private String loggedInUser; // The currently logged-in user

    /**
     * For creating the array adapter
     *
     * @param context      The context
     * @param moods        The list of moods to display
     * @param loggedInUser The currently logged-in user
     */
    public MoodArrayAdapter(Context context, ArrayList<MoodState> moods, String loggedInUser) {
        super(context, 0, moods);
        this.loggedInUser = loggedInUser;
    }

    @NonNull
    @Override
    /**
     * Sets the view of the array
     */
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.my_mood_event, parent, false);
        } else {
            view = convertView;
        }

        MoodState moodState = getItem(position);

        // Use view instead of convertView
        AppCompatButton viewMoreButton = view.findViewById(R.id.viewMoreDetails);
        TextView moodTextView = view.findViewById(R.id.text_mood);
        TextView dateTextView = view.findViewById(R.id.text_date);
        TextView usernameTextView = view.findViewById(R.id.mood_event_username);
        ImageView imageView = view.findViewById(R.id.image_mood);

        if (moodState != null) {
            String state = moodState.getMood();
            String date = moodState.formatDateTime();
            int emoji = moodState.getEmoji();
            String moodOwner = moodState.getUser();

            // Set username for the mood
            if (moodOwner != null && !moodOwner.isEmpty()) {
                usernameTextView.setText(moodOwner);

                moodTextView.setText(state);
                dateTextView.setText(date);
                imageView.setImageResource(emoji);

                // Set the text color of moodTextView using the color from MoodState
                String hexColor = "#" + moodState.getColor(); // Prepend '#' to the hex color
                moodTextView.setTextColor(Color.parseColor(hexColor));

                // Set click listener for view more button
                viewMoreButton.setOnClickListener(v -> {
                    Log.d("MoodAdapter", "Opening details for mood: " + moodState.getId() +
                            ", Mood Owner: " + moodOwner +
                            ", Logged in user: " + loggedInUser);

                    Intent intent = new Intent(getContext(), MoodDetailsActivity.class);
                    intent.putExtra("id", moodState.getId());
                    intent.putExtra("user", loggedInUser);
                    intent.putExtra("moodUser", moodOwner);
                    getContext().startActivity(intent);
                });
            }
        }
            return view;
        }
}