package com.example.lingwa.util;

import android.app.Application;

import com.example.lingwa.BuildConfig;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.FollowEntry;
import com.example.lingwa.models.Post;
import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.UserLike;
import com.example.lingwa.models.Word;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

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
        ParseObject.registerSubclass(FollowEntry.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildConfig.BACK4APP_APP_ID)
                .clientKey(BuildConfig.BACK4APP_CLIENT_KEY)
                .server("https://parseapi.back4app.com/")
                .build());

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", "1070394322763");
        installation.saveInBackground();
    }

    public static void createPushNotification(String alert, String title, ParseUser toWho) {
        JSONObject data = new JSONObject();
        // Put data in the JSON object
        try {
            data.put("alert", alert);
            data.put("title", title);
        } catch ( JSONException e) {
            // should not happen
            throw new IllegalArgumentException("unexpected parsing error", e);
        }
        // Configure the push
        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("user", toWho);

        ParsePush push = new ParsePush();
        push.setChannel("News");
        push.setData(data);
        push.setQuery(pushQuery);
        push.sendInBackground();
    }

}