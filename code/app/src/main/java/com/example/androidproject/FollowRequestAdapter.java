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

        TextView requesterUsername = view.findViewById(R.id.username_follow_request_text_view);
        ImageButton acceptButton = view.findViewById(R.id.accept_button);
        ImageButton rejectButton = view.findViewById(R.id.cancel_button);

        requesterUsername.setText(currentRequesterUsername + " wants to follow you");

        acceptButton.setOnClickListener(view1 ->{
            String currentUsername = currentUser.getUsername();
            userManager.acceptFollowRequest(currentUsername, currentRequesterUsername);
        });



        return view;
    }
}