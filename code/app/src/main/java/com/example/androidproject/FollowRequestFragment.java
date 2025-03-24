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

public class FollowRequestFragment extends Fragment {

    private ListView listView;
    private FollowRequestAdapter followRequestAdapter;
    private List<String> followRequestList;



    // This onCreate Frame was taken from ChatGPT by Rhiyon Naderi on March 22nd 2025.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follow_requests, container, false);

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

        listView = view.findViewById(R.id.follow_request_list_view);
        assert listView != null;
        assert this.getArguments() != null;
        User currentUser = (User) this.getArguments().getSerializable("currentUser");
        assert currentUser != null;
        followRequestList = currentUser.getFollowRequests();
        followRequestAdapter = new FollowRequestAdapter(getContext(), followRequestList, currentUser);
        listView.setAdapter(followRequestAdapter);
        return view;








    }
}
