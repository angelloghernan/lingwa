package com.example.lingwa.models;

import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

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

    public boolean isLikedByUser(ParseUser user) {
        ParseQuery<UserLike> likeQuery = ParseQuery.getQuery(UserLike.class);
        likeQuery.whereEqualTo(UserLike.KEY_LIKED_BY, user);
        likeQuery.whereEqualTo(UserLike.KEY_LIKED_POST, this);

        try {
            UserLike entry = likeQuery.getFirst();
            return true;
        } catch (ParseException e) {
            if (e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                Log.e("isLikedByUser", "error checking like table: " + e.toString());
            }
            return false;
        }
    }

    // Converts a date to a readable timestamp
    // ie: "x hours ago" instead of "8:30 AM UTC 23 July 2021"
    public static String toReadableTimestamp(Date timestamp) {
        // Set time constants
        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            // Try to get the difference between the time now and time
            // of post creation, and return a simplified timestamp
            // based on that difference.
            timestamp.getTime();
            long time = timestamp.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " minutes ago";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " hours ago";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " days ago";
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        // if all fails, return an empty string so the timestamp doesn't show
        return "";
    }
}
