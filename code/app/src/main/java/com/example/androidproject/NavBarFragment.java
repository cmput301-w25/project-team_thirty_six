package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Nav bar fragment functionality
 * The navBar carries the currentUsername into the intents that it opens.
 * Conatins buttons to navigate to the home, search, Create Mood, Map, and Profile page.
 */
public class NavBarFragment extends Fragment {
    private ImageView btnHome, btnSearch, btnMap, btnProfile, btnCreate;
    private String currentUser;

    /**
     * Gets a new instance of the nav bar
     * @param currentUser
     * @return the nav bar
     */
    // The following constructor code was obtained from ChatGPT
    // Prompt: How to have a navBarFragment pass a user class instance between activities in Android Studio Java.
    // Taken by: Rhiyon Naderi
    // Taken on: March 10, 2025
    public static NavBarFragment newInstance(String currentUser) {
        NavBarFragment fragment = new NavBarFragment();
        Bundle args = new Bundle();
        args.putString("currentUser", currentUser);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Runs the info to display nav bar fragment
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param saveInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_bar,
                container, false);



        if (getArguments() != null){
            currentUser = (String) getArguments().getString("currentUser");
//             Log.d("NavBarFragment", currentUser);
        }
        Log.d("NavBarFragment", "not current user");



        // Finds all of the button views
        btnHome = view.findViewById(R.id.btn_home);
        btnSearch = view.findViewById(R.id.btn_search);
        btnMap = view.findViewById(R.id.btn_map);
        btnProfile = view.findViewById(R.id.btn_profile);
        btnCreate = view.findViewById(R.id.btn_create);

        //set on click listeners for each nav bar button
        btnHome.setOnClickListener(v -> openActivity(HomePageActivity.class));
        btnSearch.setOnClickListener(v -> openActivity(SearchActivity.class));
        btnMap.setOnClickListener(v -> openActivity(LocationMapActivity.class));
        btnProfile.setOnClickListener(v -> openActivity(ProfileActivity.class));
        btnCreate.setOnClickListener(v -> openActivity(CreatePostActivity.class));
        return view;
    }

    /**
     * Opens specified activity from the nav bar
     * Checks if fragment is attached to an activity,
     * then prevents restarting if the user is on already
     * on the acitivity
     * @param activityClass the class of the activity to be opened
     */
    private void openActivity(Class<?> activityClass) {
        if (getActivity() != null) {
            if (getActivity().getClass().equals(activityClass)){
                return;
            }
            Intent intent = new Intent(getActivity(), activityClass);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        }
    }
}


