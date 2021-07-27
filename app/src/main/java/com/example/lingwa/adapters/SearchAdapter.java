package com.example.lingwa.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lingwa.R;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.Post;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private static final String TAG = "SearchAdapter";

    List<ParseUser> userList;
    Context context;
    AdapterCallback callback;

    public SearchAdapter(Context context, List<ParseUser> userList, AdapterCallback callback) {
        this.userList = userList;
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_search_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        ParseUser user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void clear() {
        userList.clear();
    }

    public void addAll(List<ParseUser> list) {
        userList.addAll(list);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvSearchUsername;
        ImageView ivSearchProfile;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvSearchUsername = itemView.findViewById(R.id.tvSearchUsername);
            ivSearchProfile = itemView.findViewById(R.id.ivSearchProfile);
            itemView.setOnClickListener(this::onClick);
        }

        public void bind(ParseUser user) {
            tvSearchUsername.setText(user.getUsername());

            try {
                String profilePictureUrl = user.getParseFile("profilePicture").getUrl();
                Glide.with(context)
                        .load(profilePictureUrl)
                        .circleCrop()
                        .into(ivSearchProfile);
            } catch (NullPointerException e){
                Log.e(TAG, "Error loading user profile picture: " + e.toString());

                Glide.with(context)
                        .load(R.drawable.default_profile_picture)
                        .circleCrop()
                        .into(ivSearchProfile);
            }
        }


        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            callback.onUserSelected(userList.get(position), position);
        }
    }

    public interface AdapterCallback {
        void onUserSelected(ParseUser user, int position);
    }
}
