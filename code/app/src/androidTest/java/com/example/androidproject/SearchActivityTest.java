package com.example.androidproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.widget.EditText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for {@link SearchActivity} using Espresso.
 * Tests user interaction with the SearchView and search result behavior.
 */
@RunWith(AndroidJUnit4.class)
public class SearchActivityTest {

    /**
     * Launches {@link SearchActivity} before each test and closes it after.
     */
    @Rule
    public ActivityScenarioRule<SearchActivity> activityRule =
            new ActivityScenarioRule<>(SearchActivity.class);

    /**
     * Tests that typing a valid username into the SearchView
     * shows a matching result on screen.
     */
    @Test
    public void testSearchInputUpdatesResults() {
        onView(withId(R.id.searchView)).perform(click());

        onView(isAssignableFrom(EditText.class))
                .perform(typeText("testUser"), closeSoftKeyboard());

        onView(allOf(
                withText("testUser"),
                isDescendantOfA(withId(R.id.recyclerView))
        )).check(matches(isDisplayed()));
    }

    /**
     * Tests that typing a non-existent username into the SearchView
     * results in an empty RecyclerView (no results shown).
     */
    @Test
    public void testSearchForUserThatDoesNotExist() {
        onView(withId(R.id.searchView)).perform(click());

        onView(isAssignableFrom(EditText.class))
                .perform(typeText("user_does_not_exist"), closeSoftKeyboard());

        onView(withId(R.id.recyclerView))
                .check(matches(hasChildCount(0)));
    }
}