package com.example.androidproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;
    private Context context;
    private String currentUser;

    /**
     * Constructor for UserAdapter
     *
     * @param users List of user objects to display
     * @param context The activity context
     */
    public UserAdapter(List<User> users, Context context, String currentUser) {
        this.users = users;
        this.context = context;
        this.currentUser = currentUser;
    }

    /**
     * Called when RecyclerView needs new UserViewHolder
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return new instance of UserViewHolder
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_item, parent, false);
        return new UserViewHolder(view);
    }

    /**
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.username.setText(user.getUsername());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OtherProfileActivity.class);
            intent.putExtra("otherUser", user.getUsername());
            intent.putExtra("currentUser", currentUser);
            context.startActivity(intent);
        });
    }

    /**
     * Returns total number of items in data
     * @return Size of user list
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder class for user items in the RecyclerView
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username;

        public UserViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.search_username_popup);
        }
    }
}