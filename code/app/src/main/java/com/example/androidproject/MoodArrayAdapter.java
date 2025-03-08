package com.example.androidproject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MoodArrayAdapter extends ArrayAdapter<MoodState> {

    public MoodArrayAdapter(Context context, ArrayList<MoodState> moods) {
        super(context, 0, moods);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.my_mood_event, parent, false);
        } else {
            view = convertView;
        }

        MoodState moodState = getItem(position);


        TextView moodTextView = view.findViewById(R.id.text_mood);
        TextView dateTextView = view.findViewById(R.id.text_date);
        ImageView imageView = view.findViewById(R.id.image_mood);


        if (moodState != null) {
            String state = "State: " + moodState.getMood();
            String date = moodState.formatDateTime();
            int emoji =  moodState.getEmoji();


            moodTextView.setText(state);
            dateTextView.setText(date);
            imageView.setImageResource(emoji);
        }

        return view;
    }
}

