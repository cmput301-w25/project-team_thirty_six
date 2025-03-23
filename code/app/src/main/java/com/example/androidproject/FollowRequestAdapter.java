package com.example.androidproject;

import android.content.Context;
import android.graphics.Movie;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * This adapter handles the individual follow requests.
 * It is called by the FollowRequestFragment in order to show each list_view item.
 */
public class FollowRequestAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> requestList;
    private User currentUser;
    private UserManager userManager;
    public FollowRequestAdapter(Context context, List<String> requestList, User currentUser){
        super(context, 0, requestList);
        this.context = context;
        this.requestList = requestList;
        this.currentUser = currentUser;
        this.userManager = new UserManager(getContext());

    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.fragment_follow_request_content, parent, false);
        }
        // Get requester username
        String currentRequesterUsername = requestList.get(position);

        // Get the buttons and textViews from the fragment_follow_request_content.xml file.
        TextView requesterUsername = view.findViewById(R.id.username_follow_request_text_view);
        ImageButton acceptButton = view.findViewById(R.id.accept_button);
        ImageButton rejectButton = view.findViewById(R.id.cancel_button);

        requesterUsername.setText(currentRequesterUsername + " wants to follow you");

        // On click method to accept a follow request
        // It calls the user manager function to handle accepting follow requests
        acceptButton.setOnClickListener(view1 ->{
            String currentUsername = currentUser.getUsername();
            userManager.acceptFollowRequest(currentUsername, currentRequesterUsername);
            currentUser.getFollowRequests().remove(currentRequesterUsername);
            notifyDataSetChanged();
        });


        // On click method to reject a follow request
        // It calls the user manager function to handle accepting follow requests
        rejectButton.setOnClickListener(view1 -> {
            String currentUsername = currentUser.getUsername();
            userManager.rejectFollowRequest(currentUsername, currentRequesterUsername);
            currentUser.getFollowRequests().remove(currentRequesterUsername);
            notifyDataSetChanged();

        });

        return view;
    }
}