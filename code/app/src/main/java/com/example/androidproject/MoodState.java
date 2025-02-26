package com.example.androidproject;

import android.graphics.Bitmap;
import android.location.Location;

import java.util.ArrayList;
import java.util.Date;

/**
 * Creates an abstract emotion class that all of the emotions extend
 */
public class MoodState {
    protected String mood;
    protected ArrayList<String> moodList;
    // Stores the hex code of the color
    protected String color;
    // Stores the id of the emoji in drawable
    protected int emoji;

    protected Date dayTime;
    // All underneath are nullable
    protected String trigger;
    protected String situation;
    protected String reason;
    protected Bitmap image;
    protected Location location;

    /**
     * Adds all the moods to the mood list for simplicity
     */
    private void FillMoodList(){
        moodList = new ArrayList<>();
        moodList.add("Anger");
        moodList.add("Confusion");
        moodList.add("Disgust");
        moodList.add("Fear");
        moodList.add("Happiness");
        moodList.add("Sadness");
        moodList.add("Shame");
        moodList.add("Surprise");
    }

    /**
     * Maps mood types to colors
     */
    private String MapMoodToColor(String mood){
        // Returns the color that the mood should be by switching between them
        switch (mood) {
            case "Anger":
                return "FF0004";
            case "Confusion":
                return "BB00FF";
            case "Disgust":
                return "2CFC03";
            case "Fear":
                return "B4EBF2";
            case "Happiness":
                return "EEFF00";
            case "Sadness":
                return "0000FF";
            case "Shame":
                return "F5A8C2";
            case "Surprise":
                return "F3AB32";
        }
        // Base case that never activates
        return new String();
    }

    /**
     * Maps mood types to emojis
     */
    private int MapMoodToEmoji(String mood){
        // Returns the integer id of the emoji (Found in drawable)
        switch (mood) {
            case "Anger":
                return R.drawable.anger;
            case "Confusion":
                return R.drawable.confusion;
            case "Disgust":
                return R.drawable.disgust;
            case "Fear":
                return R.drawable.fear;
            case "Happiness":
                return R.drawable.happiness;
            case "Sadness":
                return R.drawable.sadness;
            case "Shame":
                return R.drawable.shame;
            case "Surprise":
                return R.drawable.surprise;
        }
        // Base case that never activates
        return 0;
    }

    /**
     * Creates a constructor for mood state
     */
    public MoodState(String mood) {
        FillMoodList();
        // Throws an exception if given an invalid mood
        if (!moodList.contains(mood)){
            throw new IllegalArgumentException();
        }
        this.mood = mood;
        this.color = MapMoodToColor(mood);
        this.emoji = MapMoodToEmoji(mood);
        // Gets the day and time
        this.dayTime = new Date();
    }


    public Date getDayTime() {
        return dayTime;
    }

    public void setDayTime(Date dayTime) {
        this.dayTime = dayTime;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getSituation() {
        return situation;
    }

    public void setSituation(String situation) {
        this.situation = situation;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getMood() {
        return mood;
    }

    public String getColor() {
        return color;
    }

    public int getEmoji() {
        return emoji;
    }
}
