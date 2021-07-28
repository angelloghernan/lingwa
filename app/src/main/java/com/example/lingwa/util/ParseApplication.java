package com.example.lingwa.util;

import android.app.Application;

import com.example.lingwa.BuildConfig;
import com.example.lingwa.R;
import com.example.lingwa.models.Comment;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.FollowEntry;
import com.example.lingwa.models.Post;
import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.UserLike;
import com.example.lingwa.models.Word;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    private static final String TAG = "ParseApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the parse class and register the parse subclasses so we can use it all in our code

        ParseObject.registerSubclass(Content.class);
        ParseObject.registerSubclass(UserJoinWord.class);
        ParseObject.registerSubclass(Word.class);
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(UserLike.class);
        ParseObject.registerSubclass(Comment.class);
        ParseObject.registerSubclass(FollowEntry.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildConfig.BACK4APP_APP_ID)
                .clientKey(BuildConfig.BACK4APP_CLIENT_KEY)
                .server("https://parseapi.back4app.com/")
                .build());

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", "644753770635");
        installation.saveInBackground();
    }

}