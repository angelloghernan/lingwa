package com.example.lingwa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;

import com.example.lingwa.fragments.AddPostFragment;
import com.example.lingwa.fragments.HomeFragment;
import com.example.lingwa.fragments.MyProfileFragment;
import com.example.lingwa.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Back4App's Parse setup
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildConfig.BACK4APP_APP_ID)
                .clientKey(BuildConfig.BACK4APP_CLIENT_KEY)
                .server("wss://lingwa.b4a.io/").build()
        );
        // Init Live Query Client
        ParseLiveQueryClient parseLiveQueryClient = null;

        try {
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI("wss://lingwa.b4a.io/"));
        } catch (URISyntaxException e) {
            Log.e(TAG, "Issue creating web socket for Parse server: " + e.toString());
        }

        if (parseLiveQueryClient != null) {
            ParseQuery<ParseObject> challengeQuery = new ParseQuery<>("Challenge");
            challengeQuery.include("challenger");
            challengeQuery.whereEqualTo("challenged", ParseUser.getCurrentUser());
            SubscriptionHandling<ParseObject> subscriptionHandling = parseLiveQueryClient.subscribe(challengeQuery);

            subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, (query, challenge) -> {
               // Run code here when a challenge is started
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setMessage("New challenge from user")
                            .setTitle("Incoming Challenge");

                    builder.setPositiveButton("Accept", (dialog, which) -> {
                        Intent intent = new Intent(this, ChallengeActivity.class);
                        intent.putExtra("initiatedChallenge", false);
                        intent.putExtra("challengeId", challenge.getObjectId());
                        startActivity(intent);
                    });

                    builder.setNegativeButton("Decline", (dialog, which) -> {
                        challenge.deleteInBackground();
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                });
            });
        }


        final FragmentManager fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bnvTabs);

        final Fragment homeFragment = new HomeFragment();
        final Fragment myProfileFragment = new MyProfileFragment();
        final Fragment searchFragment = new SearchFragment();
        final Fragment addPostFragment = new AddPostFragment();

        fragmentManager.beginTransaction().replace(R.id.flContainer, homeFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch(item.getItemId()) {
                    case R.id.action_home:
                        fragment = homeFragment;
                        break;
                    case R.id.action_my_profile:
                        fragment = myProfileFragment;
                        break;
                    case R.id.action_search:
                        fragment = searchFragment;
                        break;
                    case R.id.action_new_post:
                        fragment = addPostFragment;
                        break;
                    default:
                        return false;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
    }
}