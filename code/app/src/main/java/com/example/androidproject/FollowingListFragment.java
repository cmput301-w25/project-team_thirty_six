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


/**
 * The purpose of this fragment is to handle the listView Fragment for the following list.
 * Relies on the FollowingListAdapter.
 * Displays the listView for following and defines a backButtonOnClick method.
 */
public class FollowingListFragment extends Fragment {

    private ListView listView;
    private FollowingListAdapter followingListAdapter;
    private List<String> followingList;
    private User currentUser;


    /**
     * The onCreate method for the followingListFragment, sets up a backButton on click listener
     * and opens the fragment with the populated data with help from the followingListAdapter
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The view that will contain the listView and that will be used by ProfileActivity
     */
    // This onCreate Frame was taken from ChatGPT by Rhiyon Naderi on March 22nd 2025.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following_list, container, false);

        // end of taken code

        ImageButton backButton = view.findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> {

            // Setup the follower string
            // User currentUser = (User) this.getArguments().getSerializable("currentUser");
            // int followerCount = UserManager.getCurrentUser().getFollowers().size();
            int followerCount = currentUser.getFollowers().size();
            int followingCount = currentUser.getFollowing().size();
            String followersString = String.format("%d followers", followerCount);
            String followingString = String.format("%d following", followingCount);

            // Display the following string
            TextView followingTextView = requireActivity().findViewById(R.id.followingAmountTextView);
            followingTextView.setText(followingString);

            // Display the follower string
            TextView followersTextView = requireActivity().findViewById(R.id.followerAmountTextView);
            followersTextView.setText(followersString);

            requireActivity().getSupportFragmentManager().popBackStack();



        });

        listView = view.findViewById(R.id.following_list_view);
        assert listView != null;
        assert this.getArguments() != null;
        this.currentUser = (User) this.getArguments().getSerializable("currentUser");
        assert currentUser != null;
        followingList = currentUser.getFollowing();
        followingListAdapter = new FollowingListAdapter(getContext(), followingList, currentUser);
        listView.setAdapter(followingListAdapter);
        return view;


    }
}