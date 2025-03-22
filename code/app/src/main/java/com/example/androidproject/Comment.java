package com.example.androidproject;

/**
 * Class that stores the details of a comment
 */
public class Comment {
    String username;
    String moodID;
    String text;

    /**
     * Creates a constructor for comments
     * @param username
     * @param moodID
     * @param text
     */
    public Comment(String username, String moodID, String text){
        this.username = username;
        this.moodID = moodID;
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMoodID() {
        return moodID;
    }

    public void setMoodID(String moodID) {
        this.moodID = moodID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
