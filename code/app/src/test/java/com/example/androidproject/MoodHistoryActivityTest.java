package com.example.androidproject;


import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;



public class MoodHistoryActivityTest {

    @Mock
    private MoodHistoryManager mockMoodHistoryManager;

    @Mock
    private ListView mockMoodListView;

    @Mock
    private MoodArrayAdapter mockMoodAdapter;

    private MoodHistoryActivity moodHistoryActivity;
    private ArrayList<MoodState> testMoodHistory;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        moodHistoryActivity = new MoodHistoryActivity();
        moodHistoryActivity.moodHistoryManager = mockMoodHistoryManager;
        moodHistoryActivity.moodListView = mockMoodListView;
        moodHistoryActivity.moodAdapter = mockMoodAdapter;

        // Initialize test data
        testMoodHistory = new ArrayList<>();
        MoodState mood1 = new MoodState("Happy");
        mood1.setDayTime(LocalDateTime.now());
        mood1.setReason("Feeling great!");

        MoodState mood2 = new MoodState("Sad");
        mood2.setReason("Feeling down");
        mood2.setDayTime(LocalDateTime.now().minusDays(1));

        testMoodHistory.add(mood1);
        testMoodHistory.add(mood2);

        // Set up the activity with an intent
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MoodHistoryActivity.class);
        intent.putExtra("currentUser", "testUser");
        moodHistoryActivity.setIntent(intent);
    }

    @Test
    public void testFetchMoodHistory() {
        // Mock the callback behavior
        doAnswer(invocation -> {
            MoodHistoryManager.MoodHistoryCallback callback = invocation.getArgument(1);
            callback.onCallback(testMoodHistory);
            return null;
        }).when(mockMoodHistoryManager).fetchMoodHistory(anyString(), any(MoodHistoryManager.MoodHistoryCallback.class));

        // Call the method under test
        moodHistoryActivity.fetchMoodHistory("testUser");

        // Verify that the mood history was updated
        verify(mockMoodAdapter).clear();
        verify(mockMoodAdapter).addAll(testMoodHistory);
        verify(mockMoodAdapter, times(2)).notifyDataSetChanged();
    }

    @Test
    public void testSortMoodHistory() {
        // Set up test data
        moodHistoryActivity.moodHistory = new ArrayList<>(testMoodHistory);
        moodHistoryActivity.completeMoodHistory = new ArrayList<>(testMoodHistory);

        // Call the method under test
        moodHistoryActivity.sortMoodHistory();

        // Verify that the mood history is sorted in reverse chronological order
        ArrayList<MoodState> sortedMoodHistory = new ArrayList<>(testMoodHistory);
        Collections.sort(sortedMoodHistory, (m1, m2) -> m2.getDayTime().compareTo(m1.getDayTime()));

        assertEquals(sortedMoodHistory, moodHistoryActivity.moodHistory);
        assertEquals(sortedMoodHistory, moodHistoryActivity.completeMoodHistory);
        verify(mockMoodAdapter, times(2)).notifyDataSetChanged();
    }

    @Test
    public void testFilterByRecentWeek() {
        // Set up test data
        moodHistoryActivity.moodHistory = new ArrayList<>(testMoodHistory);

        // Mock the Filter class behavior
        ArrayList<MoodState> filteredMoods = new ArrayList<>();
        filteredMoods.add(testMoodHistory.get(0));
        when(Filter.filterByRecentWeek(testMoodHistory)).thenReturn(filteredMoods);

        // Call the method under test
        moodHistoryActivity.filterByRecentWeek();

        // Verify that the adapter was updated with the filtered list
        verify(mockMoodAdapter).clear();
        verify(mockMoodAdapter).addAll(filteredMoods);
        verify(mockMoodAdapter).notifyDataSetChanged();
    }

    @Test
    public void testFilterByEmotionalState() {
        // Set up test data
        moodHistoryActivity.moodHistory = new ArrayList<>(testMoodHistory);

        // Mock the Filter class behavior
        ArrayList<MoodState> filteredMoods = new ArrayList<>();
        filteredMoods.add(testMoodHistory.get(0));
        when(Filter.filterByEmotionalState(testMoodHistory, "Happy")).thenReturn(filteredMoods);

        // Call the method under test
        moodHistoryActivity.filterByEmotionalState("Happy");

        // Verify that the adapter was updated with the filtered list
        verify(mockMoodAdapter).clear();
        verify(mockMoodAdapter).addAll(filteredMoods);
        verify(mockMoodAdapter).notifyDataSetChanged();
    }

    @Test
    public void testFilterByKeyword() {
        // Set up test data
        moodHistoryActivity.moodHistory = new ArrayList<>(testMoodHistory);

        // Mock the Filter class behavior
        ArrayList<MoodState> filteredMoods = new ArrayList<>();
        filteredMoods.add(testMoodHistory.get(0));
        when(Filter.filterByKeyword(testMoodHistory, "great")).thenReturn(filteredMoods);

        // Call the method under test
        moodHistoryActivity.filterByKeyword("great");

        // Verify that the adapter was updated with the filtered list
        verify(mockMoodAdapter).clear();
        verify(mockMoodAdapter).addAll(filteredMoods);
        verify(mockMoodAdapter).notifyDataSetChanged();
    }

    @Test
    public void testDisplayAllMoods() {
        // Set up test data
        moodHistoryActivity.moodHistory = new ArrayList<>();
        moodHistoryActivity.completeMoodHistory = new ArrayList<>(testMoodHistory);

        // Call the method under test
        moodHistoryActivity.displayAllMoods();

        // Verify that the adapter was updated with the complete list
        verify(mockMoodAdapter).clear();
        verify(mockMoodAdapter).addAll(testMoodHistory);
        verify(mockMoodAdapter).notifyDataSetChanged();
    }
}