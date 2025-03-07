package com.example.androidproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import android.graphics.Bitmap;
import android.graphics.Picture;
import android.location.Location;
import android.location.LocationManager;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Runs tests for the mood state class
 */
public class MoodStateTest {

    /**
     * Tests when an invalid mood is given that it throws an argument error
     */
    @Test
    public void testInvalidMood(){
        assertThrows(IllegalArgumentException.class, () -> {
            new MoodState("Cowardice");
        });
    }

    /**
     * Tests every mood to see if the deliver the proper emoji
     */
    @Test
    public void testMoodEmoji(){
        MoodState moodStateAnger = new MoodState("Anger");
        assertEquals(moodStateAnger.getEmoji(),R.drawable.anger);

        MoodState moodStateConfusion = new MoodState("Confusion");
        assertEquals(moodStateConfusion.getEmoji(),R.drawable.confusion);

        MoodState moodStateDisgust = new MoodState("Disgust");
        assertEquals(moodStateDisgust.getEmoji(),R.drawable.disgust);

        MoodState moodStateFear = new MoodState("Fear");
        assertEquals(moodStateFear.getEmoji(),R.drawable.fear);

        MoodState moodStateHappiness = new MoodState("Happiness");
        assertEquals(moodStateHappiness.getEmoji(),R.drawable.happiness);

        MoodState moodStateSadness = new MoodState("Sadness");
        assertEquals(moodStateSadness.getEmoji(),R.drawable.sadness);

        MoodState moodStateSurprise = new MoodState("Surprise");
        assertEquals(moodStateSurprise.getEmoji(),R.drawable.surprise);

        MoodState moodStateShame = new MoodState("Shame");
        assertEquals(moodStateShame.getEmoji(),R.drawable.shame);

    }

    /**
     * Tests every mood to see if the deliver the proper color
     */
    @Test
    public void testMoodColor(){
        MoodState moodStateAnger = new MoodState("Anger");
        assertEquals(moodStateAnger.getColor(),"FF0004");

        MoodState moodStateConfusion = new MoodState("Confusion");
        assertEquals(moodStateConfusion.getColor(),"BB00FF");

        MoodState moodStateDisgust = new MoodState("Disgust");
        assertEquals(moodStateDisgust.getColor(),"2CFC03");

        MoodState moodStateFear = new MoodState("Fear");
        assertEquals(moodStateFear.getColor(),"B4EBF2");

        MoodState moodStateHappiness = new MoodState("Happiness");
        assertEquals(moodStateHappiness.getColor(),"EEFF00");

        MoodState moodStateSadness = new MoodState("Sadness");
        assertEquals(moodStateSadness.getColor(),"0000FF");

        MoodState moodStateSurprise = new MoodState("Surprise");
        assertEquals(moodStateSurprise.getColor(),"F3AB32");

        MoodState moodStateShame = new MoodState("Shame");
        assertEquals(moodStateShame.getColor(),"F5A8C2");
    }

    /**
     * Tests the getters and setters for all of them
     */
    @Test
    public void testGettersAndSetters(){
        MoodState moodState = new MoodState("Happiness");
        assertEquals(moodState.getMood(),"Happiness");

        LocalDateTime newDate = LocalDateTime.now();
        moodState.setDayTime(newDate);
        assertEquals(moodState.getDay(),newDate.toLocalDate());
        assertEquals(moodState.getTime(),newDate.toLocalTime());

//        String newTrigger = "Loud Noises";
//        moodState.setTrigger(newTrigger);
//        assertEquals(moodState.getTrigger(),"Loud Noises");

        String newSituation = "Alone";
        moodState.setSituation(newSituation);
        assertEquals(moodState.getSituation(),"Alone");

        String newReason = "Dropped a large pan";
        moodState.setReason(newReason);
        assertEquals(moodState.getReason(),"Dropped a large pan");

    }

}
