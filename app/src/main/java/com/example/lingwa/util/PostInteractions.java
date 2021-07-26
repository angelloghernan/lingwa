package com.example.lingwa.util;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.lingwa.models.Post;
import com.example.lingwa.models.UserLike;
import com.example.lingwa.wrappers.PostWrapper;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class PostInteractions {
    private static final String TAG = "PostInteractions";
    Post post;

    public boolean postLiked;

    public PostInteractions(Post post) {
        this.post = post;
    }

    public View.OnClickListener onLikeButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!(v instanceof ImageButton)) { return; }

            ImageButton ibLikeButton = (ImageButton) v;

            if (postLiked) {
                ibLikeButton.setSelected(false);
                // This would more than likely cause problems in a fully deployed app, but
                // for the purposes of this project, it works
                post.setNumLikes(post.getNumLikes() - 1);
                unlikePost(post);
            } else {
                ibLikeButton.setSelected(true);
                post.setNumLikes(post.getNumLikes() + 1);
                likePost(post);
            }
        }
    };

    void unlikePost(Post likedPost) {
        ParseQuery<UserLike> likeQuery = ParseQuery.getQuery(UserLike.class);
        likeQuery.whereEqualTo(UserLike.KEY_LIKED_POST, likedPost);
        likeQuery.whereEqualTo(UserLike.KEY_LIKED_BY, ParseUser.getCurrentUser());

        likeQuery.getFirstInBackground(new GetCallback<UserLike>() {
            @Override
            public void done(UserLike entry, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Failed to unlike post: " + e.toString());
                    return;
                }
                postLiked = false;
                post.liked = false;
                entry.deleteEventually();
                likedPost.saveEventually();
            }
        });
    }

    void likePost(Post likedPost) {
        UserLike likeEntry = new UserLike(post, ParseUser.getCurrentUser());
        likeEntry.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error saving post: " + e.toString());
                    return;
                }
                postLiked = true;
                post.liked = true;
                likedPost.saveEventually();
            }
        });
    }
}
