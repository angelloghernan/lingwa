package com.example.lingwa.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Comment")
public class Comment extends ParseObject {
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_REPLYING_TO = "replyingTo";

    public Comment() {

    }

    public Comment(ParseUser author, Post replyingTo) {
        this.setAuthor(author);
        this.setReplyingTo(replyingTo);
    }

    public ParseUser getAuthor() { return getParseUser(KEY_AUTHOR); }

    public void setAuthor(ParseUser author) { put(KEY_AUTHOR, author); }

    public Post getReplyingTo() {
        Object value = get(KEY_REPLYING_TO);
        if (!(value instanceof Post)) { return null; }
        return (Post) value;
    }

    public void setReplyingTo(Post replyingTo) { put(KEY_REPLYING_TO, replyingTo); }

}
