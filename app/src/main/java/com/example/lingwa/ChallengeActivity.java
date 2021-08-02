package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.lingwa.models.Challenge;
import com.example.lingwa.util.Translator;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import www.sanju.motiontoast.MotionToast;

public class ChallengeActivity extends AppCompatActivity {

    private static final String TAG = "ChallengeActivity";
    boolean initiatedChallenge;
    boolean userExited = false;
    int wordIndex = 0;
    Challenge challenge;
    String challengeId;
    String identity;
    String opponentIdentity;
    ParseLiveQueryClient client;
    List<String> words;
    Context context;

    ImageView ivOpponent;
    ImageView ivCurrentUser;
    ProgressBar pbOpponent;
    ProgressBar pbCurrentUser;
    EditText etChallenge;
    Button btnChallengeSubmit;
    TextView tvChallengeWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge2);
        context = this;

        initiatedChallenge = getIntent().getBooleanExtra("initiatedChallenge", false);
        challengeId = getIntent().getStringExtra("challengeId");

        // initialize views
        if (initiatedChallenge) {
            ivOpponent = findViewById(R.id.ivChallenged);
            ivCurrentUser = findViewById(R.id.ivChallenger);
            pbOpponent = findViewById(R.id.pbChallenged);
            pbCurrentUser = findViewById(R.id.pbChallenger);
        } else {
            ivOpponent = findViewById(R.id.ivChallenger);
            ivCurrentUser = findViewById(R.id.ivChallenged);
            pbOpponent = findViewById(R.id.pbChallenger);
            pbCurrentUser = findViewById(R.id.pbChallenged);
        }
        etChallenge = findViewById(R.id.etChallenge);
        btnChallengeSubmit = findViewById(R.id.btnChallengeSubmit);
        tvChallengeWord = findViewById(R.id.tvChallengeWord);

        words = new ArrayList<>();

        // Get which key to equal to the user -- if they initiated, listen to challenges
        // where they are the challenger, else listen to challenges where they are challenged
        if (initiatedChallenge) {
            identity = "challenger";
            opponentIdentity = "challenged";
        } else {
            identity = "challenged";
            opponentIdentity = "challenger";
        }

        try {
            client = ParseLiveQueryClient.Factory.getClient(new URI("wss://lingwa.b4a.io/"));
        } catch (URISyntaxException e) {
            Log.e(TAG, "Issue creating web socket for Parse server: " + e.toString());
        }
        ParseQuery<Challenge> challengeQuery = ParseQuery.getQuery(Challenge.class);

        challengeQuery.whereEqualTo(ParseObject.KEY_OBJECT_ID, challengeId);

        SubscriptionHandling<Challenge> challengeListener = client.subscribe(challengeQuery);

        challengeListener.handleEvent(SubscriptionHandling.Event.UPDATE, (query, challenge) -> {
            // do stuff when there is an update to the challenge entry
            // ie: other user solves a word or submits an answer (correct or incorrect)

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                if (challenge == null) {
                    Log.e(TAG, "Challenge is null!");
                    return;
                }

                if (challenge.getProgress(opponentIdentity) >= 10) {
                    MotionToast.Companion.createColorToast((Activity) this,
                            "You lose :(",
                            "Your opponent won the match",
                            MotionToast.TOAST_ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.SHORT_DURATION,
                            ResourcesCompat.getFont(this, R.font.helvetica_regular));
                    challenge.deleteInBackground();
                    finish();
                    return;
                }

                String opponentAnswer = challenge.getAnswer(opponentIdentity);
                String prevOpponentAnswer = this.challenge.getAnswer(opponentIdentity);

                // Display a speech bubble below the opponent's profile with whatever they have just entered
                if (opponentAnswer != null &&
                        !opponentAnswer.equals(prevOpponentAnswer)) {
                    Balloon.Builder builder = new Balloon.Builder(this);
                    builder.setArrowOrientation(ArrowOrientation.TOP)
                            .setTextSize(18f)
                            .setAutoDismissDuration(900)
                            .setDismissWhenClicked(false)
                            .setDismissWhenTouchOutside(false)
                            .setBalloonAnimation(BalloonAnimation.ELASTIC)
                            .setText(opponentAnswer)
                            .setBackgroundColorResource(R.color.info_color);
                    Balloon balloon = builder.build();
                    balloon.show(ivOpponent);
                }

                pbOpponent.setProgress(challenge.getProgress(opponentIdentity) / words.size());

                // update class challenge entry
                this.challenge = challenge;
            });
        });

        challengeListener.handleEvent(SubscriptionHandling.Event.DELETE, (query, challenge) -> {
           // If the other user exits and deletes the challenge, this will be called.
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {

                Log.i(TAG, "other user deleted challenge");
                MotionToast.Companion.createColorToast((Activity) this,
                        "Opponent left",
                        "Your opponent left the match!",
                        MotionToast.TOAST_INFO,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        ResourcesCompat.getFont(this, R.font.helvetica_regular));

                finish();
            });
        });

        btnChallengeSubmit.setOnClickListener(submitAnswer);
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

    View.OnClickListener submitAnswer = v -> {
        String answer = etChallenge.getText().toString();
        etChallenge.setText("");
        Translator.translateWord(words.get(wordIndex), "es", "en", new Translator.TranslatorCallback() {
            @Override
            public void onTranslationSuccess(String translation) {
                if (answer.toLowerCase().equals(translation.toLowerCase())) {
                    wordIndex++;
                    if (wordIndex >= words.size()) {
                        challenge.setProgress(identity, wordIndex);
                        challenge.saveInBackground();
                        MotionToast.Companion.createColorToast((Activity) context,
                                "You win!",
                                "You won the match!",
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(context, R.font.helvetica_regular));
                        finish();
                        return;
                    }
                    tvChallengeWord.setText(words.get(wordIndex));
                    pbCurrentUser.setProgress((int)((double)(wordIndex / words.size()) * 100));
                }
                challenge.setAnswer(identity, answer);
                challenge.setProgress(identity, wordIndex);
                challenge.saveInBackground();

                // show a speech bubble under user's profile for the word they input
                Balloon.Builder builder = new Balloon.Builder(context);
                builder.setBackgroundColor(getResources().getColor(R.color.light_green))
                        .setText(answer)
                        .setAutoDismissDuration(900)
                        .setDismissWhenTouchOutside(false)
                        .setDismissWhenClicked(false)
                        .setArrowOrientation(ArrowOrientation.TOP);
                Balloon balloon = builder.build();
                balloon.show(ivCurrentUser);
            }

            @Override
            public void onTranslationFailure(Exception e) {
                Log.e(TAG, "Error checking translation: " + e.toString());
            }
        });
    };

    private void setUpChallenge() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        // get challenge table entry using its object ID
        executor.execute(() -> {
            ParseQuery<Challenge> challengeQuery = ParseQuery.getQuery(Challenge.class);
            challengeQuery.whereEqualTo(ParseObject.KEY_OBJECT_ID, challengeId);
            challengeQuery.include("challenger");
            challengeQuery.include("challenged");

            try {
                challenge = challengeQuery.getFirst();
                challenge.put(identity + "Ready", true);

                JSONArray jsonWords = challenge.getWords(identity);
                for (int i = 0; i < jsonWords.length(); i++) {
                    words.add(jsonWords.getString(i));
                }

                words.size();

                challenge.save();
            } catch (ParseException | JSONException e) {
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
                            .into((ImageView) findViewById(R.id.ivChallenger));
                    Glide.with(this)
                            .load(challenge.getParseUser("challenged").getParseFile("profilePicture").getUrl())
                            .circleCrop()
                            .placeholder(R.drawable.default_profile_picture)
                            .into((ImageView) findViewById(R.id.ivChallenged));
                } catch (NullPointerException | JSONException e) {
                    Log.e(TAG, "Failure setting up challenge UI: " + e.toString());
                }
            });
        });
    }
}