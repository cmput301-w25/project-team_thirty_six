package com.example.androidproject;

import android.content.Context;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Creates an array adapter to display the mood dropdown
 */
public class MoodSelectionAdapter extends ArrayAdapter<String> {
    /**
     * Initializes the adapter
     * @param context
     * @param moods
     */
    public MoodSelectionAdapter(Context context, ArrayList<String> moods) {
        super(context, 0, moods);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.select_mood_layout, parent, false);
        } else {
            view = convertView;
        }
        // Gets the item from the list
        String mood = getItem(position);
        // Gets the respective views
        TextView moodName = view.findViewById(R.id.select_mood_text);
        ImageView moodImage = view.findViewById(R.id.select_mood_emoji);
        moodName.setText(mood);
        // Sets the emoji to draw
        switch (mood) {
            case "Anger":
                moodImage.setImageResource(R.drawable.anger);
                break;
            case "Confusion":
                moodImage.setImageResource(R.drawable.confusion);
                break;
            case "Disgust":
                moodImage.setImageResource(R.drawable.disgust);
                break;
            case "Fear":
                moodImage.setImageResource(R.drawable.fear);
                break;
            case "Happiness":
                moodImage.setImageResource(R.drawable.happiness);
                break;
            case "Sadness":
                moodImage.setImageResource(R.drawable.sadness);
                break;
            case "Shame":
                moodImage.setImageResource(R.drawable.shame);
                break;
            case "Surprise":
                moodImage.setImageResource(R.drawable.surprise);
                break;
        }
        return view;

    }
}
