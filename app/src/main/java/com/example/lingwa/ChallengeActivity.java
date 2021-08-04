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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
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
    private static final int NUM_WORDS = 10;

    boolean initiatedChallenge;
    boolean userExited = false;
    boolean opponentIsReady = false;
    boolean opponentHasSelected = false;
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
    TextView tvChallengeTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge2);
        context = this;

        initiatedChallenge = getIntent().getBooleanExtra("initiatedChallenge", false);
        challengeId = getIntent().getStringExtra("challengeId");
        identity = getIntent().getStringExtra("identity");
        opponentIdentity = getIntent().getStringExtra("opponentIdentity");


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
        tvChallengeTip = findViewById(R.id.tvChallengeTip);

        words = new ArrayList<>();

        if (!opponentIsReady) {
            tvChallengeTip.setVisibility(View.INVISIBLE);
            btnChallengeSubmit.setClickable(false);
        }

        try {
            client = ParseLiveQueryClient.Factory.getClient(new URI("wss://lingwa.b4a.io/"));
        } catch (URISyntaxException e) {
            Log.e(TAG, "Issue creating web socket for Parse server: " + e.toString());
        }
        ParseQuery<Challenge> challengeQuery = ParseQuery.getQuery(Challenge.class);

        challengeQuery.whereEqualTo(ParseObject.KEY_OBJECT_ID, challengeId);
        challengeQuery.whereExists(opponentIdentity + Challenge.SUFFIX_WORDS);

        SubscriptionHandling<Challenge> challengeListener = client.subscribe(challengeQuery);

        challengeListener.handleSubscribe(query -> {
            // Handle stuff when the client has subscribed to the query.
            // Here, we initialize our challenge variable and see if the other players' words are selected.
            // If so, we set up the challenge so we can be ready in time
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                ParseQuery<Challenge> tempChallengeQuery = ParseQuery.getQuery(Challenge.class);
                tempChallengeQuery.whereEqualTo(Challenge.KEY_OBJECT_ID, challengeId);
                try {
                    this.challenge = tempChallengeQuery.getFirst();
                    opponentHasSelected = challenge.arePlayerWordsSelected(opponentIdentity);
                    if (opponentHasSelected) {
                        setUpChallenge();
                    }
                } catch (ParseException e) {
                    onBackPressed();
                }
            });
        });

        challengeListener.handleEvent(SubscriptionHandling.Event.ENTER, (query, challenge) -> {
            // Handle stuff when a query begins to match the parameters
            // In this case, when a query has the opponents' words existing
            // In other words, when the opponent has finished selecting their words and
            // has uploaded them to the Challenge entry in the server.
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                if (!opponentHasSelected) {
                    opponentHasSelected = true;
                    setUpChallenge();
                }
            });
        });

        challengeListener.handleEvent(SubscriptionHandling.Event.UPDATE, (query, challenge) -> {
            // do stuff when there is an update to the challenge entry
            // ie: other user solves a word or submits an answer (correct or incorrect)

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                if (challenge == null) {
                    Log.e(TAG, "Challenge is null!");
                    return;
                }

                // Since the query (which has already been caught) has updated from another source, we know that
                // this must be because the opponent has become readied. In that case,
                // we must either make sure we are *also* ready in case we somehow
                // haven't registered that they have selected their words or start the challenge right away
                if (!opponentIsReady) {
                    if (!opponentHasSelected) {
                        opponentHasSelected = true;
                        setUpChallenge();
                    } else if (challenge.getReady(opponentIdentity)) {
                        tvChallengeTip.setVisibility(View.VISIBLE);
                        btnChallengeSubmit.setClickable(true);
                        opponentIsReady = true;
                        tvChallengeWord.setText(words.get(0));
                    }
                    return;
                }

                if (challenge.getProgress(opponentIdentity) >= NUM_WORDS) {
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
                            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                            .setBalloonAnimation(BalloonAnimation.ELASTIC)
                            .setText(opponentAnswer)
                            .setBackgroundColorResource(R.color.info_color);
                    Balloon balloon = builder.build();
                    balloon.show(ivOpponent);
                }

                pbOpponent.setProgress(challenge.getProgress(opponentIdentity) * 100 / NUM_WORDS);

                // update class challenge entry
                this.challenge = challenge;
            });
        });

        challengeListener.handleEvent(SubscriptionHandling.Event.DELETE, (query, challenge) -> {
           // If the other user exits and deletes the challenge, this will be called.
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                if(userExited) {
                    return;
                }
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

        if (opponentHasSelected) {
            setUpChallenge();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            challenge.delete();
        } catch (ParseException | NullPointerException e) {
            Log.e(TAG, "Error deleting challenge onBackPressed: " + e.toString());
        }
        userExited = true;
        finish();
    }

    View.OnClickListener submitAnswer = v -> {
        if (!opponentIsReady) {
            return;
        }

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
                    pbCurrentUser.setProgress(wordIndex * 100 / NUM_WORDS);
                }
                challenge.setAnswer(identity, answer);
                challenge.setProgress(identity, wordIndex);
                challenge.saveInBackground();

                // show a speech bubble under user's profile for the word they input
                Balloon.Builder builder = new Balloon.Builder(context);
                builder.setBackgroundColor(getResources().getColor(R.color.info_color))
                        .setText(answer)
                        .setAutoDismissDuration(900)
                        .setBalloonAnimation(BalloonAnimation.ELASTIC)
                        .setTextSize(18f)
                        .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
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
        // Get the words for the player to solve and mark the player as ready
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        // get challenge table entry using its object ID
        executor.execute(() -> {
            ParseQuery<Challenge> challengeQuery = ParseQuery.getQuery(Challenge.class);
            challengeQuery.whereEqualTo(ParseObject.KEY_OBJECT_ID, challengeId);
            challengeQuery.include(Challenge.KEY_CHALLENGED);
            challengeQuery.include(Challenge.KEY_CHALLENGER);

            try {
                challenge = challengeQuery.getFirst();
                challenge.setReady(identity, true);

                JSONArray jsonWords = challenge.getWords(identity);
                for (int i = 0; i < jsonWords.length(); i++) {
                    words.add(jsonWords.getString(i));
                }
                challenge.save();
            } catch (ParseException | JSONException e) {
                Log.e(TAG, "Error preparing challenge " + e.toString());
                return;
            }

            handler.post(() -> {
                try {
                    if (opponentIsReady) {
                        tvChallengeWord.setText(words.get(0));
                    }
                    // note: duplicate code, refactor
                    Glide.with(this)
                            .load(challenge.getChallenger().getParseFile("profilePicture").getUrl())
                            .circleCrop()
                            .placeholder(R.drawable.default_profile_picture)
                            .into((ImageView) findViewById(R.id.ivChallenger));
                    Glide.with(this)
                            .load(challenge.getChallenged().getParseFile("profilePicture").getUrl())
                            .circleCrop()
                            .placeholder(R.drawable.default_profile_picture)
                            .into((ImageView) findViewById(R.id.ivChallenged));
                } catch (NullPointerException e) {
                    Log.e(TAG, "Failure setting up challenge UI: " + e.toString());
                }
            });
        });
    }
}