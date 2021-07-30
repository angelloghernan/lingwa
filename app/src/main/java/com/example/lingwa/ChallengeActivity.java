package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import org.json.JSONException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import www.sanju.motiontoast.MotionToast;

public class ChallengeActivity extends AppCompatActivity {

    private static final String TAG = "ChallengeActivity";
    boolean initiatedChallenge;
    boolean userExited = false;
    ParseObject challenge;
    String challengeId;
    String identity;
    String oppositeIdentity;
    ParseLiveQueryClient client;

    ImageView ivChallenger;
    ImageView ivChallenged;
    ProgressBar pbChallenger;
    ProgressBar pbChallenged;
    EditText etChallenge;
    Button btnChallengeSubmit;
    TextView tvChallengeWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge2);

        initiatedChallenge = getIntent().getBooleanExtra("initiatedChallenge", false);
        challengeId = getIntent().getStringExtra("challengeId");

        ivChallenged = findViewById(R.id.ivChallenged);
        ivChallenger = findViewById(R.id.ivChallenger);
        pbChallenged = findViewById(R.id.pbChallenged);
        pbChallenger = findViewById(R.id.pbChallenger);
        etChallenge = findViewById(R.id.etChallenge);
        btnChallengeSubmit = findViewById(R.id.btnChallengeSubmit);
        tvChallengeWord = findViewById(R.id.tvChallengeWord);

        try {
            client = ParseLiveQueryClient.Factory.getClient(new URI("wss://lingwa.b4a.io/"));
        } catch (URISyntaxException e) {
            Log.e(TAG, "Issue creating web socket for Parse server: " + e.toString());
        }
        ParseQuery<ParseObject> challengeQuery = new ParseQuery<>("Challenge");

        // Get which key to equal to the user -- if they initiated, listen to challenges
        // where they are the challenger, else listen to challenges where they are challenged
        if (initiatedChallenge) {
            identity = "challenger";
            oppositeIdentity = "challenged";
        } else {
            identity = "challenged";
            oppositeIdentity = "challenger";
        }
        challengeQuery.whereEqualTo(ParseObject.KEY_OBJECT_ID, challengeId);

        SubscriptionHandling<ParseObject> challengeListener = client.subscribe(challengeQuery);

        challengeListener.handleEvent(SubscriptionHandling.Event.UPDATE, (query, challenge) -> {
            // do stuff when there is an update to the challenge entry
            // ie: other user solves a word

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                // ui updates go here
            });
        });

        challengeListener.handleEvent(SubscriptionHandling.Event.DELETE, (query, challenge) -> {
           // If the other user exits and deletes the challenge, this will be called.
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                if (userExited) {
                    return;
                }
                Log.i(TAG, "other user deleted challenge");
                MotionToast.Companion.createColorToast((Activity) this,
                        "Partner left",
                        "Your challenger left!",
                        MotionToast.TOAST_INFO,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        ResourcesCompat.getFont(this, R.font.helvetica_regular));
                finish();
            });
        });

        setUpChallenge();
    }

    @Override
    public void onBackPressed() {
        try {
            challenge.delete();
        } catch (ParseException e) {
            Log.e(TAG, "Error deleting challenge onBackPressed: " + e.toString());
        }
        userExited = true;
        finish();
    }

    private void setUpChallenge() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        // get challenge table entry using its object ID
        executor.execute(() -> {
            ParseQuery<ParseObject> challengeQuery = new ParseQuery<ParseObject>("Challenge");
            challengeQuery.whereEqualTo(ParseObject.KEY_OBJECT_ID, challengeId);
            challengeQuery.include("challenger");
            challengeQuery.include("challenged");

            try {
                challenge = challengeQuery.getFirst();
                challenge.put(identity + "Ready", true);
                challenge.save();
            } catch (ParseException e) {
                Log.e(TAG, "Error preparing challenge " + e.toString());
                return;
            }

            handler.post(() -> {
                try {
                    tvChallengeWord.setText(challenge.getJSONArray(identity + "Words").getString(0));
                    // note: duplicate code, refactor
                    Glide.with(this)
                            .load(challenge.getParseUser("challenger").getParseFile("profilePicture").getUrl())
                            .circleCrop()
                            .placeholder(R.drawable.default_profile_picture)
                            .into(ivChallenger);
                    Glide.with(this)
                            .load(challenge.getParseUser("challenged").getParseFile("profilePicture").getUrl())
                            .circleCrop()
                            .placeholder(R.drawable.default_profile_picture)
                            .into(ivChallenged);
                } catch (NullPointerException | JSONException e) {
                    Log.e(TAG, "Failure setting up challenge UI: " + e.toString());
                }
            });
        });
    }
}