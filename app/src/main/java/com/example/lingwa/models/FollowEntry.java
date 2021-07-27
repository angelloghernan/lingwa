package com.example.lingwa.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("FollowEntry")
public class FollowEntry extends ParseObject {
    public static final String KEY_FOLLOWED = "followed";
    public static final String KEY_FOLLOWER = "follower";

    public FollowEntry() {
      // required empty default constructor
    }

    public FollowEntry(ParseUser follower, ParseUser followed) {
        this.setFollower(follower);
        this.setFollowed(followed);
    }

    public ParseUser getFollower() { return getParseUser(KEY_FOLLOWER); }
    public void setFollower(ParseUser follower) { put(KEY_FOLLOWER, follower); }

    public ParseUser getFollowed() { return getParseUser(KEY_FOLLOWED); }
    public void setFollowed(ParseUser followed) { put(KEY_FOLLOWED, followed); }

}
