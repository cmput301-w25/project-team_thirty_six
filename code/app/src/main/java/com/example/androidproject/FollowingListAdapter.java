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
 * This adapter handles the individual follow requests.
 * It is called by the FollowRequestFragment in order to show each list_view item.
 */
public class FollowingListAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> followingList;
    private User currentUser;
    private UserManager userManager;
    public FollowingListAdapter(Context context, List<String> followingList, User currentUser){
        super(context, 0, followingList);
        this.context = context;
        this.followingList = followingList;
        this.userManager = new UserManager(getContext());
        this.currentUser = userManager.getCurrentUser();


    }

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