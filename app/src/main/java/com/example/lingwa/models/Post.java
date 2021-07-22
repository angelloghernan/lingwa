package com.example.lingwa.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject {
    public static final String KEY_BODY = "body";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_NUM_COMMENTS = "numComments";
    public static final String KEY_NUM_LIKES = "numLikes";


    public Post() {

    }

    public Post(String body, ParseUser user) {
        this(body, user, 0, 0);
    }

    public Post(String body, ParseUser user, int numLikes, int numComments) {
        setBody(body);
        setAuthor(user);
        setNumLikes(numLikes);
        setNumComments(numComments);
    }

    public String getBody() { return getString(KEY_BODY); }

    public void setBody(String body) { put(KEY_BODY, body); }

    public ParseUser getAuthor() { return getParseUser(KEY_AUTHOR); }

    public void setAuthor(ParseUser author) { put(KEY_AUTHOR, author); }

    public int getNumComments() { return getInt(KEY_NUM_COMMENTS); }

    public void setNumComments(int num) { put(KEY_NUM_COMMENTS, num); }

    public int getNumLikes() { return getInt(KEY_NUM_LIKES); }

    public void setNumLikes(int num) { put(KEY_NUM_LIKES, num); }
}
