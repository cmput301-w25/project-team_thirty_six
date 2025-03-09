package com.example.androidproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.util.ArrayList;

public class MoodDropdownManager {
    private Context context;
    private Button dropdownButton;
    private PopupWindow popupWindow;
    private MoodSelectionAdapter adapter;
    private ArrayList<String> moodList;
    private MoodSelectedListener listener;

    // Interface for callback
    public interface MoodSelectedListener {
        void onMoodSelected(String mood);
    }

    public MoodDropdownManager(Context context, Button dropdownButton, MoodSelectedListener listener) {
        this.context = context;
        this.dropdownButton = dropdownButton;
        this.listener = listener;

        // Initialize the mood list
        moodList = new ArrayList<>();
        moodList.add("Anger");
        moodList.add("Confusion");
        moodList.add("Disgust");
        moodList.add("Fear");
        moodList.add("Happiness");
        moodList.add("Sadness");
        moodList.add("Shame");
        moodList.add("Surprise");

        adapter = new MoodSelectionAdapter(context, moodList);
        setupPopupWindow();
    }

    private void setupPopupWindow() {
        // Inflate the popup layout from XML
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.mood_dropdown_popup, null);

        // Get the ListView from the inflated layout
        ListView listView = popupView.findViewById(R.id.mood_dropdown_list);
        listView.setAdapter(adapter);

        // Set item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedMood = moodList.get(position);
                dropdownButton.setText(selectedMood);
                if (listener != null) {
                    listener.onMoodSelected(selectedMood);
                }
                popupWindow.dismiss();
            }
        });

        // Create popup window
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Make it dismissable when clicking outside
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
    }

    public void showDropdown() {
        // Measure the button if width is zero
        if (dropdownButton.getWidth() == 0) {
            dropdownButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            popupWindow.setWidth(dropdownButton.getMeasuredWidth());
        }

        // Show the popup below the button
        popupWindow.showAsDropDown(dropdownButton);
    }

    public void hideDropdown() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }
}