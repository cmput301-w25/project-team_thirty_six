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
 * This adapter handles the individual followers.
 * It is called by the FollowerListFragment in order to show each list_view item.
 * It contains an onClick method for clicking on a user, opening their profile.
 * It contains an OnClick method for removing a follower. 
 */
public class FollowerListAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> followerList;
    private User currentUser;
    private UserManager userManager;

    /**
     * Constructor for the FollowerListAdapter. calls super and sets the local variables to the arguments provided
     * @param context The context in which this is called
     * @param followerList an ArrayList of strings of the currentUser's followers
     * @param currentUser the currentUser object that is currently signed in.
     */
    public FollowerListAdapter(Context context, List<String> followerList, User currentUser){
        super(context, 0, followerList);
        this.context = context;
        this.followerList = followerList;
        this.userManager = new UserManager(getContext());
        this.currentUser = userManager.getCurrentUser();


    }

    /**
     * getView function that belongs to the ArrayAdapter class
     * Handles the removeFollowerButton onClick logic with the help of the userManager class.
     * Handles clicking on a user's profile by starting OtherProfileActivity.
     * @param position The position within the listView (index)
     * @param convertView Helps with recycling the view instead of inflating one each time.
     * @param parent The parent view (listView)
     * @return The view that will be used by the listView to display the followerList
     */
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.fragment_follower_list_content, parent, false);
        }
        // Get follower username
        String followerUsername = followerList.get(position);

        // Get the textView from the fragment_follower_list_content.xml file.
        TextView followerUsernameTextView = view.findViewById(R.id.username_follower_text_view);

        followerUsernameTextView.setText(followerUsername);

        followerUsernameTextView.setOnClickListener(view1 -> {
            //Start the OtherProfileActivity with the necessary variables passed to the intent
            Intent intent = new Intent(context, OtherProfileActivity.class);
            intent.putExtra("otherUser", followerUsername);
            intent.putExtra("currentUser", currentUser.getUsername());
            context.startActivity(intent);
        });

        // Get the remove button
        Button removeFollowerButton = view.findViewById(R.id.remove_follower_button);
        removeFollowerButton.setOnClickListener(view1 -> {
            userManager.unfollowUser(followerUsername, currentUser.getUsername());
            currentUser.removeFollowers(followerUsername);
            notifyDataSetChanged();
        });

        return view;
    }
}