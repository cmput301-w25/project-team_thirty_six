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

/**
 * Allows for the mood drop down selection
 */
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

    /**
     * Constructor to create the mood drop down manager
     * @param context
     * @param dropdownButton
     * @param listener
     */
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

    /**
     * Creates the pop window
     */
    private void setupPopupWindow() {
        // Inflate the popup layout from XML
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.mood_dropdown_popup, null);

        // Get the ListView from the inflated layout
        ListView listView = popupView.findViewById(R.id.mood_dropdown_list);
        listView.setAdapter(adapter);

        // Set item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Stores the mood that the user selects once it is clicked
             * @param parent The AdapterView where the click happened.
             * @param view The view within the AdapterView that was clicked (this
             *            will be a view provided by the adapter)
             * @param position The position of the view in the adapter.
             * @param id The row id of the item that was clicked.
             */
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

    /**
     * Displays the dropdown
     */
    public void showDropdown() {
        // Measure the button if width is zero
        if (dropdownButton.getWidth() == 0) {
            dropdownButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            popupWindow.setWidth(dropdownButton.getMeasuredWidth());
        }

        // Show the popup below the button
        popupWindow.showAsDropDown(dropdownButton);
    }

    /**
     * Hides the dropdown
     */
    public void hideDropdown() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    /**
     * Returns true if the textbox is showing
     * @return
     */
    public boolean isShowing() {
        return popupWindow.isShowing();
    }
}