package com.example.lingwa.util;

import android.app.Application;

import com.example.lingwa.BuildConfig;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.Word;
import com.parse.Parse;
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

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildConfig.BACK4APP_APP_ID)
                .clientKey(BuildConfig.BACK4APP_CLIENT_KEY)
                .server("https://parseapi.back4app.com/")
                .build());
    }

}