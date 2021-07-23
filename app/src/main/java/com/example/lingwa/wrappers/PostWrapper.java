package com.example.lingwa.wrappers;

import com.example.lingwa.models.Post;
import com.parse.ParseUser;

import org.parceler.Parcel;

import java.util.Date;
import java.util.Objects;

@Parcel
public class PostWrapper {
    public String authorId;
    public String authorUsername;
    public String authorProfilePictureUrl;
    public String body;
    public int numLikes;
    public int numComments;
    public Date timestamp;

    PostWrapper() {

    }

    PostWrapper(String authorId, String authorUsername, String body, int numLikes, int numComments) {

    }

    public static PostWrapper fromPost(Post post) {
        PostWrapper postWrapper = new PostWrapper();

        ParseUser author = post.getAuthor();
        postWrapper.authorId = author.getObjectId();
        postWrapper.authorUsername = author.getUsername();

        postWrapper.authorProfilePictureUrl = author.getParseFile("profilePicture").getUrl();

        postWrapper.body = post.getBody();
        postWrapper.numComments = post.getNumComments();
        postWrapper.numLikes = post.getNumLikes();
        postWrapper.timestamp = post.getCreatedAt();

        return postWrapper;
    }

}
