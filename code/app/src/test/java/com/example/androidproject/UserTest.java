package com.example.androidproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 *  Runs a test to ensure all user functions are valid
 */
public class UserTest {
    /**
     * Test the creation of a new user
     */
    @Test
    public void testUserCreation(){
        User newUser = new User("Test","Passtest");
        assertEquals(newUser.getUsername(),"Test");
        assertEquals(newUser.getPassword(),"Passtest");
        assertEquals(newUser.getFollowing().size(),0);
    }

    /**
     * Tests the adding of moods to a user
     */
    @Test
    public void addMoodTest() {
        User newUser = new User("Test","Passtest");

        // Tests for a first mood
        MoodState mood1 = new MoodState("Happiness");
        newUser.addMood(mood1);
        assertEquals(mood1,newUser.getMostRecentMood());
        assertTrue(newUser.getMoodHistory().contains(mood1));
        assertEquals(newUser.getMoodHistory().size(),1);

        // Tests for a second mood
        MoodState mood2 = new MoodState("Sadness");
        newUser.addMood(mood2);
        assertEquals(mood2,newUser.getMostRecentMood());
        assertTrue(newUser.getMoodHistory().contains(mood2));
        assertEquals(newUser.getMoodHistory().size(),2);

        // Tests for a third mood
        MoodState mood3 = new MoodState("Anger");
        newUser.addMood(mood3);
        assertEquals(mood3,newUser.getMostRecentMood());
        assertTrue(newUser.getMoodHistory().contains(mood3));
        assertEquals(newUser.getMoodHistory().size(),3);
    }

    /**
     * Tests the deleting of moods to a user
     */
    @Test
    public void deleteMoodTest() {
        User newUser = new User("Test","Passtest");

        // Adds a first mood
        MoodState mood1 = new MoodState("Happiness");
        newUser.addMood(mood1);


        //Makes the times not equal each other
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Adds a second mood
        MoodState mood2 = new MoodState("Sadness");
        newUser.addMood(mood2);

        //Makes the times not equal each other
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Adds a third mood for a third mood
        MoodState mood3 = new MoodState("Anger");
        newUser.addMood(mood3);

        assertEquals(mood3,newUser.getMostRecentMood());

        //Deletes a user
        newUser.deleteMood(mood3);
        assertEquals(mood2,newUser.getMostRecentMood());
        assertFalse(newUser.getMoodHistory().contains(mood3));
        assertEquals(newUser.getMoodHistory().size(),2);

    }

    /**
     * Tests invalid delete
     */
    @Test
    public void deleteInvalidMoodTest() {
        User newUser = new User("Test","Passtest");

        // Creates a first mood
        MoodState mood1 = new MoodState("Happiness");
        //Attempts to delete it
        assertThrows(IllegalArgumentException.class, () -> {
            newUser.deleteMood(mood1);
        });

    }

    /**
     * Tests the adding of followers to a user
     */
    @Test
    public void addFollowerTest() {
        User newUser = new User("Test","Passtest");

        // Tests adding a follower
        String newerUser = "Test2";
        newUser.addFollowing(newerUser);
        assertTrue(newUser.getFollowing().contains(newerUser));
        assertEquals(newUser.getFollowing().size(),1);

        // Tests adding a second follower
        String newestUser = "Test3";
        newUser.addFollowing(newestUser);
        assertTrue(newUser.getFollowing().contains(newestUser));
        assertEquals(newUser.getFollowing().size(),2);
    }

    /**
     * Tests the deleting of followers from a user
     */
    @Test
    public void deleteFollowerTest() {
        User newUser = new User("Test","Passtest");

        // Tests adding a follower
        String newerUser = "Test2";
        newUser.addFollowing(newerUser);

        // Tests adding a second follower
        String newestUser = "Test3";
        newUser.addFollowing(newestUser);

        assertEquals(newUser.getFollowing().size(),2);
        // Deletes the first following
        newUser.removeFollowing(newerUser);
        assertFalse(newUser.getFollowing().contains(newerUser));
        assertEquals(newUser.getFollowing().size(),1);

        // Deletes the second following
        newUser.removeFollowing(newestUser);
        assertFalse(newUser.getFollowing().contains(newestUser));
        assertEquals(newUser.getFollowing().size(),0);
    }

    /**
     * Tests the deletion of an invalid follower
     */
    @Test
    public void deleteInvalidFollowerTest() {
        User newUser = new User("Test", "Passtest");
        assertThrows(IllegalArgumentException.class, () -> {
            newUser.removeFollowing("HHFSDFSDG");
        });
    }

}
