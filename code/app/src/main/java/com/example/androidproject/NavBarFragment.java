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

public class NavBarFragment extends Fragment {
    private ImageView btnHome, btnFeed, btnMap, btnProfile, btnCreate;
    private String currentUser;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_bar,
                container, false);



        if (getArguments() != null){
            currentUser = (String) getArguments().getString("currentUser");
            Log.d("NavBarFragment", currentUser);
        }
        Log.d("NavBarFragment", "not current user");



        // Finds all of the button views
        btnHome = view.findViewById(R.id.btn_home);
        btnFeed = view.findViewById(R.id.btn_feed);
        btnMap = view.findViewById(R.id.btn_map);
        btnProfile = view.findViewById(R.id.btn_profile);
        btnCreate = view.findViewById(R.id.btn_create);

        //set on click listeners for each nav bar button
        btnHome.setOnClickListener(v -> openActivity(HomePageActivity.class));
        //btnFeed.setOnClickListener(v -> openActivity(FeedActivity.class));
        //btnMap.setOnClickListener(v -> openActivity(MapActivity.class));
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


