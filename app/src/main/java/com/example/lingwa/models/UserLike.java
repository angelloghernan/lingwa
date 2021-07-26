package com.example.lingwa.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("UserLike")
public class UserLike extends ParseObject {
    public static final String KEY_LIKED_BY = "likedBy";
    public static final String KEY_LIKED_POST = "likedPost";

    public UserLike() {

    }

    public UserLike(Post likedPost, ParseUser likedBy) {
        this.setLikedPost(likedPost);
        this.setLikedBy(likedBy);
    }

    public ParseUser getLikedBy() { return getParseUser(KEY_LIKED_BY); }

    public void setLikedBy(ParseUser likedBy) { put(KEY_LIKED_BY, likedBy); }

    public Post getLikedPost() {
        Object value = get(KEY_LIKED_POST);
        if (!(value instanceof Post)) { return null; }
        return (Post) value;
    }

    public void setLikedPost(Post likedPost) { put(KEY_LIKED_POST, likedPost);}
}
