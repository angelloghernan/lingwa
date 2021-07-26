package com.example.lingwa.wrappers;

import com.example.lingwa.models.Post;
import com.example.lingwa.util.PostInteractions;
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
    public String objectId;
    public int numLikes;
    public int numComments;
    public boolean liked;
    public Date timestamp;

    PostWrapper() {

    }

    public static PostWrapper fromPost(Post post) {
        PostWrapper postWrapper = new PostWrapper();

        ParseUser author = post.getAuthor();
        postWrapper.authorId = author.getObjectId();
        postWrapper.authorUsername = author.getUsername();

        postWrapper.authorProfilePictureUrl = author.getParseFile("profilePicture").getUrl();

        postWrapper.objectId = post.getObjectId();
        postWrapper.body = post.getBody();
        postWrapper.numComments = post.getNumComments();
        postWrapper.numLikes = post.getNumLikes();
        postWrapper.timestamp = post.getCreatedAt();
        postWrapper.liked = post.liked;

        return postWrapper;
    }

}
