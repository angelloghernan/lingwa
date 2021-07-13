package com.example.lingwa;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    private static final String TAG = "ParseApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the parse class and register the parse subclasses so we can use it all in our code

        // ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildConfig.BACK4APP_APP_ID)
                .clientKey(BuildConfig.BACK4APP_CLIENT_KEY)
                .server("https://parseapi.back4app.com/")
                .build());
    }

}