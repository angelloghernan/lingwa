package com.example.lingwa.adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.lingwa.R;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.Post;
import com.example.lingwa.util.PostInteractions;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import me.samlss.broccoli.Broccoli;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    Context context;
    List<Content> contentPosts;
    List<Post> posts;
    AdapterCallback callback;
    private boolean postsShown = true;

    public PostAdapter(Context context, List<Content> contentPosts, List<Post> posts, AdapterCallback callback) {
        this.context = context;
        if (contentPosts == null) {
            this.contentPosts = new ArrayList<>();
        } else {
            this.contentPosts = contentPosts;
        }
        this.posts = posts;
        this.callback = callback;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        // Make sure the view holder uses the proper layout based on if we are showing content (articles) or
        // posts from users
        if (!postsShown) {
             view = LayoutInflater.from(context).inflate(R.layout.item_content, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        if (postsShown) {
            Post post = posts.get(position);
            holder.bindPost(post);
        } else {
            Content content = contentPosts.get(position);
            holder.bindContent(content);
        }
    }

    @Override
    public int getItemCount() {
        if (postsShown) {
            return posts.size();
        }
        return contentPosts.size();
    }

    public void setPostsShown(boolean postsShown) {
        if (postsShown == this.postsShown) {
            return;
        }
        this.postsShown = postsShown;
    }

    public void clearContent() {
        contentPosts.clear();
    }

    public void clearPosts() {
        posts.clear();
    }

    public void addAllContent(List<Content> list) {
        contentPosts.addAll(list);
    }

    public void addAllPosts(List<Post> list) {
        posts.addAll(list);
    }

    public void addContent(int position, Content content) {contentPosts.add(position, content);}

    public void addPost(int position, Post post) {posts.add(position, post);}

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivContentImage;
        TextView tvContentDescription;
        TextView tvContentTitle;
        TextView tvContentAuthor;

        TextView tvPostBody;
        TextView tvPostUsername;
        ImageView ivPostProfilePicture;
        ImageButton ibLike;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this::onClick);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                if (postsShown) {
                    final Post post = posts.get(position);
                    callback.onPostSelected(position, post);
                } else {
                    final Content content = contentPosts.get(position);
                    callback.onContentSelected(position, content);
                }
            }
        }

        public void bindPost(Post post) {
            ParseUser author = post.getAuthor();
            ParseFile profilePicture = author.getParseFile("profilePicture");

            tvPostBody = itemView.findViewById(R.id.tvPostBody);
            tvPostUsername = itemView.findViewById(R.id.tvPostUsername);
            ivPostProfilePicture = itemView.findViewById(R.id.ivPostProfilePicture);
            ibLike = itemView.findViewById(R.id.ibLike);

            if (profilePicture != null) {
                Glide.with(context)
                        .load(profilePicture.getUrl())
                        .circleCrop()
                        .into(ivPostProfilePicture);
            } else {
                Glide.with(context)
                        .load(R.drawable.default_profile_picture)
                        .circleCrop()
                        .into(ivPostProfilePicture);
            }

            PostInteractions interactionHandler = new PostInteractions(post, post.liked);
            ibLike.setSelected(post.liked);
            ibLike.setOnClickListener(interactionHandler.onLikeButtonClicked);

            tvPostUsername.setText(author.getUsername());
            tvPostBody.setText(post.getBody());
        }

        public void bindContent(Content content) {
            ParseFile contentImage = content.getThumbnail();

            ivContentImage = itemView.findViewById(R.id.ivContentImage);
            tvContentDescription = itemView.findViewById(R.id.tvContentDescription);
            tvContentTitle = itemView.findViewById(R.id.tvContentTitle);
            tvContentAuthor = itemView.findViewById(R.id.tvContentAuthor);

            if (contentImage != null) {
                Glide.with(context)
                        .load(contentImage.getUrl())
                        .transform(new RoundedCorners(10))
                        .into(ivContentImage);
            }

            tvContentTitle.setText(content.getTitle());
            tvContentAuthor.setText(content.getAuthor());
            tvContentDescription.setText(content.getDescription());
        }
    }

    public interface AdapterCallback {
        void onContentSelected(int position, Content content);
        void onPostSelected(int position, Post post);
    }
}