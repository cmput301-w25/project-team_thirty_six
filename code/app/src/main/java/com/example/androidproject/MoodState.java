package com.example.androidproject;

import android.location.Location;
import android.net.Uri;

import com.google.firebase.firestore.GeoPoint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates an abstract emotion class that all of the emotions extend
 */
public class MoodState {
    protected String id;
    protected String username;
    protected String mood;
    protected ArrayList<String> moodList;
    // Stores the hex code of the color
    protected String color;
    // Stores the id of the emoji in drawable
    protected int emoji;

    protected LocalDateTime dayTime;
    // All underneath are nullable
    protected String situation;
    protected String reason;
    protected Uri image;
    protected Location location;
    protected Boolean visibility;

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
        this.dayTime = LocalDateTime.now();
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

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
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

    public LocalDate getDay() {
        return dayTime.toLocalDate();
    }

    public void setDayTime(LocalDateTime day) {
        this.dayTime = day;
    }

    public LocalTime getTime() {
        return dayTime.toLocalTime();
    }

    public LocalDateTime getDayTime() {
        return dayTime;
    }
    public String formatDateTime() {
        // Define the format you'd like, for example: "yyyy-MM-dd HH:mm:ss"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dayTime.format(formatter); // Directly format the LocalDateTime
    }
    public String getUser() {
        return username;
    }

    public void setUser(String user) {
        this.username = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Convert MoodState to a Map for Firestore

    /**
     * Takes a mood and turns it into a map of values
     * @return
     * the map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("username", username);
        map.put("mood", mood);
        map.put("color", color);
        map.put("emoji", emoji);
        map.put("dayTime", dayTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // Convert to String
        map.put("situation", situation);
        map.put("reason", reason);
        map.put("image", image != null ? image.toString() : null); // Convert Uri to String
        if (location != null) {
            Map<String, Double> locationMap = new HashMap<>();
            locationMap.put("latitude", location.getLatitude());
            locationMap.put("longitude", location.getLongitude());
            map.put("location", locationMap);
        } else {
            map.put("location", null);
        }
        return map;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }
}
