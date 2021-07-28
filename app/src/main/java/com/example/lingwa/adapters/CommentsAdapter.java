package com.example.lingwa.adapters;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lingwa.R;
import com.example.lingwa.models.Post;

import org.jetbrains.annotations.NotNull;

import java.util.List;

// NOTE: Currently unused, planned to implement into post details later
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    List<Post> commentList;
    Context context;

    public CommentsAdapter(Context context, List<Post> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Post post = commentList.get(position);

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPostBody;
        TextView tvPostUsername;
        ImageView ivPostProfilePicture;
        ImageButton ibLike;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvPostBody = itemView.findViewById(R.id.tvPostBody);
            tvPostUsername = itemView.findViewById(R.id.tvPostUsername);
            ivPostProfilePicture = itemView.findViewById(R.id.ivPostProfilePicture);
            ibLike = itemView.findViewById(R.id.ibLike);
        }

        public void bind() {

        }
    }

}
