package com.example.androidproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

import javax.annotation.Nullable;

public class FollowerListFragment extends Fragment {

    private ListView listView;
    private FollowerListAdapter followerListAdapter;
    private List<String> followerList;



    // This onCreate Frame was taken from ChatGPT by Rhiyon Naderi on March 22nd 2025.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follower_list, container, false);

        // end of taken code

        ImageButton backButton = view.findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> {

            // Setup the follower string
            int followerCount = UserManager.getCurrentUser().getFollowers().size();
            String followersString = String.format("%d followers", followerCount);

            // Display the follower string
            TextView followersTextView = requireActivity().findViewById(R.id.followerAmountTextView);
            followersTextView.setText(followersString);

            requireActivity().getSupportFragmentManager().popBackStack();



        });

        listView = view.findViewById(R.id.followers_list_view);
        assert listView != null;
        assert this.getArguments() != null;
        User currentUser = (User) this.getArguments().getSerializable("currentUser");
        assert currentUser != null;
        followerList = currentUser.getFollowers();
        followerListAdapter = new FollowerListAdapter(getContext(), followerList, currentUser);
        listView.setAdapter(followerListAdapter);
        return view;


    }
}