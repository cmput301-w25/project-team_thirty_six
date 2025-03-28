package com.example.androidproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;


/**
 * This adapter handles displaying the contents of the followingList into the listView.
 * It is called by the FollowingListFragment in order to show each list_view item.
 * It contains an onClick method for clicking on a user, opening their profile.
 * It contains an OnClick method for unfollowing a user.
 */
public class FollowingListAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> followingList;
    private User currentUser;
    private UserManager userManager;

    /**
     * Constructor for the FollowingListAdapter. calls super and sets the local variables to the arguments provided
     * @param context The context in which this is called
     * @param followingList an ArrayList of strings of the users that currentUser is following.
     * @param currentUser the currentUser object that is currently signed in.
     */
    public FollowingListAdapter(Context context, List<String> followingList, User currentUser){
        super(context, 0, followingList);
        this.context = context;
        this.followingList = followingList;
        this.userManager = new UserManager(getContext());
        this.currentUser = userManager.getCurrentUser();


    }

    /**
     * getView function that belongs to the ArrayAdapter class
     * Handles the unfollowButton onClick.
     * Handles clicking on a user's profile by starting OtherProfileActivity.
     * @param position The position within the listView (index)
     * @param convertView Helps with recycling the view instead of inflating one each time.
     * @param parent The parent view (listView)
     * @return The view that will be used by the listView to display the followingList
     */
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.fragment_following_content, parent, false);
        }
        // Get following username
        String followingUsername = followingList.get(position);

        // Get the textView from the fragment_following_content.xml file.
        TextView followingUsernameTextView = view.findViewById(R.id.username_following_text_view);

        followingUsernameTextView.setText(followingUsername);

        followingUsernameTextView.setOnClickListener(view1 -> {
            //Start the OtherProfileActivity with the necessary variables passed to the intent
            Intent intent = new Intent(context, OtherProfileActivity.class);
            intent.putExtra("otherUser", followingUsername);
            intent.putExtra("currentUser", currentUser.getUsername());
            context.startActivity(intent);
        });

        // Get the unfollow button and setup its onCLick function
        Button unfollowButton = view.findViewById(R.id.unfollow_button);
        unfollowButton.setOnClickListener(view1-> {
            userManager.unfollowUser(currentUser.getUsername(), followingUsername);
            currentUser.removeFollowing(followingUsername);
            notifyDataSetChanged();



        });

        return view;
    }
}