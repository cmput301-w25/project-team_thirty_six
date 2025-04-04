package com.example.androidproject;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchActivity allows the user to use a search interface for finding other users profiles
 *
 * This class interacts with the following components:
 * - UserAdapter: Displays the user results in a recycler view
 * - User: Model for the users information
 * - NavBarFragment: Provides navigation capabilities
 */
public class SearchActivity extends AppCompatActivity {
    private SearchView searchView;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseFirestore db;
    private String currentUser;

    /**
     * Initializes the activity, sets up the UI components and event listeners
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Retrieve the currentUser from the Intent
        currentUser = (String) getIntent().getStringExtra("currentUser");

        // Add navbar
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(currentUser))
                    .commit();
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        searchView = findViewById(R.id.searchView);
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            searchIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }

        recyclerView = findViewById(R.id.recyclerView);

        // Initialize list and adapter with empty list
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this, currentUser);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);

        // Setup search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    searchUsers(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    // Search as soon as user starts typing
                    searchUsers(newText);
                } else {
                    // Clear results when search is empty
                    userList.clear();
                    userAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        // Set focus on search view
        searchView.requestFocus();
    }

    /**
     * Search for users whose ID contains the query
     * @param query The search query text entered by the user
     */
    private void searchUsers(String query) {
        // Convert query to lowercase for case-insensitive search
        String lowercaseQuery = query.toLowerCase();

        db.collection("Users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getId();
                            // Case-insensitive contains search
                            if (userId.toLowerCase().contains(lowercaseQuery)) {
                                User user = new User(userId, userId);
                                userList.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(SearchActivity.this,
                                "Error searching users: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}